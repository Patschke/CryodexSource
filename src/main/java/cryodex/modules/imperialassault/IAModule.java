package cryodex.modules.imperialassault;

import javax.swing.JCheckBoxMenuItem;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.imperialassault.gui.IARegistrationPanel;
import cryodex.modules.imperialassault.wizard.MainPage;
import cryodex.widget.wizard.WizardOptions;
import cryodex.widget.wizard.pages.Page;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class IAModule extends Module {

	private static IAModule module;

	public static IAModule getInstance() {
		if (module == null) {
			module = new IAModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private IARegistrationPanel registrationPanel;

	private boolean isEnabled = false;

	private IAModule() {
		getRegistration().setVisible(isEnabled);
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new IARegistrationPanel();
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
	public IATournament createTournament(WizardOptions wizardOptions) {

		IATournament tournament = new IATournament(wizardOptions);

		// Add dependent events from a progressive cut
		if (wizardOptions.isMerge() == false && wizardOptions.getSelectedTournaments() != null
				&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
			tournament.addDependentTournaments(wizardOptions.getSelectedTournaments());
		}

		return tournament;
	}
	
	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "NAME", Modules.IA.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new IAPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new IATournament(element);
	}

	@Override
	public void loadModuleData(Element element) {

	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new IAPlayer(player);
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
