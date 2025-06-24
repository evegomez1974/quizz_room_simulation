package org.example;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONObject;

public class Main {
    private static final List<Buzzer> buzzers = new ArrayList<>();
    private static volatile boolean stopGame = false;

    private static final CountDownLatch startLatch = new CountDownLatch(1);
    private static CountDownLatch buzzStartLatch = new CountDownLatch(1);

    private static List<Buzzer> buzzOrder = Collections.synchronizedList(new ArrayList<>());
    private static final ReentrantLock consoleLock = new ReentrantLock();
    private static Scanner scanner = new Scanner(System.in);

    public static void initBuzzers(ClientMQTT mqtt) throws MqttException {
        String topic = "init/buzzers";
        Random random = new Random();

        safePrintln("Tape un nombre de buzzer :");
        safePrint("> ");
        int nbBuzzer = Integer.parseInt(scanner.nextLine());

        for (int i = 1; i <= nbBuzzer; i++) {
            int reactivity = 1 + random.nextInt(10); // entre 1 et 10 ms
            Buzzer buzzer = new Buzzer(i, reactivity);
            buzzers.add(buzzer);
            String jsonMessage = "{\"id\":" + i + "}";
            mqtt.publishMessage(topic, jsonMessage);
        }

    }

    public static void enableBuzzAndSendMessages(ClientMQTT mqtt) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(buzzers.size());
        Random random = new Random();

        for (Buzzer buzzer : buzzers) {
            buzzer.setCanBuzz(true);

            new Thread(() -> {
                try {
//                    Thread.sleep(buzzer.getReactivity());
                    int reactivity = 1 + random.nextInt(10);
                    Thread.sleep(reactivity);
                    if (buzzer.canBuzz()) {
                        String topic = "play/buzz";
                        String jsonMessage = "{\"buzzer\":" + buzzer.getId() + ", \"reactivity\":" + reactivity + "}";
                        mqtt.publishMessage(topic, jsonMessage);
                        safePrintln("Buzzer " + buzzer.getId() + " envoye apres " + reactivity + " ms");
                        buzzOrder.add(new Buzzer(buzzer.getId(), reactivity));
                        buzzer.setCanBuzz(false);
                    }
                } catch (MqttException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(); // Attend que tous les buzzers aient publié avant de continuer
    }

    public static void test() {
        new Thread(() -> {
            while (true) {
                safePrintln("Commande :");
                safePrint("> ");
                String input = scanner.nextLine();

                switch (input.toLowerCase()) {
                    case "liste":
                        safePrintln("Buzzers ayant buzze :");
                        synchronized (buzzOrder) {
                            if (buzzOrder.isEmpty()) {
                                safePrintln("Aucun buzzer n'a buzze pour l'instant.");
                            } else {
                                for (int i = 0; i < buzzOrder.size(); i++) {
                                    Buzzer b = buzzOrder.get(i);
                                    safePrintln((i + 1) + ". Buzzer " + b.getId() + " (" + b.getReactivity() + " ms)");
                                }
                            }
                        }
                        break;

                    case "exit":
                        safePrintln("Fin du programme (via console).");
                        System.exit(0);
                        break;

                    default:
                        safePrintln("Commande inconnue. Utilisez 'liste' ou 'exit'.");
                }
            }
        }).start();

    }

    public static void safePrintln(String message) {
        consoleLock.lock();
        try {
            System.out.println(message);
        } finally {
            consoleLock.unlock();
        }
    }

    public static void safePrint(String message) {
        consoleLock.lock();
        try {
            System.out.print(message);
        } finally {
            consoleLock.unlock();
        }
    }

    public static void main(String[] args) {
        ClientMQTT mqtt = new ClientMQTT();

        try {
            mqtt.connectToBroker();

            mqtt.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) { }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    JSONObject json = new JSONObject(payload);
                    String msg = json.getString("message");

                    if (topic.equals("play/game")) {
                        if (msg.equalsIgnoreCase("game start")) {
                            startLatch.countDown();
                        } else if (msg.equalsIgnoreCase("game stop")) {
                            stopGame = true;
                            buzzStartLatch.countDown();
                        }
                    } else if (topic.equals("play/canBuzz")) {
                        if (msg.equalsIgnoreCase("buzz start")) {
                            buzzStartLatch.countDown();
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { }
            });

            initBuzzers(mqtt);

            mqtt.subscribeToTopic("play/game");
            mqtt.subscribeToTopic("play/canBuzz");

            safePrintln("En attente du message 'game start' ...");
            startLatch.await();

            safePrintln("La partie commence !");
            test();

            while (!stopGame) {
                buzzStartLatch = new CountDownLatch(1);

                buzzStartLatch.await();

                safePrintln("\nLes buzzers peuvent buzzer !");
                buzzOrder.clear();
                enableBuzzAndSendMessages(mqtt);

                safePrintln("Les buzzers ne peuvent plus buzzer !");
                safePrintln("Commande :");
                safePrint("> ");
            }

            safePrintln("La partie est terminée.");

            mqtt.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
