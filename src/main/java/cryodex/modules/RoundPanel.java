package cryodex.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
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
import cryodex.widget.ComponentUtils;

public abstract class RoundPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private List<GamePanel> gamePanels;
    private JPanel quickEntryPanel;
    private JPanel quickEntrySubPanel;
    private JTextField tableNumber;
    private JComboBox<Player> playerCombo;
    private final JScrollPane scroll;
    private final Tournament tournament;
    private JLabel completedMatchesLabel;
    private GamePanel filteredGamePanel;

    public RoundPanel(Tournament t, List<Match> matches) {

        super(new BorderLayout());

        this.tournament = t;
        this.setBorder(BorderFactory.createLineBorder(Color.black));

        gamePanels = getGamePanels(matches);

        scroll = new JScrollPane(ComponentUtils.addToFlowLayout(buildPanel(), FlowLayout.CENTER));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scroll.getVerticalScrollBar().setValue(0);
                scroll.getVerticalScrollBar().setUnitIncrement(15);
            }
        });

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(getQuickEntryPanel(), BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);

        this.add(getInfoPanel(), BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);

        
        this.setFocusCycleRoot(true);
        generateFocusPolicy();
        
        updatePanelGUI();
    }

    public void generateFocusPolicy() {
        Vector<Component> order = new Vector<Component>(7);
        if(filteredGamePanel != null){
            filteredGamePanel.addToFocusPolicy(order);
        }
        for (GamePanel gp : gamePanels) {
            if(gp != filteredGamePanel){
                gp.addToFocusPolicy(order);
            }
        }

        if (order.isEmpty() == false) {
            this.setFocusTraversalPolicy(new MyOwnFocusTraversalPolicy(order));
        }
    }

    public JPanel getInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout());

        infoPanel.add(getCompletedMatchesLabel());

        return infoPanel;
    }

    public JLabel getCompletedMatchesLabel() {
        if (completedMatchesLabel == null) {
            completedMatchesLabel = new JLabel("Completed Matches 0/" + gamePanels.size());
        }
        return completedMatchesLabel;
    }

    public JPanel getQuickEntryPanel() {
        if (quickEntryPanel == null) {
            quickEntryPanel = new JPanel(new BorderLayout());
            quickEntryPanel.setVisible(CryodexController.getOptions().isShowQuickFind());
            ComponentUtils.forceSize(quickEntryPanel, 405, 135);

            quickEntrySubPanel = new JPanel(new BorderLayout());
            ComponentUtils.forceSize(quickEntrySubPanel, 400, 130);

            tableNumber = new JTextField(5);

            tableNumber.getDocument().addDocumentListener(new DocumentListener() {
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
                            ComponentUtils.addToHorizontalBorderLayout(new JLabel("Enter table number"), tableNumber,
                                    ComponentUtils.addToHorizontalBorderLayout(new JLabel("or choose a player"), playerCombo, null)),
                            FlowLayout.CENTER), BorderLayout.NORTH);

            quickEntryPanel.add(quickEntrySubPanel);
        }

        return quickEntryPanel;
    }

    public void update() {

        scroll.getViewport().removeAll();
        scroll.getViewport().add(ComponentUtils.addToFlowLayout(buildPanel(), FlowLayout.CENTER));
        ComponentUtils.repaint(RoundPanel.this);

        Integer i = null;
        try {
            i = Integer.parseInt(tableNumber.getText());
        } catch (NumberFormatException e) {

        }

        Player player = playerCombo.getSelectedIndex() == 0 ? null : (Player) playerCombo.getSelectedItem();

        if (player != null) {
            tableNumber.setEnabled(false);
        } else if (i != null) {
            playerCombo.setEnabled(false);
        } else {
            tableNumber.setEnabled(true);
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

        filteredGamePanel = gamePanel;
        
        if (gamePanel == null) {
            return;
        }

        quickEntrySubPanel.add(gamePanel.getPlayerTitle(), BorderLayout.CENTER);

        quickEntrySubPanel.removeAll();

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = -1;

        buildGamePanel(panel, gbc, gamePanel);

        quickEntrySubPanel.add(panel, BorderLayout.CENTER);

        ComponentUtils.repaint(quickEntrySubPanel);
        ComponentUtils.repaint(quickEntryPanel);
        
        generateFocusPolicy();
    }

    public JPanel buildPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = -1;

        for (GamePanel gp : gamePanels) {
            buildGamePanel(panel, gbc, gp);
        }

        return panel;
    }

    public abstract void buildGamePanel(JPanel panel, GridBagConstraints gbc, GamePanel gamePanel);

    public abstract List<GamePanel> getGamePanels(List<Match> matches);

    public void markInvalid() {
        for (GamePanel gamePanel : gamePanels) {
            gamePanel.markInvalid();
        }
    }

    protected Tournament getTournament() {
        return tournament;
    }

    public void resetGamePanels(boolean isTextOnly) {
        for (GamePanel gp : gamePanels) {
            gp.updateGUI();
        }
        getQuickEntryPanel().setVisible(CryodexController.getOptions().isShowQuickFind());
        ComponentUtils.repaint(this);
    }

    private void updatePanelGUI() {
        int completedMatches = 0;
        for (GamePanel gp : gamePanels) {
            if (tournament.isMatchComplete(gp.getMatch())) {
                completedMatches++;
            }
        }
        getCompletedMatchesLabel().setText("Completed Matches " + completedMatches + "/" + gamePanels.size());
    }

    public abstract class GamePanel implements FocusListener, ActionListener {

        private JLabel playersTitle;
        private final Match match;
        private boolean isLoading = false;
        private int tableNumber = -1;
        private boolean isChanging = false;
        private DropBox player1DropBox;
        private DropBox player2DropBox;

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

        public JLabel getPlayerTitle() {
            if (playersTitle == null) {

                playersTitle = new JLabel("");
                playersTitle.setFont(new Font(playersTitle.getFont().getName(), playersTitle.getFont().getStyle(), 20));
                playersTitle.setHorizontalAlignment(SwingConstants.RIGHT);
            }
            return playersTitle;
        }

        public boolean isLoading() {
            return isLoading;
        }

        public boolean isChanging() {
            return isChanging;
        }

        public void setChanging(boolean isChanging) {
            this.isChanging = isChanging;
        }

        public int getTableNumber() {
            return tableNumber;
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

        public void triggerChange(boolean isComboChange) {
            if (isLoading() || isChanging()) {
                return;
            }

            isChanging = true;

            setMatchPointsFromGUI();

            // Update combo panel
            if (isComboChange == false && CryodexController.getOptions().isEnterOnlyPoints()) {
                setResultsCombo();
            }

            setMatchResultFromGUI();

            getTournament().triggerChange();

            // Needed to hide completed matches
            updateGUI();

            updatePanelGUI();

            isChanging = false;
        }

        public DropBox getPlayer1DropBox() {
            if (player1DropBox == null) {
                player1DropBox = new DropBox(getMatch().getPlayer1());
            }

            return player1DropBox;
        }

        public DropBox getPlayer2DropBox() {
            if (player2DropBox == null) {
                player2DropBox = new DropBox(getMatch().getPlayer2());
            }

            return player2DropBox;
        }

        public void markInvalid() {
            if (getTournament().isValidResult(getMatch()) == false) {
                getPlayerTitle().setForeground(Color.red);
            } else {
                getPlayerTitle().setForeground(Color.black);
            }
        }

        public abstract void setResultsCombo();

        public abstract void setMatchPointsFromGUI();

        public abstract void setMatchResultFromGUI();

        public abstract void setGUIFromMatch();

        public abstract void updateGUI();

        public abstract void addToFocusPolicy(Vector<Component> order);
    }

    public class DropBox extends JCheckBox {

        private static final long serialVersionUID = 1L;

        public DropBox(final Player player) {
            super("Drop?");

            setHorizontalTextPosition(SwingConstants.LEFT);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (isSelected()) {
                        tournament.dropPlayer(player);
                    } else {
                        tournament.addPlayer(player);
                    }
                }
            });
        }

    }

    public static class MyOwnFocusTraversalPolicy extends FocusTraversalPolicy {
        Vector<Component> order;

        public MyOwnFocusTraversalPolicy(Vector<Component> order) {
            this.order = new Vector<Component>(order.size());
            this.order.addAll(order);
        }

        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            int idx = (order.indexOf(aComponent) + 1) % order.size();
            return order.get(idx);
        }

        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            int idx = order.indexOf(aComponent) - 1;
            if (idx < 0) {
                idx = order.size() - 1;
            }
            return order.get(idx);
        }

        public Component getDefaultComponent(Container focusCycleRoot) {
            return order.get(0);
        }

        public Component getLastComponent(Container focusCycleRoot) {
            return order.lastElement();
        }

        public Component getFirstComponent(Container focusCycleRoot) {
            return order.get(0);
        }
    }

}
