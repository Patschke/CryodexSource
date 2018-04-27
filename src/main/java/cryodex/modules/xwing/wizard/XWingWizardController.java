package cryodex.modules.xwing.wizard;

import cryodex.CryodexController.Modules;
import cryodex.modules.WizardController;
import cryodex.widget.wizard.pages.Page;

public class XWingWizardController extends WizardController {

    @Override
    public Page getAdditionalOptionsPage() {
        return new AdditionalOptionsPage();
    }

    @Override
    public String getModuleName() {
        return Modules.XWING.getName();
    }
}
