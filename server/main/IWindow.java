package main;

import java.util.ArrayList;

public interface IWindow {
	
	public DSToolBar getToolBar();
	
	public void updateUsers(ArrayList<String> users);

	public void displayNewUser(String username);

	public void removeUser(String username);

	public void setOffline(boolean b);

}
