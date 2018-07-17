package be.crypto.bot.config;

import org.springframework.stereotype.Service;

/**
 * Created by philippemaes on 26/06/2018.
 */
@Service
public class RunEnvironment {

    private boolean testEnv = false;

    public boolean isTestEnv() {
        return testEnv;
    }

    public void setTestEnv(boolean testEnv) {
        this.testEnv = testEnv;
    }
}
