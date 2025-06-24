package org.example.game;

import org.example.model.Buzzer;
import org.example.mqtt.MqttClientManager;
import org.example.util.ConsolePrinter;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class BuzzerExecutor {
    private final BuzzerManager buzzerManager;
    private final MqttClientManager mqttManager;
    private final ConsolePrinter printer = ConsolePrinter.getInstance();

    public BuzzerExecutor(BuzzerManager buzzerManager, MqttClientManager mqttManager) {
        this.buzzerManager = buzzerManager;
        this.mqttManager = mqttManager;
    }

    public void runBuzzSequence() throws InterruptedException {
        List<Buzzer> buzzers = buzzerManager.getBuzzers();
        CountDownLatch latch = new CountDownLatch(buzzers.size());
        Random random = new Random();

        for (Buzzer buzzer : buzzers) {
            buzzer.setCanBuzz(true); // reset état
            new Thread(() -> {
                try {
                    int reactivity = 1 + random.nextInt(10); // délai aléatoire
                    Thread.sleep(reactivity);

                    if (buzzer.canBuzz()) {
                        String topic = "play/buzz";
                        String payload = "{\"buzzer\":" + buzzer.getId() + ", \"reactivity\":" + reactivity + "}";
                        mqttManager.publish(topic, payload);

                        printer.println("Buzzer " + buzzer.getId() + " a buzze apres " + reactivity + " ms");

                        buzzer.setReactivity(reactivity); // mise à jour du vrai temps
                        buzzer.setCanBuzz(false);

                        buzzerManager.addToBuzzOrder(new Buzzer(buzzer.getId(), reactivity));
                    }
                } catch (Exception e) {
                    printer.println("Erreur dans le thread buzzer : " + e.getMessage());
                } finally {
                    latch.countDown(); // termine ce thread
                }
            }).start();
        }

        latch.await(); // on attend que tous les threads terminent
    }
}
