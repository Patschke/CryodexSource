package cryodex.widget.wizard.pages;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cryodex.CryodexController;
import cryodex.Language;
import cryodex.Main;
import cryodex.modules.Tournament;
import cryodex.modules.WizardController;
import cryodex.widget.ComponentUtils;
import cryodex.widget.wizard.TournamentWizard;
import cryodex.widget.wizard.WizardOptions;

public class MainPage implements Page {

    JTextField nameTextField;

    private JCheckBox mergeCB;
    private JCheckBox splitCB;
    private JTextField numSubs;
    private JCheckBox progressionCut;
    private WizardController wizardController;
    private JPanel pagePanel;

    public MainPage(WizardController wizardController) {
        this.wizardController = wizardController;
    }
    
    @Override
    public JPanel getPanel() {

        TournamentWizard.getInstance().setButtonVisibility(null, true, null);

        TournamentWizard.getInstance().setMinimumSize(new Dimension(450, 500));

        if (pagePanel == null) {
            JPanel namePanel = new JPanel(new BorderLayout());

            JLabel nameHeader = new JLabel("<HTML><H1>" + Language.name_event + "</H1></HTML>");

            nameTextField = new JTextField(10);

            namePanel.add(ComponentUtils.addToFlowLayout(nameHeader, FlowLayout.LEFT), BorderLayout.NORTH);
            namePanel.add(ComponentUtils.addToFlowLayout(nameTextField, FlowLayout.LEFT));

            JPanel creationOptionsPanel = new JPanel(new BorderLayout());
            JPanel creationOptionsContentPanel = new JPanel(new BorderLayout());

            JLabel additionalOptionsHeader = new JLabel("<HTML><H1>" + Language.additional_options + "</H1></HTML>");

            creationOptionsPanel.add(ComponentUtils.addToFlowLayout(additionalOptionsHeader, FlowLayout.LEFT), BorderLayout.NORTH);

            JPanel splitEntryPanel = new JPanel(new BorderLayout());
            ComponentUtils.forceSize(splitEntryPanel, 210, 60);

            splitCB = new JCheckBox(Language.split_into_subtournaments);
            final JLabel splitLabel = new JLabel(Language.number_of_sub_tournaments + ":");
            numSubs = new JTextField(3);

            splitLabel.setVisible(false);
            numSubs.setVisible(false);

            splitCB.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    splitLabel.setVisible(splitCB.isSelected());
                    numSubs.setVisible(splitCB.isSelected());
                }
            });

            splitEntryPanel.add(splitCB, BorderLayout.NORTH);
            splitEntryPanel.add(
                    ComponentUtils.addToFlowLayout(ComponentUtils.addToHorizontalBorderLayout(splitLabel, numSubs, null), FlowLayout.LEFT),
                    BorderLayout.CENTER);

            creationOptionsContentPanel.add(ComponentUtils.addToFlowLayout(splitEntryPanel, FlowLayout.LEFT), BorderLayout.SOUTH);

            mergeCB = new JCheckBox(Language.merge_multiple_tournaments_into_one);
            progressionCut = new JCheckBox(Language.progression_cut);
            progressionCut.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (progressionCut.isSelected()) {
                        mergeCB.setSelected(false);
                        mergeCB.setEnabled(false);
                        splitCB.setSelected(false);
                        splitCB.setEnabled(false);
                    } else {
                        mergeCB.setEnabled(true);
                        splitCB.setEnabled(true);
                    }
                }
            });

            creationOptionsContentPanel.add(ComponentUtils.addToFlowLayout(mergeCB, FlowLayout.LEFT), BorderLayout.CENTER);
            creationOptionsContentPanel.add(ComponentUtils.addToFlowLayout(progressionCut, FlowLayout.LEFT), BorderLayout.NORTH);

            creationOptionsPanel.add(ComponentUtils.addToFlowLayout(creationOptionsContentPanel, FlowLayout.LEFT), BorderLayout.CENTER);
            
            pagePanel = ComponentUtils.addToFlowLayout(ComponentUtils.addToVerticalBorderLayout(namePanel, creationOptionsPanel, null),
                    FlowLayout.CENTER);
        }
        return pagePanel;
    }

    @Override
    public void onNext() {

        WizardOptions wizardOptions = TournamentWizard.getInstance().getWizardOptions();

        wizardOptions.setName(nameTextField.getText());
        
        for(Tournament t : CryodexController.getAllTournaments()){
            if(wizardOptions.getName().equals(t.getName())){
                JOptionPane.showMessageDialog(Main.getInstance(),
                        "Tournament name already used. Please pick something different.");
                return;
            }
        }

        if (splitCB.isSelected()) {
            int subs = Integer.parseInt(numSubs.getText());
            wizardOptions.setSplit(subs);
        }

        if (progressionCut.isSelected()) {
            TournamentWizard.getInstance().setCurrentPage(wizardController.getProgressionCutPage());
        } else if (mergeCB.isSelected()) {
            TournamentWizard.getInstance().setCurrentPage(wizardController.getMergeTournamentSelectionPage());
        } else {
            TournamentWizard.getInstance().setCurrentPage(wizardController.getPlayerSelectionPage());
        }
    }

    @Override
    public void onPrevious() {
        // Do nothing
    }

    @Override
    public void onFinish() {
        // Do nothing
    }
}