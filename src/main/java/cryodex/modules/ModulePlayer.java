package cryodex.modules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cryodex.Player;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

/**
 * Contains the generic functions that exist in all standard game types. Also contains a value cache to decrease
 * expensive and redundant calculations.
 */
public abstract class ModulePlayer implements Comparable<ModulePlayer>, XMLObject {

    private Map<String, Integer> integerStatistics = new HashMap<String, Integer>();
    private Map<String, Double> doubleStatistics = new HashMap<String, Double>();

    private Player player;
    private String seedValue;

    /**
     * Standard constructor for a new player. Creates a new random seed value.
     * 
     * @param player
     */
    public ModulePlayer(Player player) {
        this.player = player;
        seedValue = String.valueOf(Math.random());
    }

    /**
     * Constructor for loading a player from an XML save.
     * 
     * @param player
     * @param e
     */
    public ModulePlayer(Player player, Element e) {
        this.player = player;

        this.seedValue = e.getStringFromChild("SEEDVALUE");
    }

    /**
     * Get random seed value for player for this game type.
     * 
     * @return randomly generated seed number as a string
     */
    public String getSeedValue() {
        return seedValue;
    }

    /**
     * Return the cached integer value for the statName in the given tournament.
     * 
     * @param tournament
     *            - which event to pull the value from
     * @param statName
     *            - which value to pull
     * @return integer value if it has been cached. Otherwise null.
     */
    protected Integer getPlayerStatisticInteger(Tournament tournament, String statName) {

        Integer value = integerStatistics.get(tournament.getName() + statName);

        return value;
    }

    /**
     * Save an integer value of the statName for the given tournament which can be retrieved later using the
     * getPlayerStatisticInteger function
     * 
     * @param tournament
     *            - which even this value is being saved for
     * @param statName
     *            - which value is being saved
     * @param value
     *            - integer value to be saved
     */
    protected void putPlayerStatisticInteger(Tournament tournament, String statName, Integer value) {
        integerStatistics.put(tournament.getName() + statName, value);
    }

    /**
     * Return the cached double value for the statName in the given tournament.
     * 
     * @param tournament
     *            - which event to pull the value from
     * @param statName
     *            - which value to pull
     * @return double value if it has been cached. Otherwise null.
     */
    protected Double getPlayerStatisticDouble(Tournament tournament, String statName) {
        Double value = doubleStatistics.get(tournament.getName() + statName);

        return value;
    }

    /**
     * Save an double value of the statName for the given tournament which can be retrieved later using the
     * getPlayerStatisticDouble function
     * 
     * @param tournament
     *            - which even this value is being saved for
     * @param statName
     *            - which value is being saved
     * @param value
     *            - double value to be saved
     */
    protected void putPlayerStatisticDouble(Tournament tournament, String statName, Double value) {
        doubleStatistics.put(tournament.getName() + statName, value);
    }

    /**
     * Removes all values from the cache to force recalculation.
     */
    public void clearCache() {
        integerStatistics.clear();
        doubleStatistics.clear();
    }

    /**
     * @return the generic Player object associated with this ModulePlayer
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the player name
     */
    public String getName() {
        return getPlayer().getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ModulePlayer arg0) {
        return this.getPlayer().getName().toUpperCase().compareTo(arg0.getPlayer().getName().toUpperCase());
    }

