package cryodex.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import cryodex.CryodexController;
import cryodex.Language;
import cryodex.Main;
import cryodex.Player;
import cryodex.modules.Tournament;

public class MassAddPanel extends JDialog {

    private static final long serialVersionUID = 1L;

    public MassAddPanel() {
        super(Main.getInstance(), "Mass Add Players", true);

        this.add(new PlayerSelect());

        MassAddPanel.this.setLocationRelativeTo(Main.getInstance());
        MassAddPanel.this.pack();
        this.setMinimumSize(new Dimension(300, 300));
    }

    private class PlayerSelect extends JPanel {

        private static final long serialVersionUID = 1L;

        private DoubleList<Player> playerList;
        private JCheckBox removeCurrentlyPlaying;
        private JCheckBox showActive;
        private JButton ok;
        private JButton cancel;

        public PlayerSelect() {
            super(new BorderLayout());

            init();
            buildPanel();
        }

        private void init() {

            List<Player> players = new ArrayList<Player>();
            players.addAll(CryodexController.getPlayers());

            for (Player p : CryodexController.getActiveTournament().getPlayers()) {
                players.remove(p);
            }
            
            playerList = new DoubleList<Player>(players, null, "Player List", "Dropping");

            ok = new JButton("Ok");
            ok.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {

                    List<Player> playersToAdd = playerList.getList2Values();

                    CryodexController.getActiveTournament().massAddPlayers(playersToAdd);

                    MassAddPanel.this.setVisible(false);
                }
            });

            cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    MassAddPanel.this.setVisible(false);
                }
            });
        }

        private void buildPanel() {
            
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

            JPanel buttonPanel = ComponentUtils.addToHorizontalBorderLayout(ok, null, cancel);

            this.add(playerList, BorderLayout.NORTH);
            this.add(ComponentUtils.addToFlowLayout(optionPanel, FlowLayout.CENTER), BorderLayout.CENTER);
            this.add(ComponentUtils.addToFlowLayout(buttonPanel, FlowLayout.CENTER), BorderLayout.SOUTH);
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

            for (Player p : CryodexController.getActiveTournament().getPlayers()) {
                filteredPlayers.remove(p);
            }
            
            playerList.setValues(filteredPlayers, playerList.getList2Values());
        }
    }
}
