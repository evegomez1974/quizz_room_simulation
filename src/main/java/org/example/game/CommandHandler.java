package org.example.game;

import org.example.model.Buzzer;
import org.example.util.ConsolePrinter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CommandHandler implements Runnable {
    private final BuzzerManager manager;

    public CommandHandler(BuzzerManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        ConsolePrinter printer = ConsolePrinter.getInstance();

        while (true) {
            printer.println("Commande :");
            printer.print("> ");
            String input = scanner.nextLine();

            switch (input.toLowerCase()) {
                case "list":
                    var order = manager.getBuzzOrder();
                    if (order.isEmpty()) {
                        printer.println("Aucun buzzer n'a buzze pour l'instant.");
                    } else {
                        for (int i = 0; i < order.size(); i++) {
                            var b = order.get(i);
                            printer.println((i + 1) + ". Buzzer " + b.getId() + " (" + b.getReactivity() + " ms)");
                        }
                    }
                    break;

                case "first":
                    List<Buzzer> buzzOrder = manager.getBuzzOrder();
                    if (buzzOrder.isEmpty()) {
                        printer.println("Aucun buzzer n'a buzze pour l'instant.");
                    } else {
                        Buzzer premier = Collections.min(buzzOrder, Comparator.comparingInt(Buzzer::getReactivity));
                        printer.println("Le buzzer qui a buzze en premier est le buzzer " + premier.getId() +
                                " avec une reactivite de " + premier.getReactivity() + " ms.");
                    }
                    break;

                case "active":
                    var allBuzzers = manager.getBuzzers();
                    var buzzedIds = manager.getBuzzOrder().stream()
                            .map(Buzzer::getId)
                            .collect(Collectors.toSet());

                    int activeCount = 0;
                    printer.println("Liste des buzzers :");
                    for (Buzzer buzzer : allBuzzers) {
                        boolean isActive = buzzedIds.contains(buzzer.getId());
                        if (isActive) activeCount++;
                        String status = isActive ? "a buzze" : "inactif";
                        printer.println(buzzer.getId() + ". Buzzer " + buzzer.getId() + " => " + status);
                    }
                    printer.println("\nTotal : " + activeCount + " buzzer(s) actif(s) / " + allBuzzers.size());
                    break;


                case "exit":
                    printer.println("Fin du programme.");
                    System.exit(0);
                    break;

                default:
                    printer.println("Commande inconnue. Utilisez 'list', 'first', 'active' ou 'exit'.");
            }
        }
    }
}
