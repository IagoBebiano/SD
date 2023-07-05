package com.example.servingwebcontent;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Client extends UnicastRemoteObject {
    public static RmiInterface server;
    // public static RMIClient_I client;

    protected Client() throws RemoteException {
        super();
    }

    public static int res;

    /**
     * Liga o Cliente ao SearchModule através do RMI
     * Apresenta um menu com as opções disponíveis
     * User insere a opção desejada e o programa executa a ação podendo pedir mais
     * inputs
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            RmiInterface sc = (RmiInterface) LocateRegistry.getRegistry(7000).lookup("rmiserver");
            Scanner scanner = new Scanner(System.in);
            String input, username, aux, url, password, pesquisa;
            char[] pass_aux;
            boolean run = true;
            boolean login = true;

            while (run) {
                System.out.println("-----   MENU    -----");
                System.out.println("1. Registar na plataforma;");
                System.out.println("2. Realizar login;");
                System.out.println("3. Indexar novo URL;");
                System.out.println("4. Indexar todos os URLs encontrados;");
                System.out.println("5. Pesquisa por termo;");
                System.out.println("6. Lista de paginas com ligacao;");
                System.out.println("7. Pagina de administracao (10 Pesquisas mais comuns);");
                System.out.println("8. Terminar ligacao;\n");
                System.out.println("Selecione uma opcao: ");

                input = scanner.nextLine();

                if (input.equals("1")) {
                    while (true) {
                        System.out.println("Introduza o username: ");
                        username = scanner.nextLine();
                        System.out.println("Insira a password: ");
                        pass_aux = System.console().readPassword();
                        password = new String(pass_aux);

                        if (sc.registo(username, password)) {
                            System.out.println("Registo executado com sucesso!");
                            break;
                        } else {
                            System.out.println("O username existente.");
                        }
                    }

                } else if (input.equals("2")) {
                    while (true) {
                        System.out.println("Username: ");
                        username = scanner.nextLine();
                        System.out.println("Password: ");
                        pass_aux = System.console().readPassword();
                        password = new String(pass_aux);
                        aux = sc.login(username, password);

                        if (aux.equals("true")) {
                            System.out.println("Login efetuado com sucesso!\n\n");
                            login = true;
                            break;

                        } else if (aux.equals("username")) {
                            System.out.println("Nome de utilizador invalido.");
                        } else {
                            System.out.println("Password invalida.");
                        }
                    }
                } else if (input.equals("3")) {
                    if (login == true) {

                        System.out.println("Insira o URL: ");

                        url = scanner.nextLine();
                        if (url.contains("http")) {
                            sc.UrlInsert(url);
                        } else {
                            // caso seja url inválido
                            System.out.println("URL invalido.");
                        }

                        // String url1 = "https://pt.wikipedia.org/wiki/Fernando_Pimenta";
                        // String url2 = "https://pt.wikipedia.org/wiki/Emanuel_Silva";
                        // url = "https://www.jn.pt/";
                        // url = "https://www.abola.pt/";

                    } else {
                        System.out.println("Login necessário!");
                    }
                } else if (input.equals("4")) {
                    if (login == true) {
                        sc.IndexAut();
                    } else {
                        System.out.println("Login necessário!");
                    }
                } else if (input.equals("5")) {
                    System.out.println("Pesquisa: ");
                    pesquisa = scanner.nextLine();
                    if (login == true) {
                        Set<URL> result = new HashSet<URL>();
                        try {

                            if (sc.notificacaoPesquisa(pesquisa).isEmpty()) {
                                System.out.println("Nao foram encontrados resultados para a pesquisa.");
                            } else {
                                result = sc.notificacaoPesquisa(pesquisa);
                                System.out.println("Links associados:");

                                for (URL url2 : result) {
                                    System.out
                                            .println("title: " + url2.title + " url: " + url2.url + "citation: "
                                                    + url2.citation);
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Exception in main: " + e);
                        }

                    } else {
                        System.out.println("Login necessário!");
                    }
                } else if (input.equals("6")) {
                    System.out.println("Pesquisa: ");
                    pesquisa = scanner.nextLine();
                    if (login == true) {
                        Set<URL> result;
                        result = sc.getLinksToPage(pesquisa);
                        System.out.println("Links associados:" + result);
                    }

                } else if (input.equals("7")) {
                    if (login == true) {
                        List<String> result;
                        result = sc.resultsRank();
                        System.out.println("********Mais Pesquisado********");
                        for (String url2 : result) {
                            System.out.printf("%s\n", url2);
                        }
                    }
                } else if (input.equals("8")) {
                    run = false;
                } else {
                    System.out.println("Input Invalido.");
                }
            }
            scanner.close();
        } catch (

        Exception e) {
            System.out.println("Exception in main: " + e);
            e.printStackTrace();
        }
    }

}