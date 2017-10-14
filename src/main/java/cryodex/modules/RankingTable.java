package cryodex.modules;

import java.awt.LayoutManager;
import java.util.Set;

import javax.swing.JPanel;

import cryodex.Player;

public abstract class RankingTable extends JPanel {

    private static final long serialVersionUID = 1L;
    
    public RankingTable(LayoutManager layout){
        super(layout);
    }

    public abstract void setPlayers(Set<Player> players);
    
    public abstract void resetPlayers();
    
}
