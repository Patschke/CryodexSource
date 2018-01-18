package cryodex.modules.krayt.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cryodex.CryodexController;
import cryodex.Player;
import cryodex.export.ExportUtils;
import cryodex.modules.ExportController;
import cryodex.modules.Match;
import cryodex.modules.Round;
import cryodex.modules.Tournament;
import cryodex.modules.krayt.KraytComparator;
import cryodex.modules.krayt.KraytMatch;
import cryodex.modules.krayt.KraytPlayer;
import cryodex.modules.krayt.KraytTournament;
import cryodex.modules.xwing.export.CACReport;
import cryodex.widget.wizard.WizardOptions;

public class KraytExportController extends ExportController {

    public String appendRankings(Tournament tournament) {
        List<Player> playerList = new ArrayList<Player>();
        List<Player> activePlayers = tournament.getPlayers();

        playerList.addAll(tournament.getAllPlayers());
        Collections.sort(playerList, new KraytComparator(tournament, KraytComparator.rankingCompare));

        String content = "<table border=\"1\"><tr><th>Rank</th><th>Name</th><th>Score</th><th>MoV</th><th>SoS</th></tr>";

        int rankCounter = 0;
        for (Player p : playerList) {

            rankCounter++;
            
            KraytPlayer xp = ((KraytTournament) tournament).getModulePlayer(p);

            String name = p.getName();

            if (activePlayers.contains(p) == false) {
                name = "(D#" + xp.getRoundDropped(tournament) + ")" + name;
            }

            content += "<tr><td>" + rankCounter + "</td><td>" + name + "</td><td>" + xp.getScore(tournament)
                    + "</td><td>" + xp.getMarginOfVictory(tournament) + "</td><td>" + xp.getAverageSoS(tournament) + "</td></tr>";
        }

        content += "</table>";

        return content + "<br>" + appendIndividualPlayerRankings(tournament);
    }

    private String appendIndividualPlayerRankings(Tournament tournament) {
        List<Player> playerList = new ArrayList<Player>();

        List<String> groupNames = new ArrayList<String>();
        for(Player p : tournament.getAllPlayers()){
            groupNames.add(p.getName());
        }
        
        Map<String, Player> playerNameMap = new HashMap<String,Player>();
        for(Player p : CryodexController.getPlayers()){
            if(groupNames.contains(p.getGroupName())){
                playerList.add(p);
                playerNameMap.put(p.getName(), p);
            }
        }
        
        WizardOptions wo = new WizardOptions();
        wo.setPoints(tournament.getPoints());
        
        Tournament subTournament = new KraytTournament(wo);
        
        List<Match> matches = new ArrayList<Match>();
        
        for(Round r : tournament.getAllRounds()){
            for(Match m : r.getMatches()){
                KraytMatch km = (KraytMatch) m;
                KraytMatch newKm = KraytMatch.copyMatch(km, km.getSuffix());
        
                Player p1 = playerNameMap.get(km.getSubplayer1());
                p1 = p1 == null ? km.getPlayer1() : p1;
                Player p2 = playerNameMap.get(km.getSubplayer2());
                p2 = p2 == null ? km.getPlayer2() : p2;
                
                newKm.setPlayer1(p1);
                newKm.setPlayer2(p2);
                
                matches.add(newKm);
            }
        }
        
        Round round = new Round(matches, subTournament);
        
        subTournament.getAllRounds().add(round);
        
        tournament = subTournament;
        
        Collections.sort(playerList, new KraytComparator(tournament, KraytComparator.rankingCompare));

        String content = "<table border=\"1\"><tr><th>Rank</th><th>Name</th><th>Group</th><th>Faction</th><th>Score</th><th>MoV</th><th>SoS</th></tr>";

        int rankingCounter = 0;
        
        for (Player p : playerList) {

            rankingCounter++;
            
            KraytPlayer xp = ((KraytTournament) tournament).getModulePlayer(p);

            String name = p.getName();
            String faction = xp.getFaction() == null ? "" : xp.getFaction().toString();
            String group = p.getGroupName();


            content += "<tr><td>" + rankingCounter + "</td><td>" + name + "</td><td>" + group + "</td><td>" + faction + "</td><td>" + xp.getScore(tournament)
                    + "</td><td>" + xp.getMarginOfVictory(tournament) + "</td><td>" + xp.getAverageSoS(tournament) + "</td></tr>";
        }

        content += "</table>";

        return content;
    }
    
    public String appendMatches(Tournament tournament, List<Match> matches) {
        String content = "";

        int counter = 1;
        for (Match m : matches) {
            KraytMatch km = (KraytMatch) m;
            String matchString = "";
            if (m.getPlayer2() == null) {
                matchString += m.getPlayer1().getName() + " has a BYE";
            } else {
                matchString += m.getPlayer1().getName() + " " + km.getSubplayer1() + " VS " + m.getPlayer2().getName() + " " + km.getSubplayer2();
                if (CryodexController.getOptions().isShowTableNumbers()) {
                    matchString = counter + ": " + matchString;
                    counter++;
                }

                if (tournament.isMatchComplete(m)) {
                    matchString += " - Match Results: ";
                    if (m.getWinner(1) != null) {
                        matchString += m.getWinner(1).getName() + " is the winner";
                    }

                    if (m.getPlayer1Points() != null && m.getPlayer2Points() != null) {
                        matchString += " " + m.getPlayer1Points() + " to " + m.getPlayer2Points();
                    }
                }
            }
            content += "<div>" + matchString + "</div>";
        }

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

                    KraytPlayer xp1 = (KraytPlayer) tournament.getModulePlayer(m.getPlayer1());
                    KraytPlayer xp2 = (KraytPlayer) tournament.getModulePlayer(m.getPlayer2());

                    matchString += "<table class=\"print-friendly\" width=100%><tr><th><h4>Round " + roundNumber + " - Table " + (index + 1)
                            + "</h4></th><th vAlign=bottom align=left><h4>" + m.getPlayer1().getName() + "</h4></th><th vAlign=bottom align=left><h4>"
                            + xp2.getName()
                            + "</h4></th></tr><tr><td><table class=\"print-friendly\" border=\"1\"><tr><th>Name</th><th>Rank</td><th>Score</th><th>MoV</th><th>SoS</th></tr><tr>"
                            + "<td class=\"smallFont\">" + xp1.getName() + "</td><td class=\"smallFont\">" + xp1.getRank(tournament)
                            + "</td><td class=\"smallFont\">" + xp1.getScore(tournament) + "</td><td class=\"smallFont\">"
                            + xp1.getMarginOfVictory(tournament) + "</td><td class=\"smallFont\">" + xp1.getAverageSoS(tournament)
                            + "</td></tr><tr><td class=\"smallFont\">" + xp2.getName() + "</td><td class=\"smallFont\">" + xp2.getRank(tournament)
                            + "</td><td class=\"smallFont\">" + xp2.getScore(tournament) + "</td><td class=\"smallFont\">"
                            + xp2.getMarginOfVictory(tournament) + "</td><td class=\"smallFont\">" + xp2.getAverageSoS(tournament)
                            + "</td></tr></table>" + "</td><td class=\"smallFont\">"
                            + "<div style=\"vertical-align: bottom; height: 100%;\">Points Killed ____________</div>" + "</br>"
                            + "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
                            + "</td><td class=\"smallFont\">"
                            + "<div style=\"vertical-align: bottom; height: 100%;\">Points Killed ____________</div>" + "</br>"
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
