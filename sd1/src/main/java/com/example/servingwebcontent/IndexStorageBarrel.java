package com.example.servingwebcontent;

//import java.net.MalformedURLException;
import org.springframework.context.annotation.Bean;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
//import java.rmi.Naming;
//import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
//import java.util.HashMap;
import java.util.Map.Entry;
//import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe que guarda atributos do Servidor
 */

class Server {

    protected int id;
    protected int carga;

    protected ArrayList<String> mensagens = new ArrayList<String>();
    protected int porto;
    protected String ip;

    public Server(int id, int carga) {
        this.id = id;
        this.carga = carga;
    }
}

/**
 * Classe principal
 */
public class IndexStorageBarrel extends Thread implements RMIInterface_ISB, Serializable {
    Set<String> linksSetaux;
    Set<String> linksSetauxaux;
    String palavra;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    String auxSaveWord = "";
    String auxSaveLink = "";
    protected Server server;
    Info inf = new Info();
    protected RMIInterface_ISB r;
    RmiInterface sm;
    int id, min;

    // ArrayList de servidores multicast online
    protected ArrayList<Server> serversOn = new ArrayList<>();

    /**
     * Inicia o servidor e prepara ligação por RMI ao SeachModule
     * 
     * @param args
     * @throws RemoteException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws RemoteException, InterruptedException {
        try {
            IndexStorageBarrel server = new IndexStorageBarrel();
            server.start();
            LocateRegistry.createRegistry(1099).rebind("rmibarrel", (Remote) server);
            System.out.println("Servidor Primario");
        } catch (RemoteException re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }

    public IndexStorageBarrel() throws RemoteException, InterruptedException {
        // Id random de servidor
        super();
        int num = (int) (Math.random() * 1000);
        // System.out.println ("SERVER " + num + " READY!!\n");

        // Para dar tempo aos outros servidores para fazer uma conexao
        // Thread.sleep(10000);

        server = new Server(num, 0);
        serversOn.add(server);
        r = (RMIInterface_ISB) UnicastRemoteObject.exportObject(this, 1099);
    }

    /**
     * Recebe informacao do Dowloader e guarda no hashmap
     * Caso receba a String "OCORRENCIAS" irá ficar à espera de receber a
     * palavra(key) e de seguida o Set<URL> correspondente
     * Caso receba a String "LINKS" irá ficar à espera de receber a link(key) e de
     * seguida o Set<URL> correspondente
     */

