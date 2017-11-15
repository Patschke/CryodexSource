package cryodex.modules;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import cryodex.Player;
import cryodex.widget.ComponentUtils;
import cryodex.widget.TimerPanel;

public abstract class RankingTable extends JPanel {

    private static final long serialVersionUID = 1L;

    private JTable table;
    private BaseRankingTableModel model;
    private final Tournament tournament;
    private JLabel title;
    private JLabel statsLabel;
    
    private long lastResetTimestamp = 0;
    private long minResetWait = 1000;

    public RankingTable(Tournament tournament) {
        super(new BorderLayout());
        
        this.tournament = tournament;
        this.model = initializeTableModel(tournament);

        getTable().setFillsViewportHeight(true);

        updateLabel();
        JPanel labelPanel = ComponentUtils.addToVerticalBorderLayout(
                getTitleLabel(), getStatsLabel(), null);
        labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(getTable());
        ComponentUtils.forceSize(this, 400, 300);
        
        this.add(labelPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(new TimerPanel(), BorderLayout.SOUTH);
    }
    
    protected Tournament getTournament(){
        return tournament;
    }
    
    public abstract BaseRankingTableModel initializeTableModel(Tournament tournament);

    private JLabel getStatsLabel() {
        if (statsLabel == null) {
            statsLabel = new JLabel();
        }
        return statsLabel;
    }

    private JLabel getTitleLabel() {
        if (title == null) {
            title = new JLabel("Player Rankings");
            title.setFont(new Font(title.getFont().getName(), title.getFont()
                    .getStyle(), 20));
        }

        return title;
    }

    public void updateLabel() {
        int total = tournament.getAllPlayers().size();
        int active = tournament.getPlayers().size();

        if (total == 0) {
            total = active;
        }

        int dropped = total - active;
        if (total == active) {
            getStatsLabel().setText("Total Players: " + total);
        } else {
            getStatsLabel().setText(
                    "Total Players: " + total + " Active Players: " + active
                            + " Dropped Players: " + dropped);
        }

    }

    private JTable getTable() {
        if (table == null) {
            table = new JTable(model);
            table.setDefaultRenderer(Object.class,
                    new RankingTableCellRenderer());
            table.setDefaultRenderer(Integer.class,
                    new RankingTableCellRenderer());
            table.getColumnModel().getColumn(0).setPreferredWidth(200);

            RankingTableCellRenderer centerRenderer = new RankingTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            
            for(int counter = 1 ; counter < table.getColumnModel().getColumnCount() ; counter++) {
                table.getColumnModel().getColumn(counter).setCellRenderer(centerRenderer);    
            }
        }
        return table;
    }

    public void setPlayers(Set<Player> players) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(players);

        Collections.sort(playerList, tournament.getRankingComparator());

        if (this.isVisible() == false) {
            this.setVisible(true);
        }
        model.setData(playerList);
        updateLabel();
    }

    public void resetPlayers() {
        //This prevents multiple resets. Need a more permanent solution.
        if(System.currentTimeMillis() - lastResetTimestamp > minResetWait){
            model.resetData();
            updateLabel();
            lastResetTimestamp = System.currentTimeMillis();
        }
    }
    
    public abstract class BaseRankingTableModel extends AbstractTableModel {

        private static final long serialVersionUID = -1591431777250055477L;

        private List<Player> data;

        public BaseRankingTableModel(List<Player> data) {
            setData(data);
        }

        public void resetData() {
            Collections.sort(data, tournament.getRankingComparator());
            this.fireTableDataChanged();
        }

        public void setData(List<Player> data) {
            this.data = data;

            Collections.sort(data, tournament.getRankingComparator());
            this.fireTableDataChanged();
        }
        
        public List<Player> getData(){
            return data;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }
    }
    
    public class RankingTableCellRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Working on adding color to the top 4/8/16 for easy visualization
//          if(row < 4){
//              c.setBackground(Color.cyan);    
//          } else if(row < 8){
//              c.setBackground(Color.green);
//          } else if(row < 16){
//              c.setBackground(Color.yellow);
//          } else if(row < 32){
//              c.setBackground(Color.red.brighter());
//          }
            
//          if(row % 2 == 1){
//              c.setBackground(c.getBackground().darker());
//          }
            
//          c.setForeground(Color.black);
            
            setBorder(noFocusBorder);
            
            return c;
        }
    }
    
    public class NoCellSelectionRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            setBorder(noFocusBorder);
            return this;
        }
    }
}
