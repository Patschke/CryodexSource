package cryodex.modules.battletech.export;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import cryodex.Player;
import cryodex.export.ExportUtils;
import cryodex.modules.ExportController;
import cryodex.modules.Match;
import cryodex.modules.Tournament;
import cryodex.modules.battletech.BTModule;
import cryodex.modules.battletech.BTPlayer;
import cryodex.modules.battletech.BTPlayer.Faction;
import cryodex.modules.battletech.BTTournament;

public class BTExportController extends ExportController {

    public String appendRankings(Tournament tournament) {
        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(tournament.getAllPlayers());
        
        return appendRanking(playerList, tournament);
    }
    
    public String appendRanking(List<Player> playerList, Tournament tournament){
        List<Player> activePlayers = tournament.getPlayers();

        Collections.sort(playerList, tournament.getRankingComparator());
    	
    	String content = "<table border=\"1\">";

        content = appendHTMLTableHeader(content, "Rank", "Name", "Faction", "Score", "Kill Points", "SoS");

        for (Player p : playerList) {

            BTPlayer xp = ((BTTournament) tournament).getModulePlayer(p);

            String name = p.getName();
            String faction = xp.getFaction() == null ? "" : xp.getFaction().toString();

            if (activePlayers.contains(p) == false) {
                name = "(D#" + xp.getRoundDropped(tournament) + ")" + name;
            }

            content = appendHTMLTableRow(content, xp.getRank(tournament), name, faction, xp.getScore(tournament), xp.getKillPoints(tournament),
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

        BTPlayer xp1 = (BTPlayer) tournament.getModulePlayer(match.getPlayer1());
        BTPlayer xp2 = (BTPlayer) tournament.getModulePlayer(match.getPlayer2());

        matchString += appendHTMLTableHeader("Name", "Rank", "Score", "Kill Points", "SoS");

        matchString += appendHTMLTableRow(xp1.getName(), xp1.getRank(tournament), xp1.getScore(tournament), xp1.getKillPoints(tournament),
                xp1.getAverageSoS(tournament));
        matchString += appendHTMLTableRow(xp2.getName(), xp2.getRank(tournament), xp2.getScore(tournament), xp2.getKillPoints(tournament),
                xp2.getAverageSoS(tournament));

        matchString += "</table>";

        matchString = matchString.replaceAll("<td>", "<td class=\"smallFont\">");

        return matchString;
    }

    public void cacReport() {}

    @Override
    public void tcxTeamReport(Tournament tournament) {}

	@Override
	public void exportMultiTournamentReport(List<Tournament> tournaments) {

		Tournament totalTournament = null;
		List<Player> originalPlayerSet = new ArrayList<Player>();
		TreeSet<Player> totalPlayers = new TreeSet<Player>();
		
		//Grab first tournament as main and collect data for dependents
		for(Tournament t : tournaments){
			if(t instanceof BTTournament){
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

        content = appendHTMLTableHeader(content, "Rank", "Name", "Faction", "Score", "Kill Points", "SoS");

        for (Player p : playerList) {

            BTPlayer xp = ((BTTournament) totalTournament).getModulePlayer(p);

            String name = p.getName();
            String faction = xp.getFaction() == null ? "" : xp.getFaction().toString();

            if (activePlayers.contains(p) == false) {
                name = "(D#" + xp.getRoundDropped(totalTournament) + ")" + name;
            }

            content = appendHTMLTableRow(content, xp.getRank(totalTournament), name, faction, xp.getScore(totalTournament), xp.getKillPoints(totalTournament),
                    xp.getAverageSoS(totalTournament));
        }

        content += "</table>";

        totalTournament.getPlayers().clear();
        totalTournament.getPlayers().addAll(originalPlayerSet);
        totalTournament.clearDependentTournaments();
        
		ExportUtils.displayHTML(content, "ExportRankings");
	
	}

	@Override
	public void exportByFaction(Tournament tournament) {
		String content = "";
		
		Map<Faction, List<Player>> factionMap = new HashMap<Faction, List<Player>>();
		
		for(Player p : tournament.getAllPlayers()){
			BTPlayer xwp = (BTPlayer) p.getModuleInfoByModule(BTModule.getInstance());
			
			List<Player> factionList = factionMap.get(xwp.getFaction());
			
			if(factionList == null){
				factionList = new ArrayList<Player>();
				factionMap.put(xwp.getFaction(), factionList);
			}
			
			factionList.add(p);
		}
		
		for(Faction f : factionMap.keySet()){
			
			content += "<h1>" + f.toString() + "</h1><br>";
			
			List<Player> players = factionMap.get(f);
			
			content += appendRanking(players, tournament);
		}
		
		ExportUtils.displayHTML(content, "ExportFactionRankings");
	}
}
