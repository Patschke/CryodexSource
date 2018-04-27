package cryodex.widget.wizard.pages;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import cryodex.CryodexController;
import cryodex.Language;
import cryodex.Player;
import cryodex.modules.Tournament;
import cryodex.modules.WizardController;
import cryodex.widget.ComponentUtils;
import cryodex.widget.SpringUtilities;
import cryodex.widget.wizard.TournamentWizard;

public class MergeTournamentSelectionPage implements Page {

        private JPanel pagePanel = null;
        private final Map<Tournament, JCheckBox> checkBoxMap = new HashMap<Tournament, JCheckBox>();
        private JRadioButton all;
        private JRadioButton manual;
        private JTextField manualInput;
        private WizardController wizardController;
        
        public MergeTournamentSelectionPage(WizardController wizardController) {
            this.wizardController = wizardController;
        }

        @Override
        public JPanel getPanel() {

            TournamentWizard.getInstance().setButtonVisibility(true, true, false);

            TournamentWizard.getInstance().setMinimumSize(new Dimension(450, 500));

            if (pagePanel == null) {
                pagePanel = new JPanel(new BorderLayout());

                JLabel header = new JLabel("<HTML><H3>" + Language.select_tournaments + "</H3></HTML>");

                JPanel listPanel = new JPanel(new SpringLayout());

                for (Tournament t : CryodexController.getAllTournaments()) {
                    JCheckBox cb = new JCheckBox(t.getName());
                    if (t.getModuleName().equals(wizardController.getModuleName())) {
                        checkBoxMap.put(t, cb);
                    }

                    listPanel.add(cb);
                }

                SpringUtilities.makeCompactGrid(listPanel, listPanel.getComponentCount(), 1, 0, 0, 0, 0);

                JLabel playersFromLabel = new JLabel("<HTML><H3>" + Language.how_many_players_from_each_event + "</H3></HTML>");
                ButtonGroup pf = new ButtonGroup();

                all = new JRadioButton(Language.all_players);
                manual = new JRadioButton(Language.let_me_pick + ":");
                manualInput = new JTextField(3);
                manualInput.setEnabled(false);

                ActionListener playersFromListener = new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        manualInput.setEnabled(manual.isSelected());
                    }
                };

                all.addActionListener(playersFromListener);
                manual.addActionListener(playersFromListener);

                pf.add(all);
                pf.add(manual);
                all.setSelected(true);

                JPanel manualPanel = ComponentUtils.addToHorizontalBorderLayout(manual, ComponentUtils.addToFlowLayout(manualInput, FlowLayout.LEFT),
                        null);

                JPanel howManyPlayersPanel = ComponentUtils.addToVerticalBorderLayout(playersFromLabel,all, manualPanel);
                
                pagePanel.add(ComponentUtils.addToFlowLayout(header, FlowLayout.LEFT), BorderLayout.NORTH);
                pagePanel.add(ComponentUtils.addToFlowLayout(listPanel, FlowLayout.LEFT), BorderLayout.CENTER);
                pagePanel.add(ComponentUtils.addToFlowLayout(howManyPlayersPanel, FlowLayout.CENTER), BorderLayout.SOUTH);
            }
            return ComponentUtils.addToFlowLayout(pagePanel, FlowLayout.CENTER);
        }

        @Override
        public void onNext() {
            TournamentWizard.getInstance().getWizardOptions().setMerge(true);
            List<Tournament> tournamentList = new ArrayList<Tournament>();
            Set<Player> playerList = new TreeSet<Player>();
            Integer playerCount = null;

            if (manual.isSelected()) {
                playerCount = Integer.parseInt(manualInput.getText());
            }
            

            for (Tournament t : checkBoxMap.keySet()) {
                if (checkBoxMap.get(t).isSelected()) {

                    tournamentList.add(t);
                    List<Player> thisTournamentPlayers = new ArrayList<Player>();
                    thisTournamentPlayers.addAll(t.getPlayers());

                    if (playerCount == null || thisTournamentPlayers.size() <= playerCount) {
                        playerList.addAll(thisTournamentPlayers);
                    } else {
                        Collections.sort(thisTournamentPlayers, t.getRankingComparator());
                        playerList.addAll(thisTournamentPlayers.subList(0, playerCount));
                    }
                }
            }


            List<Player> addingList = new ArrayList<Player>();
            addingList.addAll(playerList);
            TournamentWizard.getInstance().getWizardOptions().setPlayerList(addingList);
            TournamentWizard.getInstance().getWizardOptions().setSelectedTournaments(tournamentList);

            TournamentWizard.getInstance().setCurrentPage(wizardController.getAdditionalOptionsPage());

        }

        @Override
        public void onPrevious() {
            TournamentWizard.getInstance().goToPrevious();
        }

        @Override
        public void onFinish() {
            // Do nothing
        }
    }