package com.example.servingwebcontent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
//import java.util.HashMap;
import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader extends Thread implements Serializable {

    // Multicast
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    private long SLEEP_TIME = 5000;
    int position = 0;
    int count = 0;

    private Set<String> visitedPages = new HashSet<String>();
    private Queue<String> urlQueue = new ConcurrentLinkedQueue<>();

    // Hashmap
    ConcurrentHashMap<String, Set<URL>> ocorrencias = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Set<URL>> linkHashMap = new ConcurrentHashMap<>();



    /**
     * Se tiver sido pedida a funcionalidade 3, o Downloader vai buscar as
     * informações ao links inserido peloi cliente
     * Caso a 4 seja pedida, o Downloader vai buscar as informações aos links que
     * estão na queue
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public synchronized void download() throws IOException, IllegalArgumentException {
        if(!App.urls.isEmpty()){
            indexPagesInverso(App.urls.poll());
        }
    }

    public synchronized void download2() throws IOException, IllegalArgumentException {
        if(App.urlInUse == true){
            try {
                System.out.println(" waiting...");
                wait();
                System.out.println(" done waiting");
            } catch (Exception e) {
                System.out.println("interruptedException caught");
            }
        }else{
            App.urlInUse = true;
            if(App.url != null){
                indexPagesInverso(App.url);}
            App.url = null;
            App.urlInUse = false;
            notify();
        }
    }

    /**
     * Envia por Multicast as informações que foram obtidas
     * Caso esteja a enviar o hashmap de ocorrencias, começa por enviar
     * "OCORRENCIAS", depois a key e, por fim, o Set<URL> associados a essa key
     * Caso esteja a enviar o hashmap de linkHashMap, começa por enviar "LINKS",
     * depois a key e, por fim, o Set<URL> associados a essa key
     * 
     */
    public synchronized void run() {
        MulticastSocket socket = null;
        String check;
        System.out.println("Downloader started: " + this.getId());
        try {
            socket = new MulticastSocket(); // create socket without binding it (only for sending)
            while (true) {
                download();

                while (App.HashInUse == false && ocorrencias.isEmpty() == false) {
                    App.HashInUse = true;
                    // System.out.println("A enviar ocorrencias ");
                    for (Entry<String, Set<URL>> entry : ocorrencias.entrySet()) {
                        check = "OCORRENCIAS";
                        String aux = check;
                        byte[] buffer2 = aux.getBytes();
                        InetAddress group2 = InetAddress.getByName(MULTICAST_ADDRESS);
                        DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group2, PORT);
                        socket.send(packet2);
                        // ----- Enviar key ----\\\
                        String palavra = entry.getKey();
                        Set<URL> linksSet = entry.getValue();
                        aux = palavra;
                        buffer2 = aux.getBytes();
                        group2 = InetAddress.getByName(MULTICAST_ADDRESS);
                        packet2 = new DatagramPacket(buffer2, buffer2.length, group2, PORT);
                        socket.send(packet2);
                        // ----------------\\\
                        // Enviar valores associados a key \\
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject(linksSet);
                        byte[] bytes = bos.toByteArray();
                        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, PORT);
                        try {
                            socket.send(packet);
                        } catch (Exception e) {
                            System.out.println("Exception caught:" + e.getMessage());
                        }
                        oos.close();
                        bos.close();
                    }
                    ocorrencias.clear();
                    App.HashInUse = false;

                    try {
                        sleep((long) (Math.random() * SLEEP_TIME));
                    } catch (InterruptedException e) {

                    }
                }

                while (App.HashInUse == false && linkHashMap.isEmpty() == false) {
                    App.HashInUse = true;
                    for (Entry<String, Set<URL>> entry : linkHashMap.entrySet()) {
                        //System.out.println("-----------A enviar links----------");
                        check = "LINKS";
                        String aux = check;
                        byte[] buffer2 = aux.getBytes();
                        InetAddress group2 = InetAddress.getByName(MULTICAST_ADDRESS);
                        DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group2, PORT);
                        socket.send(packet2);
                        // ----- Enviar key ----\\\
                        //System.out.println("-----------A enviar key----------");
                        String palavra = entry.getKey();
                        //System.out.println("link chave: " + palavra);
                        Set<URL> linksSet = entry.getValue();
                        aux = palavra;
                        buffer2 = aux.getBytes();
                        group2 = InetAddress.getByName(MULTICAST_ADDRESS);
                        packet2 = new DatagramPacket(buffer2, buffer2.length, group2, PORT);
                        socket.send(packet2);
                        // ----------------\\\
                        // Enviar valores associados a key \\
                        //System.out.println("-----------A enviar valores----------");
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        System.out.println("link set: " + linksSet);
                        oos.writeObject(linksSet);
                        byte[] bytes = bos.toByteArray();
                        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, PORT);
                        try {
                            socket.send(packet);
                        } catch (Exception e) {
                            System.out.println("Exception caught:" + e.getMessage());
                        }

                        oos.close();
                        bos.close();
                    }
                    linkHashMap.clear();
                    App.HashInUse = false;

                    try {
                        sleep((long) (Math.random() * SLEEP_TIME));
                    } catch (InterruptedException e) {
                        System.out.println("InterruptedException caught");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    /**
     * Fila de links que já foram usados
     * 
     * @param fila - fila de links
     * @param url  - link a ser adicionado
     */
    public synchronized void UsedLinks(Queue<String> fila, String url) {
        while (App.getQueueInUse() == true) {
            try {
                // System.out.println("tou a espera");
                wait();
            } catch (InterruptedException e) {
                System.out.println("interruptedException caught");
            }
        }
        fila.add(url);
    }

    /**
     * Verifica se o link já foi usado
     * 
     * @param url - link a ser verificado
     * @return 1 se já foi usado, 0 se não foi usado
     */
    public synchronized int alreadyUsed(String url) {
        for (String item : App.used) {
            // System.out.printf("item: %s\n",item);
            if (item.equals(url)) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Indexa recursivamente todos os URLs presentes na fila adiconando as
     * informacoes correspondentes aos hashmaps.
     * Funcionalidade 4 do menu cliente.
     * 
     * @param fila - fila de links
     */
    public synchronized void GetInfo(Queue<String> fila) {
        System.out.println("------Started GetInfo------");
        // System.out.println("HEllo");

        while (App.getQueueInUse() == true || App.q.isEmpty()) {
            try {
                System.out.println("Waiting in GetInfo with: " + this.getId());
                wait();
            } catch (InterruptedException e) {
                System.out.println("interruptedException caught");
            }
        }

        URL nw;
        try {
            System.out.println("Started GetInfowith: " + this.getId());
            App.setQueueInUse(true);
            String url = fila.poll();
            nw = new URL(url);
            // System.out.printf("url: %s\n", url);

            if (alreadyUsed(url) == 0) {
                Document doc = Jsoup.connect(url).get();
                Elements links = doc.select("a[href]");

                for (Element link : links) {
                    String linkUrl = link.attr("abs:href");
                    if (!linkUrl.startsWith(url)) {
                        continue;
                    }
                    App.q.add(linkUrl.toString());
                    App.setQueueInUse(false);
                    notifyAll();
                    UsedLinks(App.used, url);
                }

                String content = doc.body().text();
                String[] words = content.split("\\s+");
                for (String word : words) {
                    word = word.replaceAll("[^a-zA-ZçÇÁáÉéêÍíôóÓÀàÔÕõúÚÃã]+", "");
                    if (word.isEmpty()) {
                        continue;
                    }
                    while (App.HashInUse == false) {
                        App.setHashInUse(true);
                        Set<URL> linksSet = ocorrencias.get(word);
                        if (linksSet == null) {
                            linksSet = new HashSet<>();
                            ocorrencias.put(word, linksSet);

                            // App.setQueueInUse(true);
                            linksSet.add(nw);
                            // App.setQueueInUse(false);

                        } else {
                            // App.setQueueInUse(true);
                            linksSet.add(nw);
                            // App.setQueueInUse(false);
                        }
                    }
                    App.setHashInUse(false);
                    notify();

                }

                 System.out.println("Iniciar Links Hash");
                Set<URL> linksSet = new HashSet<>();
                nw = new URL(url);
                for (Element link : links) {
                    System.out.println("link: " + link);
                    String linkUrl = link.attr("abs:href");
                    System.out.println("link: " + linkUrl);
                    if (!linkUrl.startsWith(url)) {
                        continue;
                    }
                    nw = new URL(linkUrl);
                    linksSet.add(nw);
                    System.out.println("New Link +LinksSet: " + nw );
                    linkHashMap.put(url, linksSet);
                }
                System.out.println("linkHashMap completed");

            } else {
                // System.out.println("Link já usadooooo");
            }
            App.setQueueInUse(false);
            notify();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.jsoup.helper.ValidationException e) {
            e.printStackTrace();
        }
        System.out.println("------Finished GetInfo------");
    }

    public void indexPages(String startingPage) {
        urlQueue.add(startingPage);
        Set<URL> linksOnPage = new HashSet<>();
        while (!urlQueue.isEmpty()) {
            System.out.println("------linksHash------");
            String currentPage = urlQueue.poll();
            visitedPages.add(currentPage);

            // Retrieve links on the current page
            linksOnPage = getLinksToPage(currentPage);

            // Process the links
            for (URL link : linksOnPage) {
                if (!visitedPages.contains(link.url)) {
                    urlQueue.add(link.url);
                    visitedPages.add(link.url);
                }
            }
            System.out.println("------ocurrencesHash------");
            getOcurrencesOnPage(currentPage);
            linkHashMap.put(currentPage, linksOnPage);

        }
    }
    public void indexPagesInverso(String startingPage) {
        urlQueue.add(startingPage);
        Set<String> linksOnPage = new HashSet<>();
        URL nw;

        while (!urlQueue.isEmpty()) {
            System.out.println("------linksHash------");
            String currentPage = urlQueue.poll();
            visitedPages.add(currentPage);
            nw = new URL(currentPage);
            // Retrieve links on the current page
            linksOnPage = getLinksToPage2(currentPage);

            // Process the links
            for (String link : linksOnPage) {
                if(!visitedPages.contains(link)){
                    urlQueue.add(link);
                }
                if (!visitedPages.contains(link)) {
                    linkHashMap.put(link, new HashSet<>());
                    //System.out.println("Novo link: " + link);
                }
                //System.out.println("Adicionar "+ nw.url + " que liga ao link atual " + link);
                try{
                    linkHashMap.get(link).add(nw);
                    visitedPages.add(link);
                }catch (Exception e){
                    System.out.println("Erro ao adicionar link");
                }
                
            }
            System.out.println("------ocurrencesHash------");
            getOcurrencesOnPage(currentPage);
            //linkHashMap.put(currentPage, linksOnPage);
        }

    }

    private void getOcurrencesOnPage(String currentPage) {

        Set<URL> ocorrenciasOnPage = new HashSet<>();
        Document doc = null;
        URL nw;
        try {
            doc = Jsoup.connect(currentPage).get();
        } catch (Exception e) {
            System.out.println("URL inválida");
        }
        nw = new URL(currentPage);
        String content = doc.body().text();
        String[] words = content.split("\\s+");

        for (String word : words) {
            word = word.replaceAll("[^a-zA-ZçÇÁáÉéêÍíôóÓÀàÔÕõúÚÃã]+", "");
            if (word.isEmpty()) {
                continue;
            }
            while (!App.HashInUse) {
                App.setHashInUse(true);
                ocorrenciasOnPage = ocorrencias.get(word);
                if (ocorrenciasOnPage == null) {
                    ocorrenciasOnPage = new HashSet<>();
                    //System.out.println("URL para inserir:" + nw.toString());
                    ocorrenciasOnPage.add(nw);
                } else {
                    //System.out.println("URL para inserir:" + nw.toString());
                    ocorrenciasOnPage.add(nw);
                }
            }
            App.setHashInUse(false);
            ocorrencias.put(word, ocorrenciasOnPage);
            notify();
        }
    }

    private Set<URL> getLinksToPage(String currentPage) {
        URL nw;
        Document doc = null;
        Set<URL> linksOnPage = new HashSet<>();
        //System.out.println("CurrentPAge: " + currentPage);
        try {
                //System.out.println("Iniciar Links HashSet");

                doc = Jsoup.connect(currentPage).get();
                Elements links = doc.select("a[href]");

                for (Element link : links) {
                    String linkUrl = link.attr("abs:href");
                    //System.out.println("link encontrado: " + linkUrl);
                    //System.out.println("currenPage: "+ currentPage);
                    if (linkUrl.equals(currentPage)) {
                        System.out.println("Link já inserido");
                        continue;
                    }
                    nw = new URL(linkUrl);
                    //System.out.println("New Link + LinksSet: " + nw);
                    linksOnPage.add(nw);
                }
            //System.out.println("LinksOnPage: " + linksOnPage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    return linksOnPage;
    }

    private Set<String> getLinksToPage2(String currentPage) {
        URL nw;
        Document doc = null;
        Set<String> linksOnPage = new HashSet<String>();
        //System.out.println("CurrentPAge: " + currentPage);
        try {
            //System.out.println("Iniciar Links HashSet");

            doc = Jsoup.connect(currentPage).get();
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String linkUrl = link.attr("abs:href");
                //System.out.println("link encontrado: " + linkUrl);
                //System.out.println("currenPage: "+ currentPage);
                if (linkUrl.equals(currentPage)) {
                    System.out.println("Link já inserido");
                    continue;
                }
                //System.out.println("New Link" + linkUrl);
                linksOnPage.add(linkUrl);
            }
            //System.out.println("LinksOnPage: " + linksOnPage);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return linksOnPage;
    }


    /**
     * Obtèm a informação do o url inserido pelo user e adiciona aos hashmaps.
     * Funcionalidade 3 do menu cliente.
     * 
     * @param fila - fila de links
     */
    public synchronized void GetInfoOneTime(Queue<String> fila) throws java.lang.IllegalArgumentException {
        //System.out.println("----------Started GetInfoOneTime------");
        while (App.getQueueInUse() == true) {
            try {
                System.out.println("Waiting in GetInfoOneTime");
                wait();
            } catch (InterruptedException e) {
                System.out.println("interruptedException caught");
            }
        }
        URL nw;
        Document doc = null;
        try {
            while (fila.isEmpty() != true) {
                App.setQueueInUse(true);
                String url = fila.poll();
                //System.out.println("url: " + url);
                nw = new URL(url);
                // System.out.printf("url: %s\n", url);
                App.setQueueInUse(false);
                notify();

                if (alreadyUsed(url) == 0) {
                    // System.out.println("Link não usado");

                    // Create Ocurrences Hash
                    try {
                        doc = Jsoup.connect(url).get();
                    } catch (Exception e) {
                        System.out.println("URL inválida");
                        break;
                    }

                    String content = doc.body().text();
                    String[] words = content.split("\\s+");

                    for (String word : words) {
                        word = word.replaceAll("[^a-zA-ZçÇÁáÉéêÍíôóÓÀàÔÕõúÚÃã]+", "");

                        if (word.isEmpty()) {
                            continue;
                        }

                        while (App.HashInUse == false) {
                            App.setHashInUse(true);
                            Set<URL> linksSet = ocorrencias.get(word);
                            if (linksSet == null) {
                                linksSet = new HashSet<>();
                                ocorrencias.put(word, linksSet);
                                //System.out.println("URL para inserir:" + nw.toString());
                                linksSet.add(nw);

                            } else {
                                //System.out.println("URL para inserir:" + nw.toString());
                                linksSet.add(nw);

                            }
                        }
                        App.setHashInUse(false);
                        notify();
                    }
                    // System.out.println("Ocorrencias Hash criada");

                } else {
                    // System.out.println("Link já usadooooo");
                    App.setQueueInUse(false);
                    notify();
                }


                System.out.println("Iniciar Links Hash");

                doc = Jsoup.connect(url).get();
                Elements links = doc.select("a[href]");
                Set<URL> linksSet = new HashSet<>();

                for (Element link : links) {
                    String linkUrl = link.attr("abs:href");
                    System.out.println("link: " + linkUrl);

                    System.out.println("url: "+ url);
                    if (linkUrl.equals(url)) {
                        System.out.println("Link já inserido");
                        continue;
                    }

                    nw = new URL(linkUrl);
                    System.out.println("New Link +LinksSet: " + nw);
                    linksSet.add(nw);
                    linkHashMap.put(url, linksSet);

                }
                // System.out.println("linkHashMap completed");

            }
            System.out.println("LinkHashMap: " + linkHashMap.toString());
            //System.out.println("Ocorrencias: " + ocorrencias.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println("----------Finished GetInfoOneTime------");
    }

}