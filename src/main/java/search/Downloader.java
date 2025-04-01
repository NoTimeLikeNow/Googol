package search;

import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.StringTokenizer;
import java.io.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Downloader {
    //stops words from https://gist.github.com/sebleier/554280 https://gist.github.com/alopes/5358189
    private static Set<String> loadStopWords(String filename) {
        Set<String> stopWords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.trim().split("\\s+"); // Split by any whitespace
                Collections.addAll(stopWords, words); // Add all words to the set
            }
        } catch (IOException e) {
            System.err.println("Error reading stop words file: " + e.getMessage());
        }
        return stopWords; 
    }
    
    private static Set<String> STOP_WORDS = null;

    public static void main(String[] args) {
        while (true) {
                try {
                    Gate gate = (Gate) LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1])).lookup("gateway");
                    Index index = null;


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
                            while (true) {
                                try{
                                    Set<String> barrels = gate.requestBarrelList();
                                    for(String barrel : barrels) {
                                        String[] connection = barrel.split(" ");
                                        try{
                                            index = (Index) LocateRegistry.getRegistry(connection[0], Integer.parseInt(connection[1])).lookup("Index");
                                            barrels.remove(barrel);
                                            break;
                                        }catch(Exception e){
                                            index = null;
                                            System.out.println(e);
                                        }
                                    }
                                    if (index == null) throw new Exception("trying to connect to any hostIndex");

                                    index.multicast(words, url, doc.title(), doc.body().text().substring(0,100), links, new ArrayList<String>(barrels));
                                    System.out.println("Indexed");
                                    break;

                                } catch (Exception e) {
                                    System.out.println("Waiting for an available barrel...");
                                    try {
                                        TimeUnit.SECONDS.sleep(2); 
                                    } catch (InterruptedException ee) {
                                        e.printStackTrace();
                                        System.exit(1);
                                    }
                                }
                            }
                            gate.pushNew(links);
                        } catch (Exception e) {
                            //e.printStackTrace();
                            System.out.println("Retrying Gateway on "+ args[0] + " " + args[1]  + "...");
                                    
                            try {
                                TimeUnit.SECONDS.sleep(1); 
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
            if (STOP_WORDS == null) STOP_WORDS = loadStopWords("resources/stopwords.txt");
            
            StringTokenizer st = new StringTokenizer(doc.body().text(), " \t\n\r\f,.:;!?\"'()[]{}<>-");
            HashSet<String> words = new HashSet<>();
            while (st.hasMoreTokens()){
                String word = st.nextToken();
                word.toLowerCase();
                //remove non-unicode characters
                word = word.replaceAll("[^\\p{L}]", "");
                if (!STOP_WORDS.contains(word)) words.add(word);
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

