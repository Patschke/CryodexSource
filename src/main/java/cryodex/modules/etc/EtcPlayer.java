package cryodex.modules.etc;

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

public class EtcPlayer implements Comparable<ModulePlayer>, XMLObject, ModulePlayer {

    public static enum Faction {
        IMPERIAL, REBEL, SCUM;
    }

    private Player player;
    private String seedValue;
    private String squadId;
    private Faction faction;

    private Map<String, Integer> integerStatistics = new HashMap<String, Integer>();
    private Map<String, Double> doubleStatistics = new HashMap<String, Double>();

    public EtcPlayer(Player p) {
        player = p;
        seedValue = String.valueOf(Math.random());
    }

    public EtcPlayer(Player p, Element e) {
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

        Integer score = getPlayerStatisticInteger(t,"Score");

        if (score != null) {
            return score;
        }

        score = 0;

        for (int i = 0; i < t.getRoundCount(); i++) {
            score += getRoundScore(i, t);
        }

        putPlayerStatisticInteger(t,"Score", score);

        return score;
    }

    public int getRoundScore(int zeroBasedRound, Tournament t) {

        EtcTournament etcT = (EtcTournament) t;

        Round r = t.getRound(zeroBasedRound);

        int score = 0;
        int completeGameCount = 0;

        for (Match m : r.getMatches()) {
            if (this.getPlayer().equals(m.getPlayer1()) || this.getPlayer().equals(m.getPlayer2())) {
                if (m.isBye()) {
                    if (etcT.getPlayerCount() == 6) {
                        score += 3;
                    } else if (etcT.getPlayerCount() == 3) {
                        score += 2;
                    }
                    continue;
                } else if (m.getWinner(1) != null) {
                    completeGameCount++;
                    if (this.getPlayer().equals(m.getWinner(1))) {
                        score++;
                    }
                }
            }
        }

        if (score == 0 && completeGameCount != 0) {
            score = 1;
        }

        if (score == 6) {
            score = 5;
        }

        return score;
    }

    public double getAverageScore(Tournament t) {

        Double averageScore = getPlayerStatisticDouble(t,"AverageScore");

        if (averageScore != null) {
            return averageScore;
        }

        int score = getScore(t);
        int matchCount = getPlayer().getCompletedMatches(t).size();

        averageScore = score * 1.0 / matchCount;

        putPlayerStatisticDouble(t,"AverageScore", averageScore);

        return averageScore;
    }

    public double getAverageSoS(Tournament t) {

        Double averageSos = getPlayerStatisticDouble(t,"AverageSos");

        if (averageSos != null) {
            return averageSos;
        }

        double sos = 0.0;
        List<Match> matches = getPlayer().getCompletedMatches(t);

        int numOpponents = 0;
        for (Match m : matches) {
            if (m.isBye() == false && m.getWinner(1) != null) {
                if (m.getPlayer1() == this.getPlayer()) {
                    sos += ((EtcPlayer) m.getPlayer2().getModuleInfoByModule(t.getModule())).getAverageScore(t);
                    numOpponents++;
                } else {
                    sos += ((EtcPlayer) m.getPlayer1().getModuleInfoByModule(t.getModule())).getAverageScore(t);
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

        putPlayerStatisticDouble(t,"AverageSos", averageSos);

        return averageSos;
    }

    public int getWins(Tournament t) {

        Integer score = getPlayerStatisticInteger(t,"Wins");

        if (score != null) {
            return score;
        }

        score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner(1) == this.getPlayer() || match.isBye()) {
                score++;
            }
        }

        putPlayerStatisticInteger(t,"Wins", score);

        return score;
    }

    public int getLosses(Tournament t) {

        Integer score = getPlayerStatisticInteger(t,"Losses");

        if (score != null) {
            return score;
        }

        score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner(1) != null && match.getWinner(1) != this.getPlayer()) {
                score++;
            }
        }

        putPlayerStatisticInteger(t,"Losses", score);

        return score;
    }

    public int getRank(Tournament t) {

        Integer rank = getPlayerStatisticInteger(t,"Rank");

        if (rank != null) {
            return rank;
        }

        rank = 0;

        List<Player> players = new ArrayList<Player>();
        players.addAll(t.getPlayers());
        Collections.sort(players, new EtcComparator(t, EtcComparator.rankingCompare));

        for (int i = 0; i < players.size(); i++) {
            if (((EtcTournament) t).getModulePlayer(players.get(i)) == this) {
                rank = i + 1;
                break;
            }
        }

        putPlayerStatisticInteger(t,"Rank", rank);

        return rank;
    }

    public int getEliminationRank(Tournament t) {

        Integer rank = getPlayerStatisticInteger(t,"EliminationRank");

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

        putPlayerStatisticInteger(t,"EliminationRank", rank);

        return rank;
    }

    public int getMarginOfVictory(Tournament t) {

        Integer movPoints = getPlayerStatisticInteger(t,"MOV");
        
        if (movPoints != null) {
            return movPoints;
        }
        
        EtcTournament etcT = (EtcTournament) t;

        int roundNumber = 0;

        movPoints = 0;

        for (Match match : getPlayer().getMatches(t)) {

            roundNumber++;

            Integer tournamentPoints = t.getRoundPoints(roundNumber);

            if (match.isBye()) {
                if (etcT.getPlayerCount() == 6) {
                    movPoints += 600;
                } else if (etcT.getPlayerCount() == 3) {
                    movPoints += 150 + 150 + 50;
                }
                continue;
            } else if (match.getWinner(1) == null) {
                continue;
            }

            boolean isPlayer1 = match.getPlayer1() == this.getPlayer();

            int player1Points = match.getPlayer1Points() == null ? 0 : match.getPlayer1Points();
            int player2Points = match.getPlayer2Points() == null ? 0 : match.getPlayer2Points();

            int diff = player1Points - player2Points;

            movPoints += isPlayer1 ? tournamentPoints + diff : tournamentPoints - diff;
        }

        putPlayerStatisticInteger(t,"MOV", movPoints);

        return movPoints;
    }

    /**
     * Returns true if the player has defeated every other person in their score group.
     * 
     * @param t
     * @return
     */
    public boolean isHeadToHeadWinner(Tournament t) {

        return false;
    }

    public int getRoundDropped(Tournament t) {

        Integer roundDropped = getPlayerStatisticInteger(t,"RoundDropped");

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

        putPlayerStatisticInteger(t,"RoundDropped", roundDropped);

        return roundDropped;
    }

    public String getName() {
        return getPlayer().getName();
    }

    private Integer getPlayerStatisticInteger(Tournament tournament, String statName) {

        Integer value = integerStatistics.get(tournament.getName() + statName);

        return value;
    }

    private void putPlayerStatisticInteger(Tournament tournament, String statName, Integer value) {
        integerStatistics.put(tournament.getName() + statName, value);
    }

    private Double getPlayerStatisticDouble(Tournament tournament, String statName) {
        Double value = doubleStatistics.get(tournament.getName() + statName);

        return value;
    }

    private void putPlayerStatisticDouble(Tournament tournament, String statName, Double value) {
        doubleStatistics.put(tournament.getName() + statName, value);
    }

    private void clearCache() {
        integerStatistics.clear();
        doubleStatistics.clear();
    }

    @Override
    public String getModuleName() {
        return Modules.ETC.getName();
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();

        appendXML(sb);

        return sb.toString();
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {

        clearCache();

        XMLUtils.appendObject(sb, "MODULE", Modules.ETC.getName());
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