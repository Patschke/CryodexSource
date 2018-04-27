package cryodex.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cryodex.CryodexController;
import cryodex.Main;
import cryodex.Player;
import cryodex.modules.Match;
import cryodex.modules.Menu;
import cryodex.modules.Round;
import cryodex.modules.Tournament;
import cryodex.modules.xwing.XWingTournament;
import cryodex.modules.xwing.export.XWingJSONBuilder;
import cryodex.widget.wizard.TournamentWizard;

@SuppressWarnings("serial")
public class TournamentMenu implements Menu {

    private JMenu mainMenu;

    private JMenu playersMenu;
    private JMenu roundMenu;
    private JMenu exportMenu;

    private JMenuItem deleteTournament;

    private JMenuItem cutPlayers;

    @Override
    public JMenu getMenu() {

        if (mainMenu == null) {

            mainMenu = new JMenu("Tournament");
            mainMenu.setMnemonic('T');

            JMenuItem createNewTournament = new JMenuItem("Create New Tournament");
            createNewTournament.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    TournamentWizard.getInstance().setVisible(true);
                }
            });

            deleteTournament = new JMenuItem("Delete Tournament");
            deleteTournament.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    CryodexController.deleteTournament(true);
                }
            });

            JMenuItem generateNextRound = new JMenuItem("Generate Next Round");
            generateNextRound.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (CryodexController.getActiveTournament() == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    CryodexController.getActiveTournament().generateNextRound();
                }
            });

            mainMenu.add(createNewTournament);
            mainMenu.add(deleteTournament);
            mainMenu.add(generateNextRound);
            mainMenu.add(getPlayersMenu());
            mainMenu.add(getRoundMenu());
            mainMenu.add(getExportMenu());
        }

        return mainMenu;
    }

    public JMenu getPlayersMenu() {
        if (playersMenu == null) {
            playersMenu = new JMenu("Players");

            JMenuItem addPlayer = new JMenuItem("Add Player");
            addPlayer.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {

                    if (CryodexController.getActiveTournament() == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    List<Player> players = new ArrayList<Player>();
                    players.addAll(CryodexController.getPlayers());

                    for (Player p : CryodexController.getActiveTournament().getPlayers()) {
                        players.remove(p);
                    }

                    if (players.isEmpty()) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "All players are already in the tournament.");
                    } else {

                        PlayerSelectionDialog d = new PlayerSelectionDialog(players, true) {

                            @Override
                            public void playerSelected(Player p, boolean addByes) {

                                Tournament t = CryodexController.getActiveTournament();


                                if (addByes) {
                                    for (Round r : t.getAllRounds()) {
                                        Match m = new Match();
                                        m.setPlayer1(p);
                                        r.getMatches().add(m);
                                    }
                                }

                                t.addPlayer(p);
                                
                                
                            }
                        };

                        d.setVisible(true);
                    }
                }
            });
            
            JMenuItem massAddPlayer = new JMenuItem("Mass Add Players");
            massAddPlayer.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {


                    if (CryodexController.getActiveTournament() == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    JDialog d = new MassAddPanel();
                    d.setVisible(true);
                
                }
            });

            JMenuItem dropPlayer = new JMenuItem("Drop Player");
            dropPlayer.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {

                    if (CryodexController.getActiveTournament() == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    JDialog d = new PlayerSelectionDialog(CryodexController.getActiveTournament().getPlayers(), false) {

                        @Override
                        public void playerSelected(Player p, boolean addByes) {
                            CryodexController.getActiveTournament().dropPlayer(p);
                        }
                    };
                    d.setVisible(true);

                }
            });

            JMenuItem massDropPlayer = new JMenuItem("Mass Drop Players");
            massDropPlayer.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    if (CryodexController.getActiveTournament() == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    JDialog d = new MassDropPanel();
                    d.setVisible(true);
                }
            });

            playersMenu.add(addPlayer);
            playersMenu.add(massAddPlayer);
            playersMenu.add(dropPlayer);
            playersMenu.add(massDropPlayer);
            playersMenu.add(getCutPlayers());
        }

        return playersMenu;
    }

    public JMenu getRoundMenu() {
        if (roundMenu == null) {
            roundMenu = new JMenu("Round");

            JMenuItem generateNextRound = new JMenuItem("Regenerate Round");
            generateNextRound.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    Tournament tournament = CryodexController.getActiveTournament();

                    if (tournament == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    int index = tournament.getTournamentGUI().getRoundTabbedPane().getSelectedIndex();

                    int result = JOptionPane.showConfirmDialog(Main.getInstance(),
                            "Regenerating a round will cancel all results and destroy any subsequent rounds. Are you sure you want to do this?");

                    if (result == JOptionPane.OK_OPTION) {
                        Round r = tournament.getRound(index);
                        if (r.isSingleElimination()) {
                            int playerCount = r.getMatches().size() * 2;
                            tournament.cancelRound(tournament.getRoundNumber(r));
                            tournament.generateSingleEliminationMatches(playerCount);
                        } else {
                            tournament.generateRound(index + 1);
                        }

                        tournament.getTournamentGUI().getRoundTabbedPane().validate();
                        tournament.getTournamentGUI().getRoundTabbedPane().repaint();
                    }

                    tournament.triggerChange();
                }
            });

            JMenuItem cancelRound = new JMenuItem("Cancel Round");
            cancelRound.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    Tournament tournament = CryodexController.getActiveTournament();

                    if (tournament == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    int index = tournament.getTournamentGUI().getRoundTabbedPane().getSelectedIndex();

                    if (index == 0) {
                        CryodexController.deleteTournament(true);
                        return;
                    }

                    int result = JOptionPane.showConfirmDialog(Main.getInstance(),
                            "Cancelling a round will cancel all results and destroy any subsequent rounds. Are you sure you want to do this?");

                    if (result == JOptionPane.OK_OPTION) {
                        tournament.cancelRound(index + 1);

                        tournament.getTournamentGUI().getRoundTabbedPane().setSelectedIndex(index - 1);

                        tournament.triggerChange();

                        resetMenuBar();
                    }
                }
            });

            JMenuItem swapPlayers = new JMenuItem("Swap Players");
            swapPlayers.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    Tournament tournament = CryodexController.getActiveTournament();

                    if (tournament == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    if (tournament.getSelectedRound().isComplete(tournament) == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "Current round is complete. Players cannot be swapped.");
                        return;
                    }

                    SwapPanel.showSwapPanel();
                }
            });

            roundMenu.add(generateNextRound);
            roundMenu.add(cancelRound);
            roundMenu.add(swapPlayers);
        }
        return roundMenu;
    }

    public JMenuItem getCutPlayers() {
        if (cutPlayers == null) {
            cutPlayers = new JMenuItem("Cut To Top Players");
            cutPlayers.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {

                    Tournament tournament = CryodexController.getActiveTournament();

                    if (tournament == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    String incompleteMatches = tournament.getLatestRound().isComplete(tournament);
                    if (incompleteMatches != null) {
                        JOptionPane.showMessageDialog(Main.getInstance(),
                                "<HTML>Current round is not complete. Please complete all matches before continuing.<br>" + incompleteMatches);
                        return;
                    }

                    JDialog d = new CutPlayersDialog();
                    d.setVisible(true);
                }
            });
        }
        return cutPlayers;
    }

    public JMenu getExportMenu() {
        if (exportMenu == null) {
            exportMenu = new JMenu("Export");

            JMenuItem exportPlayerList = new JMenuItem("Player List");
            exportPlayerList.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Tournament t = CryodexController.getActiveTournament();

                    if (t == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    t.getExportController().playerList(CryodexController.getActiveTournament().getPlayers());
                }
            });

            JMenuItem exportMatches = new JMenuItem("Export Matches");
            exportMatches.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Tournament t = CryodexController.getActiveTournament();

                    if (t == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    t.getExportController().exportMatches();
                }
            });

            JMenuItem exportMatchSlips = new JMenuItem("Export Match Slips");
            exportMatchSlips.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Tournament tournament = CryodexController.getActiveTournament();

                    if (tournament == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    Round round = tournament.getLatestRound();

                    int roundNumber = round.isSingleElimination() ? 0 : tournament.getRoundNumber(round);

                    tournament.getExportController().exportTournamentSlips(tournament, round.getMatches(), roundNumber);
                }
            });

            JMenuItem exportMatchSlipsWithStats = new JMenuItem("Export Match Slips With Stats");
            exportMatchSlipsWithStats.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Tournament tournament = CryodexController.getActiveTournament();

                    if (tournament == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    Round round = tournament.getLatestRound();

                    int roundNumber = round.isSingleElimination() ? 0 : tournament.getRoundNumber(round);

                    tournament.getExportController().exportTournamentSlipsWithStats(tournament, round.getMatches(), roundNumber);
                }
            });

            JMenuItem exportRankings = new JMenuItem("Export Rankings");
            exportRankings.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Tournament t = CryodexController.getActiveTournament();

                    if (t == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    t.getExportController().exportRankings(CryodexController.getActiveTournament());
                }
            });

            JMenuItem exportTournamentReport = new JMenuItem("Export Tournament Report");
            exportTournamentReport.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Tournament t = CryodexController.getActiveTournament();

                    if (t == null) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "There are no tournaments to do this action.");
                        return;
                    }

                    t.getExportController().exportTournamentReport(CryodexController.getActiveTournament());
                }
            });

            JMenuItem saveJSON = new JMenuItem("X-Wing List Jugger Data");
            saveJSON.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Tournament t = CryodexController.getActiveTournament();

                    if (t == null || t instanceof XWingTournament == false) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "No X-Wing tournament available for this operation");
                        return;
                    }

                    XWingJSONBuilder.buildTournament(t);
                }
            });

            JMenuItem cacReport = new JMenuItem("X-Wing: Campaign Against Cancer Report");
            cacReport.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Tournament t = CryodexController.getActiveTournament();

                    if (t == null || t instanceof XWingTournament == false) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "No X-Wing tournament available for this operation");
                        return;
                    }

                    t.getExportController().cacReport();
                }
            });

            JMenuItem tcxReport = new JMenuItem("X-Wing: TCX Ultimate Team Report");
            tcxReport.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Tournament t = CryodexController.getActiveTournament();

                    if (t == null || t instanceof XWingTournament == false) {
                        JOptionPane.showMessageDialog(Main.getInstance(), "No X-Wing tournament available for this operation");
                        return;
                    }

                    t.getExportController().tcxTeamReport(t);
                    ;
                }
            });

            exportMenu.add(exportPlayerList);
            exportMenu.add(exportMatches);
            exportMenu.add(exportMatchSlips);
            exportMenu.add(exportMatchSlipsWithStats);
            exportMenu.add(exportRankings);
            exportMenu.add(exportTournamentReport);
            exportMenu.add(saveJSON);
            exportMenu.add(cacReport);
            exportMenu.add(tcxReport);
        }
        return exportMenu;
    }

    @Override
    public void resetMenuBar() {

    }

    private abstract class PlayerSelectionDialog extends JDialog {

        private static final long serialVersionUID = 1945413167979638452L;

        private final JComboBox<Player> userCombo;

        public PlayerSelectionDialog(List<Player> players, boolean showAddBye) {
            super(Main.getInstance(), "Select Player", true);

            Collections.sort(players);

            JPanel mainPanel = new JPanel(new BorderLayout());

            userCombo = new JComboBox<Player>();
            for (Player p : players) {
                userCombo.addItem(p);
            }
            ComponentUtils.forceSize(userCombo, 20, 25);

            final JCheckBox addByes = new JCheckBox("Add byes for previous rounds");
            addByes.setVisible(showAddBye);

            JButton ok = new JButton("Ok");
            ok.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Player p = (Player) userCombo.getSelectedItem();

                    playerSelected(p, addByes.isSelected());

                    PlayerSelectionDialog.this.setVisible(false);
                }
            });

            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    PlayerSelectionDialog.this.setVisible(false);
                }
            });

            mainPanel.add(userCombo, BorderLayout.NORTH);
            mainPanel.add(addByes, BorderLayout.CENTER);
            mainPanel.add(ComponentUtils.addToHorizontalBorderLayout(ok, null, cancel), BorderLayout.SOUTH);

            this.add(mainPanel);

            PlayerSelectionDialog.this.setLocationRelativeTo(Main.getInstance());
            PlayerSelectionDialog.this.pack();
            this.setMinimumSize(new Dimension(200, 0));
        }

        public abstract void playerSelected(Player p, boolean addByes);
    }

    private class CutPlayersDialog extends JDialog {

        private static final long serialVersionUID = 1945413167979638452L;

        private final JComboBox<Integer> cutCombo;

        public CutPlayersDialog() {
            super(Main.getInstance(), "Cut Players", true);

            JPanel mainPanel = new JPanel(new BorderLayout());

            int currentPlayers = CryodexController.getActiveTournament().getPlayers().size();

            Integer[] options = { 2, 4, 8, 16, 32, 64 };

            while (options[options.length - 1] > currentPlayers) {
                Integer[] tempOptions = new Integer[options.length - 1];
                for (int i = 0; i < tempOptions.length; i++) {
                    tempOptions[i] = options[i];
                }
                options = tempOptions;
            }

            cutCombo = new JComboBox<Integer>(options);
            ComponentUtils.forceSize(cutCombo, 10, 25);

            JButton add = new JButton("Make Cut");
            add.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {

                    Integer p = (Integer) cutCombo.getSelectedItem();

                    CryodexController.getActiveTournament().generateSingleEliminationMatches(p);

                    CutPlayersDialog.this.setVisible(false);
                }
            });

            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    CutPlayersDialog.this.setVisible(false);
                }
            });

            mainPanel.add(ComponentUtils.addToHorizontalBorderLayout(new JLabel("Cut to top: "), cutCombo, null), BorderLayout.CENTER);
            mainPanel.add(ComponentUtils.addToHorizontalBorderLayout(add, null, cancel), BorderLayout.SOUTH);

            this.add(mainPanel);

            CutPlayersDialog.this.setLocationRelativeTo(Main.getInstance());
            CutPlayersDialog.this.pack();
            this.setMinimumSize(new Dimension(200, 0));
        }
    }
}