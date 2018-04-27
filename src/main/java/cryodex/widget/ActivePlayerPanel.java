package cryodex.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cryodex.CryodexController;
import cryodex.Main;
import cryodex.Player;

@SuppressWarnings("serial")
public class ActivePlayerPanel extends JPanel {

    public static void showActivePanel() {
        JDialog manualModificationPanel = new JDialog(Main.getInstance(), "Active Players", true);
        JPanel panel = new JPanel(new BorderLayout());
        manualModificationPanel.getContentPane().add(panel);
        panel.add(new ActivePlayerPanel(manualModificationPanel), BorderLayout.CENTER);
        manualModificationPanel.setPreferredSize(new Dimension(450, 600));
        manualModificationPanel.pack();

        manualModificationPanel.setVisible(true);
    }

    private JButton saveButton;
    private JButton closeButton;
    private JButton selectAllButton;
    private JButton removeAllButton;

    private JPanel playerListPanel;

    private final JDialog parent;

    private List<PlayerPanel> playerPanels = new ArrayList<PlayerPanel>();

    private static final String filterHint = "Filter Player List";
    private boolean filtering = false;
    private JTextField playerSearchField;
    private JButton clearFilter;
    
    private JLabel activePlayerLabel;

    private ActivePlayerPanel(JDialog parent) {
        super(new BorderLayout());

        this.parent = parent;

        buildPanel();

        final JScrollPane checkboxScrollPanel = new JScrollPane(ComponentUtils.addToFlowLayout(getPlayerListPanel(), FlowLayout.CENTER));
        checkboxScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        checkboxScrollPanel.setBorder(BorderFactory.createEmptyBorder());

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                checkboxScrollPanel.getVerticalScrollBar().setValue(0);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(getSelectAllButton());
        buttonPanel.add(getSaveButton());
        buttonPanel.add(getCloseButton());
        buttonPanel.add(getRemoveAllButton());

        JPanel filterPanel = ComponentUtils.addToFlowLayout(ComponentUtils.addToHorizontalBorderLayout(getActivePlayerLabel(), getPlayerFilterTextField(), getClearFilterButton()), FlowLayout.CENTER);

        this.add(filterPanel, BorderLayout.NORTH);
        this.add(checkboxScrollPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        updateActivePlayerCount();
    }

    private JButton getSaveButton() {
        if (saveButton == null) {
            saveButton = new JButton("Save");
            saveButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    for (PlayerPanel p : playerPanels) {
                        p.save();
                    }
                    
                    Main.getInstance().getRegisterPanel().updateCounterLabel();
                    CryodexController.saveData();
                }
            });

        }
        return saveButton;
    }

    private JButton getCloseButton() {
        if (closeButton == null) {
            closeButton = new JButton("Close");

            closeButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    parent.setVisible(false);
                }
            });
        }

        return closeButton;
    }

    private JButton getSelectAllButton() {
        if (selectAllButton == null) {
            selectAllButton = new JButton("Select All");

            selectAllButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    for(PlayerPanel p : playerPanels){
                        p.getCheckbox().setSelected(true);
                    }
                }
            });
        }

        return selectAllButton;
    }
    
    private JButton getRemoveAllButton() {
        if (removeAllButton == null) {
            removeAllButton = new JButton("Remove All");

            removeAllButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    for(PlayerPanel p : playerPanels){
                        p.getCheckbox().setSelected(false);
                    }
                }
            });
        }

        return removeAllButton;
    }
    
    private JPanel getPlayerListPanel() {
        if (playerListPanel == null) {
            playerListPanel = new JPanel(new GridBagLayout());
        }

        return playerListPanel;
    }

    private void buildPanel() {

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = -1;

        JPanel mainPanel = getPlayerListPanel();

        for (Player p : CryodexController.getPlayers()) {
            PlayerPanel playerPanel = new PlayerPanel(p);
            playerPanels.add(playerPanel);

            gbc.gridy++;
            gbc.gridx = 0;
            mainPanel.add(playerPanel.getPlayerNameLabel(), gbc);

            gbc.gridx = 1;
            mainPanel.add(playerPanel.getCheckbox(), gbc);
        }
    }
    
    public JLabel getActivePlayerLabel() {
        if (activePlayerLabel == null) {
            activePlayerLabel = new JLabel();
        }

        return activePlayerLabel;
    }
    
    public void updateActivePlayerCount(){
        int total = playerPanels.size();
        int active = 0;
        
        for(PlayerPanel playerPanel : playerPanels){
            if(playerPanel.getCheckbox().isSelected()){
                active++;
            }
        }
        
        getActivePlayerLabel().setText("Active Players: " + active + "/" + total + "  ");
    }

    public JTextField getPlayerFilterTextField() {
        if (playerSearchField == null) {
            playerSearchField = new JTextField(filterHint, 10);
            playerSearchField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (playerSearchField.getText().equals(filterHint)) {
                        playerSearchField.setText("");
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (playerSearchField.getText().trim().equals("")) {
                        playerSearchField.setText(filterHint);
                    }
                }
            });

            playerSearchField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    filterPlayerList(playerSearchField.getText().trim().toLowerCase());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filterPlayerList(playerSearchField.getText().trim().toLowerCase());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filterPlayerList(playerSearchField.getText().trim().toLowerCase());
                }
            });
        }
        return playerSearchField;
    }

    public JButton getClearFilterButton() {

        if (clearFilter == null) {

            clearFilter = new JButton("Clear Filter");

            clearFilter.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    playerSearchField.setText(filterHint);
                    filterPlayerList(filterHint);
                }
            });
        }

        return clearFilter;
    }
    
    /**
     * Filter list to only include elements whose toString function contains the filterText. Elements that have a word
     * that starts with the filter text are given priority.
     */
    private void filterPlayerList(String filterText) {

        // Prevent the function from multiple simultaneous executions
        if (!filtering) {
            filtering = true;

            // Reset list for blank filter
            if (filterText.equals("") || filterText.equalsIgnoreCase(filterHint)) {
                //Set all visible
                for(PlayerPanel playerPanel : playerPanels){
                    playerPanel.setVisible(true);
                }
            } else {
                //remove non matches
                for(PlayerPanel playerPanel : playerPanels){
                    if(playerPanel.getPlayer().getName().toUpperCase().contains(filterText.toUpperCase())){
                        playerPanel.setVisible(true);
                    } else {
                        playerPanel.setVisible(false);
                    }
                }
            }
            filtering = false;
        }
    }
    
    private class PlayerPanel {

        private Player player;
        private JCheckBox checkbox;
        private JLabel playerNameLabel;

        public PlayerPanel(Player player) {

            this.player = player;

            playerNameLabel = new JLabel(player.getName());

            checkbox = new JCheckBox();
            checkbox.setSelected(player.isActive());
            checkbox.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateActivePlayerCount();
                }
            });
        }

        public JCheckBox getCheckbox() {
            return checkbox;
        }

        public JLabel getPlayerNameLabel() {
            return playerNameLabel;
        }

        public void save() {
            player.setActive(checkbox.isSelected());
        }

        public void setVisible(boolean b) {
            getPlayerNameLabel().setVisible(b);
            getCheckbox().setVisible(b);
        }

        public Player getPlayer() {
            return player;
        }
    }
}
