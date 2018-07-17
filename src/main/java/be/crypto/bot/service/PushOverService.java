package be.crypto.bot.service;

import be.crypto.bot.config.Constants;
import be.crypto.bot.domain.OrderType;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by philippemaes on 03/07/2018.
 */
@Service
public class PushOverService {

    private static final String PUSHOVER_URL = "https://api.pushover.net/1/messages.json";

    public void sendFillNotification(String marketName, Double qty, Double rate, OrderType type) throws IOException {
        String title = (type.equals(OrderType.BUY) ? "Buy" : "Sell") + " order filled";
        String message = (type.equals(OrderType.BUY) ? "Bought" : "Sold") + " " + qty + " " + marketName + " @ price " + String.format("%.8f", rate);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(PUSHOVER_URL);
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("token", Constants.PUSHOVER_API_TOKEN));
        params.add(new BasicNameValuePair("user", Constants.PUSHOVER_USER_KEY));
        params.add(new BasicNameValuePair("device", Constants.PUSHOVER_DEVICE_NAME));
        params.add(new BasicNameValuePair("title", title));
        params.add(new BasicNameValuePair("message", message));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        client.execute(httpPost);
    }
}
