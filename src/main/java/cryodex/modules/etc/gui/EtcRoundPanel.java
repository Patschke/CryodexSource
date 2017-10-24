package cryodex.modules.etc.gui;

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
import cryodex.modules.RoundPanel;
import cryodex.modules.Match.GameResult;
import cryodex.modules.etc.EtcMatch;
import cryodex.modules.etc.EtcTournament;
import cryodex.widget.ComponentUtils;
import cryodex.widget.ConfirmationTextField;

public class EtcRoundPanel extends RoundPanel {

    private static final long serialVersionUID = 1L;

    private final List<Match> matches;
    private final List<GamePanel> gamePanels = new ArrayList<GamePanel>();
    private JPanel quickEntryPanel;
    private JPanel quickEntrySubPanel;
    private JTextField roundNumber;
    private JComboBox<Player> playerCombo;
    private final JScrollPane scroll;

    private final EtcTournament tournament;

    public EtcRoundPanel(EtcTournament t, List<Match> matches) {

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
        ComponentUtils.repaint(EtcRoundPanel.this);

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

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(gamePanel.getPlayerTitle(), gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(gamePanel.getResultCombo(), gbc);

        if (gamePanel.getMatch().getPlayer2() != null) {
            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;

            gbc.gridx = 1;
            panel.add(gamePanel.getPlayer1KillLabel(), gbc);

            gbc.gridx = 2;
            panel.add(gamePanel.getPlayer1KillPointsField(), gbc);

            gbc.gridx = 3;
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(gamePanel.getPlayer1KillPointsField().getIndicator(), gbc);

            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;

            gbc.gridx = 1;
            panel.add(gamePanel.getPlayer2KillLabel(), gbc);

            gbc.gridx = 2;
            panel.add(gamePanel.getPlayer2KillPointsField(), gbc);

            gbc.gridx = 3;
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(gamePanel.getPlayer2KillPointsField().getIndicator(), gbc);
        }

        quickEntrySubPanel.add(panel, BorderLayout.CENTER);

        ComponentUtils.repaint(quickEntrySubPanel);
        ComponentUtils.repaint(quickEntryPanel);
    }

    public JPanel buildPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = -1;

        for (GamePanel gp : gamePanels) {
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.EAST;
            panel.add(gp.getPlayerTitle(), gbc);

            gbc.gridx = 2;
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(gp.getResultCombo(), gbc);

            if (gp.getMatch().getPlayer2() != null) {
                gbc.gridy++;
                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.NONE;

                gbc.gridx = 1;
                panel.add(gp.getPlayer1KillLabel(), gbc);

                gbc.gridx = 2;
                panel.add(gp.getPlayer1KillPointsField(), gbc);

                gbc.gridx = 3;
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(gp.getPlayer1KillPointsField().getIndicator(), gbc);

                gbc.gridy++;
                gbc.gridwidth = 1;
                gbc.anchor = GridBagConstraints.EAST;

                gbc.gridx = 1;
                panel.add(gp.getPlayer2KillLabel(), gbc);

                gbc.gridx = 2;
                panel.add(gp.getPlayer2KillPointsField(), gbc);

                gbc.gridx = 3;
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(gp.getPlayer2KillPointsField().getIndicator(), gbc);
            }
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
        private final EtcMatch match;
        private JLabel playersTitle;
        private JComboBox<String> resultsCombo;
        private ConfirmationTextField player1KillPoints;
        private ConfirmationTextField player2KillPoints;
        private JLabel player1KillLabel;
        private JLabel player2KillLabel;
        private boolean isLoading = false;
        private boolean isChanging = false;
        private int tableNumber = -1;

        public GamePanel(int tableNumber, Match match) {

            this.tableNumber = tableNumber;

            isLoading = true;

            if(match instanceof EtcMatch){
            	this.match = (EtcMatch) match;
            } else {
            	throw new NullPointerException("Invalid Match");
            }

            setGUIFromMatch();

            isLoading = false;
        }

        private Match getMatch() {
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

        private String[] getComboValues() {

            if (match.getPlayer2() == null) {
                String[] values = { "Select a result", "BYE" };
                return values;
            } else {
                String generic = CryodexController.getOptions().isEnterOnlyPoints() ? "Enter results" : "Select a result";
                String[] values = { generic, "WIN - " + match.getPlayer1().getName() + " " + match.getSuffix(), "WIN - " + match.getPlayer2().getName() + " " + match.getSuffix()};
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

            if (match.getPlayer1Points() != null || match.getPlayer2Points() != null) {

                Integer p1points = match.getPlayer1Points() == null ? 0 : match.getPlayer1Points();
                Integer p2points = match.getPlayer2Points() == null ? 0 : match.getPlayer2Points();

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
            if (tournament.isValidResult(match) == false) {
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
            match.setPlayer1PointsDestroyed(player1points);

            // Set player 2 points
            Integer player2points = null;
            try {
                player2points = Integer.valueOf(player2KillPoints.getText());
            } catch (Exception e) {

            }
            match.setPlayer2PointsDestroyed(player2points);
        }
        
        private void setMatchResultFromGUI() {
            
            switch (getResultCombo().getSelectedIndex()) {
            case 0:
                match.setGame1Result(null);
                match.setBye(false);
                break;
            case 1:
                if (match.getPlayer2() == null) {
                    match.setBye(true);
                } else {
                    match.setGame1Result(GameResult.PLAYER_1_WINS);
                }
                break;
            case 2:
                match.setGame1Result(GameResult.PLAYER_2_WINS);
                break;
            default:
                break;
            }
        }

        private void setGUIFromMatch() {

            if (tournament.isMatchComplete(match)) {
                if (match.isBye()) {
                    getResultCombo().setSelectedIndex(1);
                } else {
                    if (match.getWinner(1) == match.getPlayer1()) {
                        getResultCombo().setSelectedIndex(1);
                    } else if (match.getWinner(1) == match.getPlayer2()) {
                        getResultCombo().setSelectedIndex(2);
                    }
                }
            }

            if (match.getPlayer2() != null) {
                if (match.getPlayer1Points() != null) {
                    getPlayer1KillPointsField().setText(String.valueOf(match.getPlayer1Points()));
                }
                if (match.getPlayer2Points() != null) {
                    getPlayer2KillPointsField().setText(String.valueOf(match.getPlayer2Points()));
                }
            }
            
            // Special exception for bye matches
            if(match.getPlayer2() == null && CryodexController.getOptions().isEnterOnlyPoints()){
                match.setBye(true);
                getResultCombo().setSelectedIndex(1);
            }

            updateGUI();
        }

        private void updateGUI() {
            String titleText = null;

            boolean showKillPoints = CryodexController.getOptions().isShowKillPoints();
            boolean enterOnlyPoints = CryodexController.getOptions().isEnterOnlyPoints();
            boolean hideCompletedMatches = CryodexController.getOptions().isHideCompleted();

            boolean visible = hideCompletedMatches == false || tournament.isMatchComplete(match) == false;

            getPlayer1KillLabel().setVisible(visible && showKillPoints);
            getPlayer1KillPointsField().setVisible(visible && showKillPoints);
            getPlayer2KillLabel().setVisible(visible && showKillPoints);
            getPlayer2KillPointsField().setVisible(visible && showKillPoints);
            getPlayerTitle().setVisible(visible);
            getResultCombo().setVisible(visible);

            if (match.getPlayer2() == null) {
                titleText = match.getPlayer1().getName() + " " + match.getSuffix() + " has a BYE";
            } else {
                titleText = match.getPlayer1().getName() + " " + match.getSuffix() + " VS " + match.getPlayer2().getName() + " " + match.getSuffix();
                if (match.isDuplicate()) {
                    titleText = "(Duplicate)" + titleText;
                }

                if (CryodexController.getOptions().isShowTableNumbers()) {
                    titleText = tableNumber + ": " + titleText;
                }

                getPlayer1KillLabel().setText(match.getPlayer1().getName() + " " + match.getSuffix() + " kill points");
                getPlayer2KillLabel().setText(match.getPlayer2().getName() + " " + match.getSuffix() + " kill points");
            }

            getPlayerTitle().setText(titleText);

            if (enterOnlyPoints) {

                getResultCombo().setEnabled(false);

                if (match.getPlayer1Points() != null && match.getPlayer2Points() != null) {
                    Integer p1points = match.getPlayer1Points();
                    Integer p2points = match.getPlayer2Points();

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
