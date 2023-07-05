package com.example.servingwebcontent;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Set;

public interface RMIInterface_ISB extends Remote {
    /**
     * Procura as palavras na hashmap e retorna os links comuns a estas
     * Chama a funcao Importancia para ordenar os links
     * 
     * @param termos palavras a procurar
     * @return links comuns
     */
    public Set<URL> pesquisa(String termos) throws RemoteException;

    /**
     * Metodo implementado do RMI que recebe uma pesquisa e retorna o resultado da
     * mesma
     */
    public Set<URL> getInformation(String termos) throws RemoteException;

    /**
     * Devolve uma lista com todos os URLs que apontam para a pagina passada como
     * argumento
     *
     * @return Set<String>
     */
    public Set<URL> getLinksToPage(String page) throws RemoteException, ConcurrentModificationException;

    /**
     * Procura ligar-se ao servidor com menor carga
     */
    public int ReturnMenorCarga() throws RemoteException;

    /**
     * 10 pesquisas mais comuns realizadas pelos utilizadores, funcionalidade 7 do
     * menu
     * 
     * @return Top 10 de links ordenados por importancia
     */
    public List<String> ranking() throws RemoteException;
}