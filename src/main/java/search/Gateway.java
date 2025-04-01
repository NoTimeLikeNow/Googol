package search;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.*;

public class Gateway extends UnicastRemoteObject implements Gate {
    BlockingQueue<String> urlsToIndex;
    Set<String> barrels;
    Boolean maintenance = false;

    public Gateway() throws RemoteException {
        super();
        urlsToIndex = new LinkedBlockingQueue<String>();  
        barrels = new HashSet<>();
    }


    public static void main(String args[]) {
        System.setProperty("java.rmi.server.hostname", args[0]);
        try {
            Gateway server = new Gateway();
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
            registry.rebind("gateway", server);
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


        if (barrels == null || barrels.isEmpty()) {
            System.out.println("waiting for a hostIndex to join the gateway");
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


        if (index == null) {
            System.out.println("gateway trying to regain connection to any hostIndex");
            throw new RemoteException("gateway trying to regain connection to any hostIndex");
        }
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

        if (barrels == null || barrels.isEmpty()) {
            System.out.println("waiting for a hostIndex to join the gateway");
            throw new RemoteException("Waiting for a hostIndex to join the gateway...");
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

        if (index == null) {
            System.out.println("gateway trying to regain connection to any hostIndex");
            throw new RemoteException("gateway trying to regain connection to any hostIndex");
        }
        try {
            word = word.toLowerCase();
            result = index.searchWord(word);  
        }catch (Exception e) {
            System.out.println(e);
            throw new RemoteException("gateway trying to regain connection to any hostIndex");    
        }
        return result;
    }


    
    public Set<String> requestBarrelList() throws java.rmi.RemoteException{
        if (maintenance) throw new RemoteException("Maintenance...");

        if (barrels == null || barrels.isEmpty()) {
            throw new RemoteException("waiting for a hostIndex to join the gateway");
        }
        return barrels;
    }
    public void pushNew(Set<String> urls) throws java.rmi.RemoteException{
        Index index = null;
        
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

        if (index == null) throw new RemoteException("gateway trying to regain connection to any hostIndex");
        
        for(String url : urls){
            try {
                if (index.checkURL(url)) {
                    urlsToIndex.put(url);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); //restore interrupted status
                throw new RemoteException("Thread interrupted while adding URL to queue", e);
            }catch (RemoteException ee){
                throw new RemoteException("Index unavaible", ee);
            }
            
        }
    }
    
    public void addBarrel(String connetion, Boolean lock) throws java.rmi.RemoteException{
        maintenance = lock;
        if(barrels.contains(connetion)) {
            return;
        }
        synchronized(barrels){
            barrels.add(connetion);
        }
    }

    public void removeBarrel(String connetion) throws java.rmi.RemoteException{
        synchronized(barrels){
            barrels.remove(connetion);
        }
    }
    
    
}
