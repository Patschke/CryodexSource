package cryodex.modules.runewars;

import javax.swing.JCheckBoxMenuItem;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.runewars.gui.RunewarsRegistrationPanel;
import cryodex.modules.runewars.wizard.MainPage;
import cryodex.widget.wizard.WizardOptions;
import cryodex.widget.wizard.pages.Page;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class RunewarsModule extends Module {

	private static RunewarsModule module;

	public static RunewarsModule getInstance() {
		if (module == null) {
			module = new RunewarsModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private RunewarsRegistrationPanel registrationPanel;

	private boolean isEnabled = false;

	private RunewarsModule() {
		getRegistration().setVisible(isEnabled);
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new RunewarsRegistrationPanel();
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
	public RunewarsTournament createTournament(WizardOptions wizardOptions) {

		RunewarsTournament tournament = new RunewarsTournament(wizardOptions);

		// Add dependent events from a progressive cut
		if (wizardOptions.isMerge() == false && wizardOptions.getSelectedTournaments() != null
				&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
			tournament.addDependentTournaments(wizardOptions.getSelectedTournaments());
		}

		return tournament;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "NAME", Modules.RUNEWARS.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new RunewarsPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new RunewarsTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {

	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new RunewarsPlayer(player);
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
	public Page getMainWizardPage() {
		return new MainPage();
	}
}
