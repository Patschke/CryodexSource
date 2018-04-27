package cryodex.modules.destiny.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import cryodex.CryodexController;
import cryodex.modules.Match;
import cryodex.modules.Match.GameResult;
import cryodex.modules.RoundPanel;
import cryodex.modules.destiny.DestinyTournament;

public class DestinyRoundPanel extends RoundPanel {

    private static final long serialVersionUID = 1L;

    public DestinyRoundPanel(DestinyTournament t, List<Match> matches) {
        super(t, matches);
    }

    @Override
    public void buildGamePanel(JPanel panel, GridBagConstraints gbc, cryodex.modules.RoundPanel.GamePanel gamePanel) {
        GamePanel gp = (GamePanel) gamePanel;

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(gp.getPlayerTitle(), gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(gp.getResultCombo(), gbc);
    }

    @Override
    public List<cryodex.modules.RoundPanel.GamePanel> getGamePanels(List<Match> matches) {
        List<RoundPanel.GamePanel> gamePanels = new ArrayList<RoundPanel.GamePanel>();

        int counter = 0;
        for (Match match : matches) {
            counter++;
            GamePanel gp = new GamePanel(counter, match);
            gamePanels.add(gp);
        }

        return gamePanels;
    }

    private class GamePanel extends RoundPanel.GamePanel {

        private JComboBox<String> resultsCombo;

        public GamePanel(int tableNumber, Match match) {
            super(tableNumber, match);
        }

        private String[] getComboValues() {

            if (getMatch().getPlayer2() == null) {
                String[] values = { "Select a result", "BYE" };
                return values;
            } else {
                String generic = "Select a result";
                String[] values = { generic, "WIN - " + getMatch().getPlayer1().getName(), "WIN - " + getMatch().getPlayer2().getName() };
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

        public void setMatchResultFromGUI() {

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

        public void setGUIFromMatch() {

            if (getTournament().isMatchComplete(getMatch())) {
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

            // Special exception for bye matches
            if (getMatch().isBye()) {
                getResultCombo().setSelectedIndex(1);
            }

            updateGUI();
        }

        public void updateGUI() {
            String titleText = null;

            boolean hideCompletedMatches = CryodexController.getOptions().isHideCompleted();

            boolean visible = hideCompletedMatches == false || getTournament().isMatchComplete(getMatch()) == false;

            getPlayerTitle().setVisible(visible);
            getResultCombo().setVisible(visible);

            if (getMatch().getPlayer2() == null) {
                titleText = getMatch().getPlayer1().getName() + " has a BYE";
            } else {
                titleText = getMatch().getPlayer1().getName() + " VS " + getMatch().getPlayer2().getName();
                if (getMatch().isDuplicate()) {
                    titleText = "(Duplicate)" + titleText;
                }

                if (CryodexController.getOptions().isShowTableNumbers()) {
                    titleText = getTableNumber() + ": " + titleText;
                }
            }

            getPlayerTitle().setText(titleText);

            getResultCombo().setEnabled(true);
        }

        @Override
        public void setResultsCombo() {
            // No Function
        }

        @Override
        public void setMatchPointsFromGUI() {
            // No Function
        }

        @Override
        public void addToFocusPolicy(Vector<Component> order) {
            // No Function
        }
    }
}
