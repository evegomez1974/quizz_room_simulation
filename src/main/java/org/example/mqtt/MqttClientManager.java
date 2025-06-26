package org.example.mqtt;

import org.eclipse.paho.client.mqttv3.*;

public class MqttClientManager {
    private static final String BROKER_URL = "ssl://70d3d122435c4c66875841a4ee5e6c0b.s1.eu.hivemq.cloud:8883";
    private static final String CLIENT_ID = "JavaClientTest";
    private static final String USERNAME = "user_quizz_room";
    private static final String PASSWORD = "Quizz_room/buzzer25";

    private MqttClient client;

    public void connect() throws MqttException {
        client = new MqttClient(BROKER_URL, CLIENT_ID);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());
        options.setCleanSession(true);

        client.connect(options);

        if (client.isConnected()) {
            System.out.println("Connecte au broker MQTT");
        } else {
            System.err.println("Echec de connexion au broker MQTT");
        }
    }

    public void disconnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
            System.out.println("Deconnecte du broker MQTT");
        }
    }

    public void subscribe(String topic) throws MqttException {
        if (client != null && client.isConnected()) {
            client.subscribe(topic);
            System.out.println("Souscription au topic : " + topic);
        }
    }

    public void publish(String topic, String payload) throws MqttException {
        if (client != null && client.isConnected()) {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1); // Assure une livraison au moins une fois
            client.publish(topic, message);
        }
    }

    public void setCallback(MqttCallback callback) {
        if (client != null) {
            client.setCallback(callback);
        }
    }
}
