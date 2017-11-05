package cryodex.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import cryodex.Player;

public class RandomMatchGeneration {

	private final Tournament tournament;
	private final List<Player> players;
	private List<List<Player>> pointGroups;

	public RandomMatchGeneration(Tournament tournament,
                                 List<Player> players) {
		this.tournament = tournament;
		this.players = players;
	}

	public List<Match> generateMatches() {

		if(players == null || players.isEmpty()){
			return new ArrayList<Match>();
		}

		getPointGroups();

		List<Match> matches = null;

		if(pointGroups.isEmpty() == false) {
			matches = resolvePointGroup(null, 0);
		}

		return matches;
	}

	private void getPointGroups(){
		TreeMap<Integer, List<Player>> playerMap = new TreeMap<Integer, List<Player>>(
				new Comparator<Integer>() {

					@Override
					public int compare(Integer arg0, Integer arg1) {
						return arg0.compareTo(arg1) * -1;
					}
				});

		for (Player p : players) {
			Integer points = tournament.getModulePlayer(p).getScore(tournament);

			List<Player> pointGroup = playerMap.get(points);

			if (pointGroup == null) {
				pointGroup = new ArrayList<>();
				playerMap.put(points, pointGroup);
			}

			pointGroup.add(p);
		}

		pointGroups = new ArrayList<List<Player>>();
		for(Integer i : playerMap.keySet()){
			pointGroups.add(playerMap.get(i));
		}
	}

	private List<Match> resolvePointGroup(Player carryOverPlayer,
			int pointGroupCounter) {

		if(pointGroupCounter >= pointGroups.size()){
			return new ArrayList<>();
		}

		// Get the point group players and mix them up
		List<Player> playerList = pointGroups.get(pointGroupCounter);
		Collections.shuffle(playerList);

		// Get the player to carry over to the next group
		Player newCarryOverPlayer = null;
		int carryOverPlayerIndex = playerList.size();
		boolean isCarryOver = carryOverPlayer == null ? carryOverPlayerIndex % 2 == 1
				: carryOverPlayerIndex % 2 == 0;

		while (true) {

			List<Player> tempList = new ArrayList<>();
			tempList.addAll(playerList);

			if (isCarryOver) {
				carryOverPlayerIndex--;
				newCarryOverPlayer = playerList.get(carryOverPlayerIndex);
				tempList.remove(newCarryOverPlayer);
			}

			List<Match> returnedMatches = getRandomMatches(
					carryOverPlayer, tempList);

			// If the list was good or if there was no carry over players that
			// can change things up
			if (isCarryOver == false || carryOverPlayerIndex == 0
					|| Match.hasDuplicate(returnedMatches) == false) {

				List<Player> nextPointGroup = null;
				if(pointGroupCounter + 1 < pointGroups.size()){
					nextPointGroup = pointGroups.get(pointGroupCounter + 1);
				}

				// If this was the last point group
				if (nextPointGroup == null) {
					return returnedMatches;
				} else {
					// Else, check the next point group
					List<Match> nextPointGroupMatches = resolvePointGroup(
							newCarryOverPlayer, pointGroupCounter + 1);

					// Again, continue if the list is good or there are no other
					// options
					if (isCarryOver == false
							|| carryOverPlayerIndex == 0
							|| Match.hasDuplicate(nextPointGroupMatches) == false) {
						returnedMatches.addAll(nextPointGroupMatches);
						return returnedMatches;
					}
				}
			}
		}
	}

	/**
	 * Recursive call to find a non duplicate match of remaining players in a
	 * group
	 * 
	 * @param carryOverPlayer
	 * @param players
	 * @return
	 */
	private List<Match> getRandomMatches(Player carryOverPlayer,
			List<Player> players) {

		List<Match> matches = new ArrayList<>();

		// if there are no players, return no matches
		if (players.isEmpty()) {
			return matches;
		}

		Match m = new Match();

		List<Match> subMatches = new ArrayList<>();

		// If there is a carry over player, they are always player 1
		if (carryOverPlayer != null) {
			m.setPlayer1(carryOverPlayer);
			for (int counter = 0; counter < players.size(); counter++) {

				m.setPlayer2(players.get(counter));
				m.checkDuplicate(tournament.getAllRounds());

				// Continue if the match is not a duplicate or this is the last
				// chance
				if (m.isDuplicate() == false || counter == players.size() - 1) {
					List<Player> nextPlayers = new ArrayList<Player>();
					nextPlayers.addAll(players);
					nextPlayers.remove(m.getPlayer2());
					subMatches = getRandomMatches(null, nextPlayers);

					// if no duplicates, stop, else try again
					if (Match.hasDuplicate(subMatches) == false) {
						matches.add(m);
						matches.addAll(subMatches);
						return matches;
					}
				}
			}
		} else {

			// this is the same as the previous one, except the first player in
			// the list is player 1 and both player 1 and 2 must be removed from
			// the continuing list

			m.setPlayer1(players.get(0));

			// Loop through each other player to find a match
			for (int counter = 1; counter < players.size(); counter++) {

				// add new player and check if it is valid
				m.setPlayer2(players.get(counter));
				m.checkDuplicate(tournament.getAllRounds());

				// Continue if the match is not a duplicate or this is the last
				// chance
				if (m.isDuplicate() == false || counter == players.size() - 1) {

					// Create the list of remaining players
					List<Player> nextPlayers = new ArrayList<Player>();
					nextPlayers.addAll(players);
					nextPlayers.remove(m.getPlayer1());
					nextPlayers.remove(m.getPlayer2());

					// Call function recursively
					subMatches = getRandomMatches(null, nextPlayers);

					// if no duplicates, stop, else try again
					if (Match.hasDuplicate(subMatches) == false) {
						matches.add(m);
						matches.addAll(subMatches);
						return matches;
					}
				}
			}

		}

		// If we got here than we gave up, there was no chance of finding a non
		// duplicate group
		matches.add(m);
		matches.addAll(subMatches);
		return matches;
	}
}
