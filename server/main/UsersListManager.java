package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
public class UsersListManager extends JPanel {

	private WindowManager window;
	private JPanel mainList;

	public UsersListManager(WindowManager window, String username) {

		this.window = window;

		setLayout(new BorderLayout());

		mainList = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		mainList.add(new JPanel(), gbc);

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));

		JLabel usernameLabel = new JLabel(username) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(0, 30);
			}
		};
		GridBagConstraints nameC = new GridBagConstraints();
		nameC.anchor = GridBagConstraints.CENTER;
		nameC.fill = GridBagConstraints.HORIZONTAL;
		nameC.gridx = 0;
		nameC.weightx = 3;
		nameC.insets = new Insets(0, 5, 0, 0);
		panel.add(usernameLabel, nameC);

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.weightx = 1;
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		mainList.add(panel, gbc2, 0);

		add(new JScrollPane(mainList));

	}

	public void displayNewUser(String username) {

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));

		JLabel usernameLabel = new JLabel(username) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(0, 30);
			}
		};
		GridBagConstraints nameC = new GridBagConstraints();
		nameC.anchor = GridBagConstraints.WEST;
		nameC.fill = GridBagConstraints.BOTH;
		nameC.gridx = 0;
		nameC.weightx = 3;
		nameC.insets = new Insets(0, 5, 0, 0);
		panel.add(usernameLabel, nameC);

		JButton disconnect = new JButton() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(20, 20);
			}
		};
		disconnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.disconnectUser(username);
			}
		});
		URL imageURL = getClass().getResource("/images/close.png");
		if (imageURL != null) { // image found

			ImageIcon imageIcon = new ImageIcon(imageURL);
			Image image = imageIcon.getImage(); // transform it
			Image newimg = image.getScaledInstance(15, 15, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
			imageIcon = new ImageIcon(newimg); // transform it back
			disconnect.setIcon(imageIcon);
		} else { // no image found
			disconnect.setText("X");
			System.err.println("Resource not found: close.png");
		}
		disconnect.setContentAreaFilled(false);
		disconnect.setBorderPainted(false);
		GridBagConstraints disconnectC = new GridBagConstraints();
		disconnectC.gridx = 1;
		disconnectC.anchor = GridBagConstraints.EAST;
		panel.add(disconnect, disconnectC);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		mainList.add(panel, gbc, 1);

		this.revalidate();
	}

	public void removeUser(String username) {

		for (Component c : mainList.getComponents()) {
			JPanel p = (JPanel) c;
			if (p.getComponents().length > 1) {
				if (((JLabel) p.getComponent(0)).getText().equals(username)) {
					mainList.remove(c);
				}
			}
		}
		this.revalidate();
	}

}
