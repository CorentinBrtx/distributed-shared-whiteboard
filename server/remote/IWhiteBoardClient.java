package remote;

import java.awt.Cursor;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.JButton;

import components.DSComponent;

public interface IWhiteBoardClient extends Remote {

	public void setComponents(ArrayList<DSComponent> components) throws RemoteException;

	public void remove(JButton deleteButton) throws RemoteException;

	public void delete(DSComponent component) throws RemoteException;

	public void add(JButton deleteButton) throws RemoteException;

	public void setCursor(Cursor cursor) throws RemoteException;
	
	public void updateComponent(DSComponent component) throws RemoteException;
	
	public void addComponent(DSComponent component) throws RemoteException;
	
	public void deleteComponent(DSComponent component) throws RemoteException;
	
	public String getUsername() throws RemoteException;

	public void updateUsers(ArrayList<String> users) throws RemoteException;

	public void displayNewUser(String username) throws RemoteException;

	public void removeUser(String username) throws RemoteException;

	public void kickedOut(String message) throws RemoteException;

	public boolean authorizeNewConnection(String username) throws RemoteException;
}
