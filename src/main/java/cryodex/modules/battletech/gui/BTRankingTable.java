package cryodex.modules.battletech.gui;

import java.util.List;

import cryodex.Player;
import cryodex.modules.RankingTable;
import cryodex.modules.Tournament;
import cryodex.modules.battletech.BTPlayer;

public class BTRankingTable extends RankingTable {

	private static final long serialVersionUID = 5587297504827909147L;

	public BTRankingTable(Tournament tournament) {
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
				value = "Kills";
				break;
			case 3:
				value = "SoS";
				break;
			case 4:
				value = "Record";
				break;
			case 5:
				value = "Byes";
				break;
			}
			return value;
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			BTPlayer user = (BTPlayer) getTournament().getModulePlayer(getData().get(arg0));
			Object value = null;
			switch (arg1) {
			case 0:
				value = " " + user.getPlayer().getName();
				if (getTournament().getPlayers().contains(user.getPlayer()) == false) {
					value = " (D#" + user.getRoundDropped(getTournament()) + ")"
							+ value;
				}
				value = "" + (arg0+1) + ". " + value;
				break;
			case 1:
				value = user.getScore(getTournament());
				break;
			case 2:
				value = user.getKillPoints(getTournament());
				break;
			case 3:
				value = user.getAverageSoS(getTournament());
				break;
			case 4:
				value = user.getWins(getTournament()) + " / "
						+ user.getLosses(getTournament());
				break;
			case 5:
				value = user.getPlayer().getByes(getTournament());
				break;

			}
			return value;
		}
	}

	

}
