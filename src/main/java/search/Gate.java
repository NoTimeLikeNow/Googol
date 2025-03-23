package search;

import java.rmi.*;
import java.util.*;

public interface Gate extends Remote {
    public String takeNext() throws RemoteException;
    public void putNew(String url) throws java.rmi.RemoteException;
    public List<String> searchWord(String word) throws java.rmi.RemoteException;
    public List<String> addBarrel(String ip) throws java.rmi.RemoteException;
    public List<String> requestBarrelList() throws java.rmi.RemoteException;
}
