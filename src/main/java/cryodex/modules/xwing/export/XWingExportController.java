package cryodex.modules.xwing.export;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import cryodex.Player;
import cryodex.export.ExportUtils;
import cryodex.modules.ExportController;
import cryodex.modules.Match;
import cryodex.modules.Tournament;
import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingTournament;

public class XWingExportController extends ExportController {

    public String appendRankings(Tournament tournament) {
        List<Player> playerList = new ArrayList<Player>();
        List<Player> activePlayers = tournament.getPlayers();

        playerList.addAll(tournament.getAllPlayers());
        Collections.sort(playerList, tournament.getRankingComparator());

        String content = "<table border=\"1\">";

        content = appendHTMLTableHeader(content, "Rank", "Name", "Faction", "Score", "MoV", "SoS");

        for (Player p : playerList) {

            XWingPlayer xp = ((XWingTournament) tournament).getModulePlayer(p);

            String name = p.getName();
            String faction = xp.getFaction() == null ? "" : xp.getFaction().toString();

            if (activePlayers.contains(p) == false) {
                name = "(D#" + xp.getRoundDropped(tournament) + ")" + name;
            }

            content = appendHTMLTableRow(content, xp.getRank(tournament), name, faction, xp.getScore(tournament), xp.getMarginOfVictory(tournament),
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

        XWingPlayer xp1 = (XWingPlayer) tournament.getModulePlayer(match.getPlayer1());
        XWingPlayer xp2 = (XWingPlayer) tournament.getModulePlayer(match.getPlayer2());

        matchString += appendHTMLTableHeader("Name", "Rank", "Score", "MoV", "SoS");

        matchString += appendHTMLTableRow(xp1.getName(), xp1.getRank(tournament), xp1.getScore(tournament), xp1.getMarginOfVictory(tournament),
                xp1.getAverageSoS(tournament));
        matchString += appendHTMLTableRow(xp2.getName(), xp2.getRank(tournament), xp2.getScore(tournament), xp2.getMarginOfVictory(tournament),
                xp2.getAverageSoS(tournament));

        matchString += "</table>";

        matchString = matchString.replaceAll("<td>", "<td class=\"smallFont\">");

        return matchString;
    }

    public void cacReport() {

        String content = CACReport.generateCACReport();

        ExportUtils.displayHTML(content, "Campaign Against Cancer Report");

    }

    @Override
    public void tcxTeamReport(Tournament tournament) {
        TCXTeamExport tcxExport = new TCXTeamExport(tournament);

        ExportUtils.displayHTML(tcxExport.output(), "TCX Ultimate Team Report");
    }

	@Override
	public void exportMultiTournamentReport(List<Tournament> tournaments) {

		Tournament totalTournament = null;
		List<Player> originalPlayerSet = new ArrayList<Player>();
		TreeSet<Player> totalPlayers = new TreeSet<Player>();
		
		//Grab first tournament as main and collect data for dependents
		for(Tournament t : tournaments){
			if(t instanceof XWingTournament){
				if(t.getDependentTournaments() != null && t.getDependentTournaments().size() > 0){
					JOptionPane.showMessageDialog((Component) null, "This kind of export doesn't work with tournaments with dependant tournaments. The programer is lazy, sorry?", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(totalTournament == null){
					totalTournament = t;
					originalPlayerSet.addAll(t.getPlayers());
				} else {
					totalTournament.addDependentTournament(t);
				}
				
				totalPlayers.addAll(t.getAllPlayers());
			}
		}
		
		//Combined data into one tournament
		totalTournament.getPlayers().clear();
		totalTournament.getPlayers().addAll(totalPlayers);

		//Create content
        List<Player> playerList = new ArrayList<Player>();
        List<Player> activePlayers = totalTournament.getPlayers();

        playerList.addAll(totalTournament.getAllPlayers());
        Collections.sort(playerList, totalTournament.getRankingComparator());

        String content = "<table border=\"1\">";

        content = appendHTMLTableHeader(content, "Rank", "Name", "Faction", "Score", "MoV", "SoS");

        for (Player p : playerList) {

            XWingPlayer xp = ((XWingTournament) totalTournament).getModulePlayer(p);

            String name = p.getName();
            String faction = xp.getFaction() == null ? "" : xp.getFaction().toString();

            if (activePlayers.contains(p) == false) {
                name = "(D#" + xp.getRoundDropped(totalTournament) + ")" + name;
            }

            content = appendHTMLTableRow(content, xp.getRank(totalTournament), name, faction, xp.getScore(totalTournament), xp.getMarginOfVictory(totalTournament),
                    xp.getAverageSoS(totalTournament));
        }

        content += "</table>";

        totalTournament.getPlayers().clear();
        totalTournament.getPlayers().addAll(originalPlayerSet);
        totalTournament.clearDependentTournaments();
        
		ExportUtils.displayHTML(content, "ExportRankings");
	
	}
}
