package cryodex.modules.battletech.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

import cryodex.Language;
import cryodex.modules.Tournament.InitialSeedingEnum;
import cryodex.widget.ComponentUtils;
import cryodex.widget.SpringUtilities;
import cryodex.widget.wizard.TournamentWizard;
import cryodex.widget.wizard.WizardOptions;
import cryodex.widget.wizard.WizardUtils;
import cryodex.widget.wizard.WizardUtils.SplitOptions;
import cryodex.widget.wizard.pages.Page;

public class AdditionalOptionsPage implements Page {

    private JRadioButton randomRB;
    private JRadioButton byGroupRB;
    private JRadioButton byRankingRB;
    private JCheckBox nonSwiss;
    private JRadioButton singleElimination;
    private JRadioButton roundRobin;

    private JRadioButton splitRandomRB;
    private JRadioButton splitByGroupRB;
    private JRadioButton splitByRanking;

    private JPanel pagePanel;

    @Override
    public JPanel getPanel() {

        TournamentWizard.getInstance().setButtonVisibility(true, null, true);

        TournamentWizard.getInstance().setMinimumSize(new Dimension(450, 500));

        WizardOptions wizardOptions = TournamentWizard.getInstance().getWizardOptions();

        if (pagePanel == null) {

            JPanel initialPairingPanel = new JPanel(new BorderLayout());

            JLabel header = new JLabel("<HTML><H3>" + Language.first_round_pairing + "</H3></HTML>");

            initialPairingPanel.add(ComponentUtils.addToFlowLayout(header, FlowLayout.LEFT), BorderLayout.NORTH);

            JPanel tournamentTypesPanel = new JPanel(new SpringLayout());

            ButtonGroup bg = new ButtonGroup();

            // Another radiobutton-group for Single Elim or Round Robin
            ButtonGroup bg2 = new ButtonGroup();

            randomRB = new JRadioButton(Language.random);
            byGroupRB = new JRadioButton(Language.seperate_by_group_name);
            byRankingRB = new JRadioButton(Language.by_ranking);

            nonSwiss = new JCheckBox("Non-Swiss alternatives");
            singleElimination = new JRadioButton("<HTML>" + Language.start_as_single_elimination + "</HTML>");
            roundRobin = new JRadioButton("<HTML>Start event as Round Robin</HTML>");

            bg.add(randomRB);
            bg.add(byGroupRB);
            bg.add(byRankingRB);

            // Adding buttons to buttongroup nr 2
            bg2.add(singleElimination);
            bg2.add(roundRobin);

            randomRB.setSelected(true);

            tournamentTypesPanel.add(randomRB);
            tournamentTypesPanel.add(byGroupRB);
            if (wizardOptions.getSelectedTournaments() != null && wizardOptions.getSelectedTournaments().isEmpty() == false) {
                tournamentTypesPanel.add(byRankingRB);
            }
            tournamentTypesPanel.add(nonSwiss);
            tournamentTypesPanel.add(singleElimination);
//            tournamentTypesPanel.add(roundRobin);
            roundRobin.setEnabled(false);
            singleElimination.setEnabled(false);

            SpringUtilities.makeCompactGrid(tournamentTypesPanel, tournamentTypesPanel.getComponentCount(), 1, 0, 0, 0, 0);

            initialPairingPanel.add(ComponentUtils.addToFlowLayout(tournamentTypesPanel, FlowLayout.LEFT), BorderLayout.CENTER);

            ActionListener customListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // Enable/Disable parts of UI
                    if (nonSwiss.isSelected()) {
                        singleElimination.setEnabled(true);
                        roundRobin.setEnabled(true);
                        if (roundRobin.isSelected()) {
                            byGroupRB.setEnabled(false);
                            randomRB.setEnabled(false);
                        } else {
                            byGroupRB.setEnabled(true);
                            randomRB.setEnabled(true);
                        }
                    } else {
                        singleElimination.setEnabled(false);
                        singleElimination.setSelected(false);
                        roundRobin.setEnabled(false);
                        roundRobin.setSelected(false);
                        byGroupRB.setEnabled(true);
                        randomRB.setEnabled(true);
                    }

                }
            };

