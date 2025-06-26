package org.example.game;

import org.example.model.Buzzer;
import org.example.mqtt.MqttClientManager;
import org.example.util.ConsolePrinter;

import java.util.ArrayList;
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

        List<Buzzer> willBuzz = new ArrayList<>();

        // Décider aléatoirement qui buzz (au moins 1)
        for (Buzzer buzzer : buzzers) {
            boolean shouldBuzz = random.nextBoolean(); // 50% de chances
            if (shouldBuzz) {
                willBuzz.add(buzzer);
            }
        }

        // S’assurer qu’au moins un buzzer buzz
        if (willBuzz.isEmpty()) {
            Buzzer randomBuzzer = buzzers.get(random.nextInt(buzzers.size()));
            willBuzz.add(randomBuzzer);
        }

        for (Buzzer buzzer : buzzers) {
            buzzer.setCanBuzz(true);
            new Thread(() -> {
                try {
                    int reactivity = 1 + random.nextInt(10); // délai aléatoire
                    Thread.sleep(reactivity);

                    if (willBuzz.contains(buzzer)) {
                        String topic = "play/buzz";
                        String payload = "{\"buzzer\":" + buzzer.getId() + ", \"reactivity\":" + reactivity + "}";
                        mqttManager.publish(topic, payload);

                        printer.println("Buzzer " + buzzer.getId() + " a buzze apres " + reactivity + " ms");

                        buzzer.setReactivity(reactivity);
                        buzzer.setCanBuzz(false);

                        buzzerManager.addToBuzzOrder(new Buzzer(buzzer.getId(), reactivity));
                    }
                } catch (Exception e) {
                    printer.println("Erreur dans le thread buzzer : " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
    }
}
