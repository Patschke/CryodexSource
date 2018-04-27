package cryodex.modules.runewars.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cryodex.Player;
import cryodex.modules.ExportController;
import cryodex.modules.Match;
import cryodex.modules.Tournament;
import cryodex.modules.runewars.RunewarsPlayer;
import cryodex.modules.runewars.RunewarsTournament;

public class RunewarsExportController extends ExportController{

	public String appendRankings(Tournament tournament) {
		List<Player> playerList = new ArrayList<Player>();
		List<Player> activePlayers = tournament.getPlayers();

		playerList.addAll(tournament.getAllPlayers());
        Collections.sort(playerList, tournament.getRankingComparator());

        String content = "<table border=\"1\">";

        content = appendHTMLTableHeader(content, "Rank", "Name", "Score", "MoV", "SoS");

		for (Player p : playerList) {

			RunewarsPlayer xp = ((RunewarsTournament) tournament).getModulePlayer(p);

			String name = p.getName();

			if (activePlayers.contains(p) == false) {
				name = "(D#" + xp.getRoundDropped(tournament) + ")" + name;
			}

			content = appendHTMLTableRow(content, xp.getRank(tournament), name, xp.getScore(tournament), xp.getMarginOfVictory(tournament),
                    xp.getAverageSoS(tournament));
		}

		content += "</table>";

		return content;
	}

    @Override
    public String getValueLabel() {
        return "Points Destroyed";
    }

    @Override
    public String getMatchStats(Match match, Tournament tournament) {

        String matchString = "<table class=\"print-friendly\" border=\"1\">";

        RunewarsPlayer xp1 = (RunewarsPlayer) tournament.getModulePlayer(match.getPlayer1());
        RunewarsPlayer xp2 = (RunewarsPlayer) tournament.getModulePlayer(match.getPlayer2());

        matchString += appendHTMLTableHeader("Name", "Rank", "Score", "MoV", "SoS");

        matchString += appendHTMLTableRow(xp1.getName(), xp1.getRank(tournament), xp1.getScore(tournament), xp1.getMarginOfVictory(tournament),
                xp1.getAverageSoS(tournament));
        matchString += appendHTMLTableRow(xp2.getName(), xp2.getRank(tournament), xp2.getScore(tournament), xp2.getMarginOfVictory(tournament),
                xp2.getAverageSoS(tournament));

        matchString += "</table>";

        matchString.replaceAll("<td>", "<td class=\"smallFont\">");

        return matchString;
    }

	@Override
	public void cacReport() {
        // Not Supported
	}

	@Override
	public void tcxTeamReport(Tournament tournament) {
        // Not Supported
		
	}

}