    public synchronized void run() {

        loadObjFile();
        MulticastSocket socket = null;
        InetAddress group = null;
        int alternate = 0;
        String check;

        try {
            socket = new MulticastSocket(PORT); // create socket and bind it
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            Set<URL> receivedMap2 = null;
            String key = "";

            while (true) {
                // System.out.println("Alternate: " + alternate);
                byte[] buffer = new byte[65515];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byte[] bytesMapa = packet.getData();

                if (alternate == 0) {
                    check = new String(bytesMapa, 0, packet.getLength());
                    // System.out.println("Check: " + check);
                    if (check.equals("OCORRENCIAS")) {
                        alternate = 1;
                        continue;
                    }
                    if (check.equals("LINKS")) {
                        alternate = 3;
                        continue;
                    }
                }

                try {
                    // Espera receber a palavra(key) e de seguida o Set<URL> correspondente
                    if (alternate == 1) {
                        key = new String(bytesMapa, 0, packet.getLength());
                        // System.out.println("Recebendo key de ocorrencias: " + key);
                        if (Info.ocorrencias.get(key) == null) {
                            Info.ocorrencias.put(key, new HashSet<URL>());
                        }
                        alternate = 2;

                    }

                    if (alternate == 2) {
                        socket.receive(packet);
                        ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
                        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
                        Object obj = (Set<URL>) objectStream.readObject();// your object
                        // System.out.println("askfnkiasd");
                        receivedMap2 = (Set<URL>) obj;
                        Info.ocorrencias.get(key).addAll(receivedMap2);
                        // do something with mySet
                        alternate = 0;
                    }

                    // Espera receber o link(key) e de seguida o Set<URL> correspondente
                    if (alternate == 3) {
                        key = new String(bytesMapa, 0, packet.getLength());
                        // System.out.println("Recebendo key de links: " + key);
                        if (Info.linkHashMap.get(key) == null) {
                            Info.linkHashMap.put(key, new HashSet<URL>());
                        }
                        alternate = 4;

                    }

                    if (alternate == 4) {
                        // System.out.println("Recebendo set links...");
                        socket.receive(packet);
                        ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
                        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
                        Object obj = (Set<URL>) objectStream.readObject();// your object
                        // System.out.println("askfnkiasd");
                        receivedMap2 = (Set<URL>) obj;
                        Info.linkHashMap.get(key).addAll(receivedMap2);

                        // do something with mySet
                        alternate = 0;

                    }
                    System.out.println("linkHashMap " + Info.linkHashMap);
                    System.out.println("ocorrencias " + Info.ocorrencias);


                } catch (EOFException e) {
                    System.err.println("End of input stream reached unexpectedly: " + e.getMessage());
                    continue;
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error deserializing data: " + e.getMessage());
                    continue;
                }
            }

        } catch (IOException e) {
            System.err.println("Error creating or joining multicast group: " + e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.leaveGroup(group);
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Recebe a URL e incrementa o numero de vezes que foi pesquisada
     * 
     * @param url URL a ser incrementada
     */
    public void MaisPesquisadas(String url) {
        Integer key;
        try {
            if (Info.maisPesquisado.get(url) == null) {
                Info.maisPesquisado.put(url, 1);
            } else {
                key = Info.maisPesquisado.get(url);
                Info.maisPesquisado.put(url, key + 1);
            }
        } catch (NullPointerException e) {
            System.out.println("Erro no MaisPesquisadas: " + e.getMessage());
        }
    }

    /**
     * Procura ligar-se ao servidor com menor carga
     */
    public int ReturnMenorCarga() throws RemoteException {
        id = serversOn.get(0).id;
        min = serversOn.get(0).carga;

        for (Server servidor : serversOn) {
            if (servidor.carga < min) {
                min = servidor.carga;
                id = servidor.id;
            } else if (servidor.carga == min) {
                if (servidor.id < id) {
                    min = servidor.carga;
                    id = servidor.id;
                }
            }
        }
        saveObjFile();
        return id;
    }

    /**
     * Prints hashmap
     * 
     * @param i 1 = ocorrencias, 2 = links
     */
    private void printHashMap(int i) {
        String key2;
        Set<URL> value;
        if (i == 1) {
            for (Entry<String, Set<URL>> entry : Info.ocorrencias.entrySet()) {
                key2 = entry.getKey();
                value = entry.getValue();
                System.out.println("key:" + key2);
                for (URL url2 : value) {
                    System.out
                            .println("title: " + url2.title + " url: " + url2.url + "citation: "
                                    + url2.citation);
                }
            }
        }

        if (i == 2) {
            for (Entry<String, Set<URL>> entry : Info.linkHashMap.entrySet()) {
                key2 = entry.getKey();
                value = entry.getValue();
                System.out.println("key:" + key2);
                for (URL url2 : value) {
                    System.out
                            .println("title: " + url2.title + " url: " + url2.url + "citation: "
                                    + url2.citation);
                }
            }
        }

    }

    /**
     * Envia a mensagen
     * 
     * @param message
     * @throws RemoteException
     */
    public void sendMessage(String message) throws RemoteException {
        System.out.println("Mensagem recebida do cliente: " + message);
    }

    /**
     * Carrega o ficheiro de dados guardado, preenchendo as hashmaps com os dados
     */
    public void loadObjFile() {
        File f = new File("UserData.obj");
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            inf = (Info) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("File not found or working.");
            saveObjFile();
        }
    }

    /**
     * Guarda o ficheiro de dados quando o cliente encerra a sessão corretamente
     */
    public void saveObjFile() {
        File f = new File("UserData.obj");

        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(inf);
            oos.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error creating file.");
        } catch (IOException e) {
            System.out.println("Error writing file.");
        }
    }

    /**
     * Procura as palavras na hashmap e retorna os links comuns a estas
     * Chama a funcao Importancia para ordenar os links
     * 
     * @param termos palavras a procurar
     * @return links comuns
     */
    public Set<URL> pesquisaAntiga(String termos) throws RemoteException, ConcurrentModificationException {
        System.out.println("---Entering pesquisa---");
        System.out.println("Pesquisa recebida: " + termos);
        String[] words = termos.split("\\s+");
        Set<URL> linksComuns = new HashSet<>();

        for (String palavra : words) {
            Set<URL> linksDaPalavra = Info.ocorrencias.get(palavra);
            if (linksDaPalavra != null) {
                if (linksComuns.isEmpty()) {
                    // se este é o primeiro conjunto, apenas copia para o conjunto comum
                    linksComuns.addAll(linksDaPalavra);
                    // System.out.println("links comuns1: " + linksComuns);
                } else {
                    // mantém apenas os elementos comuns entre os conjuntos
                    for (URL links : linksComuns) {
                        if (linksDaPalavra.contains(links)) {
                            linksComuns.add(links);
                        }
                    }
                }
            } else {
                System.out.println("Palavra não encontrada: " + palavra);
            }
        }
        if (linksComuns.isEmpty()) {

        }
        System.out.println("links comuns: " + linksComuns);
        Set<URL> imp = Importancia(linksComuns);
        System.out.println("Importancia: " + imp);
        //Set<URL> set = new HashSet<URL>(imp);
        for (URL url : imp) {
            System.out.println("url: " + url.url);
            MaisPesquisadas(url.url);
        }
        saveObjFile();
        System.out.println("---Exiting pesquisa---");
        return (Set<URL>) (imp != null ? imp : Collections.emptySet());

    }

    public Set<URL> pesquisa(String termos) throws RemoteException, ConcurrentModificationException {
        System.out.println("---Entering pesquisa---");
        System.out.println("Pesquisa recebida: " + termos);
        MaisPesquisadas(termos);
        String[] words = termos.split("\\s+");
        Set<URL> linksComuns = new HashSet<>();

        for (String palavra : words) {
            Set<URL> linksDaPalavra = Info.ocorrencias.get(palavra);
            if (linksDaPalavra != null) {
                if (linksComuns.isEmpty()) {
                    // se este é o primeiro conjunto, apenas copia para o conjunto comum
                    linksComuns.addAll(linksDaPalavra);
                    // System.out.println("links comuns1: " + linksComuns);
                } else {
                    // mantém apenas os elementos comuns entre os conjuntos
                    for (URL links : linksComuns) {
                        if (linksDaPalavra.contains(links)) {
                            linksComuns.add(links);
                        }
                    }
                }
            } else {
                System.out.println("Palavra não encontrada: " + palavra);
            }
        }
        if (linksComuns.isEmpty()) {
        }

        System.out.println("links comuns: " + linksComuns);

        Set<URL> ordenados = ordenadorImportancia(linksComuns);
        System.out.println("Importancia: " + ordenados);
        saveObjFile();
        System.out.println("---Exiting pesquisa---");
        return (Set<URL>) (ordenados != null ? ordenados : Collections.emptySet());

    }

    /**
     * 10 pesquisas mais comuns realizadas pelos utilizadores, funcionalidade 7 do
     * menu
     * 
     * @return Top 10 de links ordenados por importancia
     */
    @Bean
    public List<String> ranking() throws RemoteException {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(Info.maisPesquisado.entrySet());
        System.out.println("List:" + list);
        List<String> rank = new ArrayList<>();
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        System.out.println(list);

        for (int i = 0; i < 10 && i < list.size(); i++) {
            rank.add(list.get(i).getKey());
        }
        return rank;
    }

    /**
     * Devolve um Set de links ordenados por importancia
     * Quanto mais ligações tiver um link, mais importante é
     * @param links
     * @return
     * @throws RemoteException
     * @throws ConcurrentModificationException
     */
    public Set<URL> ordenadorImportancia(Set<URL> links) throws RemoteException, ConcurrentModificationException {
        System.out.println("------Enter getLinksToPageInverso------");
        //System.out.println("Links recebidos: " + links);
        Set<URL> linksToPage = new LinkedHashSet<>();
        Set<String> linksToPageAux = new HashSet<>();
        Set<String> linksVazios = new HashSet<>();
        int max = -1;
        int size = links.size();
        //System.out.println("Size: " + size);
        String aux = null;
        URL aux2 = null;

        for(int i = 0; i < size; i++) {
            for (URL url : links) {
                //System.out.println("URL A PROCURAR: " + url.url);
                if(Info.linkHashMap.get(url.url) == null) {
                    linksVazios.add(url.url);
                    continue;
                }
                for (Entry<String, Set<URL>> entry : Info.linkHashMap.entrySet()) {
                    if (entry.getKey().equals(url.url)) {
                        System.out.println("entry.getKey(): " + entry.getKey() + " url.url: " + url.url);
                        System.out.println("Max: " + max + " Tamanho: " + entry.getValue().size());
                        System.out.println("linksToPageAux: " + linksToPageAux);
                        if (max < entry.getValue().size() && !linksToPageAux.contains(url.url)) {
                            System.out.println("Entrou no if");
                            System.out.println(" url.url: " + url.url);
                            max = entry.getValue().size();
                            aux2 = new URL(entry.getKey());
                            aux = url.url;
                        }
                    }
                }
            }
            if(aux2 != null) {
                System.out.println("LINK INSERIDO: " + aux2);
                linksToPage.add(aux2);
                linksToPageAux.add(aux);
                aux2 = null;
            }
            max = -1;
        }
        for(String s : linksVazios) {
            linksToPage.add(new URL(s));
        }
        System.out.println("linksToPage: " + linksToPage);
        System.out.println("------Exit getLinksToPageInverso------");
        return linksToPage;
    }

    /**
     * Consulta lista de páginas com ligação para uma página específica.
     * Funcionalidade 6 do menu
     *
     * @param page página a procurar
     * @return lista de páginas
     */
    public Set<URL> getLinksToPage(String page) throws RemoteException, ConcurrentModificationException {
        System.out.println("------Enter getLinksToPage------");
        System.out.println("Page: " + page);
        Set<URL> linksToPage = new HashSet<>();
        //MaisPesquisadas(page);           ---------------------------> Se for preciso adicionar a página às mais pesquisadas DESCOMENTAR

        for (Entry<String, Set<URL>> entry : Info.linkHashMap.entrySet()) {
            if (entry.getKey().equals(page)) {
                linksToPage = entry.getValue();

                }
            }
        linksToPage = ordenadorImportancia(linksToPage);
        return linksToPage;
    }

    /**
     * Resultados de pesquisa ordenados por importância
     * 1. Verificar o numero de links associados a cada link
     * 2. Ordenar os links por ordem decrescente de links associados
     * 3. Retornar os links ordenados
     *
     * @param linkss links a ordenar
     * @return linksOrdenados links ordenados
     * @throws RemoteException
     * @throws ConcurrentModificationException
     */

    public Set<URL> Importancia(Set<URL> linkss) throws RemoteException, ConcurrentModificationException {

        int MaxSize = 0;
        Set<URL> linksOrdenados = new HashSet<URL>();
        URL auxLink = new URL();
        int bullet = linkss.size();
        for (int i = 0; i < bullet; i++) {
            for (URL link2 : linkss) {
                if (getLinksToPage(link2.url).size() >= MaxSize) {  //ALGO AQUI ESTA ERRADO
                    MaxSize = getLinksToPage(link2.url).size();
                    auxLink = link2;
                }
            }
            linksOrdenados.add(auxLink);
            linkss.remove(auxLink);
            MaxSize = 0;
        }
        saveObjFile();
        return linksOrdenados;
    }



    /**
     * Metodo implementado do RMI que recebe uma pesquisa e retorna o resultado da
     * mesma
     */
    public Set<URL> getInformation(String termos) throws RemoteException {
        Set<URL> result;
        result = pesquisa(termos);
        return result;
    }

}

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    String username;
    String password;
    ArrayList<String> searches = new ArrayList<>();
    User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

class Info implements Serializable {
    private static final long serialVersionUID = 1L;
    ArrayList<User> users = new ArrayList<>();
    static ConcurrentHashMap<String, Set<URL>> receivedMap = null;
    static ConcurrentHashMap<String, Set<URL>> ocorrencias = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Set<URL>> linkHashMap = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Integer> maisPesquisado = new ConcurrentHashMap<>();
}