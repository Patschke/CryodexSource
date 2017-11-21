package cryodex.modules.l5r;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Match;
import cryodex.modules.Match.GameResult;
import cryodex.modules.ModulePlayer;
import cryodex.modules.Tournament;
import cryodex.xml.XMLUtils.Element;

public class L5RPlayer extends ModulePlayer {

    public L5RPlayer(Player p) {
        super(p);
    }

    public L5RPlayer(Player p, Element e) {
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
                score += L5RConstants.BYE_POINTS;
            }

            if (match.getPlayer1() == this.getPlayer()) {
                if (match.getGame1Result() == GameResult.PLAYER_1_WINS) {
                    score += L5RConstants.WIN_POINTS;
                } else if (match.getGame1Result() == GameResult.PLAYER_2_WINS) {
                    score += L5RConstants.LOSS_POINTS;
                } else if (match.getGame1Result() == GameResult.PLAYER_1_MOD_WINS) {
                    score += L5RConstants.MOD_WIN_POINTS;
                } else if (match.getGame1Result() == GameResult.PLAYER_2_MOD_WINS) {
                    score += L5RConstants.MOD_LOSS_POINTS;
                }
            } else {
                if (match.getGame1Result() == GameResult.PLAYER_1_WINS) {
                    score += L5RConstants.LOSS_POINTS;
                } else if (match.getGame1Result() == GameResult.PLAYER_2_WINS) {
                    score += L5RConstants.WIN_POINTS;
                } else if (match.getGame1Result() == GameResult.PLAYER_1_MOD_WINS) {
                    score += L5RConstants.MOD_LOSS_POINTS;
                } else if (match.getGame1Result() == GameResult.PLAYER_2_MOD_WINS) {
                    score += L5RConstants.MOD_WIN_POINTS;
                }
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

        int roundNumber = 0;

        movPoints = 0;

        for (Match match : getPlayer().getMatches(t)) {

            roundNumber++;

            Integer tournamentPoints = t.getRoundPoints(roundNumber);

            if (match.isBye()) {
                movPoints += tournamentPoints + (tournamentPoints / 2);
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

        putPlayerStatisticInteger(t, "MOV", movPoints);

        return movPoints;
    }

    @Override
    public String getModuleName() {
        return Modules.L5R.getName();
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {
        super.appendXML(sb);

        return sb;
    }
}
