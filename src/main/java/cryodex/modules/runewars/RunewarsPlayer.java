package cryodex.modules.runewars;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Match;
import cryodex.modules.ModulePlayer;
import cryodex.modules.Tournament;
import cryodex.xml.XMLUtils.Element;

public class RunewarsPlayer extends ModulePlayer {

    public static final int BYE_MOV = 70;
    public static final int MAX_MOV = 200;
    public static final int CONCEDE_MOV = 70;
    public static final int CONCEDE_WIN_SCORE = 8;
    public static final int CONCEDE_LOSE_SCORE = 0;

    /**
     * This enum represents the table of match score to tournament points
     */
    public enum ScoreTableEnum {
        THRESHOLD_6(0, 29, 6, 5), THRESHOLD_7(30, 69, 7, 4), THRESHOLD_8(70, 109, 8, 3), THRESHOLD_9(110, 149, 9, 2), THRESHOLD_10(150, 200, 10, 1);

        private int lowerLimit;
        private int upperLimit;
        private int winScore;
        private int loseScore;

        private ScoreTableEnum(int lowerLimit, int upperLimit, int winScore, int loseScore) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
            this.winScore = winScore;
            this.loseScore = loseScore;
        }

        public int getLowerLimit() {
            return lowerLimit;
        }

        public int getUpperLimit() {
            return upperLimit;
        }

        public int getWinScore() {
            return winScore;
        }

        public int getLoseScore() {
            return loseScore;
        }

        private static ScoreTableEnum getResultByMOV(int mov) {
            ScoreTableEnum result = ScoreTableEnum.THRESHOLD_6;

            if (mov >= ScoreTableEnum.THRESHOLD_6.getLowerLimit() && mov <= ScoreTableEnum.THRESHOLD_6.getUpperLimit()) {
                result = ScoreTableEnum.THRESHOLD_6;

            } else if (mov >= ScoreTableEnum.THRESHOLD_7.getLowerLimit() && mov <= ScoreTableEnum.THRESHOLD_7.getUpperLimit()) {
                result = ScoreTableEnum.THRESHOLD_7;

            } else if (mov >= ScoreTableEnum.THRESHOLD_8.getLowerLimit() && mov <= ScoreTableEnum.THRESHOLD_8.getUpperLimit()) {
                result = ScoreTableEnum.THRESHOLD_8;

            } else if (mov >= ScoreTableEnum.THRESHOLD_9.getLowerLimit() && mov <= ScoreTableEnum.THRESHOLD_9.getUpperLimit()) {
                result = ScoreTableEnum.THRESHOLD_9;

            } else if (mov >= ScoreTableEnum.THRESHOLD_10.getLowerLimit()) {
                result = ScoreTableEnum.THRESHOLD_10;
            }

            return result;
        }

        public static int getWinScore(int mov) {
            return getResultByMOV(mov).getWinScore();
        }

        public static int getLoseScore(int mov) {
            return getResultByMOV(mov).getLoseScore();
        }
    }

    public RunewarsPlayer(Player p) {
        super(p);
    }

    public RunewarsPlayer(Player p, Element e) {
        super(p, e);
    }

    public int getScore(Tournament t) {
        Integer score = getPlayerStatisticInteger(t, "Score");

        if (score != null) {
            return score;
        }

        score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.isBye()) {
                score += 8;
            } else {

                if (match.getWinner(1) == null) {
                    continue;
                }

                int mov = getWinnerMOV(match);

                int matchScore = 0;
                if (match.getWinner(1) == this.getPlayer()) {
                    matchScore = ScoreTableEnum.getWinScore(mov);
                } else {
                    if (match.isConcede()) {
                        matchScore = CONCEDE_LOSE_SCORE;
                    } else {
                        matchScore = ScoreTableEnum.getLoseScore(mov);
                    }
                }
                score += matchScore;
            }
        }

        putPlayerStatisticInteger(t, "Score", score);

        return score;
    }

    public int getMarginOfVictory(Tournament t) {

        Integer movPoints = getPlayerStatisticInteger(t, "MOV");

        if (movPoints != null) {
            return movPoints;
        }

        movPoints = 0;

        for (Match match : getPlayer().getMatches(t)) {
            movPoints += getMatchMOV(match);
        }

        putPlayerStatisticInteger(t, "MOV", movPoints);

        return movPoints;
    }

    public int getMatchMOV(Match match) {
        if (match.isBye()) {
            return BYE_MOV;

        } else if (match.getWinner(1) == null) {
            return 0;
        }

        if (match.getWinner(1) == this.getPlayer()) {
            return getWinnerMOV(match);
        }

        return 0;
    }

    public int getWinnerMOV(Match match) {
        int mov = 0;
        try {

            int player1Score = match.getPlayer1Points() == null ? 0 : match.getPlayer1Points();
            int player2Score = match.getPlayer2Points() == null ? 0 : match.getPlayer2Points();

            if (match.getPlayer1() == match.getWinner(1)) {
                mov = player1Score - player2Score;
            } else {
                mov = player2Score - player1Score;
            }
        } catch (Exception e) {
        }

        if (match.isConcede() && mov < CONCEDE_MOV) {
            mov = CONCEDE_MOV;
        }

        // Check to see if MOV is outside the min/max
        mov = mov < 0 ? 0 : mov;
        mov = mov > MAX_MOV ? MAX_MOV : mov;

        return mov;
    }

    @Override
    public String getModuleName() {
        return Modules.RUNEWARS.getName();
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {
        super.appendXML(sb);

        return sb;
    }
}
