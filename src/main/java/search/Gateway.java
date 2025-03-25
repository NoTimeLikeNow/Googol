package search;

import java.rmi.*;
import java.rmi.server.*;
import java.text.CollationElementIterator;
import java.rmi.registry.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.Result;

import java.io.*;

import java.util.*;

public class Gateway extends UnicastRemoteObject implements Gate {
    BlockingQueue<String> urlsToIndex;
    Set<String> barrels;

    public Gateway() throws RemoteException {
        super();
        urlsToIndex = new LinkedBlockingQueue<String>();  
        barrels = new HashSet<>();
    }


    public static void main(String args[]) {
        try {
            Gateway server = new Gateway();
            Registry registry = LocateRegistry.createRegistry(8183);
            registry.rebind("index", server);
            String input = "";
            Scanner scanner = new Scanner(System.in);
            
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

    public List<String> putNew(String url) throws java.rmi.RemoteException {
        Index index = null;
        Set<String> result;

        synchronized (barrels){
            if (barrels == null || barrels.isEmpty()) {
                throw new RemoteException("waiting for a hostIndex to join the gateway");
            }
            
            for(String barrel : barrels) {
                String[] connection = barrel.split(" ");
                try{
                    index = (Index) LocateRegistry.getRegistry(connection[0], Integer.parseInt(connection[1])).lookup("Index");
                    break;
                }catch(Exception e){
                    index = null;
                    System.out.println(e);
                }
            }
        }

        if (index == null) throw new RemoteException("gateway trying to regain connection to any hostIndex");

        try {
            result = index.requestReferences(url);
            if (result == null || result.isEmpty()) {
                try {
                    urlsToIndex.put(url);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); //restore interrupted status
                    throw new RemoteException("Thread interrupted while adding URL to queue", e);
                }
                return Collections.emptyList();
            }else {
                return new ArrayList<>(result);
            }

        }catch (Exception e) {
            System.out.println(e);
            throw new RemoteException("gateway trying to regain connection to any hostIndex");    
        }
        

    }

    
    public List<String> searchWord(String word) throws java.rmi.RemoteException {
        List<String> result;
        Index index = null;

        synchronized (barrels){
            if (barrels == null || barrels.isEmpty()) {
                throw new RemoteException("waiting for a hostIndex to join the gateway");
            }
            
            for(String barrel : barrels) {
                String[] connection = barrel.split(" ");
                try{
                    index = (Index) LocateRegistry.getRegistry(connection[0], Integer.parseInt(connection[1])).lookup("Index");
                    break;
                }catch(Exception e){
                    index = null;
                    System.out.println(e);
                }
            }
        }

        if (index == null) throw new RemoteException("gateway trying to regain connection to any hostIndex");

        try {
            word = word.toLowerCase();
            result = index.searchWord(word);  
        }catch (Exception e) {
            System.out.println(e);
            throw new RemoteException("gateway trying to regain connection to any hostIndex");    
        }
        return result;
    }


    public void addBarrel(String connetion) throws java.rmi.RemoteException{
        synchronized(barrels){
            barrels.add(connetion);
        }
    }

    public Set<String> requestBarrelList() throws java.rmi.RemoteException{
        synchronized(barrels){
            if (barrels == null || barrels.isEmpty()) {
                throw new RemoteException("waiting for a hostIndex to join the gateway");
            }
            return barrels;
        }
    }
}
