package cryodex.modules.etc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import cryodex.Main;
import cryodex.Player;
import cryodex.modules.Match;

/**
 * Generate matches in order of ranking or as close to it as possible. This algorithm finds the best possible match set
 * that holds truest to the original ranking. Every permutation is calculated and given a score based on the distance
 * each player is from their true ranking. The permutation with the lowest score wins.
 * 
 * @author cbrown
 * 
 */
public class EtcOrderedMatchGeneration {

    private final EtcTournament tournament;
    private final List<Player> players;

    private Integer lowScore = null;
    private List<Match> matchSetAtLowScore = null;

    public EtcOrderedMatchGeneration(EtcTournament tournament, List<Player> players) {
        this.tournament = tournament;
        this.players = players;

    }

    public List<Match> generateMatches() {

        List<Player> tempList = new ArrayList<>();
        tempList.addAll(players);
        Collections.sort(tempList, new EtcComparator(tournament, EtcComparator.rankingCompare));

        generateMatch(new ArrayList<EtcMatch>(), tempList);

        // If no valid match set was found then we create the true ranking match
        // set and return it
        if (matchSetAtLowScore == null) {
            matchSetAtLowScore = new ArrayList<>();

            for (int counter = 0; counter < players.size(); counter += 2) {
                EtcMatch m = new EtcMatch(players.get(counter), players.get(counter + 1));
                m.checkDuplicate(tournament.getAllRounds());
                matchSetAtLowScore.add(m);
            }

        }

        if (lowScore != null && lowScore != 0) {
            JOptionPane.showMessageDialog(Main.getInstance(),
                    "Matches were modified to avoid duplicate pairings. Avoidance score = " + lowScore + " (2-10 is a minor change)");
        }

        List<Match> tempMatches = new ArrayList<Match>();
        tempMatches.addAll(matchSetAtLowScore);
        matchSetAtLowScore.clear();

        for (Match m : tempMatches) {
            for (int i = 1; i < tournament.getPlayerCount(); i++) {
                EtcMatch assocMatch = EtcMatch.copyMatch(m, String.valueOf(i));
                matchSetAtLowScore.add(assocMatch);
            }
            ((EtcMatch)m).setSuffix(tournament.getPlayerCount() + "");
            matchSetAtLowScore.add(m);
        }

        return matchSetAtLowScore;
    }

    private void generateMatch(List<EtcMatch> matches, List<Player> player1List) {

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

    private void getPlayer2(Player player1, List<EtcMatch> matches, List<Player> player2List) {
        for (Player player2 : player2List) {
            EtcMatch xm = new EtcMatch(player1, player2);
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

    private boolean shouldContinue(List<EtcMatch> matches) {
        if (lowScore == null) {
            return true;
        }

        int score = getScore(matches);

        System.out.println(score + " " + lowScore);

        return score < lowScore;
    }

    private void scorePermutation(List<EtcMatch> matches) {
        int score = getScore(matches);

        if (lowScore == null || score < lowScore) {
            matchSetAtLowScore = new ArrayList<>();
            matchSetAtLowScore.addAll(matches);

            lowScore = score;
        }
    }

    private int getScore(List<EtcMatch> matches) {

        // order players
        Collections.sort(players, new EtcComparator(tournament, EtcComparator.rankingCompare));

        // get list of players in order of matches
        List<Player> playerByMatchOrder = new ArrayList<Player>();
        for (EtcMatch xm : matches) {
            playerByMatchOrder.add(xm.getPlayer1());
            playerByMatchOrder.add(xm.getPlayer2());
        }

        int score = 0;

        for (int counter = 0; counter < playerByMatchOrder.size(); counter++) {
            int index = players.indexOf(playerByMatchOrder.get(counter));

            score += Math.abs(counter - index);
        }

        return score;
    }
}
