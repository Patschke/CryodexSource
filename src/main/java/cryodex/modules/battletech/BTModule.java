package cryodex.modules.battletech;

import javax.swing.JCheckBoxMenuItem;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.WizardController;
import cryodex.modules.battletech.gui.BTRegistrationPanel;
import cryodex.modules.battletech.wizard.BTWizardController;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class BTModule extends Module {

	private static BTModule module;

	public static BTModule getInstance() {
		if (module == null) {
			module = new BTModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private BTRegistrationPanel registrationPanel;

	private boolean isEnabled = true;

	private BTModule() {

	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new BTRegistrationPanel();
		}
		return registrationPanel;
	}

	@Override
	public void setModuleEnabled(Boolean enabled) {
		isEnabled = enabled;

		getRegistration().setVisible(enabled);
	}

	@Override
	public boolean isModuleEnabled() {
		return isEnabled;
	}


	@Override
	public BTTournament createTournament(WizardOptions wizardOptions) {

		BTTournament tournament = new BTTournament(wizardOptions);

		// Add dependent events from a progressive cut
		if (wizardOptions.isMerge() == false && wizardOptions.getSelectedTournaments() != null
				&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
			tournament.addDependentTournaments(wizardOptions.getSelectedTournaments());
		}

		return tournament;
	}
	
	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "NAME", Modules.BETTLETECH.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new BTPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new BTTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new BTPlayer(player);
	}

	@Override
	public JCheckBoxMenuItem getViewMenuItem() {
		return viewMenuItem;
	}

	@Override
	public void setViewMenuItem(JCheckBoxMenuItem viewMenuItem) {
		this.viewMenuItem = viewMenuItem;
	}

	@Override
	public WizardController getWizardController() {
		return new BTWizardController();
	}
}
