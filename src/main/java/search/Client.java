package search;

import java.rmi.registry.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
//server.putNew("https://pt.wikipedia.org/wiki/Wikip%C3%A9dia:P%C3%A1gina_principal");

public class Client {
    public static void main(String[] args) {
        try {
            Gate gate = (Gate) LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1])).lookup("gateway");
                Scanner scanner = new Scanner(System.in); 
                String input = "";  
                while (!"endSearch".equalsIgnoreCase(input)){
                    System.out.println("Server ready. Waiting for input...");
                    input = scanner.nextLine();
                    while (!"endSearch".equalsIgnoreCase(input)){
                        try{
                            if (input.contains("https://") || input.contains("www.") || input.contains("http://")){
                                List<String> result = gate.putNew(input);
                                if (!(result == null || result.isEmpty())) {
                                    System.out.println("\nEnter b to break results output. Enter anything else to continue");
                                    int count = 0;
                                    for(String s : result){
                                        count++;
                                        if (count % 10 == 0){
                                            input = scanner.nextLine();
                                            if (input.equalsIgnoreCase("b")) break;
                                        }
                                        System.out.println(s);
                                    }
                                    System.out.println("Total: " + result.size());
                                
                                }else System.out.println("\n Linked will be Indexed");
                                break;
                            }    
                            
                            List<String> result =  gate.searchWord(input);
                            if (!(result == null || result.isEmpty())) {
                                int count = 0;
                                System.out.println("\nEnter b to break results output. Enter anything else to continue");
                                for (String s : result) {
                                    if (count % 30 == 0){
                                        System.out.println("Page " + (count/30 + 1) + "-" + (result.size()/30+1));
                                        input = scanner.nextLine();
                                        if (input.equalsIgnoreCase("b")) break;
                                        System.out.println("\nEnter b to break results output. Enter anything else to continue");
                                    }
                                    if (count % 3 == 0)System.out.println(" ");
                                    
                                    System.out.println(s);
                                    count++;
                                }
                            }else System.out.println("\n No Results Found");
                            break;
                        }catch (Exception e) {
                            System.out.println("Searching...");
                            try {
                                TimeUnit.SECONDS.sleep(1); 
                            } catch (InterruptedException ee) {
                                e.printStackTrace();
                                System.exit(1);
                            }
                        }

                    }

                }
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
    }
}