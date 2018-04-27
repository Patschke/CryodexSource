package cryodex.modules.etc.wizard;

import cryodex.CryodexController.Modules;
import cryodex.modules.WizardController;
import cryodex.widget.wizard.pages.Page;

public class EtcWizardController extends WizardController {

    @Override
    public Page getAdditionalOptionsPage() {
        return new AdditionalOptionsPage();
    }

    @Override
    public Page getMainPage() {
        return new MainPage();
    }
    
    @Override
    public Page getPlayerSelectionPage() {
        return new PlayerSelectionPage();
    }

    @Override
    public String getModuleName() {
        return Modules.ETC.getName();
    }
}