package search;

import java.rmi.*;
import java.util.*;

public interface Index extends Remote {
    public void addToIndex(Set<String> words, String url, String title, Set<String> links) throws java.rmi.RemoteException;
    public List<String> searchWord(String word) throws java.rmi.RemoteException;
    public Set<String> requestReferences(String Url) throws java.rmi.RemoteException;
}
