package cryodex.modules;

import cryodex.widget.wizard.pages.MainPage;
import cryodex.widget.wizard.pages.MergeTournamentSelectionPage;
import cryodex.widget.wizard.pages.Page;
import cryodex.widget.wizard.pages.PlayerSelectionPage;
import cryodex.widget.wizard.pages.ProgressionCutPage;

public abstract class WizardController {

    public abstract Page getAdditionalOptionsPage();
    
    public Page getProgressionCutPage(){
        return new ProgressionCutPage(this);
    }
    
    public Page getPlayerSelectionPage(){
        return new PlayerSelectionPage(this);
    }
    
    public Page getMergeTournamentSelectionPage() {
        return new MergeTournamentSelectionPage(this);
    }
    
    public Page getMainPage() {
        return new MainPage(this);
    }
    
    public abstract String getModuleName();
}