    /**
     * This function calculates the average strength of schedule for a player in the given tournament.
     * 
     * Average strength of schedule is calculated by adding together the average score of each opponent then dividing
     * that by the number of total matches.
     * 
     * The value is returned as a double value rounded to the second decimal point.
     * 
     * @param t
     *            - tournament to calculate from
     * @return - double value of the average strength of schedule. Will return a '0' if there are no matches.
     */
    public double getAverageSoS(Tournament t) {

        Double averageSos = getPlayerStatisticDouble(t, "AverageSos");

        if (averageSos != null) {
            return averageSos;
        }

        double sos = 0.0;
        List<Match> matches = getPlayer().getCompletedMatches(t);

        int numOpponents = 0;
        for (Match m : matches) {
            if (m.isBye() == false && m.getWinner(1) != null) {
                if (m.getPlayer1() == this.getPlayer()) {
                    sos += t.getModulePlayer(m.getPlayer2()).getAverageScore(t);
                    numOpponents++;
                } else {
                    sos += t.getModulePlayer(m.getPlayer1()).getAverageScore(t);
                    numOpponents++;
                }
            }
        }

        // if they don't have any opponents recorded yet, don't divide by 0
        averageSos = numOpponents > 0 ? sos / numOpponents : 0;
        if (Double.isNaN(averageSos) != true) {
            BigDecimal bd = new BigDecimal(averageSos);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            averageSos = bd.doubleValue();
        }

        putPlayerStatisticDouble(t, "AverageSos", averageSos);

        return averageSos;
    }

    /**
     * Returns the average score for a player in a given tournament.
     * 
     * Average score is calculated by dividing the total player score by the number of played matches.
     * 
     * @param t
     *            - tournament to calculate from
     * @return the average score as a double value
     */
    public double getAverageScore(Tournament t) {

        Double averageScore = getPlayerStatisticDouble(t, "AverageScore");

        if (averageScore != null) {
            return averageScore;
        }

        int score = getScore(t);
        int matchCount = getPlayer().getCompletedMatches(t).size();

        averageScore = score * 1.0 / matchCount;

        putPlayerStatisticDouble(t, "AverageScore", averageScore);

        return averageScore;
    }

    /**
     * Calculates the extended strength of schedule for the player in the given tournament.
     * 
     * Extended strength of schedule is calculated by totaling the average strength of schedule for each opponent and
     * divide by the number of opponents.
     * 
     * @param t
     *            - tournament to calculate from
     * @return Extended Strength Of Schedule as a double
     */
    public double getExtendedStrengthOfSchedule(Tournament t) {
        double sos = 0;
        List<Match> matches = getPlayer().getMatches(t);

        int numOpponents = 0;
        for (Match m : matches) {
            if (m.isBye() == false & m.getWinner(1) != null) {
                if (m.getPlayer1() == this.getPlayer()) {
                    sos += t.getModulePlayer(m.getPlayer2()).getAverageSoS(t);
                    numOpponents++;
                } else {
                    sos += t.getModulePlayer(m.getPlayer1()).getAverageSoS(t);
                    numOpponents++;
                }
            }
        }

        // if they don't have any opponents recorded yet, don't divide by 0
        double averageSos = numOpponents > 0 ? sos / numOpponents : 0;
        if (Double.isNaN(averageSos) != true) {
            BigDecimal bd = new BigDecimal(averageSos);
            bd = bd.setScale(3, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
        return averageSos;
    }

    /**
     * Returns the number of wins for a player in a given tournament.
     * 
     * @param t
     *            - tournament to calculate from
     * @return number of wins as an integer
     */
    public int getWins(Tournament t) {

        Integer score = getPlayerStatisticInteger(t, "Wins");

        if (score != null) {
            return score;
        }

        score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner(1) == this.getPlayer() || match.isBye()) {
                score++;
            }
        }

        putPlayerStatisticInteger(t, "Wins", score);

        return score;
    }

