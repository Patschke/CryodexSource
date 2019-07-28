package cryodex.modules.legion.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cryodex.Player;
import cryodex.modules.ExportController;
import cryodex.modules.Match;
import cryodex.modules.Tournament;
import cryodex.modules.legion.LegionPlayer;
import cryodex.modules.legion.LegionTournament;

public class LegionExportController extends ExportController {

	public String appendRankings(Tournament tournament) {
		List<Player> playerList = new ArrayList<Player>();
		List<Player> activePlayers = tournament.getPlayers();

		playerList.addAll(tournament.getAllPlayers());
        Collections.sort(playerList, tournament.getRankingComparator());

        String content = "<table border=\"1\">";

        content = appendHTMLTableHeader(content, "Rank", "Name", "Score", "SoS", "Ext SoS");

		for (Player p : playerList) {

			LegionPlayer xp = ((LegionTournament) tournament).getModulePlayer(p);

			String name = p.getName();

			if (activePlayers.contains(p) == false) {
				name = "(D#" + xp.getRoundDropped(tournament) + ")" + name;
			}

			content = appendHTMLTableRow(content, xp.getRank(tournament), name, xp.getScore(tournament),
                    xp.getAverageSoS(tournament), xp.getExtendedStrengthOfSchedule(tournament));
		}

		content += "</table>";

		return content;
	}
	
	@Override
	public String getValueLabel() {
	    return "Winner";
	}

    @Override
    public String getMatchStats(Match match, Tournament tournament) {

        String matchString = "<table class=\"print-friendly\" border=\"1\">";

        LegionPlayer xp1 = (LegionPlayer) tournament.getModulePlayer(match.getPlayer1());
        LegionPlayer xp2 = (LegionPlayer) tournament.getModulePlayer(match.getPlayer2());

        matchString += appendHTMLTableHeader("Name", "Rank", "Score", "SoS", "Ext SoS");

        matchString += appendHTMLTableRow(xp1.getName(), xp1.getRank(tournament), xp1.getScore(tournament), xp1.getAverageSoS(tournament),
                xp1.getExtendedStrengthOfSchedule(tournament));
        matchString += appendHTMLTableRow(xp2.getName(), xp2.getRank(tournament), xp2.getScore(tournament), xp2.getAverageSoS(tournament),
                xp2.getExtendedStrengthOfSchedule(tournament));

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

	@Override
	public void exportMultiTournamentReport(List<Tournament> tournaments) {
		// TODO Auto-generated method stub
		
	}

}
