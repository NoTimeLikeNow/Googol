package search;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.*;

public class IndexServer extends UnicastRemoteObject implements Index {
    BlockingQueue<String> urlsToIndex;

    ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicInteger>> indexedItems;

    public IndexServer() throws RemoteException {
        super();
        urlsToIndex = new LinkedBlockingQueue<String>();   
        indexedItems = new ConcurrentHashMap<>();    
    }


    public static void main(String args[]) {
        try {
            IndexServer server = new IndexServer();
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
            //TODO: This approach needs to become interactive. Use a Scanner(System.in) to create a rudimentary user interface to:
            //1.  Add urls for indexing
            //2. search indexed urls
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

    public void addToIndex(String word, String url) throws java.rmi.RemoteException {
        //ensure the word is in the map, other methods aren't thread safe
        indexedItems.computeIfAbsent(word, k -> new ConcurrentHashMap<>());
    
        //ensure the URL entry is present 
        indexedItems.get(word).computeIfAbsent(url, k -> new AtomicInteger(0));
    
        //increment the count
        indexedItems.get(word).get(url).incrementAndGet();
    }

    
    public List<String> searchWord(String word) throws java.rmi.RemoteException {
        word = word.toLowerCase();
        ConcurrentHashMap<String, AtomicInteger> urls = indexedItems.get(word);
        
        //avoids NullPointerException, God knows i had enough of those
        if (urls == null) return Collections.emptyList(); 
        
        return new ArrayList<>(urls.keySet());
    }
}
