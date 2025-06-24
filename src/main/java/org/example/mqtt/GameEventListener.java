package org.example.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class GameEventListener implements MqttCallback {
    private final CountDownLatch startLatch;
    private final AtomicReference<CountDownLatch> buzzStartLatchRef;
    private final Runnable stopCallback;

    public GameEventListener(CountDownLatch startLatch, AtomicReference<CountDownLatch> buzzStartLatchRef, Runnable stopCallback) {
        this.startLatch = startLatch;
        this.buzzStartLatchRef = buzzStartLatchRef;
        this.stopCallback = stopCallback;
    }

    @Override
    public void connectionLost(Throwable cause) { }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        JSONObject json = new JSONObject(payload);

        String msg = json.optString("message");

        if (topic.equals("play/game")) {
            if (msg.equalsIgnoreCase("game start")) {
                startLatch.countDown();
            } else if (msg.equalsIgnoreCase("game stop")) {
                stopCallback.run();
                buzzStartLatchRef.get().countDown();
            }
        } else if (topic.equals("play/canBuzz")) {
            if (msg.equalsIgnoreCase("buzz start")) {
                System.out.println("\n signal 'buzz start'...");
                buzzStartLatchRef.get().countDown();
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) { }
}
