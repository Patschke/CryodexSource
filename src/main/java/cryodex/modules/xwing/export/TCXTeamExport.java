package cryodex.modules.xwing.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cryodex.Player;
import cryodex.modules.Tournament;
import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingTournament;

public class TCXTeamExport {

	private Tournament tournament;

	private List<TeamStats> teamStats;

	public TCXTeamExport(Tournament tournament) {
		this.tournament = tournament;
	}

	private void process() {

		Map<String, List<Player>> groupMap = new HashMap<String, List<Player>>();

		for (Player p : tournament.getPlayers()) {

			List<Player> players = groupMap.get(p.getGroupName());
			if (players == null) {
				players = new ArrayList<Player>();
				groupMap.put(p.getGroupName(), players);
			}
			players.add(p);
		}

		teamStats = new ArrayList<TeamStats>();

		for (String teamName : groupMap.keySet()) {
			if (teamName.trim().isEmpty() == false) {
				List<Player> playerGroup = groupMap.get(teamName);
				if (playerGroup != null && playerGroup.size() >= 2) {
					teamStats.add(new TeamStats(teamName, playerGroup));
				}
			}
		}

	}

	public String output() {
		process();
		Collections.sort(teamStats);

		String content = "<table border=\"1\"><tr><th>Rank</th><th>Name</th><th>Top 2 MoV</th><th>Average MoV</th><th>Total Score</th></tr>";

		int rankCount = 0;

		for (TeamStats tx : teamStats) {

			rankCount++;

			content += "<tr><td>" + rankCount + "</td><td>" + tx.getTeamName() + "</td><td>" + tx.getTop2MOV()
					+ "</td><td>" + tx.getAverageMOV() + "</td><td>" + tx.getTotalScore() + "</td></tr>";
		}

		content += "</table>";

		return content;

	}

	private class TeamStats implements Comparable<TeamStats> {

		private String teamName;
		private List<Player> players;
		private int top2MOV = 0;
		private int averageMOV = 0;
		private int totalScore = 0;

		public TeamStats(String teamName, List<Player> players) {
			this.teamName = teamName;
			this.players = players;

			calc();
		}

		private void calc() {
			// Add ranking comparator
			Collections.sort(players, ((XWingTournament) tournament).getRankingComparator());

			XWingPlayer xwingPlayer1 = ((XWingTournament) tournament).getXWingPlayer(players.get(0));
			XWingPlayer xwingPlayer2 = ((XWingTournament) tournament).getXWingPlayer(players.get(1));

			top2MOV = xwingPlayer1.getMarginOfVictory(tournament) + xwingPlayer2.getMarginOfVictory(tournament);

			for (Player player : players) {
				XWingPlayer xwingPlayer = ((XWingTournament) tournament).getXWingPlayer(player);
				averageMOV += xwingPlayer.getMarginOfVictory(tournament);
				totalScore += xwingPlayer.getScore(tournament);
			}

			averageMOV = averageMOV / players.size();
		}

		public int getTop2MOV() {
			return top2MOV;
		}

		public int getAverageMOV() {
			return averageMOV;
		}

		public int getTotalScore() {
			return totalScore;
		}

		public String getTeamName() {
			return teamName;
		}

		@Override
		public int compareTo(TeamStats o) {

			// Compare top2MOV
			if (this.getTop2MOV() == o.getTop2MOV()) {
				// Do nothing
			} else if (this.getTop2MOV() > o.getTop2MOV()) {
				return -1;
			} else {
				return 1;
			}

			// Compare averageMOV
			if (this.getAverageMOV() == o.getAverageMOV()) {
				// Do nothing
			} else if (this.getAverageMOV() > o.getAverageMOV()) {
				return -1;
			} else {
				return 1;
			}

			// Compare totalScore
			if (this.getTotalScore() == o.getTotalScore()) {
				// Do nothing
			} else if (this.getTotalScore() > o.getTotalScore()) {
				return -1;
			} else {
				return 1;
			}

			return 0;
		}
	}

}