            nonSwiss.addActionListener(customListener);
            roundRobin.addActionListener(customListener);
            singleElimination.addActionListener(customListener);

            JPanel splitOptionsPanel = new JPanel(new BorderLayout());

            JLabel splitOptionsHeader = new JLabel("<HTML><H3>" + Language.how_to_split_tournament + "</H3></HTML>");

            splitOptionsPanel.add(ComponentUtils.addToFlowLayout(splitOptionsHeader, FlowLayout.LEFT), BorderLayout.NORTH);

            JPanel splitOptionsSubPanel = new JPanel(new SpringLayout());

            ButtonGroup splitOptionsBG = new ButtonGroup();

            splitRandomRB = new JRadioButton(Language.random);
            splitByGroupRB = new JRadioButton(Language.separate_by_group_name);
            splitByRanking = new JRadioButton(Language.split_by_ranking);

            splitOptionsBG.add(splitRandomRB);
            splitOptionsBG.add(splitByGroupRB);
            splitOptionsBG.add(splitByRanking);

            splitOptionsSubPanel.add(splitRandomRB);
            splitOptionsSubPanel.add(splitByGroupRB);
            if (wizardOptions.getSelectedTournaments() != null && wizardOptions.getSelectedTournaments().isEmpty() == false) {
                splitOptionsSubPanel.add(splitByRanking);
                TournamentWizard.getInstance().setMinimumSize(new Dimension(450, 550));
            }

            SpringUtilities.makeCompactGrid(splitOptionsSubPanel, splitOptionsSubPanel.getComponentCount(), 1, 0, 0, 0, 0);

            splitOptionsPanel.add(ComponentUtils.addToFlowLayout(splitOptionsSubPanel, FlowLayout.LEFT), BorderLayout.CENTER);

            pagePanel = new JPanel(new FlowLayout());

            pagePanel.add(ComponentUtils.addToVerticalBorderLayout(initialPairingPanel, 
                    wizardOptions.getSplit() > 1 ? splitOptionsPanel : null, null));
        }

        return pagePanel;
    }

    @Override
    public void onNext() {
        // Do nothing
    }

    @Override
    public void onPrevious() {
        TournamentWizard.getInstance().goToPrevious();
    }

    @Override
    public void onFinish() {

        WizardOptions wizardOptions = TournamentWizard.getInstance().getWizardOptions();
            wizardOptions.setPoints(200);

        if (randomRB.isSelected()) {
            wizardOptions.setInitialSeedingEnum(InitialSeedingEnum.RANDOM);
        } else if (byGroupRB.isSelected()) {
            wizardOptions.setInitialSeedingEnum(InitialSeedingEnum.BY_GROUP);
        } else if (byRankingRB.isSelected()) {
            wizardOptions.setInitialSeedingEnum(InitialSeedingEnum.IN_ORDER);
        }

        if (nonSwiss.isSelected()) {
            if (singleElimination.isSelected()) {
                wizardOptions.setSingleElimination(true);
            } else if (roundRobin.isSelected()) {
//                wizardOptions.setRoundRobin(true);
            }
        }

        if (wizardOptions.getSplit() > 1) {
            SplitOptions splitOption = null;

            if (splitByGroupRB.isSelected()) {
                splitOption = SplitOptions.GROUP;
            } else if (splitByRanking.isSelected()) {
                splitOption = SplitOptions.RANKING;
            } else if (splitRandomRB.isSelected()) {
                splitOption = SplitOptions.RANDOM;
            }

            WizardUtils.createSplitTournament(splitOption);
        } else {
            WizardUtils.createTournament();
        }
    }
}