package cryodex.modules.battletech.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cryodex.CryodexController;
import cryodex.modules.Match;
import cryodex.modules.Match.GameResult;
import cryodex.modules.RoundPanel;
import cryodex.modules.Tournament;
import cryodex.widget.ComponentUtils;
import cryodex.widget.ConfirmationTextField;

public class BTRoundPanel extends RoundPanel {

    private static final long serialVersionUID = 1L;

    public BTRoundPanel(Tournament t, List<Match> matches) {
        super(t, matches);

    }

    public void buildGamePanel(JPanel panel, GridBagConstraints gbc, RoundPanel.GamePanel gamePanel) {

        GamePanel gp = (GamePanel) gamePanel;

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(gp.getPlayerTitle(), gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 3;
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

            gbc.gridx = 4;
            gbc.insets = new Insets(0, 10, 0, 0);
            panel.add(gp.getPlayer1DropBox(), gbc);
            gbc.insets = new Insets(0, 0, 0, 0);

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

            gbc.gridx = 4;
            gbc.insets = new Insets(0, 10, 0, 0);
            panel.add(gp.getPlayer2DropBox(), gbc);
            gbc.insets = new Insets(0, 0, 0, 0);
        }
    }

    @Override
    public List<RoundPanel.GamePanel> getGamePanels(List<Match> matches) {
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
        private ConfirmationTextField player1KillPoints;
        private ConfirmationTextField player2KillPoints;
        private JLabel player1KillLabel;
        private JLabel player2KillLabel;

        public GamePanel(int tableNumber, Match match) {
            super(tableNumber, match);
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
        

        public void setMatchPointsFromGUI() {
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

            if (getMatch().getPlayer2() != null) {
                if (getMatch().getPlayer1Points() != null) {
                    getPlayer1KillPointsField().setText(String.valueOf(getMatch().getPlayer1Points()));
                }
                if (getMatch().getPlayer2Points() != null) {
                    getPlayer2KillPointsField().setText(String.valueOf(getMatch().getPlayer2Points()));
                }
            }


            updateGUI();
        }

        public void updateGUI() {
            String titleText = null;

            boolean showKillPoints = CryodexController.getOptions().isShowKillPoints();
            boolean hideCompletedMatches = CryodexController.getOptions().isHideCompleted();

            boolean visible = hideCompletedMatches == false || getTournament().isMatchComplete(getMatch()) == false;

            getPlayer1KillLabel().setVisible(visible && showKillPoints);
            getPlayer1KillPointsField().setVisible(visible && showKillPoints);
            getPlayer2KillLabel().setVisible(visible && showKillPoints);
            getPlayer2KillPointsField().setVisible(visible && showKillPoints);
            getPlayerTitle().setVisible(visible);
            getResultCombo().setVisible(visible);
            getPlayer1DropBox().setVisible(visible);
            getPlayer2DropBox().setVisible(visible);

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

                getPlayer1KillLabel().setText(getMatch().getPlayer1().getName() + " kill points");
                getPlayer2KillLabel().setText(getMatch().getPlayer2().getName() + " kill points");
            }

            getPlayerTitle().setText(titleText);

        }

        @Override
        public void addToFocusPolicy(Vector<Component> order) {
            if (getMatch().getPlayer2() != null) {
                order.add(getPlayer1KillPointsField());
                order.add(getPlayer2KillPointsField());
            }
        }

		@Override
		public void setResultsCombo() {
			// Do Nothing
		}
    }

}
