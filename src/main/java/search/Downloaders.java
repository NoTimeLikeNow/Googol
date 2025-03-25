package search;

import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Downloaders {
    public static void main(String[] args) {
        while (true) {
                try {
                    Gate gate = (Gate) LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1])).lookup("gateway");
                    Index index = null;
                    
                    Set<String> barrels = gate.requestBarrelList();
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
                    

                    if (index == null) throw new Exception("trying to connect to any hostIndex");


                    while (true) {
                        try{
                            String url = gate.takeNext();               
                            System.out.println(url);
                            Document doc = Jsoup.connect(url).get();
                            //System.out.println(doc);
                            //Todo: Read JSOUP documentation and parse the html to index the keywords. 
                            //Then send back to server via index.addToIndex(...)
                            
                            Set<String> words = extractWords(doc);
                            //System.out.println("Extracted Words:\n " + words);
                            
                            Set<String> links = extractLinks(doc);
                            //System.out.println("Extracted Links:\n " + links);
                            
                            index.addToIndex(words, url, doc.title(),links);
                            
                            gate.pushNew(links);
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                TimeUnit.SECONDS.sleep(2); 
                            } catch (InterruptedException ee) {
                                e.printStackTrace();
                                System.exit(1);
                            }
                            break;
                        }
                    } 
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        TimeUnit.SECONDS.sleep(2); 
                    } catch (InterruptedException ee) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
        }
        
        // Extract words from the page body
        private static Set<String> extractWords(Document doc) {
            
            StringTokenizer st = new StringTokenizer(doc.body().text(), ",");
            HashSet<String> words = new HashSet<>();
            while (st.hasMoreTokens()){
                words.add(st.nextToken());
            }
            
            return words; 
        }
        
        //extract links from the page
     private static Set<String> extractLinks(Document doc) {

        Set<String> links = new HashSet<>();
        
        Elements elements = doc.select("a[href]");
        
        for (Element element : elements) {
            String link = element.absUrl("href"); //gets absolute URL
            if (!link.isEmpty()) {
                links.add(link);
            }
        }
        return links;
    }
}

