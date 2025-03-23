package search;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.*;

public class IndexServer extends UnicastRemoteObject implements Index {
    private String[] urlsToIndex;
    private String[][] indexedItems;

    public IndexServer() throws RemoteException {
        super();
        //This structure has a number of problems. The first is that it is fixed size. Can you enumerate the others?
        urlsToIndex = new String[10];        
    }

    public static void main(String args[]) {
        try {
            IndexServer server = new IndexServer();
            Registry registry = LocateRegistry.createRegistry(8183);
            registry.rebind("index", server);
            System.out.println("Server ready. Waiting for input...");

            //TODO: This approach needs to become interactive. Use a Scanner(System.in) to create a rudimentary user interface to:
            //1. Add urls for indexing
            //2. search indexed urls
            server.putNew("https://pt.wikipedia.org/wiki/Wikip%C3%A9dia:P%C3%A1gina_principal");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private long counter = 0, timestamp = System.currentTimeMillis();;

    public String takeNext() throws RemoteException {
        try {
            if (counter == 10) {
                System.out.println("Indexing speed: " + (10.0 / (System.currentTimeMillis() - timestamp) * 1000.0) + " pages/second, Memory usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
                counter = 0;
                timestamp = System.currentTimeMillis();
            }
            counter++;
            return urlsToIndex[0]; 
        } catch (InterruptedException e) {
            throw new RemoteException("Interrupted", e);
        }
    }

    public void putNew(String url) throws java.rmi.RemoteException {
        //TODO: Example code. Must be changed to use structures that have primitives such as .add(...)
        urlsToIndex[0] = url;

    }

    public void addToIndex(String word, String url) throws java.rmi.RemoteException {
        //TODO: not implemented
    }

    
    public ??<String> searchWord(String word) throws java.rmi.RemoteException {
        //TODO: not implemented
        return ??<String>;
    }
}