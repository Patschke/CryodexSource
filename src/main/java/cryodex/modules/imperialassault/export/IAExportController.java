package cryodex.modules.imperialassault.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cryodex.Player;
import cryodex.export.ExportUtils;
import cryodex.modules.ExportController;
import cryodex.modules.Match;
import cryodex.modules.Tournament;
import cryodex.modules.imperialassault.IAComparator;
import cryodex.modules.imperialassault.IAPlayer;
import cryodex.modules.imperialassault.IATournament;

public class IAExportController extends ExportController{

	public String appendRankings(Tournament tournament) {
		List<Player> playerList = new ArrayList<Player>();
		List<Player> activePlayers = tournament.getPlayers();

		playerList.addAll(tournament.getAllPlayers());
		Collections.sort(playerList, new IAComparator(tournament, IAComparator.rankingCompare));

		String content = "<table border=\"1\"><tr><th>Rank</th><th>Name</th><th>Score</th><th>SoS</th><th>Ext SoS</th></tr>";

		for (Player p : playerList) {

			IAPlayer xp = ((IATournament) tournament).getModulePlayer(p);

			String name = p.getName();

			if (activePlayers.contains(p) == false) {
				name = "(D#" + xp.getRoundDropped(tournament) + ")" + name;
			}

			content += "<tr><td>" + xp.getRank(tournament) + "</td><td>" + name + "</td><td>"
					+ xp.getScore(tournament) + "</td><td>"
					+ xp.getAverageSoS(tournament) + "</td><td>"
					+ xp.getExtendedStrengthOfSchedule(tournament) + "</td></tr>";
		}

		content += "</table>";

		return content;
	}

	public void exportTournamentSlipsWithStats(Tournament tournament, List<Match> matches, int roundNumber) {

		int slipsPerPage = 6;

		String content = "";

		int increment = matches.size() / slipsPerPage;
		increment = increment + (matches.size() % slipsPerPage > 0 ? 1 : 0);

		int pageCounter = 1;

		int index = 0;

		while (pageCounter <= increment) {
			for (; index < matches.size(); index = index + increment) {

				Match m = matches.get(index);

				String matchString = "";
				if (m.getPlayer2() != null) {

					IAPlayer xp1 = (IAPlayer) m.getPlayer1().getModuleInfoByModule(tournament.getModule());
					IAPlayer xp2 = (IAPlayer) m.getPlayer2().getModuleInfoByModule(tournament.getModule());

					matchString += "<table class=\"print-friendly\" width=100%><tr><th><h4>Round " + roundNumber
							+ " - Table " + (index + 1) + "</h4></th><th vAlign=bottom align=left><h4>"
							+ m.getPlayer1().getName() + "</h4></th><th vAlign=bottom align=left><h4>" + xp2.getName()
							+ "</h4></th></tr><tr><td><table class=\"print-friendly\" border=\"1\"><tr><th>Name</th><th>Rank</td><th>Score</th><th>MoV</th><th>SoS</th></tr><tr>"
							+ "<td class=\"smallFont\">" + xp1.getName() + "</td><td class=\"smallFont\">"
							+ xp1.getRank(tournament) + "</td><td class=\"smallFont\">" + xp1.getScore(tournament)
							+ "</td><td class=\"smallFont\">" + xp1.getAverageSoS(tournament)
							+ "</td><td class=\"smallFont\">" + xp1.getExtendedStrengthOfSchedule(tournament)
							+ "</td></tr><tr><td class=\"smallFont\">" + xp2.getName() + "</td><td class=\"smallFont\">"
							+ xp2.getRank(tournament) + "</td><td class=\"smallFont\">" + xp2.getScore(tournament)
							+ "</td><td class=\"smallFont\">" + xp2.getAverageSoS(tournament)
							+ "</td><td class=\"smallFont\">" + xp2.getExtendedStrengthOfSchedule(tournament) + "</td></tr></table>"
							+ "</td><td class=\"smallFont\">"
							+ "<div style=\"vertical-align: bottom; height: 100%;\">Points Killed ____________</div>"
							+ "</br>"
							+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
							+ "</td><td class=\"smallFont\">"
							+ "<div style=\"vertical-align: bottom; height: 100%;\">Points Killed ____________</div>"
							+ "</br>"
							+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
							+ "</td></tr></table>";

					matchString += "<hr>";

					content += matchString;
				}
			}
			content += "<div class=\"pagebreak\">&nbsp;</div>";
			index = pageCounter;
			pageCounter++;
		}

		ExportUtils.displayHTML(content, "ExportMatchSlips");
	}

	@Override
	public void cacReport() {
		// TODO Auto-generated method stub
	}

	@Override
	public void tcxTeamReport(Tournament tournament) {
		// TODO Auto-generated method stub
		
	}

}
