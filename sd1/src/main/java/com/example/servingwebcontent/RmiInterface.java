package com.example.servingwebcontent;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;


public interface RmiInterface extends Remote {
    /**
     * Verifica se o utilizador existe e se a password está correta para realizar o
     * login
     * 
     * @param user
     * @param pass
     * @return
     * @throws RemoteException
     */
    public String login(String user, String pass) throws RemoteException;

    /**
     * Regista um novo utilizador na plataforma
     * 
     * @param user
     * @param pass
     * @return
     * @throws RemoteException
     */
    public boolean registo(String user, String pass) throws RemoteException;

    /**
     * Adiciona um novo URL à lista de URLs
     * 
     * @param url
     * @throws RemoteException
     * @throws java.lang.IllegalArgumentException
     */
    public void UrlInsert(String url) throws RemoteException, java.lang.IllegalArgumentException;

    /**
     * Inicializa um novo Downloader para indexar todos os URLs da fila de espera
     * Pode ser chamado a partir do menu do cliente com a funcionalidade "4. Indexar
     * todos os URLs encontrados"
     * 
     * @throws RemoteException
     */
    public void IndexAut() throws RemoteException;

    /**
     * Devolve uma lista com os URLs que contem os termos passados como argumento
     * Usada para a funcionalidade 3. Pesquisa de URLs
     * 
     * @return Set<URL> lista com os URLs que contem os termos passados como
     *         argumento
     * @throws RemoteException
     */
    public Set<URL> notificacaoPesquisa(String termos) throws RemoteException;

    /**
     * Devolve uma lista com todos os URLs que apontam para a pagina passada como
     * argumento
     *
     * @param page url da pagina
     * @return Set<String>
     * @throws RemoteException
     */
    public Set<URL> getLinksToPage(String page) throws RemoteException;

    /**
     * Obtem a carga do servidor
     * 
     * @return
     * @throws RemoteException
     */
    public int getCarga() throws RemoteException;

    /**
     * Devolve uma lista com os URLs ordenados por relevancia
     * Usada para a funcionalidade 5. Ranking de URLs
     * 
     * @return List<String> lista com os URLs ordenados por relevancia
     * @throws RemoteException
     */
    public List<String> resultsRank() throws RemoteException;
}
