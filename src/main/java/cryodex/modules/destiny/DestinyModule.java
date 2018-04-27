package cryodex.modules.destiny;

import javax.swing.JCheckBoxMenuItem;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.WizardController;
import cryodex.modules.destiny.gui.DestinyRegistrationPanel;
import cryodex.modules.destiny.wizard.DestinyWizardController;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class DestinyModule extends Module {

	private static DestinyModule module;

	public static DestinyModule getInstance() {
		if (module == null) {
			module = new DestinyModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private DestinyRegistrationPanel registrationPanel;

	private boolean isEnabled = false;

	private DestinyModule() {
		getRegistration().setVisible(isEnabled);
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new DestinyRegistrationPanel();
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
	public DestinyTournament createTournament(WizardOptions wizardOptions) {

		DestinyTournament tournament = new DestinyTournament(wizardOptions);

		// Add dependent events from a progressive cut
		if (wizardOptions.isMerge() == false && wizardOptions.getSelectedTournaments() != null
				&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
			tournament.addDependentTournaments(wizardOptions.getSelectedTournaments());
		}

		return tournament;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "NAME", Modules.DESTINY.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new DestinyPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new DestinyTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new DestinyPlayer(player);
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
        return new DestinyWizardController();
    }
}
