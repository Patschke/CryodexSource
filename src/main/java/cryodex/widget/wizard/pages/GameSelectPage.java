package cryodex.widget.wizard.pages;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

import cryodex.CryodexController.Modules;
import cryodex.Language;
import cryodex.modules.Module;
import cryodex.widget.ComponentUtils;
import cryodex.widget.SpringUtilities;
import cryodex.widget.wizard.TournamentWizard;

public class GameSelectPage implements Page{

	ButtonGroup gameGroup;
    JPanel pagePanel;
    
	@Override
	public JPanel getPanel() {

        TournamentWizard.getInstance().setButtonVisibility(null, true, null);

        TournamentWizard.getInstance().setMinimumSize(new Dimension(450, 300));

        if (pagePanel == null) {
        	
            JPanel namePanel = new JPanel(new BorderLayout());
            JLabel nameHeader = new JLabel("<HTML><H1>" + Language.choose_game + "</H1></HTML>");
            namePanel.add(ComponentUtils.addToFlowLayout(nameHeader, FlowLayout.LEFT), BorderLayout.NORTH);
            
            JPanel creationOptionsPanel = new JPanel(new BorderLayout());
            JPanel creationOptionsContentPanel = new JPanel(new SpringLayout());

            gameGroup = new ButtonGroup();
            
            for(Modules m : Modules.values()){
            	JRadioButton button = new JRadioButton(m.getName());
            	gameGroup.add(button);
            	creationOptionsContentPanel.add(button);
            }

            SpringUtilities.makeCompactGrid(creationOptionsContentPanel, creationOptionsContentPanel.getComponentCount(), 1, 0, 0, 0, 0);
            
            creationOptionsPanel.add(ComponentUtils.addToFlowLayout(creationOptionsContentPanel, FlowLayout.LEFT), BorderLayout.CENTER);
            
            pagePanel = ComponentUtils.addToFlowLayout(ComponentUtils.addToVerticalBorderLayout(namePanel, creationOptionsPanel, null),
                    FlowLayout.CENTER);
        }
        return pagePanel;
    }
	
    public String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }

	@Override
	public void onNext() {
		String moduleName = getSelectedButtonText(gameGroup);
		
		Module m = Modules.getModuleByName(moduleName);
		
		TournamentWizard.getInstance().getWizardOptions().setModule(m);
		
		TournamentWizard.getInstance().setCurrentPage(m.getMainWizardPage());
	}

	@Override
	public void onPrevious() {
		// There is no previous action
	}

	@Override
	public void onFinish() {
		// There is no finish action
	}

}
