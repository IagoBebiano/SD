package com.example.servingwebcontent;

//import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Hello world!
 *
 */

public class App {
    public static Queue<String> q = new ConcurrentLinkedQueue<>();
    public static Queue<String> used = new ConcurrentLinkedQueue<>();
    public static Queue<String> auxInsertUmaVez = new ConcurrentLinkedQueue<>();

    public static Queue<String> urls = new ConcurrentLinkedQueue<>();
    public static String url = new String();
    public static boolean queueInUse = false;
    public static boolean HashInUse = false;

    public static boolean urlInUse = false;

    public static void main(String[] args) {

        System.out.println("Projeto 1 de SD!");

    }

    public synchronized static Queue<String> getQ() {
        return q;
    }

    public static boolean getQueueInUse() {
        return queueInUse;
    }

    public static void setQueueInUse(boolean queueInUse) {
        App.queueInUse = queueInUse;
    }

    public static boolean getHashInUse() {
        return HashInUse;
    }

    public static void setHashInUse(boolean HashInUse) {
        App.HashInUse = HashInUse;
    }

}