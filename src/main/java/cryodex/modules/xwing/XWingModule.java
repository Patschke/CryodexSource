package cryodex.modules.xwing;

import javax.swing.JCheckBoxMenuItem;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.WizardController;
import cryodex.modules.xwing.gui.XWingRegistrationPanel;
import cryodex.modules.xwing.wizard.XWingWizardController;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class XWingModule extends Module {

	private static XWingModule module;

	public static XWingModule getInstance() {
		if (module == null) {
			module = new XWingModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private XWingRegistrationPanel registrationPanel;

	private boolean isEnabled = true;

	private XWingModule() {

	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new XWingRegistrationPanel();
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
	public XWingTournament createTournament(WizardOptions wizardOptions) {

		XWingTournament tournament = new XWingTournament(wizardOptions);

		// Add dependent events from a progressive cut
		if (wizardOptions.isMerge() == false && wizardOptions.getSelectedTournaments() != null
				&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
			tournament.addDependentTournaments(wizardOptions.getSelectedTournaments());
		}

		return tournament;
	}
	
	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "NAME", Modules.XWING.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new XWingPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new XWingTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new XWingPlayer(player);
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
		return new XWingWizardController();
	}
}
