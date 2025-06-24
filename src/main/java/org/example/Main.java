package org.example;

import org.example.game.*;
import org.example.mqtt.GameEventListener;
import org.example.mqtt.MqttClientManager;
import org.example.model.Buzzer;
import org.example.util.ConsolePrinter;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) {
        ConsolePrinter printer = ConsolePrinter.getInstance();
        Scanner scanner = new Scanner(System.in);

        MqttClientManager mqttManager = new MqttClientManager();
        BuzzerManager buzzerManager = new BuzzerManager();

        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicReference<CountDownLatch> buzzStartLatchRef = new AtomicReference<>(new CountDownLatch(1));
        AtomicBoolean stopGame = new AtomicBoolean(false);

        try {
            mqttManager.connect();

            // Configurer l'écouteur MQTT (Observer pattern)
            mqttManager.setCallback(new GameEventListener(startLatch, buzzStartLatchRef, () -> stopGame.set(true)));

            mqttManager.subscribe("play/game");
            mqttManager.subscribe("play/canBuzz");

            // === Initialisation des buzzers via la factory ===
            printer.println("Tape un nombre de buzzers :");
            printer.print("> ");
            int nbBuzzers = Integer.parseInt(scanner.nextLine());

            for (int i = 1; i <= nbBuzzers; i++) {
                Buzzer buzzer = BuzzerFactory.createBuzzer(i);
                buzzerManager.addBuzzer(buzzer);

                String msg = "{\"id\":" + buzzer.getId() + "}";
                mqttManager.publish("init/buzzers", msg);
            }


//            printer.println("En attente du message 'game start' ...");
            printer.println("La partie n'a pas encore commence...");
            startLatch.await();
            printer.println("La partie commence !");

            // Démarre la console dans un thread séparé
            new Thread(new CommandHandler(buzzerManager)).start();

            while (!stopGame.get()) {
                buzzStartLatchRef.set(new CountDownLatch(1));
//                printer.println("\nEn attente du signal 'buzz start'...");
                printer.println("\nEn attente de la prochaine question...");
                buzzStartLatchRef.get().await();

                buzzerManager.clearBuzzOrder();
                printer.println("Les buzzers peuvent buzzer !");

                BuzzerExecutor buzzExecutor = new BuzzerExecutor(buzzerManager, mqttManager);
                buzzExecutor.runBuzzSequence();

                printer.println("Fin de sequence de buzz.");
                printer.println("Commande :");
                printer.print("> ");
            }

            printer.println("La partie est terminee.");
            mqttManager.disconnect();

        } catch (Exception e) {
            printer.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
