package com.example.servingwebcontent;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.example.servingwebcontent.hackernews.HackerNewsItemRecord;
import com.example.servingwebcontent.hackernews.HackerNewsItemResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;





@Controller
public class GreetingController {
    
    private SimpMessagingTemplate simpMessagingTemplate;

    //@Autowired
    /*public GreetingController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }*/



    
    @GetMapping("/")
    public String redirect() {
        return "redirect:/search";
    }

    ///PROJETO\\\\

    @GetMapping("/admin")
    public  String admin(Model model) throws RemoteException {
        RmiInterface sc = null;
        List<String> result;
        try {
            sc = (RmiInterface) LocateRegistry.getRegistry(7000).lookup("rmiserver");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }


        result = sc.resultsRank();
        model.addAttribute("ranking", result);
        return "admin2";
    }
    
    @Scheduled(fixedRate = 2000)
    public void returnsResults() throws AccessException, RemoteException, NotBoundException {
        RmiInterface sc = null;
        sc = (RmiInterface) LocateRegistry.getRegistry(7000).lookup("rmiserver");

        try {
            simpMessagingTemplate.convertAndSend("/topic/messages", sc.resultsRank()); 
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }


    @GetMapping("/consult")
    public String consult() {
        return "consult";
    }

    @PostMapping("/consult")
    public String consult(@RequestParam("query") String query, Model model) throws RemoteException {
        RmiInterface sc = null;
        Set<URL> result = new HashSet<URL>();
        try {
            sc = (RmiInterface) LocateRegistry.getRegistry(7000).lookup("rmiserver");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }

        if (sc.getLinksToPage(query).isEmpty()) {
            System.out.println("Link não apresenta ligações vindas de outros urls.");
        } else {
            result = sc.getLinksToPage(query);
            model.addAttribute("consultResults", result);

        }
        return "consult_results";
    }


    @GetMapping("/search")
    public String searchForm () {
        return "search"; // Thymeleaf template name for the submission form
    }

    @PostMapping("/search")
    public String search(@RequestParam("query") String query, Model model) throws RemoteException {
        RmiInterface sc = null;
        Set<URL> result = new HashSet<URL>();
        Set<URL> aux = new HashSet<URL>();
        try {
            sc = (RmiInterface) LocateRegistry.getRegistry(7000).lookup("rmiserver");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        if (sc.notificacaoPesquisa(query).isEmpty()) {
            System.out.println("Nao foram encontrados resultados para a pesquisa.");
        } else {
            aux = hackerNewsTopStories(query);
            result = sc.notificacaoPesquisa(query);
            System.out.println("Links associados:");
            model.addAttribute("searchResults", result);
            model.addAttribute("searchResults2", aux);
        }
        

            // Add the search results or relevant data to the model
            // This data can be displayed in the search results page

        return "search_results"; // Thymeleaf template name for displaying search results

    }


        // Display the form to submit a new URL
        @GetMapping("/index")
        public String submitForm () {
            return "index_form"; // Thymeleaf template name for the submission form
        }

        // Process the submitted URL and perform indexing
        @PostMapping("/index")
        public String processSubmission (@RequestParam("url") String url, Model model){
            RmiInterface sc = null;
            try {
                sc = (RmiInterface) LocateRegistry.getRegistry(7000).lookup("rmiserver");
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            } catch (NotBoundException e) {
                throw new RuntimeException(e);
            }
            // Perform indexing logic here
            // You can access the submitted URL via the 'url' parameter

            if (url.contains("http")) {
                try {
                    sc.UrlInsert(url);
                    sc.IndexAut();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // caso seja url inválido
                System.out.println("URL invalido.");
            }
            // Extract words from the text of the URL and associate them in the inverted index
            // Implement the crawler or downloader logic to fetch and parse the URL's content

            // Add a success message or any other relevant data to the model
            // This data can be displayed on a confirmation page

            return "confirmation_page"; // Thymeleaf template name for the confirmation page
        }






        public Set<URL> hackerNewsTopStories(@RequestParam(value = "search") String search) throws RemoteException, IllegalArgumentException {

            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "https://hacker-news.firebaseio.com/v0/topstories.json";
            Long[] topStoryIds = restTemplate.getForObject(apiUrl, Long[].class);
            System.out.println(topStoryIds);
            List<HackerNewsItemRecord> hackerNewsItems = new ArrayList<>();
            Set<URL> set = new HashSet<>();
            URL nw = new URL();
    
            // Iterate through each ID and fetch the story details
            for (int i = 0; i < 10; i++) {
                long itemId = topStoryIds[i];
                String itemUrl = String.format("https://hacker-news.firebaseio.com/v0/item/%d.json", itemId);
                HackerNewsItemResponse itemResponse = restTemplate.getForObject(itemUrl, HackerNewsItemResponse.class);
                System.out.println(itemResponse);
                // Check if the story contains the search terms (if provided)
                if (search == null || itemResponse.getTitle().toLowerCase().contains(search.toLowerCase())) {
                    HackerNewsItemRecord itemRecord = new HackerNewsItemRecord(itemResponse.getId(), null, itemResponse.getTitle(), itemResponse.getUrl(), itemId, itemUrl, null, itemUrl, i, hackerNewsItems, i, hackerNewsItems, i, itemUrl, itemUrl);
                    nw = new URL(itemResponse.getUrl());
                    set.add(nw);
                    //sc.UrlInsert(itemResponse.getUrl());
                }
            }
    
            //model.addAttribute("items", hackerNewsItems);
            return set;
        }

    }