
/*package com.example.servingwebcontent;


import com.example.servingwebcontent.RmiInterface;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.List;

@Controller
public class WebSocketController {

    @MessageMapping("/admin")
    @SendTo("/topic/admin")
    public String handleUpdate(String message) {
        // Process the received message and generate updates
        String update = generateUpdate();

        // Send the update to connected clients
        return update;
    }

    private String generateUpdate() {
        // Logic to generate updates
        return "New update";
    }

    @MessageMapping("/getTopSearches")
    @SendTo("/topic/topSearches")
    public List<String> getTopSearches() throws RemoteException {
        // Your logic to retrieve the top 10 searches
        RmiInterface sc = null;
        List<String> result;
        try {
            sc = (RmiInterface) LocateRegistry.getRegistry(7000).lookup("rmiserver");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
        List<String> topSearches = sc.resultsRank();

        return topSearches;
    }
}*/
