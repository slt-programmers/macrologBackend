package csl.websocket.notification;

import csl.websocket.NotificationEndpoint;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PocNotificationService {

    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static NotificationEndpoint notifyEndpoint = new NotificationEndpoint();

    public static void printSomethingPeriodically() throws IOException{
        Runnable printMessage = new Runnable() {
            @Override
            public void run() {
                try {
                    notifyEndpoint.onMessage("Hello");
                } catch(IOException ex) {
                  ex.printStackTrace();
                }
            }
        };
        scheduler.scheduleAtFixedRate(printMessage, 5, 5, TimeUnit.SECONDS);
    }
}
