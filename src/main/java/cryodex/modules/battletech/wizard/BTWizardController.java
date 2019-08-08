package cryodex.modules.battletech.wizard;

import cryodex.CryodexController.Modules;
import cryodex.modules.WizardController;
import cryodex.widget.wizard.pages.Page;

public class BTWizardController extends WizardController {

    @Override
    public Page getAdditionalOptionsPage() {
        return new AdditionalOptionsPage();
    }

    @Override
    public String getModuleName() {
        return Modules.BETTLETECH.getName();
    }
}
