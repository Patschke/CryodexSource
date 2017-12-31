package cryodex.modules.krayt;

import javax.swing.JCheckBoxMenuItem;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.krayt.wizard.MainPage;
import cryodex.modules.krayt.gui.KraytRegistrationPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.widget.wizard.pages.Page;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class KraytModule extends Module {

	private static KraytModule module;

	public static KraytModule getInstance() {
		if (module == null) {
			module = new KraytModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private KraytRegistrationPanel registrationPanel;

	private boolean isEnabled = true;

	private KraytModule() {

	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new KraytRegistrationPanel();
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
	public KraytTournament createTournament(WizardOptions wizardOptions) {

		KraytTournament tournament = new KraytTournament(wizardOptions);

		// Add dependent events from a progressive cut
		if (wizardOptions.isMerge() == false && wizardOptions.getSelectedTournaments() != null
				&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
			tournament.addDependentTournaments(wizardOptions.getSelectedTournaments());
		}

		return tournament;
	}
	
	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "NAME", Modules.KRAYT.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new KraytPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new KraytTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new KraytPlayer(player);
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
