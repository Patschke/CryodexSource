package cryodex.widget.wizard.pages;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cryodex.CryodexController;
import cryodex.Language;
import cryodex.Player;
import cryodex.modules.Tournament;
import cryodex.modules.WizardController;
import cryodex.widget.ComponentUtils;
import cryodex.widget.DoubleList;
import cryodex.widget.wizard.TournamentWizard;

public class PlayerSelectionPage implements Page {

        private DoubleList<Player> playerList;
        private JCheckBox removeCurrentlyPlaying;
        private JCheckBox showActive;
        private WizardController wizardController;
        private JPanel pagePanel;

        public PlayerSelectionPage(WizardController wizardController) {
            this.wizardController = wizardController;
        }
        
        @Override
        public JPanel getPanel() {

            TournamentWizard.getInstance().setButtonVisibility(true, true, null);

            TournamentWizard.getInstance().setMinimumSize(new Dimension(450, 500));

            if (pagePanel == null) {

                pagePanel = new JPanel(new BorderLayout());

                JLabel header = new JLabel("<HTML><H1>" + Language.select_players + "</H1></HTML>");

                pagePanel.add(ComponentUtils.addToFlowLayout(header, FlowLayout.CENTER), BorderLayout.NORTH);

                playerList = new DoubleList<Player>(CryodexController.getPlayers(), null, Language.available_players, Language.event_players);

                pagePanel.add(playerList, BorderLayout.CENTER);

                removeCurrentlyPlaying = new JCheckBox(Language.remove_players_currently_in_an_event);
                removeCurrentlyPlaying.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        filterPlayers();
                    }
                });
                
                showActive = new JCheckBox(Language.show_only_active);
                showActive.addActionListener(new ActionListener(){

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        filterPlayers();           
                    }
                    
                });

                JPanel optionPanel = ComponentUtils.addToVerticalBorderLayout(null, removeCurrentlyPlaying, showActive);
                
                pagePanel.add(ComponentUtils.addToFlowLayout(optionPanel, FlowLayout.CENTER), BorderLayout.SOUTH);

            }

            return pagePanel;
        }
        
        private void filterPlayers(){
            
            List<Player> inactivePlayers = new ArrayList<Player>();
            TreeSet<Player> busyPlayers = new TreeSet<Player>();
            
            if (removeCurrentlyPlaying.isSelected()) {
                for (Tournament t : CryodexController.getAllTournaments()) {
                    busyPlayers.addAll(t.getPlayers());
                }
            }
            
            if (showActive.isSelected()) {
                for(Player p : CryodexController.getPlayers()){
                    if(p.isActive() == false){
                        inactivePlayers.add(p);
                    }
                }
            }
            
            List<Player> filteredPlayers = new ArrayList<Player>();
            filteredPlayers.addAll(CryodexController.getPlayers());
            
            filteredPlayers.removeAll(inactivePlayers);
            filteredPlayers.removeAll(busyPlayers);
            filteredPlayers.removeAll(playerList.getList2Values());
            
            playerList.setValues(filteredPlayers, playerList.getList2Values());
        }

        @Override
        public void onNext() {
            List<Player> xwingPlayerList = new ArrayList<>();
            for (Player p : playerList.getList2Values()) {
                xwingPlayerList.add(p);
            }
            TournamentWizard.getInstance().getWizardOptions().setPlayerList(xwingPlayerList);
            TournamentWizard.getInstance().setCurrentPage(wizardController.getAdditionalOptionsPage());
        }

        @Override
        public void onPrevious() {
            TournamentWizard.getInstance().goToPrevious();
        }

        @Override
        public void onFinish() {
            // Do Nothing
        }

    }