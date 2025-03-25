package search;

import java.rmi.*;
import java.util.*;

public interface Gate extends Remote {
    public String takeNext() throws RemoteException;
    public List<String> putNew(String url) throws java.rmi.RemoteException;
    public List<String> searchWord(String word) throws java.rmi.RemoteException;
    public void addBarrel(String connetion) throws java.rmi.RemoteException;
    public Set<String> requestBarrelList() throws java.rmi.RemoteException;
    public void pushNew(Set<String> urls) throws java.rmi.RemoteException;
}
