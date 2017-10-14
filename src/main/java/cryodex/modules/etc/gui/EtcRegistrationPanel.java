package cryodex.modules.etc.gui;

import javax.swing.JPanel;

import cryodex.Player;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.etc.EtcPlayer;

public class EtcRegistrationPanel implements RegistrationPanel {

	private JPanel panel;

	@Override
	public JPanel getPanel() {
		if (panel == null) {

		}

		return null;
	}

	@Override
	public void save(Player player) {

		EtcPlayer xp = null;

		// get module information
		if (player.getModuleInfo() != null
				&& player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof EtcPlayer) {
					xp = (EtcPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp == null) {
			xp = new EtcPlayer(player);
			player.getModuleInfo().add(xp);
		}

	}

	@Override
	public void load(Player player) {
		EtcPlayer xp = null;

		// get module information
		if (player != null && player.getModuleInfo() != null
				&& player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof EtcPlayer) {
					xp = (EtcPlayer) mp;
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
