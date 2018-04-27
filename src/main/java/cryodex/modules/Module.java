package cryodex.modules;

import javax.swing.JCheckBoxMenuItem;

import cryodex.CryodexController;
import cryodex.MenuBar;
import cryodex.Player;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils.Element;

public abstract class Module implements XMLObject {

	public abstract RegistrationPanel getRegistration();

	public abstract void setModuleEnabled(Boolean enabled);

	public abstract boolean isModuleEnabled();

	public abstract Tournament loadTournament(Element element);

	public abstract void loadModuleData(Element element);

	public abstract ModulePlayer loadPlayer(Player p, Element element);

	public abstract ModulePlayer getNewModulePlayer(Player player);
	
	public abstract JCheckBoxMenuItem getViewMenuItem();
	
	public abstract void setViewMenuItem(JCheckBoxMenuItem viewMenuItem);
	
	public abstract Tournament createTournament(WizardOptions wizardOptions);

    public void initializeTournament(WizardOptions wizardOptions) {

        Tournament tournament = createTournament(wizardOptions);
        
        CryodexController.registerTournament(tournament);

        tournament.startTournament();

        MenuBar.getInstance().resetMenuBar();

        CryodexController.saveData();
    }
	
	public abstract WizardController getWizardController();
}
