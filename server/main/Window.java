package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import components.DSComponent;
import remote.IWhiteBoardClient;
import remote.IWhiteBoardServer;

@SuppressWarnings("serial")
public class Window extends JFrame implements IWindow {

	private WhiteBoard whiteBoard;
	private DSToolBar toolBar;
	private IWhiteBoardServer server;
	private UsersList usersListPanel;
	private boolean offline = false;
	private Object fileLocation;
	private JFileChooser fc;

	public Window(String username, String host, int server_port, int local_port) {

		if (host == null) {
			host = "localhost";
		}
		if (server_port == 0) {
			server_port = 1099;
		}
		if (local_port == 0) {
			local_port = 1099;
		}

		try {

			this.setTitle("DS WhiteBoard - " + username);
			this.setLayout(new BorderLayout());
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (!offline) {
						try {
							server.exit(username);
						} catch (RemoteException e1) {
							e1.printStackTrace();
						}
					}
				}
			});

			Registry registry_local;

			try {
				registry_local = LocateRegistry.createRegistry(local_port);
			} catch (RemoteException e1) {
				registry_local = LocateRegistry.getRegistry(local_port);
			}

			Registry registry_server = LocateRegistry.getRegistry(host, server_port);
			this.server = (IWhiteBoardServer) registry_server.lookup("WhiteBoardServer");

			this.whiteBoard = new WhiteBoard(this, username, server);

			this.toolBar = new DSToolBar(whiteBoard);
			this.add(toolBar, BorderLayout.PAGE_START);

			this.usersListPanel = new UsersList();

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, whiteBoard, usersListPanel);
			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerLocation(650);

			// Provide minimum sizes for the two components in the split pane
			whiteBoard.setMinimumSize(new Dimension(100, 50));
			usersListPanel.setMinimumSize(new Dimension(50, 50));

			this.add(splitPane, BorderLayout.CENTER);

			IWhiteBoardClient stub = (IWhiteBoardClient) UnicastRemoteObject.exportObject(whiteBoard, 0);
			registry_local.rebind(username, stub);

			JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
			menu.setMnemonic(KeyEvent.VK_F);
			menu.getAccessibleContext().setAccessibleDescription("File options");
			menuBar.add(menu);

			JMenuItem menuItem = new JMenuItem("Save", KeyEvent.VK_S);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription("Save in a file");
			menuItem.addActionListener(event -> save());
			menu.add(menuItem);

			menuItem = new JMenuItem("Save as...", KeyEvent.VK_A);
			menuItem.setAccelerator(
					KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK + ActionEvent.CTRL_MASK));
			menuItem.addActionListener(event -> save_as());
			menuItem.getAccessibleContext().setAccessibleDescription("Save as a special type of file");
			menu.add(menuItem);

			this.setJMenuBar(menuBar);

			fc = new JFileChooser();

			if (server.registerNewUser(username, InetAddress.getLocalHost().getHostAddress())) {
				this.setSize(800, 600);
				this.setLocationRelativeTo(null);
				this.setVisible(true);
			} else {
				System.out.println("Sorry, the host refused your connection to the whiteboard.");
				offline = true;
				System.exit(1);
			}

		} catch (RemoteException e) {
			System.out.println("The server is not available at this port and address");
		} catch (NotBoundException e) {
			System.out.println("The server is not available at this port and address");
		} catch (UnknownHostException e) {
			System.out.println("The server address is incorrect");
		}

	}

	public static void main(String[] args) {

		String username = null;
		String host = null;
		int server_port = 0;
		int local_port = 0;
		try {
			if (args.length == 1) {
				username = args[0];
			} else if (args.length == 2) {
				host = args[0];
				username = args[1];
			} else if (args.length == 3) {
				host = args[0];
				server_port = Integer.valueOf(args[1]);
				username = args[2];
			} else if (args.length == 4) {
				host = args[0];
				server_port = Integer.valueOf(args[1]);
				local_port = Integer.valueOf(args[2]);
				username = args[3];
			} else {
				System.out.println("The parameters you wrote don't respect the required format.");
				System.out.println("Please respect one of the following : ");
				System.out.println("<String> username");
				System.out.println("<String> server_ip_address   <String> username");
				System.out.println("<String> server_ip_address   <Integer> server_port   <String> username");
				System.out.println(
						"<String> server_ip_address   <Integer> server_port   <Integer> local_port   <String> username");
				System.exit(0);
			}
		} catch (NumberFormatException e) {
			System.out.println("The parameters you wrote don't respect the required format.");
			System.out.println("Please respect one of the following : ");
			System.out.println("<String> username");
			System.out.println("<String> server_ip_address   <String> username");
			System.out.println("<String> server_ip_address   <Integer> server_port   <String> username");
			System.out.println(
					"<String> server_ip_address   <Integer> server_port   <Integer> local_port   <String> username");
			System.exit(0);
		}
		new Window(username, host, server_port, local_port);
	}

	private void save() {
		if (this.fileLocation != null) {
			try {
				FileOutputStream fos = new FileOutputStream(fileLocation + ".dswb");
				ObjectOutputStream oos = new ObjectOutputStream(fos);

				DSComponent[] components = this.whiteBoard.getDSComponents();

				oos.writeObject(components);
				oos.close();

			} catch (IOException ex) {
				this.save_as();
			}
		} else {
			save_as();
		}
	}

	private void save_as() {

		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileLocation = fc.getSelectedFile().getPath();
			save();
		}
	}

	@Override
	public DSToolBar getToolBar() {
		return toolBar;
	}

	public void updateUsers(ArrayList<String> users) {
		this.usersListPanel.updateUsersList(users);
		this.repaint();
	}

	@Override
	public void displayNewUser(String username) {
		return;
	}

	@Override
	public void removeUser(String username) {
		return;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

}