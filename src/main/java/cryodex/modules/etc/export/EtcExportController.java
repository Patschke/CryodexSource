package cryodex.modules.etc.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cryodex.Player;
import cryodex.export.ExportUtils;
import cryodex.modules.ExportController;
import cryodex.modules.Match;
import cryodex.modules.Tournament;
import cryodex.modules.etc.EtcComparator;
import cryodex.modules.etc.EtcPlayer;
import cryodex.modules.etc.EtcTournament;
import cryodex.modules.xwing.export.CACReport;

public class EtcExportController extends ExportController {

	public String appendRankings(Tournament tournament) {
		List<Player> playerList = new ArrayList<Player>();
		List<Player> activePlayers = tournament.getPlayers();

		playerList.addAll(tournament.getAllPlayers());
		Collections.sort(playerList, new EtcComparator(tournament, EtcComparator.rankingCompare));

		String content = "<table border=\"1\"><tr><th>Rank</th><th>Name</th><th>Faction</th><th>Score</th><th>MoV</th><th>SoS</th></tr>";

		for (Player p : playerList) {

			EtcPlayer xp = ((EtcTournament) tournament).getEtcPlayer(p);

			String name = p.getName();
			String faction = xp.getFaction() == null ? "" : xp.getFaction().toString();

			if (activePlayers.contains(p) == false) {
				name = "(D#" + xp.getRoundDropped(tournament) + ")" + name;
			}

			content += "<tr><td>" + xp.getRank(tournament) + "</td><td>" + name + "</td><td>" + faction + "</td><td>"
					+ xp.getScore(tournament) + "</td><td>" + xp.getMarginOfVictory(tournament) + "</td><td>"
					+ xp.getAverageSoS(tournament) + "</td></tr>";
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

					EtcPlayer xp1 = (EtcPlayer) m.getPlayer1().getModuleInfoByModule(tournament.getModule());
					EtcPlayer xp2 = (EtcPlayer) m.getPlayer2().getModuleInfoByModule(tournament.getModule());

					matchString += "<table class=\"print-friendly\" width=100%><tr><th><h4>Round " + roundNumber
							+ " - Table " + (index + 1) + "</h4></th><th vAlign=bottom align=left><h4>"
							+ m.getPlayer1().getName() + "</h4></th><th vAlign=bottom align=left><h4>" + xp2.getName()
							+ "</h4></th></tr><tr><td><table class=\"print-friendly\" border=\"1\"><tr><th>Name</th><th>Rank</td><th>Score</th><th>MoV</th><th>SoS</th></tr><tr>"
							+ "<td class=\"smallFont\">" + xp1.getName() + "</td><td class=\"smallFont\">"
							+ xp1.getRank(tournament) + "</td><td class=\"smallFont\">" + xp1.getScore(tournament)
							+ "</td><td class=\"smallFont\">" + xp1.getMarginOfVictory(tournament)
							+ "</td><td class=\"smallFont\">" + xp1.getAverageSoS(tournament)
							+ "</td></tr><tr><td class=\"smallFont\">" + xp2.getName() + "</td><td class=\"smallFont\">"
							+ xp2.getRank(tournament) + "</td><td class=\"smallFont\">" + xp2.getScore(tournament)
							+ "</td><td class=\"smallFont\">" + xp2.getMarginOfVictory(tournament)
							+ "</td><td class=\"smallFont\">" + xp2.getAverageSoS(tournament) + "</td></tr></table>"
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

	public void cacReport() {

		String content = CACReport.generateCACReport();

		ExportUtils.displayHTML(content, "Campaign Against Cancer Report");

	}

	@Override
	public void tcxTeamReport(Tournament tournament) {
		// TODO Auto-generated method stub
		
	}
}
