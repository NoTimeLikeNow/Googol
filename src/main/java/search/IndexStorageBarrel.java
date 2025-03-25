package search;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.*;

public class IndexStorageBarrel extends UnicastRemoteObject implements Index {
    BlockingQueue<String> urlsToIndex;

    ConcurrentHashMap<String, Set<String>> indexedItems;
    ConcurrentHashMap<String, ConcurrentHashMap<String, Boolean>> foundURLs;

    // Custom comparator to sort by the size of the set
    Comparator<String> comparator = Comparator.<String>comparingInt(key -> foundURLs.get(key).size()).reversed();

    ConcurrentSkipListMap <String, ConcurrentHashMap<String, Set<String>>> rankings;


 
    public IndexStorageBarrel() throws RemoteException {
        super();
        urlsToIndex = new LinkedBlockingQueue<String>();   
        indexedItems = new ConcurrentHashMap<>();   
        foundURLs = new ConcurrentHashMap<>();
        rankings = new ConcurrentSkipListMap<>(comparator);

    }


    public static void main(String args[]) {
        try {
            IndexStorageBarrel server = new IndexStorageBarrel();
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
            registry.rebind("IndexStorageBarrel", server);

            
        
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        while(true);
    }

    private long counter = 0, timestamp = System.currentTimeMillis();;

  

    public void addToIndex(Set<String> words, String url, String title, Set<String> links) throws java.rmi.RemoteException{
        synchronized(indexedItems){
            for(String word : words){   
                indexedItems.computeIfAbsent(word, k -> new HashSet<>());
                
                indexedItems.get(word).add(url);
            }
        }
        
        for(String link : links){   
            synchronized(foundURLs){
                foundURLs.computeIfAbsent(link, k -> new ConcurrentHashMap<>());
                
                foundURLs.get(link).putIfAbsent(url, false);
            }
            synchronized(rankings){
                //ERRO
                rankings.computeIfAbsent(link, k -> new ConcurrentHashMap<>());
                
                rankings.get(link).computeIfAbsent(title, k -> new HashSet<>());

                rankings.get(link).get(title).add(url);
            }
        }
        

    }

    
    public List<String> searchWord(String search) throws java.rmi.RemoteException {
        search = search.toLowerCase();
        String[] words = search.split(search, ' ');
        ArrayList<String> results;
        
        Set<String> urls = indexedItems.get(words[0]);
        for (String word : words){
            urls.retainAll(indexedItems.get(word));
        }
        
        //avoids NullPointerException, God knows we've all had enough of those
        if (urls == null || urls.isEmpty()) return Collections.emptyList(); 
        
        Set<String> temp = rankings.keySet();
        for (String url : temp){
            for(String result : urls){
                if (temp.contains(result)) {
                    results.add(ranking.get(result))
                }
            }
        }
        
        return results;
    }
}