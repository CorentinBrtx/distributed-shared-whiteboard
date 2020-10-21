package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.MatteBorder;

@SuppressWarnings("serial")
public class UsersList extends JPanel {

	private JPanel mainList;

	public UsersList() {

		setLayout(new BorderLayout());

		mainList = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		mainList.add(new JPanel(), gbc);

		add(new JScrollPane(mainList));

	}

	public void updateUsersList(ArrayList<String> users) {

		mainList.removeAll();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		mainList.add(new JPanel(), gbc);

		for (String user : users) {

			JPanel panel = new JPanel(new GridBagLayout());
			panel.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));

			JLabel usernameLabel = new JLabel(user) {
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

			GridBagConstraints gbc2 = new GridBagConstraints();
			gbc2.gridwidth = GridBagConstraints.REMAINDER;
			gbc2.fill = GridBagConstraints.HORIZONTAL;
			gbc2.weightx = 1;
			mainList.add(panel, gbc2, 0);

		this.revalidate();
		
		}
	}

}
