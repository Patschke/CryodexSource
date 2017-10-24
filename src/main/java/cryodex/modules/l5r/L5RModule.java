package cryodex.modules.l5r;

import javax.swing.JCheckBoxMenuItem;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.MenuBar;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.l5r.gui.L5RRegistrationPanel;
import cryodex.modules.l5r.wizard.MainPage;
import cryodex.widget.wizard.WizardOptions;
import cryodex.widget.wizard.pages.Page;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class L5RModule implements Module {

	private static L5RModule module;

	public static L5RModule getInstance() {
		if (module == null) {
			module = new L5RModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private L5RRegistrationPanel registrationPanel;

	private boolean isEnabled = false;

	private L5RModule() {
		getRegistration().setVisible(isEnabled);
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new L5RRegistrationPanel();
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
	public L5RTournament createTournament(WizardOptions wizardOptions) {

		L5RTournament tournament = new L5RTournament(wizardOptions);

		// Add dependent events from a progressive cut
		if (wizardOptions.isMerge() == false && wizardOptions.getSelectedTournaments() != null
				&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
			tournament.addDependentTournaments(wizardOptions.getSelectedTournaments());
		}

		return tournament;
	}
	
	@Override
	public void initializeTournament(WizardOptions wizardOptions) {

		L5RTournament tournament = createTournament(wizardOptions);
		
		CryodexController.registerTournament(tournament);

		tournament.startTournament();

		MenuBar.getInstance().resetMenuBar();

		CryodexController.saveData();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "NAME", Modules.L5R.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new L5RPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new L5RTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new L5RPlayer(player);
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
