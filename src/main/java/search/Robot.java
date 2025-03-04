package search;

import java.rmi.registry.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Robot {
    public static void main(String[] args) {
        try {
            Index index = (Index) LocateRegistry.getRegistry(8183).lookup("index");
            while (true) {
                try{
                    String url = index.takeNext();               
                    System.out.println(url);
                    Document doc = Jsoup.connect(url).get();
                    //System.out.println(doc);
                    //Todo: Read JSOUP documentation and parse the html to index the keywords. 
                    //Then send back to server via index.addToIndex(...)
                
                    Set<String> words = extractWords(doc);
                    System.out.println("Extracted Words:\n " + words);
                    
                    Set<String> links = extractLinks(doc);
                    System.out.println("Extracted Links:\n " + links);

                    for (String word : words) index.addToIndex(word, url);

                    for (String link : links) index.putNew(link);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Extract words from the page body
    private static Set<String> extractWords(Document doc) {

        String text = doc.body().text();

        //tokenize by non-word characters
        String[] tokens = text.toLowerCase().split("\\W+"); 

        return new HashSet<>(Arrays.asList(tokens)); 
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

