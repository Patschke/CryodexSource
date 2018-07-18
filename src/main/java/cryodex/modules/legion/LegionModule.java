package cryodex.modules.legion;

import javax.swing.JCheckBoxMenuItem;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.WizardController;
import cryodex.modules.legion.gui.LegionRegistrationPanel;
import cryodex.modules.legion.wizard.LegionWizardController;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class LegionModule extends Module {

	private static LegionModule module;

	public static LegionModule getInstance() {
		if (module == null) {
			module = new LegionModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private LegionRegistrationPanel registrationPanel;

	private boolean isEnabled = false;

	private LegionModule() {
		getRegistration().setVisible(isEnabled);
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new LegionRegistrationPanel();
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
	public LegionTournament createTournament(WizardOptions wizardOptions) {

		LegionTournament tournament = new LegionTournament(wizardOptions);

		// Add dependent events from a progressive cut
		if (wizardOptions.isMerge() == false && wizardOptions.getSelectedTournaments() != null
				&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
			tournament.addDependentTournaments(wizardOptions.getSelectedTournaments());
		}

		return tournament;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "NAME", Modules.LEGION.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new LegionPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new LegionTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {
		
	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new LegionPlayer(player);
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
        return new LegionWizardController();
    }
}
