package org.example.game;

import org.example.util.ConsolePrinter;

import java.util.Scanner;

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
                        printer.println("Aucun buzzer n'a buzze.");
                    } else {
                        for (int i = 0; i < order.size(); i++) {
                            var b = order.get(i);
                            printer.println((i + 1) + ". Buzzer " + b.getId() + " (" + b.getReactivity() + " ms)");
                        }
                    }
                    break;

                case "exit":
                    printer.println("Fin du programme.");
                    System.exit(0);
                    break;

                default:
                    printer.println("Commande inconnue. Utilisez 'liste' ou 'exit'.");
            }
        }
    }
}
