package search;

import java.rmi.*;
import java.util.*;

public interface Index extends Remote {    
    //indices words,adds the url into the ranking, and saves the url's title
    public void addToIndex(Set<String> words, String url, String title, String summary, Set<String> links) throws java.rmi.RemoteException;
    public void rollBack(Set<String> words, String url, String title, String summary, Set<String> links) throws java.rmi.RemoteException;
    public void multicast(Set<String> words, String url, String title, String summary, Set<String> links, List<String> barrels) throws java.rmi.RemoteException;
   
    //searsh the index for websites that include these words
    public List<String> searchWord(String words) throws java.rmi.RemoteException;
    
    //who points to url
    public Set<String> requestReferences(String Url) throws java.rmi.RemoteException;

    //checks if url has already been indexed
    public Boolean checkURL(String url) throws java.rmi.RemoteException;

    //updates newly open Barrels
    public ArrayList<Object> getIndexState() throws java.rmi.RemoteException;

}
