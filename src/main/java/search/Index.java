package search;

import java.rmi.*;
import java.util.*;

public interface Index extends Remote {
    public void addToIndex(String word, String url) throws java.rmi.RemoteException;
    public List<String> searchWord(String word) throws java.rmi.RemoteException;
}
