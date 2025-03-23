package search;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.*;

public class Gateway extends UnicastRemoteObject implements Gate {
    BlockingQueue<String> urlsToIndex;
    CopyOnWriteArrayList<String> Barrels;

    public Gateway() throws RemoteException {
        super();
        urlsToIndex = new LinkedBlockingQueue<String>();   
    }


    public static void main(String args[]) {
        try {
            Gateway server = new Gateway();
            Registry registry = LocateRegistry.createRegistry(8183);
            registry.rebind("index", server);
            String input = "";
            Scanner scanner = new Scanner(System.in);
            server.putNew("https://pt.wikipedia.org/wiki/Wikip%C3%A9dia:P%C3%A1gina_principal");

            while (!"endSearch".equalsIgnoreCase(input)){
                System.out.println("Server ready. Waiting for input...");
                input = scanner.nextLine();

                List<String> result =  server.searchWord(input);
                for (String s : result) System.out.println(s);
            }
            scanner.close();
            
        
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private long counter = 0, timestamp = System.currentTimeMillis();;

    public String takeNext() throws RemoteException {
        String temp;
        try {
            temp = urlsToIndex.take(); //sleeps if the queue is empty
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); //restore interrupted status
            throw new RemoteException("Thread interrupted while adding URL to queue", e);
        }
        return temp;
    }

    public void putNew(String url) throws java.rmi.RemoteException {
        try {
            urlsToIndex.put(url);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); //restore interrupted status
            throw new RemoteException("Thread interrupted while adding URL to queue", e);
        }

    }

    
    public List<String> searchWord(String word) throws java.rmi.RemoteException {
        word = word.toLowerCase();
    
        
        return new ArrayList<>(urls.keySet());
    }
}
