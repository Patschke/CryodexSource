package cryodex.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import cryodex.CryodexController;
import cryodex.Player;
import cryodex.export.ExportUtils;

public abstract class ExportController {

    public abstract String appendRankings(Tournament tournament);

    public abstract void exportTournamentSlipsWithStats(Tournament tournament, List<Match> matches, int roundNumber);

    public abstract void cacReport();
    
    public abstract void tcxTeamReport(Tournament tournament);
    
    public abstract String getValueLabel();
    
	public void playerList(List<Player> players) {
		Set<Player> sortedPlayers = new TreeSet<Player>();
		sortedPlayers.addAll(players);

		StringBuilder sb = new StringBuilder();

		for (Player p : sortedPlayers) {
			sb.append(p.getName()).append("<br>");
		}

		ExportUtils.displayHTML(sb.toString(), "Player List");
	}
	
	public void exportTournamentSlips(Tournament tournament, List<Match> matches, int roundNumber) {

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
					matchString += "<table class=\"print-friendly\" width=100%><tr><td><h4>" + tournament.getName() + " - Round " + roundNumber
							+ " - Table " + (index + 1) + "</h4></td><td vAlign=bottom align=left><h4>"
							+ m.getPlayer1().getName() + "</h4></td><td vAlign=bottom align=left><h4>"
							+ m.getPlayer2().getName() + "</h4></td></tr><tr><td>" + "</td><td class=\"smallFont\">"
							+ "<div style=\"vertical-align: bottom; height: 100%;\">" + getValueLabel() + " ____________</div>"
							+ "</br>"
							+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
							+ "</td><td class=\"smallFont\">"
							+ "<div style=\"vertical-align: bottom; height: 100%;\">" + getValueLabel() + " ____________</div>"
							+ "</br>"
							+ "<div style=\"vertical-align: top; height: 100%;\"><input type=\"checkbox\">I wish to drop</input></div>"
							+ "</td></tr></table>";

					matchString += "<br><br><br><hr>";

					content += matchString;
				}
			}
			content += "<div class=\"pagebreak\">&nbsp;</div>";
			index = pageCounter;
			pageCounter++;
		}

		ExportUtils.displayHTML(content, "ExportMatchSlips");
	}
	
	public void exportTournamentReport(Tournament tournament) {
		String content = "";
		int roundNumber = 1;
		for (Round r : tournament.getAllRounds()) {
			if (r.isSingleElimination()) {
				content += "<h3>Top " + (r.getMatches().size() * 2) + "</h3>";
			} else {
				content += "<h3>Round " + roundNumber + "</h3>";
			}
			content += appendMatches(tournament, r.getMatches());

			roundNumber++;
		}

		content += "<h3>Rankings</h3>";
		content += appendRankings(tournament);

		ExportUtils.displayHTML(content, "TournamentReport");
	}
	
	public void exportMatches() {

		Tournament tournament = CryodexController.getActiveTournament();

		List<Tournament> tournamentSubset = new ArrayList<Tournament>();
		if (tournament.getName().endsWith(" 1")) {

			String name = tournament.getName().substring(0, tournament.getName().lastIndexOf(" "));

			List<Tournament> tournaments = CryodexController.getAllTournaments();

			for (Tournament t : tournaments) {
				if (t.getName().contains(name)) {
					tournamentSubset.add(t);
				}
			}
		} else {
			tournamentSubset.add(tournament);
		}

		String content = "";

		for (Tournament t : tournamentSubset) {

			Round round = t.getLatestRound();

			int roundNumber = round.isSingleElimination() ? 0 : t.getRoundNumber(round);

			List<Match> matches = round.getMatches();

			content += "<h3>Event: " + t.getName() + "</h3>";

			if (roundNumber == 0) {
				content += "<h3>Top " + (matches.size() * 2) + "</h3>";
			} else {
				content += "<h3>Round " + roundNumber + "</h3>";
			}

			content += printPairings(matches);
		}
		ExportUtils.displayHTML(content, "ExportMatch");
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
	
	public void exportRankings(Tournament tournament) {

		String content = appendRankings(tournament);

		ExportUtils.displayHTML(content, "ExportRankings");
	}

	public String printPairings(List<Match> matches) {
		List<String> matchStrings = new ArrayList<String>();

		int counter = 1;
		String matchString = null;
		for (Match m : matches) {
			if (m.getPlayer2() == null) {
				matchString = m.getPlayer1().getName() + " has a bye";
				matchStrings.add(matchString);
			} else {
				matchString = m.getPlayer1().getName() + " vs " + m.getPlayer2() + " at table " + counter;
				matchStrings.add(matchString);
				matchString = m.getPlayer2().getName() + " vs " + m.getPlayer1() + " at table " + counter;
				matchStrings.add(matchString);
			}
			counter++;
		}

		String content = "";

		Collections.sort(matchStrings);

		for (String s : matchStrings) {
			content += "<div>" + s + "</div>";
		}

		return content;
	}
}
