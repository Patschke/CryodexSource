package cryodex.modules.swlcg.gui;

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
import cryodex.modules.swlcg.SWLCGTournament;
import cryodex.widget.ComponentUtils;

public class SWLCGRoundPanel extends RoundPanel {

	private static final long serialVersionUID = 1L;

	private final List<Match> matches;
	private final List<GamePanel> gamePanels = new ArrayList<GamePanel>();
	private JPanel quickEntryPanel;
	private JPanel quickEntrySubPanel;
	private JTextField roundNumber;
	private JComboBox<Player> playerCombo;
	private final JScrollPane scroll;

	private final SWLCGTournament tournament;

	public SWLCGRoundPanel(SWLCGTournament t, List<Match> matches) {

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
					.add(ComponentUtils
							.addToFlowLayout(
									ComponentUtils.addToHorizontalBorderLayout(new JLabel("Enter table number"),
											roundNumber, ComponentUtils.addToHorizontalBorderLayout(
													new JLabel("or choose a player"), playerCombo, null)),
									FlowLayout.CENTER),
							BorderLayout.NORTH);

			quickEntryPanel.add(quickEntrySubPanel);
		}

		return quickEntryPanel;
	}

	public void update() {

		scroll.getViewport().removeAll();
		scroll.getViewport().add(ComponentUtils.addToFlowLayout(buildPanel(), FlowLayout.CENTER));
		ComponentUtils.repaint(SWLCGRoundPanel.this);

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
		panel.add(gamePanel.getGame1ResultCombo(), gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(gamePanel.getGame2ResultCombo(), gbc);

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
			panel.add(gp.getGame1ResultCombo(), gbc);

			gbc.gridy++;
			gbc.fill = GridBagConstraints.BOTH;
			panel.add(gp.getGame2ResultCombo(), gbc);
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

	private class GamePanel implements FocusListener, ActionListener  {
        private final Match match;
        private JLabel playersTitle;
        private JComboBox<String> game1ResultsCombo;
        private JComboBox<String> game2ResultsCombo;
        private boolean isLoading = false;
        private boolean isChanging = false;
        private int tableNumber = -1;

        public GamePanel(int tableNumber, Match match) {

            this.tableNumber = tableNumber;

            isLoading = true;

            this.match = match;

			setGUIFromMatch();

			isLoading = false;
        }

        public Match getMatch() {
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

        private String[] getComboValues() {

            if (match.getPlayer2() == null) {
                String[] values = { "Select a result", "BYE" };
                return values;
            } else {
                String generic = "Select a result";
                String[] values = { generic, "WIN - " + match.getPlayer1().getName(), "WIN - " + match.getPlayer2().getName(), "DRAW" };
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

            tournament.triggerChange();
            
            // Needed to hide completed matches
            updateGUI();
            
            isChanging = false;
        }

        private void setMatchPointsFromGUI() {

            if (isLoading) {
                return;
            }

            switch (game1ResultsCombo.getSelectedIndex()) {
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
            case 3:
                match.setGame1Result(GameResult.DRAW);
                break;
            default:
                break;
            }

            switch (game2ResultsCombo.getSelectedIndex()) {
            case 0:
                match.setGame2Result(null);
                match.setBye(false);
                break;
            case 1:
                if (match.getPlayer2() == null) {
                    match.setBye(true);
                } else {
                    match.setGame2Result(GameResult.PLAYER_1_WINS);
                }
                break;
            case 2:
                match.setGame2Result(GameResult.PLAYER_2_WINS);
                break;
            case 3:
                match.setGame2Result(GameResult.DRAW);
                break;
            default:
                break;
            }
            tournament.getTournamentGUI().getRankingTable().resetPlayers();

        }

        private void setGUIFromMatch() {

			if (tournament.isMatchComplete(match)) {
				if (match.isBye()) {
					getGame1ResultCombo().setSelectedIndex(1);
					getGame2ResultCombo().setSelectedIndex(1);
				} else {
					if (match.getGame1Result() == GameResult.PLAYER_1_WINS) {
	                    getGame1ResultCombo().setSelectedIndex(1);
	                } else if (match.getGame1Result() == GameResult.PLAYER_2_WINS) {
	                    getGame1ResultCombo().setSelectedIndex(2);
	                } else if (match.getGame1Result() == GameResult.DRAW) {
	                    getGame1ResultCombo().setSelectedIndex(3);
	                }

	                if (match.getGame2Result() == GameResult.PLAYER_1_WINS) {
	                    getGame2ResultCombo().setSelectedIndex(1);
	                } else if (match.getGame2Result() == GameResult.PLAYER_2_WINS) {
	                    getGame2ResultCombo().setSelectedIndex(2);
	                } else if (match.getGame2Result() == GameResult.DRAW) {
	                    getGame2ResultCombo().setSelectedIndex(3);
	                }
				}
			}

			// Special exception for bye matches
			if (match.getPlayer2() == null) {
				match.setBye(true);
				getGame1ResultCombo().setSelectedIndex(1);
				getGame2ResultCombo().setSelectedIndex(1);
			}

			updateGUI();
		}
        
        private void updateGUI() {
			String titleText = null;

			boolean hideCompletedMatches = CryodexController.getOptions().isHideCompleted();

			boolean visible = hideCompletedMatches == false || tournament.isMatchComplete(match) == false;

			getPlayerTitle().setVisible(visible);
			getGame1ResultCombo().setVisible(visible);
			getGame2ResultCombo().setVisible(visible);

			if (match.getPlayer2() == null) {
				titleText = match.getPlayer1().getName() + " has a BYE";
			} else {
				titleText = match.getPlayer1().getName() + " VS " + match.getPlayer2().getName();
				if (match.isDuplicate()) {
					titleText = "(Duplicate)" + titleText;
				}

				if (CryodexController.getOptions().isShowTableNumbers()) {
					titleText = tableNumber + ": " + titleText;
				}
			}

			getPlayerTitle().setText(titleText);

			getGame1ResultCombo().setEnabled(true);
			getGame2ResultCombo().setEnabled(true);
		}

        public void markInvalid() {
            if (tournament.isValidResult(match) == false) {
                getPlayerTitle().setForeground(Color.red);
            } else {
                getPlayerTitle().setForeground(Color.black);
            }
        }
    }

	public void markInvalid() {
		for (GamePanel gamePanel : gamePanels) {
			gamePanel.markInvalid();
		}
	}

}
