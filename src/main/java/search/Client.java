package search;

import java.rmi.registry.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
//server.putNew("https://pt.wikipedia.org/wiki/Wikip%C3%A9dia:P%C3%A1gina_principal");

public class Client {
    public static void main(String[] args) {
        try {
            Gate gate = (Gate) LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1])).lookup("gateway");
            while (true) {
                Scanner scanner = new Scanner(System.in); 
                try{
                    String input = "";  
                    while (!"endSearch".equalsIgnoreCase(input)){
                        System.out.println("Server ready. Waiting for input...");
                        input = scanner.nextLine();
                        
                        List<String> result =  gate.searchWord(input);
                        for (String s : result) System.out.println(s);
                    }
                    scanner.close();
                } catch (Exception e) {
                    scanner.close();
                    e.printStackTrace();
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
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

