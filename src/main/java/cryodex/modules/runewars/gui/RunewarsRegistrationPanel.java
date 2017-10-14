package cryodex.modules.runewars.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import cryodex.Player;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.runewars.RunewarsPlayer;

public class RunewarsRegistrationPanel implements RegistrationPanel {

	private JPanel panel;

	@Override
	public JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.anchor = GridBagConstraints.EAST;
			panel.add(new JLabel("<html><b>Runewars</b></html>"), gbc);

			gbc.gridy++;
			panel.add(new JLabel("Squadron Builder ID"), gbc);

			gbc.gridy++;

		}

		// I'm not sure any of this information is really necessary. Removing it for the time being.
		return null;
	}

	@Override
	public void save(Player player) {

		RunewarsPlayer xp = null;

		// get module information
		if (player.getModuleInfo() != null && player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof RunewarsPlayer) {
					xp = (RunewarsPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp == null) {
			xp = new RunewarsPlayer(player);
			player.getModuleInfo().add(xp);
		}

		// update module information
	}

	@Override
	public void load(Player player) {
		RunewarsPlayer xp = null;

		// get module information
		if (player != null && player.getModuleInfo() != null && player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof RunewarsPlayer) {
					xp = (RunewarsPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp != null) {
			// nothing currently
		} else {
			clearFields();
		}
	}

	@Override
	public void clearFields() {
		
	}

	@Override
	public void setVisible(boolean isVisible) {
		if(getPanel() != null){
			getPanel().setVisible(isVisible);
		}
	}
}
