package org.example.mqtt;

import org.eclipse.paho.client.mqttv3.*;

public class MqttClientManager {
    private static final String BROKER_URL = "";
    private static final String CLIENT_ID = "";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

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
