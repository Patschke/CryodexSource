package cryodex.modules.krayt.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cryodex.CryodexController;
import cryodex.Player;
import cryodex.modules.Match;
import cryodex.modules.Match.GameResult;
import cryodex.modules.RoundPanel;
import cryodex.modules.krayt.KraytMatch;
import cryodex.modules.krayt.KraytPlayer;
import cryodex.modules.krayt.KraytTournament;
import cryodex.widget.ComponentUtils;
import cryodex.widget.ConfirmationTextField;

public class KraytRoundPanel extends RoundPanel {

    private static final long serialVersionUID = 1L;

    private final List<Match> matches;
    private final List<GamePanel> gamePanels = new ArrayList<GamePanel>();
    private JPanel quickEntryPanel;
    private JPanel quickEntrySubPanel;
    private JTextField roundNumber;
    private JComboBox<Player> playerCombo;
    private final JScrollPane scroll;

    private final KraytTournament tournament;

    public KraytRoundPanel(KraytTournament t, List<Match> matches) {

        super(new BorderLayout());

        this.tournament = t;
        this.matches = matches;
        this.setBorder(BorderFactory.createLineBorder(Color.black));

        int counter = 1;
        for (Match match : matches) {
            GamePanel gpanel = new GamePanel(counter, match);
            gamePanels.add(gpanel);
            counter++;
        }

        scroll = new JScrollPane(ComponentUtils.addToFlowLayout(buildPanel(), FlowLayout.CENTER));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scroll.getVerticalScrollBar().setValue(0);
                scroll.getVerticalScrollBar().setUnitIncrement(15);
            }
        });

        this.add(getQuickEntryPanel(), BorderLayout.NORTH);
        this.add(scroll, BorderLayout.CENTER);
    }

    public JPanel getQuickEntryPanel() {
        if (quickEntryPanel == null) {
            quickEntryPanel = new JPanel(new BorderLayout());
            quickEntryPanel.setVisible(CryodexController.getOptions().isShowQuickFind());
            ComponentUtils.forceSize(quickEntryPanel, 405, 135);

            quickEntrySubPanel = new JPanel(new BorderLayout());
            ComponentUtils.forceSize(quickEntrySubPanel, 400, 130);

            roundNumber = new JTextField(5);

            roundNumber.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    update();
                }
            });

            List<Player> playerList = new ArrayList<Player>();

            playerList.add(new Player());
            playerList.addAll(tournament.getPlayers());

            Collections.sort(playerList);

            playerCombo = new JComboBox<Player>(playerList.toArray(new Player[playerList.size()]));

            playerCombo.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    update();
                }
            });

            quickEntryPanel
                    .add(ComponentUtils.addToFlowLayout(
                            ComponentUtils.addToHorizontalBorderLayout(new JLabel("Enter table number"), roundNumber,
                                    ComponentUtils.addToHorizontalBorderLayout(new JLabel("or choose a player"), playerCombo, null)),
                            FlowLayout.CENTER), BorderLayout.NORTH);

            quickEntryPanel.add(quickEntrySubPanel);
        }

        return quickEntryPanel;
    }

    public void update() {

        scroll.getViewport().removeAll();
        scroll.getViewport().add(ComponentUtils.addToFlowLayout(buildPanel(), FlowLayout.CENTER));
        ComponentUtils.repaint(KraytRoundPanel.this);

        Integer i = null;
        try {
            i = Integer.parseInt(roundNumber.getText());
        } catch (NumberFormatException e) {

        }

        Player player = playerCombo.getSelectedIndex() == 0 ? null : (Player) playerCombo.getSelectedItem();

        if (player != null) {
            roundNumber.setEnabled(false);
        } else if (i != null) {
            playerCombo.setEnabled(false);
        } else {
            roundNumber.setEnabled(true);
            playerCombo.setEnabled(true);
        }

        GamePanel gamePanel = null;
        if (i != null) {
            if (i > gamePanels.size()) {
                return;
            }

            gamePanel = gamePanels.get(i - 1);
        } else if (player != null) {
            for (GamePanel g : gamePanels) {
                if (g.getMatch().getPlayer1() == player) {
                    gamePanel = g;
                    break;
                } else if (g.getMatch().getPlayer2() != null && g.getMatch().getPlayer2() == player) {
                    gamePanel = g;
                    break;
                }
            }
        }

        if (gamePanel == null) {
            return;
        }

        quickEntrySubPanel.add(gamePanel.getPlayerTitle(), BorderLayout.CENTER);

        quickEntrySubPanel.removeAll();

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        addGamePanel(gamePanel, panel, gbc);

        quickEntrySubPanel.add(ComponentUtils.addToFlowLayout(panel, FlowLayout.CENTER), BorderLayout.CENTER);

        ComponentUtils.repaint(quickEntrySubPanel);
        ComponentUtils.repaint(quickEntryPanel);
    }
    
    public void addGamePanel(GamePanel gp, JPanel panel, GridBagConstraints gbc){
      gbc.gridy++;
      gbc.gridx = 0;
      gbc.gridwidth = 4;
      gbc.anchor = GridBagConstraints.EAST;
      panel.add(gp.getPlayerTitle(), gbc);

      gbc.gridx = 4;
      gbc.gridwidth = 2;
      gbc.fill = GridBagConstraints.BOTH;
      panel.add(gp.getResultCombo(), gbc);

      if (gp.getMatch().getPlayer2() != null) {
          gbc.gridy++;
          gbc.gridwidth = 1;
          gbc.fill = GridBagConstraints.NONE;
          gbc.anchor = GridBagConstraints.EAST;
          
          gbc.weightx = 1;
          gbc.gridx = 1;
          panel.add(gp.getPlayer1KillLabel(), gbc);

          gbc.weightx = 0;
          gbc.gridx = 2;
          panel.add(gp.getPlayer1KillPointsField(), gbc);

          gbc.gridx = 3;
          gbc.anchor = GridBagConstraints.WEST;
          panel.add(gp.getPlayer1KillPointsField().getIndicator(), gbc);
          
          gbc.gridx = 4;
          gbc.anchor = GridBagConstraints.EAST;
          panel.add(gp.getSubPlayer1Label(), gbc);
          
          gbc.gridx = 5;
          gbc.anchor = GridBagConstraints.WEST;
          panel.add(gp.getSubPlayer1Combo(), gbc);

          gbc.gridy++;
          gbc.anchor = GridBagConstraints.EAST;

          gbc.gridx = 1;
          gbc.weightx = 1;
          panel.add(gp.getPlayer2KillLabel(), gbc);

          gbc.gridx = 2;
          gbc.weightx = 0;
          panel.add(gp.getPlayer2KillPointsField(), gbc);

          gbc.gridx = 3;
          gbc.anchor = GridBagConstraints.WEST;
          panel.add(gp.getPlayer2KillPointsField().getIndicator(), gbc);
          
          gbc.gridx = 4;
          gbc.anchor = GridBagConstraints.EAST;
          panel.add(gp.getSubPlayer2Label(), gbc);
          
          gbc.gridx = 5;
          gbc.anchor = GridBagConstraints.WEST;
          panel.add(gp.getSubPlayer2Combo(), gbc);
      }
    }

    public JPanel buildPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        
        for (GamePanel gp : gamePanels) {
            addGamePanel(gp, panel, gbc);
        }
        
        return panel;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void resetGamePanels(boolean isTextOnly) {
        for (GamePanel gp : gamePanels) {
            gp.updateGUI();
        }
        getQuickEntryPanel().setVisible(CryodexController.getOptions().isShowQuickFind());
        ComponentUtils.repaint(this);
    }

    private class GamePanel implements FocusListener, ActionListener {
        private final KraytMatch match;
        private JLabel playersTitle;
        private JComboBox<String> resultsCombo;
        private ConfirmationTextField player1KillPoints;
        private ConfirmationTextField player2KillPoints;
        private JLabel player1KillLabel;
        private JLabel player2KillLabel;
        private JLabel subPlayer1Label;
        private JLabel subPlayer2Label;
        private JComboBox<String> subPlayer1Combo;
        private JComboBox<String> subPlayer2Combo;
        private boolean isLoading = false;
        private boolean isChanging = false;
        private int tableNumber = -1;

        public GamePanel(int tableNumber, Match match) {

            this.tableNumber = tableNumber;

            isLoading = true;

            if(match instanceof KraytMatch){
            	this.match = (KraytMatch) match;
            } else {
            	throw new NullPointerException("Invalid Match");
            }

            setGUIFromMatch();

            isLoading = false;
        }

        private KraytMatch getMatch() {
            return match;
        }

        private JLabel getPlayerTitle() {
            if (playersTitle == null) {

                playersTitle = new JLabel("");
                playersTitle.setFont(new Font(playersTitle.getFont().getName(), playersTitle.getFont().getStyle(), 20));
                playersTitle.setHorizontalAlignment(SwingConstants.RIGHT);
            }
            return playersTitle;
        }

        private JLabel getPlayer1KillLabel() {
            if (player1KillLabel == null) {
                player1KillLabel = new JLabel();
            }
            return player1KillLabel;
        }

        private JLabel getPlayer2KillLabel() {
            if (player2KillLabel == null) {
                player2KillLabel = new JLabel();
            }
            return player2KillLabel;
        }
        
        private JLabel getSubPlayer1Label() {
            if (subPlayer1Label == null) {
                subPlayer1Label = new JLabel();
            }
            return subPlayer1Label;
        }

        private JLabel getSubPlayer2Label() {
            if (subPlayer2Label == null) {
                subPlayer2Label = new JLabel();
            }
            return subPlayer2Label;
        }
        
        private JComboBox<String> getSubPlayer1Combo(){
            if(subPlayer1Combo == null){
                
                KraytPlayer kraytPlayer = tournament.getModulePlayer(match.getPlayer1());
                
                List<String> playerNames = new ArrayList<String>();
                playerNames.add("");
                for(Player p : kraytPlayer.getSubPlayers()){
                    playerNames.add(p.getName());
                }
                
                String[] pnsa = (String[]) playerNames.toArray(new String[playerNames.size()]);
                
                subPlayer1Combo = new JComboBox<String>(pnsa);
            }
            
            return subPlayer1Combo;
        }
        
        private JComboBox<String> getSubPlayer2Combo(){
            if(subPlayer2Combo == null){
                
                KraytPlayer kraytPlayer = tournament.getModulePlayer(match.getPlayer2());
                
                List<String> playerNames = new ArrayList<String>();
                playerNames.add("");
                for(Player p : kraytPlayer.getSubPlayers()){
                    playerNames.add(p.getName());
                }
                
                String[] pnsa = (String[]) playerNames.toArray(new String[playerNames.size()]);
                
                subPlayer2Combo = new JComboBox<String>(pnsa);
            }
            
            return subPlayer2Combo;
        }

        private String[] getComboValues() {

            if (getMatch().getPlayer2() == null) {
                String[] values = { "Select a result", "BYE" };
                return values;
            } else {
                String generic = CryodexController.getOptions().isEnterOnlyPoints() ? "Enter results" : "Select a result";
                String[] values = { generic, "WIN - " + getMatch().getPlayer1().getName() + " " + getMatch().getSuffix(), "WIN - " + getMatch().getPlayer2().getName() + " " + getMatch().getSuffix()};
                return values;
            }
        }

        private JComboBox<String> getResultCombo() {
            if (resultsCombo == null) {

                resultsCombo = new JComboBox<String>(getComboValues());

                resultsCombo.setRenderer(new DefaultListCellRenderer() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void paint(Graphics g) {
                        setForeground(Color.BLACK);
                        super.paint(g);
                    }
                });

                resultsCombo.addActionListener(GamePanel.this);
            }
            return resultsCombo;
        }

        public ConfirmationTextField getPlayer1KillPointsField() {
            if (player1KillPoints == null) {
                player1KillPoints = new ConfirmationTextField();
                player1KillPoints.addFocusListener(GamePanel.this);
                ComponentUtils.forceSize(player1KillPoints, 50, 25);
            }

            return player1KillPoints;
        }

        public ConfirmationTextField getPlayer2KillPointsField() {
            if (player2KillPoints == null) {
                player2KillPoints = new ConfirmationTextField();
                player2KillPoints.addFocusListener(GamePanel.this);
                ComponentUtils.forceSize(player2KillPoints, 50, 25);
            }

            return player2KillPoints;
        }

        /**
         * This function sets the combo box value to the winner of the match based on points.
         */
        private void setResultsCombo() {

            boolean enterOnlyPoints = CryodexController.getOptions().isEnterOnlyPoints();

            if (getMatch().getPlayer1Points() != null || getMatch().getPlayer2Points() != null) {

                Integer p1points = getMatch().getPlayer1Points() == null ? 0 : getMatch().getPlayer1Points();
                Integer p2points = getMatch().getPlayer2Points() == null ? 0 : getMatch().getPlayer2Points();

                if (p1points.equals(p2points)) {
                    // Only reset the result if it was not enabled before. This
                    // prevents the combo box from resetting if the result
                    // didn't actually change.
                    if (getResultCombo().isEnabled() == false) {
                        getResultCombo().setSelectedIndex(0);
                    }
                    getResultCombo().setEnabled(true);
                }
                if (p1points > p2points) {
                    getResultCombo().setSelectedIndex(1);
                    getResultCombo().setEnabled(!enterOnlyPoints);
                }

                if (p2points > p1points) {
                    getResultCombo().setSelectedIndex(2);
                    getResultCombo().setEnabled(!enterOnlyPoints);
                }
            } else {
                getResultCombo().setSelectedIndex(0);
                getResultCombo().setEnabled(!enterOnlyPoints);
            }
        }

        public void markInvalid() {
            if (tournament.isValidResult(getMatch()) == false) {
                getPlayerTitle().setForeground(Color.red);
            } else {
                getPlayerTitle().setForeground(Color.black);
            }
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            triggerChange(true);
        }

        @Override
        public void focusGained(FocusEvent e) {
            // DO NOTHING!!!
        }

        @Override
        public void focusLost(FocusEvent e) {
            triggerChange(false);
        }

        private void triggerChange(boolean isComboChange) {
            if (isLoading || isChanging) {
                return;
            }
            isChanging = true;

            setMatchPointsFromGUI();

            // Update combo panel
            if (isComboChange == false && CryodexController.getOptions().isEnterOnlyPoints()) {
                setResultsCombo();
            }
            
            setMatchResultFromGUI();

            tournament.triggerChange();
            
            // Needed to hide completed matches
            updateGUI();
            
            isChanging = false;
        }

        private void setMatchPointsFromGUI() {
         // Set player 1 points
            Integer player1points = null;
            try {
                player1points = Integer.valueOf(player1KillPoints.getText());
            } catch (Exception e) {

            }
            getMatch().setPlayer1PointsDestroyed(player1points);

            // Set player 2 points
            Integer player2points = null;
            try {
                player2points = Integer.valueOf(player2KillPoints.getText());
            } catch (Exception e) {

            }
            getMatch().setPlayer2PointsDestroyed(player2points);
            
        }
        
        private void setMatchResultFromGUI() {

            getMatch().setSubplayer1((String) getSubPlayer1Combo().getSelectedItem());
            getMatch().setSubplayer2((String) getSubPlayer2Combo().getSelectedItem());
            
            switch (getResultCombo().getSelectedIndex()) {
            case 0:
                getMatch().setGame1Result(null);
                break;
            case 1:
                if (getMatch().isBye() == false) {
                    getMatch().setGame1Result(GameResult.PLAYER_1_WINS);
                }
                break;
            case 2:
                getMatch().setGame1Result(GameResult.PLAYER_2_WINS);
                break;
            default:
                break;
            }
        }

        private void setGUIFromMatch() {

            if (tournament.isMatchComplete(getMatch())) {
                if (getMatch().isBye()) {
                    getResultCombo().setSelectedIndex(1);
                } else {
                    if (getMatch().getWinner(1) == getMatch().getPlayer1()) {
                        getResultCombo().setSelectedIndex(1);
                    } else if (getMatch().getWinner(1) == getMatch().getPlayer2()) {
                        getResultCombo().setSelectedIndex(2);
                    }
                }
            }

            if (getMatch().getPlayer2() != null) {
                if (getMatch().getPlayer1Points() != null) {
                    getPlayer1KillPointsField().setText(String.valueOf(getMatch().getPlayer1Points()));
                }
                if (getMatch().getPlayer2Points() != null) {
                    getPlayer2KillPointsField().setText(String.valueOf(getMatch().getPlayer2Points()));
                }
            }
            
            // Special exception for bye matches
            if(getMatch().isBye() && CryodexController.getOptions().isEnterOnlyPoints()){
                getResultCombo().setSelectedIndex(1);
            }

            String subPlayer1 = match.getSubplayer1();
            if(subPlayer1 == null){
                getSubPlayer1Combo().setSelectedIndex(0);
            } else {
                getSubPlayer1Combo().setSelectedItem(subPlayer1);    
            }
            
            String subPlayer2 = match.getSubplayer2();
            if(subPlayer2 == null){
                getSubPlayer2Combo().setSelectedIndex(0);
            } else {
                getSubPlayer2Combo().setSelectedItem(subPlayer2);    
            }
            
            updateGUI();
        }

        private void updateGUI() {
            String titleText = null;

            boolean showKillPoints = CryodexController.getOptions().isShowKillPoints();
            boolean enterOnlyPoints = CryodexController.getOptions().isEnterOnlyPoints();
            boolean hideCompletedMatches = CryodexController.getOptions().isHideCompleted();

            boolean visible = hideCompletedMatches == false || tournament.isMatchComplete(getMatch()) == false;

            getPlayer1KillLabel().setVisible(visible && showKillPoints);
            getPlayer1KillPointsField().setVisible(visible && showKillPoints);
            getPlayer2KillLabel().setVisible(visible && showKillPoints);
            getPlayer2KillPointsField().setVisible(visible && showKillPoints);
            getPlayerTitle().setVisible(visible);
            getResultCombo().setVisible(visible);

            if (getMatch().getPlayer2() == null) {
                titleText = getMatch().getPlayer1().getName() + " has a BYE";
            } else {
                titleText = getMatch().getPlayer1().getName() + " " + getMatch().getSuffix() + " VS " + getMatch().getPlayer2().getName() + " " + getMatch().getSuffix();
                if (getMatch().isDuplicate()) {
                    titleText = "(Duplicate)" + titleText;
                }

                if (CryodexController.getOptions().isShowTableNumbers()) {
                    titleText = tableNumber + ": " + titleText;
                }

                getPlayer1KillLabel().setText(getMatch().getPlayer1().getName() + " " + getMatch().getSuffix() + " kill points");
                getPlayer2KillLabel().setText(getMatch().getPlayer2().getName() + " " + getMatch().getSuffix() + " kill points");

                getSubPlayer1Label().setText(getMatch().getPlayer1().getName() + " selected player");
                getSubPlayer2Label().setText(getMatch().getPlayer2().getName() + " selected player");
            }

            getPlayerTitle().setText(titleText);

            if (enterOnlyPoints) {

                getResultCombo().setEnabled(false);

                if (getMatch().getPlayer1Points() != null && getMatch().getPlayer2Points() != null) {
                    Integer p1points = getMatch().getPlayer1Points();
                    Integer p2points = getMatch().getPlayer2Points();

                    if (p1points.equals(p2points)) {
                        getResultCombo().setEnabled(true);
                    }
                }
            } else {
                getResultCombo().setEnabled(true);
            }
        }
    }

    public void markInvalid() {
        for (GamePanel gamePanel : gamePanels) {
            gamePanel.markInvalid();
        }
    }

}
