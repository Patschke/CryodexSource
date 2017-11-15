package cryodex.modules.swlcg.gui;

import java.util.List;

import cryodex.Player;
import cryodex.modules.RankingTable;
import cryodex.modules.Tournament;
import cryodex.modules.swlcg.SWLCGPlayer;

public class SWLCGRankingTable extends RankingTable {

	private static final long serialVersionUID = 5587297504827909147L;

	public SWLCGRankingTable(Tournament tournament) {
        super(tournament);
    }
    
    public BaseRankingTableModel initializeTableModel(Tournament tournament){
        return new RankingTableModel(tournament.getPlayers());
    }

    private class RankingTableModel extends BaseRankingTableModel {

        private static final long serialVersionUID = -1591431777250055477L;

        public RankingTableModel(List<Player> data) {
            super(data);
        }       
        
        		@Override
		public String getColumnName(int column) {
			String value = null;
			switch (column) {
			case 0:
				value = "Name";
				break;
			case 1:
				value = "Score";
				break;
			case 2:
				value = "SoS";
				break;
			case 3:
				value = "Ext SoS";
				break;
			case 4:
				value = "Byes";
				break;
			}
			return value;
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			SWLCGPlayer user = (SWLCGPlayer) getData().get(arg0).getModuleInfoByModule(getTournament().getModule());
			Object value = null;
			switch (arg1) {
			case 0:
				value = " " + user.getPlayer().getName();
				if (getTournament().getPlayers().contains(user.getPlayer()) == false) {
					value = "(D#" + user.getRoundDropped(getTournament()) + ")"
							+ value;
				}
				value = "" + (arg0+1) + ". " + value;
				break;
			case 1:
				value = user.getScore(getTournament());
				break;
			case 2:
				value = user.getAverageSoS(getTournament());
				break;
			case 3:
				value = user.getExtendedStrengthOfSchedule(getTournament());
				break;
			case 4:

				value = user.getPlayer().getByes(getTournament());
				break;

			}
			return value;
		}

	}
}
