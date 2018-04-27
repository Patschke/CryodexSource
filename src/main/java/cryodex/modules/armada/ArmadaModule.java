package cryodex.modules.armada;

import javax.swing.JCheckBoxMenuItem;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.RegistrationPanel;
import cryodex.modules.Tournament;
import cryodex.modules.WizardController;
import cryodex.modules.armada.gui.ArmadaRegistrationPanel;
import cryodex.modules.armada.wizard.ArmadaWizardController;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class ArmadaModule extends Module {

	private static ArmadaModule module;

	public static ArmadaModule getInstance() {
		if (module == null) {
			module = new ArmadaModule();
		}

		return module;
	}

	private JCheckBoxMenuItem viewMenuItem;
	private ArmadaRegistrationPanel registrationPanel;

	private boolean isEnabled = false;

	private ArmadaModule() {
		getRegistration().setVisible(isEnabled);
	}

	@Override
	public RegistrationPanel getRegistration() {
		if (registrationPanel == null) {
			registrationPanel = new ArmadaRegistrationPanel();
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
	public ArmadaTournament createTournament(WizardOptions wizardOptions) {

		ArmadaTournament tournament = new ArmadaTournament(wizardOptions);

		// Add dependent events from a progressive cut
		if (wizardOptions.isMerge() == false && wizardOptions.getSelectedTournaments() != null
				&& wizardOptions.getSelectedTournaments().isEmpty() == false) {
			tournament.addDependentTournaments(wizardOptions.getSelectedTournaments());
		}

		return tournament;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "NAME", Modules.ARMADA.getName());
		return sb;
	}

	@Override
	public ModulePlayer loadPlayer(Player p, Element element) {
		return new ArmadaPlayer(p, element);
	}

	@Override
	public Tournament loadTournament(Element element) {
		return new ArmadaTournament(element);
	}

	@Override
	public void loadModuleData(Element element) {

	}

	@Override
	public ModulePlayer getNewModulePlayer(Player player) {
		return new ArmadaPlayer(player);
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
        return new ArmadaWizardController();
    }
}
