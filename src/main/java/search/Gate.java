package search;

import java.rmi.*;
import java.util.*;

public interface Gate extends Remote {
    //returns next url for indexing
    public String takeNext() throws RemoteException;

    //add's url if not indexed yet, if it is returns all the websites that reference that url
    public List<String> putNew(String url) throws java.rmi.RemoteException;
    
    public List<String> searchWord(String word) throws java.rmi.RemoteException;
    public void addBarrel(String connetion, Boolean lock) throws java.rmi.RemoteException;
    public void removeBarrel(String connetion) throws java.rmi.RemoteException;
    public Set<String> requestBarrelList() throws java.rmi.RemoteException;
    public void pushNew(Set<String> urls) throws java.rmi.RemoteException;
}
