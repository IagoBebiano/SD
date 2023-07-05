package com.example.servingwebcontent;

import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Set;
import java.net.MalformedURLException;
import java.rmi.*;

/**
 * Classe que guarda informacoes sobre o cliente que se liga ao Servidor RMI
 */

public class SearchModule extends UnicastRemoteObject implements RmiInterface {
    private static final long serialVersionUID = 1L;
    public static RMIInterface_ISB server;
    public static RmiInterface sm;

    Info info = new Info();

    protected SearchModule() throws RemoteException {
        super();
    }

    public void SaveInfo(Info inf) {
        info = inf;
    }

    /**
     * Regista um novo utilizador na plataforma
     */
    public boolean registo(String user, String pass) throws RemoteException {
        for (User u : info.users) {
            if (u.username.equals(user)) {
                return false;
            }
        }
        User newUser = new User(user, pass);
        info.users.add(newUser);
        return true;
    }

    /**
     * Verifica se o utilizador existe e se a password estÃ¡ correta para realizar o
     * login
     */
    public String login(String user, String pass) throws RemoteException {
        for (User u : info.users) {
            if (u.username.equals(user)) {
                if (u.password.equals(pass)) {
                    return "true";
                } else {
                    return "password";
                }
            } else {
                return "username";
            }
        }
        return "";
    }

    public synchronized void UrlInsert(String url) throws RemoteException, java.lang.IllegalArgumentException {

        if (App.getQueueInUse() == true) {
            try {
                System.out.println(" waiting...");
                wait();
                System.out.println(" done waiting");
            } catch (Exception e) {
                System.out.println("interruptedException caught");
            }
        }
        App.urls.add(url);
        //App.url = url;
        try {
            Downloader downloader = new Downloader();
            downloader.start();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e);
        }
    }
    /**
     * Insere um novo URL na fila de espera para ser indexado
     * Inicializa um novo Downloader para indexar o URL
     */
    public synchronized void UrlInsert1(String url) throws RemoteException, java.lang.IllegalArgumentException {

        if (App.getQueueInUse() == true) {
            try {
                System.out.println(" waiting...");
                wait();
                System.out.println(" done waiting");
            } catch (Exception e) {
                System.out.println("interruptedException caught");
            }
        }
        App.setQueueInUse(true);
        App.q.add(url);
        App.auxInsertUmaVez.add(url);
        App.setQueueInUse(false);
        notify();
        Client.res = 1;

        try {
            Downloader downloader = new Downloader();
            downloader.start();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e);
        }
    }

    /**
     * Inicializa um novo Downloader para indexar todos os URLs da fila de espera
     * Pode ser chamado a partir do menu do cliente com a funcionalidade "4. Indexar
     * todos os URLs encontrados"
     */
    public void IndexAut() throws RemoteException {
        Client.res = 0;
        try {
            Downloader downloader = new Downloader();
            downloader.start();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e);
        }

    }

    /**
     * Devolve uma lista com os URLs que contem os termos passados como argumento
     * Usada para a funcionalidade 3. Pesquisa de URLs
     * 
     * @return Set<URL> lista com os URLs que contem os termos passados como
     *         argumento
     * @throws RemoteException
     */
    public Set<URL> notificacaoPesquisa(String termos) throws RemoteException {
        System.out.println("Searching for " + termos + " ...");
        return server.getInformation(termos);

    }

    /**
     * Devolve uma lista com os URLs ordenados por relevancia
     * Usada para a funcionalidade 5. Ranking de URLs
     * 
     * @return List<String> lista com os URLs ordenados por relevancia
     * @throws RemoteException
     */
    public List<String> resultsRank() throws RemoteException {
        return server.ranking();
    }

    /**
     * Devolve uma lista com todos os URLs que apontam para a pagina passada como
     * argumento
     *
     * @return Set<String>
     */
    public Set<URL> getLinksToPage(String page) throws RemoteException, ConcurrentModificationException {
        return server.getLinksToPage(page);
    }

    /**
     * Devolve a carga do servidor
     */
    public int getCarga() throws RemoteException {
        return server.ReturnMenorCarga();
    }

    /**
     * Inicializa o servidor RMI
     * 
     * @param args
     * @throws MalformedURLException
     * @throws NotBoundException
     */
    public static void main(String[] args) throws MalformedURLException, NotBoundException {
        try {
            RmiInterface si = new SearchModule();
            server = (RMIInterface_ISB) LocateRegistry.getRegistry(1099).lookup("rmibarrel");
            LocateRegistry.createRegistry(7000).rebind("rmiserver", (Remote) si);
            System.out.println("RMI Server ready...");
        } catch (RemoteException re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }

}