package cryodex.modules.swlcg.gui;

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
import cryodex.modules.swlcg.SWLCGTournament;

public class SWLCGRoundPanel extends RoundPanel {

    private static final long serialVersionUID = 1L;

    public SWLCGRoundPanel(SWLCGTournament t, List<Match> matches) {
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
        panel.add(gp.getGame1ResultCombo(), gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(gp.getGame2ResultCombo(), gbc);
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
        private JComboBox<String> game1ResultsCombo;
        private JComboBox<String> game2ResultsCombo;

        public GamePanel(int tableNumber, Match match) {
            super(tableNumber, match);
        }

        private String[] getComboValues() {

            if (getMatch().getPlayer2() == null) {
                String[] values = { "Select a result", "BYE" };
                return values;
            } else {
                String generic = "Select a result";
                String[] values = { generic, "WIN - " + getMatch().getPlayer1().getName(), "WIN - " + getMatch().getPlayer2().getName(), "DRAW" };
                return values;
            }
        }

        private JComboBox<String> getGame1ResultCombo() {
            if (game1ResultsCombo == null) {

                game1ResultsCombo = new JComboBox<String>(getComboValues());

                game1ResultsCombo.setRenderer(new DefaultListCellRenderer() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void paint(Graphics g) {
                        setForeground(Color.BLACK);
                        super.paint(g);
                    }
                });

                game1ResultsCombo.addActionListener(this);
            }
            return game1ResultsCombo;
        }

        private JComboBox<String> getGame2ResultCombo() {
            if (game2ResultsCombo == null) {

                game2ResultsCombo = new JComboBox<String>(getComboValues());

                game2ResultsCombo.setRenderer(new DefaultListCellRenderer() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void paint(Graphics g) {
                        setForeground(Color.BLACK);
                        super.paint(g);
                    }
                });

                game2ResultsCombo.addActionListener(this);
            }
            return game2ResultsCombo;
        }

        public void setMatchPointsFromGUI() {

            if (isLoading()) {
                return;
            }

            switch (game1ResultsCombo.getSelectedIndex()) {
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
            case 3:
                getMatch().setGame1Result(GameResult.DRAW);
                break;
            default:
                break;
            }

            switch (game2ResultsCombo.getSelectedIndex()) {
            case 0:
                getMatch().setGame2Result(null);
                break;
            case 1:
                if (getMatch().isBye() == false) {
                    getMatch().setGame2Result(GameResult.PLAYER_1_WINS);
                }
                break;
            case 2:
                getMatch().setGame2Result(GameResult.PLAYER_2_WINS);
                break;
            case 3:
                getMatch().setGame2Result(GameResult.DRAW);
                break;
            default:
                break;
            }

        }

        public void setGUIFromMatch() {

                if (getMatch().isBye()) {
                    getGame1ResultCombo().setSelectedIndex(1);
                    getGame2ResultCombo().setSelectedIndex(1);
                } else {
                    if (getMatch().getGame1Result() == GameResult.PLAYER_1_WINS) {
                        getGame1ResultCombo().setSelectedIndex(1);
                    } else if (getMatch().getGame1Result() == GameResult.PLAYER_2_WINS) {
                        getGame1ResultCombo().setSelectedIndex(2);
                    } else if (getMatch().getGame1Result() == GameResult.DRAW) {
                        getGame1ResultCombo().setSelectedIndex(3);
                    }

                    if (getMatch().getGame2Result() == GameResult.PLAYER_1_WINS) {
                        getGame2ResultCombo().setSelectedIndex(1);
                    } else if (getMatch().getGame2Result() == GameResult.PLAYER_2_WINS) {
                        getGame2ResultCombo().setSelectedIndex(2);
                    } else if (getMatch().getGame2Result() == GameResult.DRAW) {
                        getGame2ResultCombo().setSelectedIndex(3);
                    }
                }

            // Special exception for bye matches
            if (getMatch().isBye()) {
                getGame1ResultCombo().setSelectedIndex(1);
                getGame2ResultCombo().setSelectedIndex(1);
            }

            updateGUI();
        }

        public void updateGUI() {
            String titleText = null;

            boolean hideCompletedMatches = CryodexController.getOptions().isHideCompleted();

            boolean visible = hideCompletedMatches == false || getTournament().isMatchComplete(getMatch()) == false;

            getPlayerTitle().setVisible(visible);
            getGame1ResultCombo().setVisible(visible);
            getGame2ResultCombo().setVisible(visible);

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

            getGame1ResultCombo().setEnabled(true);
            getGame2ResultCombo().setEnabled(true);
        }

        @Override
        public void setResultsCombo() {
            // No function
        }

        @Override
        public void setMatchResultFromGUI() {
            // No function
        }

        @Override
        public void addToFocusPolicy(Vector<Component> order) {
            // Do Nothing
        }
    }
}
