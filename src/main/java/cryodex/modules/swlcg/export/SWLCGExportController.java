package cryodex.modules.swlcg.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cryodex.CryodexController;
import cryodex.Player;
import cryodex.modules.ExportController;
import cryodex.modules.Match;
import cryodex.modules.Tournament;
import cryodex.modules.swlcg.SWLCGPlayer;
import cryodex.modules.swlcg.SWLCGTournament;

public class SWLCGExportController extends ExportController {

    public String appendRankings(Tournament tournament) {
        List<Player> playerList = new ArrayList<Player>();
        List<Player> activePlayers = tournament.getPlayers();

        playerList.addAll(tournament.getAllPlayers());
        Collections.sort(playerList, tournament.getRankingComparator());

        String content = "<table border=\"1\">";

        content = appendHTMLTableHeader(content, "Rank", "Name", "Score", "SoS", "Ext SoS");

        for (Player p : playerList) {

            SWLCGPlayer xp = ((SWLCGTournament) tournament).getModulePlayer(p);

            String name = p.getName();

            if (activePlayers.contains(p) == false) {
                name = "(D#" + xp.getRoundDropped(tournament) + ")" + name;
            }

            content = appendHTMLTableRow(content, xp.getRank(tournament), name, xp.getScore(tournament), xp.getAverageSoS(tournament),
                    xp.getExtendedStrengthOfSchedule(tournament));
        }

        content += "</table>";

        return content;
    }

    @Override
    public String getValueLabel() {
        return "Points";
    }

    @Override
    public String getMatchStats(Match match, Tournament tournament) {

        String matchString = "<table class=\"print-friendly\" border=\"1\">";

        SWLCGPlayer xp1 = (SWLCGPlayer) tournament.getModulePlayer(match.getPlayer1());
        SWLCGPlayer xp2 = (SWLCGPlayer) tournament.getModulePlayer(match.getPlayer2());

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
        // Not supported
    }

    public String appendMatches(Tournament tournament, List<Match> matches) {
        String content = "";

        int counter = 1;
        for (Match m : matches) {
            String matchString = "";
            if (m.getPlayer2() == null) {
                matchString += m.getPlayer1().getName() + " has a BYE";
            } else {
                matchString += m.getPlayer1().getName() + " VS " + m.getPlayer2().getName();
                if (CryodexController.getOptions().isShowTableNumbers()) {
                    matchString = counter + ": " + matchString;
                    counter++;
                }

                if (tournament.isMatchComplete(m)) {
                    switch (m.getGame1Result()) {
                    case DRAW:
                        matchString += "Game 1 is a draw.";
                        break;
                    case PLAYER_1_WINS:
                        matchString += m.getPlayer1() + " wins Game 1.";
                        break;
                    case PLAYER_2_WINS:
                        matchString += m.getPlayer2() + " wins Game 1.";
                        break;
                    default:
                        matchString += "error";
                        break;
                    }

                    switch (m.getGame2Result()) {
                    case DRAW:
                        matchString += "Game 2 is a draw.";
                        break;
                    case PLAYER_1_WINS:
                        matchString += m.getPlayer1() + " wins Game 2.";
                        break;
                    case PLAYER_2_WINS:
                        matchString += m.getPlayer2() + " wins Game 2.";
                        break;
                    default:
                        matchString += "error";
                        break;
                    }
                }
            }
            content += "<div>" + matchString + "</div>";
        }

        return content;
    }

    @Override
    public void tcxTeamReport(Tournament tournament) {
        // Not supported
    }

	@Override
	public void exportMultiTournamentReport(List<Tournament> tournaments) {
		// TODO Auto-generated method stub
		
	}
}
