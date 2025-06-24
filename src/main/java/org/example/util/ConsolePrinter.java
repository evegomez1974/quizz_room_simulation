package org.example.util;

import java.util.concurrent.locks.ReentrantLock;

public class ConsolePrinter {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final ConsolePrinter INSTANCE = new ConsolePrinter();

    private ConsolePrinter() {}

    public static ConsolePrinter getInstance() {
        return INSTANCE;
    }

    public void println(String msg) {
        lock.lock();
        try {
            System.out.println(msg);
        } finally {
            lock.unlock();
        }
    }

    public void print(String msg) {
        lock.lock();
        try {
            System.out.print(msg);
        } finally {
            lock.unlock();
        }
    }
}
