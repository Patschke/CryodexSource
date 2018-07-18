package cryodex.modules.legion;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Match;
import cryodex.modules.ModulePlayer;
import cryodex.modules.Tournament;
import cryodex.xml.XMLUtils.Element;

public class LegionPlayer extends ModulePlayer {

    public LegionPlayer(Player p) {
        super(p);
    }

    public LegionPlayer(Player p, Element e) {
        super(p, e);
    }

    public int getScore(Tournament t) {
        Integer score = getPlayerStatisticInteger(t, "Score");

        if (score != null) {
            return score;
        }

        score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner(1) == this.getPlayer()) {
                score += LegionConstants.WIN_POINTS;
            } else if (match.isBye()) {
                score += LegionConstants.BYE_POINTS;
            } else {
                score += LegionConstants.LOSS_POINTS;
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
        return Modules.LEGION.getName();
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();

        appendXML(sb);

        return sb.toString();
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {

        super.appendXML(sb);

        return sb;
    }
}
