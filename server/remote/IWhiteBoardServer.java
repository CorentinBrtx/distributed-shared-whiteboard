package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import components.DSComponent;
import components.TextComponent;

public interface IWhiteBoardServer extends Remote {

	public boolean registerNewUser(String username, String host) throws RemoteException;

	public ArrayList<DSComponent> getComponents(String username) throws RemoteException;

	public ArrayList<String> getUsers(String username) throws RemoteException;

	public boolean addNewComponent(String username, DSComponent component) throws RemoteException;

	public boolean updateComponent(String username, DSComponent editComponent) throws RemoteException;

	public boolean availableForEdit(String username, DSComponent editComponent) throws RemoteException;

	public boolean deleteComponent(String username, DSComponent componentInEdit) throws RemoteException;

	public void exit(String username) throws RemoteException;

	public boolean availableForWriting(String username, TextComponent textComponent) throws RemoteException;

}
