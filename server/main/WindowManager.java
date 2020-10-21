package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import components.DSComponent;
import remote.IWhiteBoardClient;
import remote.WhiteBoardServer;

@SuppressWarnings("serial")
public class WindowManager extends JFrame implements IWindow {

	private WhiteBoard whiteBoard;
	private DSToolBar toolBar;
	private WhiteBoardServer server;
	private UsersListManager usersListPanel;
	private String username;
	private String fileLocation;
	private JFileChooser fc;

	public WindowManager(String username, int port) {

		this.username = username;

		try {

			Registry registry_server = LocateRegistry.createRegistry(port);

			System.out.println("Your IP address is : " + InetAddress.getLocalHost().getHostAddress());

			this.setTitle("DS WhiteBoard Manager - " + username);
			this.setLayout(new BorderLayout());
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					server.exitManager(username);
				}
			});

			this.server = new WhiteBoardServer(username);

			this.whiteBoard = new WhiteBoard(this, username, server);

			this.toolBar = new DSToolBar(whiteBoard);
			this.add(toolBar, BorderLayout.PAGE_START);

			this.usersListPanel = new UsersListManager(this, this.username);

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, whiteBoard, usersListPanel);
			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerLocation(650);

			// Provide minimum sizes for the two components in the split pane
			whiteBoard.setMinimumSize(new Dimension(100, 50));
			usersListPanel.setMinimumSize(new Dimension(50, 50));

			this.add(splitPane, BorderLayout.CENTER);

			IWhiteBoardClient stub = (IWhiteBoardClient) UnicastRemoteObject.exportObject(whiteBoard, 0);
			registry_server.rebind(username, stub);
			server.registerNewUser(username, "localhost");
			registry_server.rebind("WhiteBoardServer", server);

			System.out.println("Server is ready");

			JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
			menu.setMnemonic(KeyEvent.VK_F);
			menu.getAccessibleContext().setAccessibleDescription("File options");
			menuBar.add(menu);

			JMenuItem menuItem = new JMenuItem("New", KeyEvent.VK_N);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription("Open a new whiteboard");
			menuItem.addActionListener(event -> createNew());
			menu.add(menuItem);

			menuItem = new JMenuItem("Open File...", KeyEvent.VK_O);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription("Open from a file");
			menuItem.addActionListener(event -> open());
			menu.add(menuItem);

			menuItem = new JMenuItem("Save", KeyEvent.VK_S);
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

			this.setSize(800, 600);
			this.setLocationRelativeTo(null);
			this.setVisible(true);

		} catch (RemoteException e) {
			System.out.println("Something went wrong when creating the server.");
			System.out.println("Please make sure your parameters respect one of the following format : ");
			System.out.println("<String> username");
			System.out.println("<Integer> port   <String> username");
			System.exit(0);
		} catch (UnknownHostException e) {
			System.out.println("Something went wrong when creating the server.");
			System.out.println("The localhost might be unaivable.");
			System.out.println("Please make sure your parameters respect one of the following format : ");
			System.out.println("<String> username");
			System.out.println("<Integer> port   <String> username");
			System.exit(0);
		}

	}

	private void createNew() {
		this.save();
		this.server.setComponents(new ArrayList<DSComponent>());
	}

	public static void main(String[] args) {

		String username = null;
		int port = 1099;

		try {
			if (args.length == 1) {
				username = args[0];
			} else if (args.length == 2) {
				port = Integer.valueOf(args[0]);
				username = args[1];
			} else {
				System.out.println("The parameters you wrote don't respect the required format.");
				System.out.println("Please respect one of the following : ");
				System.out.println("<String> username");
				System.out.println("<Integer> port   <String> username");
				System.exit(0);
			}
		} catch (NumberFormatException e) {
			System.out.println("The parameters you wrote don't respect the required format.");
			System.out.println("Please respect one of the following : ");
			System.out.println("<String> username");
			System.out.println("<Integer> port   <String> username");
			System.exit(0);
		}

		if (username == null) {
			System.out.println("The parameters you wrote don't respect the required format.");
			System.out.println("Please respect one of the following : ");
			System.out.println("<String> username");
			System.out.println("<Integer> port   <String> username");
			System.exit(0);
		}

		new WindowManager(username, port);

	}

	private void open() {

		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			try {
				File file = fc.getSelectedFile();

				FileInputStream f_in = new FileInputStream(file);
				ObjectInputStream obj_in = new ObjectInputStream(f_in);

				this.save();

				DSComponent[] components = (DSComponent[]) obj_in.readObject();

				obj_in.close();

				this.server.setComponents(new ArrayList<DSComponent>(Arrays.asList(components)));

			} catch (FileNotFoundException e) {
				System.out.println("This file doesn't exist");
			} catch (IOException e) {
				System.out.println("Something went wrong with the file you selected");
			} catch (ClassNotFoundException e) {
				System.out.println("Something went wrong with the file you selected");
			}
		}
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

	public void displayNewUser(String username) {
		this.usersListPanel.displayNewUser(username);
		this.repaint();
	}

	public void disconnectUser(String username) {
		this.server.disconnectUser(username);
	}

	@Override
	public void removeUser(String username) {
		this.usersListPanel.removeUser(username);
		this.repaint();
	}

	@Override
	public void updateUsers(ArrayList<String> users) {
		return;
	}

	@Override
	public void setOffline(boolean b) {
		return;
	}

}