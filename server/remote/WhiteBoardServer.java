package remote;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import components.DSComponent;
import components.TextComponent;

@SuppressWarnings("serial")
public class WhiteBoardServer extends UnicastRemoteObject implements IWhiteBoardServer {

	private ArrayList<DSComponent> components = new ArrayList<DSComponent>();
	private ArrayList<String> users = new ArrayList<String>();
	private HashMap<String, IWhiteBoardClient> usersRMI = new HashMap<String, IWhiteBoardClient>();
	private String manager;
	private int complete = 0;

	public WhiteBoardServer(String manager) throws RemoteException {
		super();

		this.manager = manager;

	}

	@Override
	public synchronized ArrayList<DSComponent> getComponents(String username) {
		return this.components;
	}

	public synchronized void setComponents(ArrayList<DSComponent> components) {
		this.components = components;
		for (DSComponent component : components) {
			component.stopEditing(null, component.getUsername());
			if (component instanceof TextComponent) {
				((TextComponent) component).stopWriting(component.getUsername());
			}
		}
		for (IWhiteBoardClient user : usersRMI.values()) {
			try {
				user.setComponents(components);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized ArrayList<String> getUsers(String username) {
		return this.users;
	}

	public synchronized boolean addNewComponent(String username, DSComponent component) {
		if (!this.components.contains(component)) {
			this.components.add(component);
			addForAll(username, component);
			return true;
		} else {
			return false;
		}
	}

	private void addForAll(String username, DSComponent component) {
		complete = 0;
		for (String user : usersRMI.keySet()) {
			if (!user.equals(username)) {
				complete += 1;
				new Thread(() -> {
					try {
						usersRMI.get(user).addComponent(component);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					complete -= 1;
				}).start();
			}
		}
		while (complete != 0) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized boolean updateComponent(String username, DSComponent editComponent) {
		if (users.contains(username)) {
			for (DSComponent component : components) {
				if (component.equals(editComponent)) {
					components.remove(component);
					components.add(editComponent);
					updateForAll(username, editComponent);
					return true;

				}
			}
			return false;
		} else {
			return false;
		}

	}

	private void updateForAll(String username, DSComponent component) {
		complete = 0;
		for (String user : usersRMI.keySet()) {
			if (!user.equals(username)) {
				complete += 1;
				new Thread(() -> {
					try {
						usersRMI.get(user).updateComponent(component);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					complete -= 1;
				}).start();
			}
		}

		while (complete != 0) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized boolean availableForEdit(String username, DSComponent editComponent) {
		if (users.contains(username)) {
			for (DSComponent component : components) {
				if (component.equals(editComponent)) {
					if (component.getUsername().equals("")) {
						component.setUsername(username);
						return true;
					}
				}
			}
			return false;
		} else {
			return false;
		}
	}

	public synchronized boolean availableForWriting(String username, TextComponent textComponent) {
		if (users.contains(username)) {
			for (DSComponent component : components) {
				if (component.equals(textComponent)) {
					if (component.getUsername().equals("")) {
						component.setUsername(username);
						return true;
					}
				}
			}
			return false;
		} else {
			return false;
		}
	}

	public synchronized boolean deleteComponent(String username, DSComponent editComponent) {
		if (users.contains(username)) {
			for (DSComponent component : components) {
				if (component.equals(editComponent)) {
					components.remove(component);
					deleteForAll(username, component);
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	private void deleteForAll(String username, DSComponent component) {

		complete = 0;
		for (String user : usersRMI.keySet()) {
			complete += 1;
			new Thread(() -> {
				try {
					if (!user.equals(username)) {
						usersRMI.get(user).deleteComponent(component);
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				complete -= 1;
			}).start();
		}
		while (complete != 0) {
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized boolean registerNewUser(String username, String host) {
		if (!users.contains(username)) {
			try {
				if (username.equals(manager) || usersRMI.get(manager).authorizeNewConnection(username)) {
					users.add(username);
					IWhiteBoardClient newUser;
					Registry registry_user = LocateRegistry.getRegistry(host);
					newUser = (IWhiteBoardClient) registry_user.lookup(username);
					usersRMI.put(username, newUser);
					System.out.println("New user: " + username);
					newUser.setComponents(this.components);
					for (IWhiteBoardClient user : usersRMI.values()) {
						user.updateUsers(users);
					}
					if (!username.equals(this.manager)) {
						usersRMI.get(this.manager).displayNewUser(username);
					}
				} else {
					return false;
				}
			} catch (AccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		} else {
			return false;
		}

	}

	public synchronized void disconnectUser(String username) {

		try {
			if (users.contains(username)) {
				users.remove(username);
				new Thread(() -> {
					try {
						usersRMI.get(username).kickedOut("USER_KICKED_OUT");
						usersRMI.remove(username);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}).start();
				for (String user : users) {
					usersRMI.get(user).updateUsers(users);
				}
				usersRMI.get(this.manager).removeUser(username);
				for (DSComponent component : components) {
					if (component.getUsername().equals(username)) {
						component.setUsername("");
						updateForAll(username, component);
					}
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public synchronized void exit(String username) {
		try {
			if (users.contains(username)) {
				users.remove(username);
				usersRMI.remove(username);
				for (IWhiteBoardClient user : usersRMI.values()) {
					user.updateUsers(users);
				}
				usersRMI.get(this.manager).removeUser(username);
				for (DSComponent component : components) {
					if (component.getUsername().equals(username)) {
						component.setUsername("");
						updateForAll(username, component);
					}
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void exitManager(String username) {
		for (String user : users) {
			new Thread(() -> {
				try {
					usersRMI.get(user).kickedOut("MANAGER_EXIT");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();
		}
	}

}
