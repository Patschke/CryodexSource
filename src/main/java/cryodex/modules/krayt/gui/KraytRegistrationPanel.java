package cryodex.modules.krayt.gui;

import javax.swing.JPanel;

import cryodex.Player;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.krayt.KraytPlayer;

public class KraytRegistrationPanel implements RegistrationPanel {

	private JPanel panel;

	@Override
	public JPanel getPanel() {
		if (panel == null) {

		}

		return null;
	}

	@Override
	public void save(Player player) {

		KraytPlayer xp = null;

		// get module information
		if (player.getModuleInfo() != null
				&& player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof KraytPlayer) {
					xp = (KraytPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp == null) {
			xp = new KraytPlayer(player);
			player.getModuleInfo().add(xp);
		}

	}

	@Override
	public void load(Player player) {
		KraytPlayer xp = null;

		// get module information
		if (player != null && player.getModuleInfo() != null
				&& player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof KraytPlayer) {
					xp = (KraytPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp != null) {
			//none
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
