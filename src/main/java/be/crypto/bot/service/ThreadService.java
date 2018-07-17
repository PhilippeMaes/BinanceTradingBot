package be.crypto.bot.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by philippemaes on 15/06/2018.
 */
@Service
public class ThreadService {

    private ExecutorService executor;

    @PostConstruct
    private void init() {
        executor = Executors.newFixedThreadPool(10);
    }

    public void addTask(Runnable task) {
        executor.execute(task);
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
