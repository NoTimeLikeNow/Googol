package search;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.concurrent.*;
import java.io.*;
import java.util.*;

public class IndexStorageBarrel extends UnicastRemoteObject implements Index, Serializable {
    Set<String> indexUrls;
    ConcurrentHashMap<String, ArrayList<String>> pageInfo;
    ConcurrentHashMap<String, Set<String>> indexedItems;
    
    ConcurrentHashMap<String, Set<String>> foundURLs;

    // Custom comparator to sort by the size of the set
    transient  Comparator<String> comparator = Comparator.<String>comparingInt(key -> foundURLs.get(key).size()).reversed();
 
    public IndexStorageBarrel() throws RemoteException {
        super();
        indexUrls = new HashSet<>();
        indexedItems = new ConcurrentHashMap<>();   
        foundURLs = new ConcurrentHashMap<>();
        pageInfo = new ConcurrentHashMap<>();
    }


    public static void main(String args[]) {
        System.setProperty("java.rmi.server.hostname", args[0]);
        String connection = args[0] + " " + args[1];
        Boolean update = false;
        IndexStorageBarrel server; 
        try {
            server = new IndexStorageBarrel();
            Index index;
            Gate gateway = (Gate) LocateRegistry.getRegistry(args[2], Integer.parseInt(args[3])).lookup("gateway");
            try {
                Set <String> availableBarrels = gateway.requestBarrelList();
                if (!(availableBarrels == null || availableBarrels.isEmpty())){
                    for (String ip_port: availableBarrels){
                        String [] ipport = ip_port.split (" ");
                        ArrayList<Object> temp;
                        try{
                            index = (Index)LocateRegistry.getRegistry (ipport[0], Integer.parseInt(ipport[1])).lookup("barrel");
                            temp = index.getIndexState();
                            gateway.addBarrel(connection,true); //lock
                        } catch (Exception e){
                            System.out.println("Couldn't reach barrel");
                            continue;
                            
                        }
                        server.indexUrls = (Set<String>)temp.get(0);
                        server.indexedItems = (ConcurrentHashMap<String, Set<String>>)temp.get(1);
                        server.foundURLs = (ConcurrentHashMap<String, Set<String>>)temp.get(2);
                        server.pageInfo = (ConcurrentHashMap<String, ArrayList<String>>)temp.get(3);
                        update = true;
                        System.out.println("Update Found");
                        break;
                    }
                }else System.out.println("No updates");
            } catch (Exception e) {
                System.out.println("No updates");
            }
                
            if(update == false){
                try  {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream("resources/backup.obj"));
                    System.out.println("Looking for backup");
                    IndexStorageBarrel save = (IndexStorageBarrel)ois.readObject(); // Read the object from the binary file
                    synchronized(server.indexUrls){
                        server.indexUrls = save.indexUrls;
                    }
                    synchronized(server.indexedItems){
                        server.indexedItems = save.indexedItems;
                    }
                    synchronized(server.foundURLs){
                        server.foundURLs = save.foundURLs;

                    }
                    synchronized(server.pageInfo){
                        server.pageInfo = save.pageInfo;
                    }
                    
                    ois.close();
                    System.out.println("Backup Loaded");
                }catch (Exception e) {
                    System.out.println("No Backup Found");
                }
            }
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[1]));
            registry.rebind("Index", server);
            gateway.addBarrel(connection, false); //unlock

            System.out.println("Index Storage Barrel running...");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                
                System.out.println("Shutdown triggered\n Saving state, might take some time...\n");
                try{
                    gateway.removeBarrel(connection);
                }catch(Exception e){
                    e.printStackTrace();
                }
                
                String filename = "resources/backup.obj";
                File file = new File(filename);
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
                    out.writeObject(server);
                    System.out.println("\nSaved barrel in " + filename);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }));


        } catch (Exception e) {
            e.printStackTrace();
        }

        
    }

    private long counter = 0, timestamp = System.currentTimeMillis();;

  
    public void multicast(Set<String> words, String url, String title,  String summary, Set<String> links, List<String> barrels) throws java.rmi.RemoteException{
        if (indexUrls.contains(url)) return;
        ArrayList<Index>commits = new ArrayList<>();
            for (String next : barrels){
                try{
                    String[] connection = next.split(" ");
                    Index barrel = (Index) LocateRegistry.getRegistry(connection[0], Integer.parseInt(connection[1])).lookup("Index");
                    commits.add(barrel);
                }catch(Exception e){
                    throw new RemoteException("Rollback...");
                }
            }
            for (Index barrel : commits){
                try{
                    barrel.addToIndex(words, url, title, summary, links);
                }catch(Exception e){
                    for(Index commit : commits){
                        try{
                            commit.rollBack(words, url, title, summary, links);
                        }catch(Exception ee){

                        }
                    }
                   return;
                }
            }
            this.addToIndex(words, url, title, summary, links);
            return ;
    }
    public void rollBack(Set<String> words, String url, String title, String summary, Set<String> links) throws java.rmi.RemoteException{
        synchronized(indexUrls){
            indexUrls.remove(url);
        }
        synchronized(indexedItems){
            for(String word : words){  
                indexedItems.get(word).remove(url);
            }
        }

        for(String link : links){   
            synchronized(foundURLs){
                foundURLs.get(link).remove(url);
            }
        }
    }
    public void addToIndex(Set<String> words, String url, String title, String summary, Set<String> links) throws java.rmi.RemoteException{
        if (indexUrls.contains(url)) return;
        indexUrls.add(url);
        synchronized(indexedItems){
            for(String word : words){   
                indexedItems.computeIfAbsent(word, k -> new HashSet<>());
                
                indexedItems.get(word).add(url);
            }
            
        }
        synchronized(pageInfo){
            ArrayList<String> info = new ArrayList<>(Arrays.asList(title, summary));
            pageInfo.put(url, info);
        }

        
        synchronized(foundURLs){
            foundURLs.computeIfAbsent(url, k -> new HashSet<>());
                for(String link : links){   
                    foundURLs.computeIfAbsent(link, k -> new HashSet<>());
                    foundURLs.get(link).add(url);
                    
            }
        }
    }

    public List<String> searchWord(String search) throws java.rmi.RemoteException {
        search = search.toLowerCase();
        String[] words = search.split(search, ' ');
        ArrayList<String> results = new ArrayList<>() , urlssorted;  
        Set<String> urls = indexedItems.get(words[0]);

        for (String word : words){
            if (urls == null || urls.isEmpty()) return Collections.emptyList(); 
            urls.retainAll(indexedItems.get(word));
        }
        if (urls == null || urls.isEmpty()) return Collections.emptyList(); 
        
        urlssorted = new ArrayList<>(urls);
        urlssorted.sort(comparator);

        for(String s : urlssorted){
            ArrayList<String> temp = pageInfo.get(s);
            if(temp == null || temp.isEmpty() || temp.size()<2){
                results.add( "No Title");
                results.add(s);
                results.add( "No summary found");
            }else {
                results.add( temp.get(0));
                results.add(s);
                results.add( temp.get(1));
            }
        }
        System.out.println("Size of results: " + results.size());
        //System.out.println("Rank of #1: " + foundURLs.get(results.get(0)).size());
        //System.out.println("Rank of #2: " + foundURLs.get(results.get(1)).size());
        return results;
    }

    public Boolean checkURL(String url) throws java.rmi.RemoteException{
        if(indexUrls.contains(url)) return false;
        else return true;
    }

    public Set<String> requestReferences(String Url) throws java.rmi.RemoteException{
        synchronized(foundURLs){
            return foundURLs.get(Url);
        }
    }

    public ArrayList<Object> getIndexState() throws java.rmi.RemoteException{
        ArrayList<Object> state = new ArrayList<>(); 
        synchronized(this){ 
            state.add(this.indexUrls);
            state.add(this.indexedItems);
            state.add(this.foundURLs);
            state.add(this.pageInfo);
        }  
        return state;
    }    
}