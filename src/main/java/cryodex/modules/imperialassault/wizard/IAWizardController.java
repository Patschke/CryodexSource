package cryodex.modules.imperialassault.wizard;

import cryodex.CryodexController.Modules;
import cryodex.modules.WizardController;
import cryodex.widget.wizard.pages.Page;

public class IAWizardController extends WizardController {

    @Override
    public Page getAdditionalOptionsPage() {
        return new AdditionalOptionsPage();
    }

    @Override
    public String getModuleName() {
        return Modules.IA.getName();
    }
}