    /**
     * Returns the number of losses for a player in a given tournament.
     * 
     * @param t
     *            - tournament to calculate from
     * @return number of losses as an integer
     */
    public int getLosses(Tournament t) {

        Integer score = getPlayerStatisticInteger(t, "Losses");

        if (score != null) {
            return score;
        }

        score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner(1) != null && match.getWinner(1) != this.getPlayer()) {
                score++;
            }
        }

        putPlayerStatisticInteger(t, "Losses", score);

        return score;
    }

    /**
     * Calculates the round a player dropped from the given tournament. The value returned is the first round the player
     * does not exist in. For example, if the player had a match in rounds 1, 2, and 3, the function would return a 4.
     * 
     * @param t
     *            - tournament to calculate from
     * @return the first round a player did not play as an integer
     */
    public int getRoundDropped(Tournament t) {

        Integer roundDropped = getPlayerStatisticInteger(t, "RoundDropped");

        if (roundDropped != null) {
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

        putPlayerStatisticInteger(t, "RoundDropped", roundDropped);

        return roundDropped;
    }

    /**
     * Returns the player rank based on the event's ranking comparator
     * 
     * @param t
     *            - the tournament to calculate from
     * @return the player rank as an integer
     */
    public int getRank(Tournament t) {

        Integer rank = getPlayerStatisticInteger(t, "Rank");

        if (rank != null) {
            return rank;
        }

        rank = 0;

        List<Player> players = new ArrayList<Player>();
        players.addAll(t.getPlayers());
        Collections.sort(players, t.getRankingComparator());

        for (int i = 0; i < players.size(); i++) {
            if (t.getModulePlayer(players.get(i)) == this) {
                rank = i + 1;
                break;
            }
        }

        putPlayerStatisticInteger(t, "Rank", rank);

        return rank;
    }

    /**
     * Returns the rank of a player in elimination rounds. For example, if the player made it to the top 4, a 4 would be
     * returned. If the player did not make it to elimination rounds, a 0 will be returned.
     * 
     * @param t
     *            - tournament to calculate from
     * @return the elimination rank as an integer. Returns a 0 if no elimination rank.
     */
    public int getEliminationRank(Tournament t) {

        Integer rank = getPlayerStatisticInteger(t, "EliminationRank");

        if (rank != null) {
            return rank;
        }

        rank = 0;

        for (Round r : t.getAllRounds()) {
            if (r.isSingleElimination()) {
                for (Match m : r.getMatches()) {
                    if ((m.getPlayer1() == this.getPlayer() || m.getPlayer2() == this.getPlayer())
                            && (m.getWinner(1) != null && m.getWinner(1) != this.getPlayer())) {
                        return r.getMatches().size() * 2;
                    }

                    if (r.getMatches().size() == 1 && m.getWinner(1) != null && m.getWinner(1) == this.getPlayer()) {
                        return 1;
                    }
                }
            }
        }

        putPlayerStatisticInteger(t, "EliminationRank", rank);

        return rank;
    }

    /**
     * Returns true if the player has defeated every other person in their score group.
     * 
     * @param t
     *            - tournament to calculate from
     * @return true if the player defeated every other player in their score group. False otherwise
     */
    public boolean isHeadToHeadWinner(Tournament t) {

        Integer h2hWinner = getPlayerStatisticInteger(t, "H2H");

        if (h2hWinner != null) {
            return h2hWinner == 1;
        }

        h2hWinner = 1;

        if (t != null) {
            int score = getScore(t);
            List<ModulePlayer> players = new ArrayList<ModulePlayer>();
            for (Player p : t.getPlayers()) {
                ModulePlayer xp = t.getModulePlayer(p);
                if (xp != this && xp.getScore(t) == score) {
                    players.add(xp);
                }
            }

            if (players.isEmpty()) {
                h2hWinner = 0;
            } else {

                playerLoop: for (ModulePlayer p : players) {
                    for (Match m : p.getPlayer().getMatches(t)) {
                        if (m.getWinner(1) != null && m.getWinner(1) == this.getPlayer()) {
                            continue playerLoop;
                        }
                    }
                    h2hWinner = 0;
                }
            }
        }

        putPlayerStatisticInteger(t, "H2H", h2hWinner);

        return h2hWinner == 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getPlayer().getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see cryodex.xml.XMLObject#appendXML(java.lang.StringBuilder)
     */
    @Override
    public StringBuilder appendXML(StringBuilder sb) {

        clearCache();

        XMLUtils.appendObject(sb, "SEEDVALUE", getSeedValue());
        XMLUtils.appendObject(sb, "MODULE", getModuleName());

        return sb;
    }

    /**
     * @return the module name for the event this player is from
     */
    public abstract String getModuleName();

    /**
     * This function calculates the score for a player in a given tournament type
     * 
     * @param t
     *            - tournament to calculate from
     * @return the score as an integer value
     */
    public abstract int getScore(Tournament t);
}
