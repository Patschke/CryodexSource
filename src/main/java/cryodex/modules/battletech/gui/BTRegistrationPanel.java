package cryodex.modules.battletech.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cryodex.Player;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.battletech.BTPlayer;
import cryodex.modules.battletech.BTPlayer.Faction;

public class BTRegistrationPanel implements RegistrationPanel {

	private JTextField squadField;
	private JPanel panel;
	private JComboBox<Faction> factionCombo;

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
			panel.add(new JLabel("<html><b>Battle Tech</b></html>"), gbc);

			gbc.gridy++;
			panel.add(new JLabel("Squadron Builder ID"), gbc);

			gbc.gridy++;
			panel.add(getSquadField(), gbc);
			
			gbc.gridy++;
			panel.add(new JLabel("Faction"), gbc);
			
			gbc.gridy++;
			panel.add(getFactionCombo(), gbc);
		}

		return null;
	}

	private JComboBox<Faction> getFactionCombo() {

		if(factionCombo == null){
			factionCombo = new JComboBox<BTPlayer.Faction>();
			factionCombo.addItem(Faction.IMPERIAL);
			factionCombo.addItem(Faction.REBEL);
			factionCombo.addItem(Faction.SCUM);
			factionCombo.addItem(Faction.RESISTANCE);
			factionCombo.addItem(Faction.FIRST_ORDER);
			factionCombo.addItem(Faction.SEPARATISTS);
			factionCombo.addItem(Faction.REPUBLIC);
			factionCombo.setSelectedIndex(-1);
		}
		
		return factionCombo;
	}

	private JTextField getSquadField() {
		if (squadField == null) {
			squadField = new JTextField();
		}
		return squadField;
	}

	@Override
	public void save(Player player) {

		BTPlayer xp = null;

		// get module information
		if (player.getModuleInfo() != null
				&& player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof BTPlayer) {
					xp = (BTPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp == null) {
			xp = new BTPlayer(player);
			player.getModuleInfo().add(xp);
		}

		// update module information
		xp.setSquadId(getSquadField().getText());
		xp.setFaction((Faction) getFactionCombo().getSelectedItem());
	}

	@Override
	public void load(Player player) {
		BTPlayer xp = null;

		// get module information
		if (player != null && player.getModuleInfo() != null
				&& player.getModuleInfo().isEmpty() == false) {
			for (ModulePlayer mp : player.getModuleInfo()) {
				if (mp instanceof BTPlayer) {
					xp = (BTPlayer) mp;
					break;
				}
			}
		}

		// if no module information, create one and add it to player
		if (xp != null) {
			getSquadField().setText(xp.getSquadId());
			getFactionCombo().setSelectedItem(xp.getFaction());
		} else {
			clearFields();
		}
	}

	@Override
	public void clearFields() {
		getSquadField().setText("");
		getFactionCombo().setSelectedIndex(-1);
	}

	@Override
	public void setVisible(boolean isVisible) {
		if(getPanel() != null){
			getPanel().setVisible(isVisible);
		}
	}
}
