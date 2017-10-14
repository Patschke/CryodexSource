package cryodex.modules.xwing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import cryodex.Main;
import cryodex.Player;
import cryodex.modules.Match;

/**
 * Generate matches in order of ranking or as close to it as possible. This
 * algorithm finds the best possible match set that holds truest to the original
 * ranking. Every permutation is calculated and given a score based on the
 * distance each player is from their true ranking. The permutation with the
 * lowest score wins.
 * 
 * @author cbrown
 * 
 */
public class XWingOrderedMatchGeneration {

	private final XWingTournament tournament;
	private final List<Player> players;

	private Integer lowScore = null;
	private List<Match> matchSetAtLowScore = null;

	public XWingOrderedMatchGeneration(XWingTournament tournament,
			List<Player> players) {
		this.tournament = tournament;
		this.players = players;

	}

	public List<Match> generateMatches() {

		List<Player> tempList = new ArrayList<>();
		tempList.addAll(players);
		Collections.sort(tempList, new XWingComparator(tournament,
				XWingComparator.pairingCompare));

		generateMatch(new ArrayList<Match>(), tempList);

		// If no valid match set was found then we create the true ranking match
		// set and return it
		if (matchSetAtLowScore == null) {
			matchSetAtLowScore = new ArrayList<>();

			for (int counter = 0; counter < players.size(); counter += 2) {
				Match m = new Match(players.get(counter),
						players.get(counter + 1));
				m.checkDuplicate(tournament.getAllRounds());
				matchSetAtLowScore.add(m);
			}

		}

		if (lowScore != null && lowScore != 0) {
			JOptionPane.showMessageDialog(Main.getInstance(),
					"Matches were modified to avoid duplicate pairings. Avoidance score = "
							+ lowScore + " (2-10 is a minor change)");
		}

		return matchSetAtLowScore;
	}

	private void generateMatch(List<Match> matches,
			List<Player> player1List) {

		if (player1List.size() == 0) {
			scorePermutation(matches);
			return;
		}

		for (Player xp : player1List) {

			List<Player> player2List = new ArrayList<>();
			player2List.addAll(player1List);
			player2List.remove(xp);

			getPlayer2(xp, matches, player2List);

			if (lowScore != null && lowScore <= 2) {
				return;
			}
		}
	}

	private void getPlayer2(Player player1, List<Match> matches,
			List<Player> player2List) {
		for (Player player2 : player2List) {
			Match xm = new Match(player1, player2);
			xm.checkDuplicate(tournament.getAllRounds());

			if (xm.isDuplicate() == false) {
				matches.add(xm);
				if (shouldContinue(matches)) {
					List<Player> player1List = new ArrayList<>();
					player1List.addAll(player2List);
					player1List.remove(xm.getPlayer2());

					generateMatch(matches, player1List);
				}
			}

			matches.remove(xm);

			if (lowScore != null && lowScore <= 2) {
				return;
			}
		}
	}

	private boolean shouldContinue(List<Match> matches) {
		if (lowScore == null) {
			return true;
		}
		return getScore(matches) < lowScore;
	}

	private void scorePermutation(List<Match> matches) {
		int score = getScore(matches);

		if (lowScore == null || score < lowScore) {
			matchSetAtLowScore = new ArrayList<>();
			matchSetAtLowScore.addAll(matches);

			lowScore = score;
		}
	}

	private int getScore(List<Match> matches) {

		// order players
		Collections.sort(players, new XWingComparator(tournament,
				XWingComparator.pairingCompare));

		// get list of players in order of matches
		List<XWingPlayer> playerByMatchOrder = new ArrayList<XWingPlayer>();
		for (Match xm : matches) {
			playerByMatchOrder.add((XWingPlayer) xm.getPlayer1().getModuleInfoByModule(tournament.getModule()));
			playerByMatchOrder.add((XWingPlayer) xm.getPlayer2().getModuleInfoByModule(tournament.getModule()));
		}

		int score = 0;

		for (int counter = 0; counter < playerByMatchOrder.size(); counter++) {
			int index = players.indexOf(playerByMatchOrder.get(counter));

			score += Math.abs(counter - index);
		}

		return score;
	}
}
