package org.example;

import org.eclipse.paho.client.mqttv3.*;

public class ClientMQTT {
    private static final String BROKER = "ssl://70d3d122435c4c66875841a4ee5e6c0b.s1.eu.hivemq.cloud:8883";
    private static final String CLIENT_ID = "JavaClientTest";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private MqttClient client;

    public void connectToBroker() throws MqttException {
        client = new MqttClient(BROKER, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());
        options.setCleanSession(true);

        client.connect(options);

        if (client.isConnected()) {
            System.out.println("Connecte a MQTT");
        } else {
            System.err.println("Echec de connexion");
        }
    }

    public void subscribeToTopic(String topic) throws MqttException {
        client.subscribe(topic);
        System.out.println("Souscrit a : " + topic);
    }

    public void publishMessage(String topic, String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        client.publish(topic, message);
//        System.out.println("Message publie sur [" + topic + "] : " + payload);
    }

    public void disconnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
//            System.out.println("Deconnecte du broker");
        }
    }

    public void setCallback(MqttCallback callback) {
        client.setCallback(callback);
    }

}
