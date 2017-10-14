package cryodex.modules.xwing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Match;
import cryodex.modules.ModulePlayer;
import cryodex.modules.Round;
import cryodex.modules.Tournament;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class XWingPlayer implements Comparable<ModulePlayer>, XMLObject, ModulePlayer {

	public static enum Faction {
		IMPERIAL, REBEL, SCUM;
	}

	private Player player;
	private String seedValue;
	private String squadId;
	private Faction faction;

	private Map<String, Integer> integerStatistics = new HashMap<String, Integer>();
	private Map<String, Double> doubleStatistics = new HashMap<String, Double>();

	public XWingPlayer(Player p) {
		player = p;
		seedValue = String.valueOf(Math.random());
	}

	public XWingPlayer(Player p, Element e) {
		this.player = p;
		this.seedValue = e.getStringFromChild("SEEDVALUE");
		this.squadId = e.getStringFromChild("SQUADID");
		String factionString = e.getStringFromChild("FACTION");

		if (factionString != null && factionString.isEmpty() == false) {
			faction = Faction.valueOf(factionString);
		} else {
			faction = Faction.IMPERIAL;
		}
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public void setPlayer(Player player) {
		this.player = player;
	}

	public String getSeedValue() {
		return seedValue;
	}

	public void setSeedValue(String seedValue) {
		this.seedValue = seedValue;
	}

	public String getSquadId() {
		return squadId;
	}

	public void setSquadId(String squadId) {
		this.squadId = squadId;
	}

	public Faction getFaction() {
		return faction;
	}

	public void setFaction(Faction faction) {
		this.faction = faction;
	}



	@Override
	public String toString() {
		return getPlayer().getName();
	}

	public int getScore(Tournament t) {

		Integer score = getPlayerStatisticInteger("Score");

		if(score != null){
			return score;
		}

		score = 0;
		for (Match match : getPlayer().getMatches(t)) {
			if (match.getWinner() == this.getPlayer()) {
				score += XWingConstants.WIN_POINTS;
			} else if (match.isBye()) {
				score += XWingConstants.BYE_POINTS;
			} else {
				score += XWingConstants.LOSS_POINTS;
			}
		}

		putPlayerStatisticInteger("Score", score);

		return score;
	}

	public double getAverageScore(Tournament t) {

		Double averageScore = getPlayerStatisticDouble("AverageScore");

		if(averageScore != null){
			return averageScore;
		}

		int score = getScore(t);
		int matchCount = getPlayer().getCompletedMatches(t).size();

		averageScore = score * 1.0 / matchCount; 

		putPlayerStatisticDouble("AverageScore", averageScore);

		return averageScore;
	}

	public double getAverageSoS(Tournament t) {

		Double averageSos = getPlayerStatisticDouble("AverageSos");

		if(averageSos != null){
			return averageSos;
		}

		double sos = 0.0;
		List<Match> matches = getPlayer().getCompletedMatches(t);

		int numOpponents = 0;
		for (Match m : matches) {
			if (m.isBye() == false && m.getWinner() != null) {
				if (m.getPlayer1() == this.getPlayer()) {
					sos += ((XWingPlayer) m.getPlayer2().getModuleInfoByModule(t.getModule())).getAverageScore(t);
					numOpponents++;
				} else {
					sos += ((XWingPlayer) m.getPlayer1().getModuleInfoByModule(t.getModule())).getAverageScore(t);
					numOpponents++;
				}
			}
		}

		// if they don't have any opponents recorded yet, don't divide by 0
		averageSos = numOpponents>0 ? sos / numOpponents : 0;
		if (Double.isNaN(averageSos) != true) {
			BigDecimal bd = new BigDecimal(averageSos);
			bd = bd.setScale(2, RoundingMode.HALF_UP);
			averageSos = bd.doubleValue();
		}

		putPlayerStatisticDouble("AverageSos", averageSos);

		return averageSos;
	}

	public int getWins(Tournament t) {

		Integer score = getPlayerStatisticInteger("Wins");

		if(score != null){
			return score;
		}

		score = 0;
		for (Match match : getPlayer().getMatches(t)) {
			if (match.getWinner() == this.getPlayer() || match.isBye()) {
				score++;
			}
		}

		putPlayerStatisticInteger("Wins", score);

		return score;
	}

	public int getLosses(Tournament t) {

		Integer score = getPlayerStatisticInteger("Losses");

		if(score != null){
			return score;
		}

		score = 0;
		for (Match match : getPlayer().getMatches(t)) {
			if (match.getWinner() != null && match.getWinner() != this.getPlayer()) {
				score++;
			}
		}

		putPlayerStatisticInteger("Losses", score);

		return score;
	}



	public int getRank(Tournament t) {

		Integer rank = getPlayerStatisticInteger("Rank");

		if(rank != null){
			return rank;
		}

		rank = 0;

		List<Player> players = new ArrayList<Player>();
		players.addAll(t.getPlayers());
		Collections.sort(players, new XWingComparator(t, XWingComparator.rankingCompare));

		for (int i = 0; i < players.size(); i++) {
			if (((XWingTournament)t).getXWingPlayer(players.get(i)) == this) {
				rank = i + 1;
				break;
			}
		}

		putPlayerStatisticInteger("Rank", rank);

		return rank;
	}

	public int getEliminationRank(Tournament t) {

		Integer rank = getPlayerStatisticInteger("EliminationRank");

		if(rank != null){
			return rank;
		}

		rank = 0;

		for (Round r : t.getAllRounds()) {
			if (r.isSingleElimination()) {
				for (Match m : r.getMatches()) {
					if ((m.getPlayer1() == this.getPlayer() || m.getPlayer2() == this.getPlayer()) && (m.getWinner() != null && m.getWinner() != this.getPlayer())) {
						return r.getMatches().size() * 2;
					}

					if (r.getMatches().size() == 1 && m.getWinner() != null && m.getWinner() == this.getPlayer()) {
						return 1;
					}
				}
			}
		}

		putPlayerStatisticInteger("EliminationRank", rank);

		return rank;
	}

	public int getMarginOfVictory(Tournament t) {

		Integer movPoints = getPlayerStatisticInteger("MOV");

		if(movPoints != null){
			return movPoints;
		}

		int roundNumber = 0;

		movPoints = 0;

		for (Match match : getPlayer().getMatches(t)) {

			roundNumber++;

			Integer tournamentPoints = t.getRoundPoints(roundNumber);

			if (match.isBye()) {
				movPoints += tournamentPoints + (tournamentPoints / 2);
				continue;
			} else if (match.getWinner() == null) {
				continue;
			}

			boolean isPlayer1 = match.getPlayer1() == this.getPlayer();

			int player1Points = match.getPlayer1Points() == null ? 0 : match.getPlayer1Points();
			int player2Points = match.getPlayer2Points() == null ? 0 : match.getPlayer2Points();

			int diff = player1Points - player2Points;

			movPoints += isPlayer1 ? tournamentPoints + diff : tournamentPoints - diff;
		}

		putPlayerStatisticInteger("MOV", movPoints);

		return movPoints;
	}

	/**
	 * Returns true if the player has defeated every other person in their score group.
	 * 
	 * @param t
	 * @return
	 */
	public boolean isHeadToHeadWinner(Tournament t) {

		Integer h2hWinner = getPlayerStatisticInteger("H2H");

		if(h2hWinner != null){
			return h2hWinner == 1;
		}

		h2hWinner = 1;

		if (t != null) {
			int score = getScore(t);
			List<XWingPlayer> players = new ArrayList<XWingPlayer>();
			for (Player p : t.getPlayers()) {
				XWingPlayer xp = ((XWingTournament)t).getXWingPlayer(p);
				if (xp != this && xp.getScore(t) == score) {
					players.add(xp);
				}
			}

			if (players.isEmpty()) {
				h2hWinner = 0;
			} else {

				playerLoop: for (XWingPlayer p : players) {
					for (Match m : p.getPlayer().getMatches(t)) {
						if (m.getWinner() != null && m.getWinner() == this.getPlayer()) {
							continue playerLoop;
						}
					}
					h2hWinner = 0;
				}	
			}
		}

		putPlayerStatisticInteger("H2H", h2hWinner);

		return h2hWinner == 1;
	}

	public int getRoundDropped(Tournament t) {

		Integer roundDropped = getPlayerStatisticInteger("RoundDropped");

		if(roundDropped != null){
			return roundDropped;
		}

		roundDropped = 0;

		for (int i = t.getAllRounds().size(); i > 0; i--) {

			boolean found = false;
			Round r = t.getAllRounds().get(i - 1);
			for (Match m : r.getMatches()) {
				if (m.getPlayer1() == this.getPlayer()) {
					found = true;
					break;
				} else if (m.isBye() == false && m.getPlayer2() == this.getPlayer()) {
					found = true;
					break;
				}
			}

			if (found) {
				roundDropped = i + 1;
				break;
			}
		}

		putPlayerStatisticInteger("RoundDropped", roundDropped);

		return roundDropped;
	}

	public String getName() {
		return getPlayer().getName();
	}

	private Integer getPlayerStatisticInteger(String statName){
		
		Integer value = integerStatistics.get(statName);
		
		return value;
	}

	private void putPlayerStatisticInteger(String statName, Integer value){
		integerStatistics.put(statName, value);
	}

	private Double getPlayerStatisticDouble(String statName){
		Double value = doubleStatistics.get(statName);
		
		return value;
	}

	private void putPlayerStatisticDouble(String statName, Double value){
		doubleStatistics.put(statName, value);
	}
	
	private void clearCache(){
		integerStatistics.clear();
		doubleStatistics.clear();
	}

	@Override
	public String getModuleName() {
		return Modules.XWING.getName();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();

		appendXML(sb);

		return sb.toString();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		clearCache();
		
		XMLUtils.appendObject(sb, "MODULE", Modules.XWING.getName());
		XMLUtils.appendObject(sb, "SEEDVALUE", getSeedValue());
		XMLUtils.appendObject(sb, "SQUADID", getSquadId());
		XMLUtils.appendObject(sb, "FACTION", getFaction());

		return sb;
	}

	@Override
	public int compareTo(ModulePlayer arg0) {
		return this.getPlayer().getName().toUpperCase().compareTo(arg0.getPlayer().getName().toUpperCase());
	}
}
