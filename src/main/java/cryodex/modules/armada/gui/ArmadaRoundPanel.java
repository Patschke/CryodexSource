package cryodex.modules.armada.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
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
import cryodex.modules.armada.ArmadaTournament;
import cryodex.widget.ComponentUtils;
import cryodex.widget.ConfirmationTextField;

public class ArmadaRoundPanel extends RoundPanel {

    private static final long serialVersionUID = 1L;

    public ArmadaRoundPanel(ArmadaTournament t, List<Match> matches) {
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
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(gp.getResultCombo(), gbc);

        if (gp.getMatch().getPlayer2() != null) {
            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;

            gbc.gridx = 1;
            panel.add(gp.getPlayer1ScoreLabel(), gbc);

            gbc.gridx = 2;
            panel.add(gp.getPlayer1ScoreField(), gbc);

            gbc.gridx = 3;
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(gp.getPlayer1ScoreField().getIndicator(), gbc);

            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;

            gbc.gridx = 1;
            panel.add(gp.getPlayer2ScoreLabel(), gbc);

            gbc.gridx = 2;
            panel.add(gp.getPlayer2ScoreField(), gbc);

            gbc.gridx = 3;
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(gp.getPlayer2ScoreField().getIndicator(), gbc);
        }
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
        private ConfirmationTextField player1Score;
        private ConfirmationTextField player2Score;
        private JLabel player1ScoreLabel;
        private JLabel player2ScoreLabel;

        public GamePanel(int tableNumber, Match match) {
            super(tableNumber, match);
        }

        private JLabel getPlayer1ScoreLabel() {
            if (player1ScoreLabel == null) {
                player1ScoreLabel = new JLabel();
            }
            return player1ScoreLabel;
        }

        private JLabel getPlayer2ScoreLabel() {
            if (player2ScoreLabel == null) {
                player2ScoreLabel = new JLabel();
            }
            return player2ScoreLabel;
        }

        private String[] getComboValues() {

            if (getMatch().getPlayer2() == null) {
                String[] values = { "Select a result", "BYE" };
                return values;
            } else {
                String generic = "Select a result";
                String[] values = { generic, "WIN - " + getMatch().getPlayer1().getName(), "WIN - " + getMatch().getPlayer2().getName(),
                        getMatch().getPlayer1().getName() + " conceded", getMatch().getPlayer2().getName() + " conceded" };
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

        public ConfirmationTextField getPlayer1ScoreField() {
            if (player1Score == null) {
                player1Score = new ConfirmationTextField();
                player1Score.addFocusListener(GamePanel.this);
                ComponentUtils.forceSize(player1Score, 50, 25);
            }

            return player1Score;
        }

        public ConfirmationTextField getPlayer2ScoreField() {
            if (player2Score == null) {
                player2Score = new ConfirmationTextField();
                player2Score.addFocusListener(GamePanel.this);
                ComponentUtils.forceSize(player2Score, 50, 25);
            }

            return player2Score;
        }

        public void setMatchPointsFromGUI() {
            // Set player 1 points
            Integer player1points = null;
            try {
                player1points = Integer.valueOf(player1Score.getText());
            } catch (Exception e) {

            }
            getMatch().setPlayer1PointsDestroyed(player1points);

            // Set player 2 points
            Integer player2points = null;
            try {
                player2points = Integer.valueOf(player2Score.getText());
            } catch (Exception e) {

            }
            getMatch().setPlayer2PointsDestroyed(player2points);
        }

        public void setMatchResultFromGUI() {

            switch (getResultCombo().getSelectedIndex()) {
            case 0:
                getMatch().setGame1Result(null);
                getMatch().setConcede(false);
                break;
            case 1:
                if (getMatch().isBye() == false) {
                    getMatch().setGame1Result(GameResult.PLAYER_1_WINS);
                }
                getMatch().setConcede(false);
                break;
            case 2:
                getMatch().setGame1Result(GameResult.PLAYER_2_WINS);
                getMatch().setConcede(false);
                break;
            case 3:
                getMatch().setGame1Result(GameResult.PLAYER_2_WINS);
                getMatch().setConcede(true);
                break;
            case 4:
                getMatch().setGame1Result(GameResult.PLAYER_1_WINS);
                getMatch().setConcede(true);
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
                    if (getMatch().isConcede()) {
                        if (getMatch().getWinner(1) == getMatch().getPlayer1()) {
                            getResultCombo().setSelectedIndex(4);
                        } else if (getMatch().getWinner(1) == getMatch().getPlayer2()) {
                            getResultCombo().setSelectedIndex(3);
                        }
                    } else {
                        if (getMatch().getWinner(1) == getMatch().getPlayer1()) {
                            getResultCombo().setSelectedIndex(1);
                        } else if (getMatch().getWinner(1) == getMatch().getPlayer2()) {
                            getResultCombo().setSelectedIndex(2);
                        }
                    }
                }
            }

            if (getMatch().getPlayer2() != null) {
                if (getMatch().getPlayer1Points() != null) {
                    getPlayer1ScoreField().setText(String.valueOf(getMatch().getPlayer1Points()));
                }
                if (getMatch().getPlayer2Points() != null) {
                    getPlayer2ScoreField().setText(String.valueOf(getMatch().getPlayer2Points()));
                }
            }

            updateGUI();
        }

        public void updateGUI() {
            String titleText = null;

            boolean showKillPoints = CryodexController.getOptions().isShowKillPoints();
            boolean hideCompletedMatches = CryodexController.getOptions().isHideCompleted();

            boolean visible = hideCompletedMatches == false || getTournament().isMatchComplete(getMatch()) == false;

            getPlayer1ScoreLabel().setVisible(visible && showKillPoints);
            getPlayer1ScoreField().setVisible(visible && showKillPoints);
            getPlayer2ScoreLabel().setVisible(visible && showKillPoints);
            getPlayer2ScoreField().setVisible(visible && showKillPoints);
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

                getPlayer1ScoreLabel().setText(getMatch().getPlayer1().getName() + " score");
                getPlayer2ScoreLabel().setText(getMatch().getPlayer2().getName() + " score");
            }

            getPlayerTitle().setText(titleText);

            getResultCombo().setEnabled(true);
        }

        @Override
        public void setResultsCombo() {
            // No Function
        }

        @Override
        public void addToFocusPolicy(Vector<Component> order) {
            if (getMatch().getPlayer2() != null) {
                order.add(getPlayer1ScoreField());
                order.add(getPlayer2ScoreField());
            }
        }
    }
}
