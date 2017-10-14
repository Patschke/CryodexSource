package cryodex.modules.imperialassault;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Match;
import cryodex.modules.ModulePlayer;
import cryodex.modules.Round;
import cryodex.modules.Tournament;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class IAPlayer implements Comparable<ModulePlayer>, XMLObject, ModulePlayer {

    public static enum Faction {
        IMPERIAL, REBEL, SCUM;
    }

    private Player player;
    private String seedValue;
    private String squadId;
    private Faction faction;

    public IAPlayer(Player p) {
        player = p;
        seedValue = String.valueOf(Math.random());
    }

    public IAPlayer(Player p, Element e) {
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
        int score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner() == this.getPlayer()) {
                score += IAConstants.WIN_POINTS;
            } else if (match.isBye()) {
                score += IAConstants.BYE_POINTS;
            } else {
                score += IAConstants.LOSS_POINTS;
            }
        }
        
        return score;
    }

    public double getAverageScore(Tournament t) {

        int score = getScore(t);
        int matchCount = getPlayer().getMatches(t).size();

        return score * 1.0 / matchCount;
    }

    public double getAverageSoS(Tournament t) {
        double sos = 0.0;
        List<Match> matches = getPlayer().getMatches(t);

        int numOpponents = 0;
        for (Match m : matches) {
            if (m.isBye() == false && m.getWinner() != null) {
                if (m.getPlayer1() == this.getPlayer()) {
                    sos += ((IAPlayer) m.getPlayer2().getModuleInfoByModule(t.getModule())).getAverageScore(t);
                    numOpponents++;
                } else {
                    sos += ((IAPlayer) m.getPlayer1().getModuleInfoByModule(t.getModule())).getAverageScore(t);
                    numOpponents++;
                }
            }
        }

		// if they don't have any opponents recorded yet, don't divide by 0
        double averageSos = numOpponents>0 ? sos / numOpponents : 0;
        if (Double.isNaN(averageSos) != true) {
            BigDecimal bd = new BigDecimal(averageSos);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
        return averageSos;
    }
    
    public double getExtendedStrengthOfSchedule(Tournament t) {
		double sos = 0;
		List<Match> matches = getPlayer().getMatches(t);

		int numOpponents = 0;
		for (Match m : matches) {
			if (m.isBye() == false & m.getWinner() != null) {
				if (m.getPlayer1() == this.getPlayer()) {
					sos += ((IATournament) t).getIAPlayer(m.getPlayer2()).getAverageSoS(t);
					numOpponents++;
				} else {
					sos += ((IATournament) t).getIAPlayer(m.getPlayer1()).getAverageSoS(t);
					numOpponents++;
				}
			}
		}

		// if they don't have any opponents recorded yet, don't divide by 0
		double averageSos = numOpponents>0 ? sos / numOpponents : 0;
        if (Double.isNaN(averageSos) != true) {
            BigDecimal bd = new BigDecimal(averageSos);
            bd = bd.setScale(3, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
        return averageSos;
	}

    public int getWins(Tournament t) {
        int score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner() == this.getPlayer() || match.isBye()) {
                score++;
            }
        }
        return score;
    }

    public int getLosses(Tournament t) {
        int score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner() != null && match.getWinner() != this.getPlayer()) {
                score++;
            }
        }
        return score;
    }

    public int getRank(Tournament t) {
        List<Player> players = new ArrayList<Player>();
        players.addAll(t.getPlayers());
        Collections.sort(players, new IAComparator(t, IAComparator.rankingCompare));

        for (int i = 0; i < players.size(); i++) {
            if (((IATournament) t).getIAPlayer(players.get(i)) == this) {
                return i + 1;
            }
        }

        return 0;
    }

    public int getEliminationRank(Tournament t) {

        int rank = 0;

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

        return rank;
    }

    public int getMarginOfVictory(Tournament t) {

        int roundNumber = 0;

        Integer movPoints = 0;

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
        return movPoints;
    }

    /**
     * Returns true if the player has defeated every other person in their score group.
     * 
     * @param t
     * @return
     */
    public boolean isHeadToHeadWinner(Tournament t) {

        if (t != null) {
            int score = getScore(t);
            List<IAPlayer> players = new ArrayList<IAPlayer>();
            for (Player p : t.getPlayers()) {
                IAPlayer xp = ((IATournament) t).getIAPlayer(p);
                if (xp != this && xp.getScore(t) == score) {
                    players.add(xp);
                }
            }

            if (players.isEmpty()) {
                return false;
            }

            playerLoop: for (IAPlayer p : players) {
                for (Match m : p.getPlayer().getMatches(t)) {
                    if (m.getWinner() != null && m.getWinner() == this.getPlayer()) {
                        continue playerLoop;
                    }
                }
                return false;
            }
        }

        return true;
    }

    public int getRoundDropped(Tournament t) {
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
                return i + 1;
            }
        }

        return 0;
    }

    public String getName() {
        return getPlayer().getName();
    }

    @Override
    public String getModuleName() {
        return Modules.IA.getName();
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();

        appendXML(sb);

        return sb.toString();
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {

        XMLUtils.appendObject(sb, "MODULE", Modules.IA.getName());
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
