package cryodex.modules;

import java.util.List;

import cryodex.CryodexController;
import cryodex.Player;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class Match implements XMLObject {

    public static enum GameResult {
        PLAYER_1_WINS, PLAYER_2_WINS, PLAYER_1_MOD_WINS, PLAYER_2_MOD_WINS, DRAW;
    }

    private Player player1;
    private Player player2;
    private GameResult game2Result;
    private GameResult game1Result;
    private Integer player1PointsDestroyed;
    private Integer player2PointsDestroyed;
    private boolean isDuplicate;
    private boolean isConcede;
    private String matchLabel;

    public Match() {

    }

    public Match(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public Match(Element matchElement) {

        String player1String = matchElement.getStringFromChild("PLAYER1");
        player1 = CryodexController.getPlayerByID(player1String);

        String player2String = matchElement.getStringFromChild("PLAYER2");
        player2 = CryodexController.getPlayerByID(player2String);

        isDuplicate = matchElement.getBooleanFromChild("ISDUPLICATE", false);
        isConcede = matchElement.getBooleanFromChild("ISCONCEDE", false);

        player1PointsDestroyed = matchElement.getIntegerFromChild("PLAYER1POINTS");
        player2PointsDestroyed = matchElement.getIntegerFromChild("PLAYER2POINTS");

        String game1ResultString = matchElement.getStringFromChild("GAME1RESULT");
        if (game1ResultString != null) {
            game1Result = GameResult.valueOf(game1ResultString);
        }

        String game2ResultString = matchElement.getStringFromChild("GAME2RESULT");
        if (game2ResultString != null) {
            game2Result = GameResult.valueOf(game2ResultString);
        }

        matchLabel = matchElement.getStringFromChild("MATCHLABEL");
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public boolean isBye() {
        return player2 == null;
    }

    public Integer getPlayer1Points() {
        return player1PointsDestroyed;
    }

    public void setPlayer1PointsDestroyed(Integer player1PointsDestroyed) {
        this.player1PointsDestroyed = player1PointsDestroyed;
    }

    public Integer getPlayer2Points() {
        return player2PointsDestroyed;
    }

    public void setPlayer2PointsDestroyed(Integer player2PointsDestroyed) {
        this.player2PointsDestroyed = player2PointsDestroyed;
    }

    public GameResult getGame1Result() {
        return game1Result;
    }

    public void setGame1Result(GameResult game1Result) {
        this.game1Result = game1Result;
    }

    public GameResult getGame2Result() {
        return game2Result;
    }

    public void setGame2Result(GameResult game2Result) {
        this.game2Result = game2Result;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public boolean isConcede() {
        return isConcede;
    }

    public void setConcede(boolean isConcede) {
        this.isConcede = isConcede;
    }
    
    public Player getWinner(int i){
        
        GameResult result = null;
        
        if(i == 1){
            result = getGame1Result();
        } else if(i == 2){
            result = getGame2Result();
        }
        
        if(result == GameResult.PLAYER_1_WINS || result == GameResult.PLAYER_1_MOD_WINS){
            return getPlayer1();
        } else if(result == GameResult.PLAYER_2_WINS || result == GameResult.PLAYER_2_MOD_WINS){
            return getPlayer2();
        }
        
        return null;
    }

    public void clear() {

        player1 = null;
        player2 = null;
        game2Result = null;
        game1Result = null;
        player1PointsDestroyed = null;
        player2PointsDestroyed = null;
        isDuplicate = false;
        isConcede = false;
        matchLabel = null;
    }

    public void checkDuplicate(List<Round> rounds) {

        if (this.getPlayer2() == null) {
            this.setDuplicate(false);
            return;
        }

        for (Round r : rounds) {
            if (r.isSingleElimination()) {
                continue;
            }

            for (Match match : r.getMatches()) {
                if (match.getPlayer2() == null || match == this) {
                    continue;
                }

                if ((match.getPlayer1() == this.getPlayer1() && match.getPlayer2() == this.getPlayer2())
                        || (match.getPlayer1() == this.getPlayer2() && match.getPlayer2() == this.getPlayer1())) {
                    this.setDuplicate(true);
                    return;
                }
            }
        }

        this.setDuplicate(false);
    }

    @Override
    public String toString() {
        return getPlayer1() + " vs " + getPlayer2();
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {

        XMLUtils.appendObject(sb, "PLAYER1", getPlayer1().getSaveId());
        XMLUtils.appendObject(sb, "PLAYER2", getPlayer2() == null ? "" : getPlayer2().getSaveId());
        // XMLUtils.appendObject(sb, "WINNER", getWinner() == null ? "" : getWinner().getSaveId());
        XMLUtils.appendObject(sb, "ISBYE", isBye());
        XMLUtils.appendObject(sb, "PLAYER1POINTS", getPlayer1Points());
        XMLUtils.appendObject(sb, "PLAYER2POINTS", getPlayer2Points());
        XMLUtils.appendObject(sb, "ISDUPLICATE", isDuplicate());
        XMLUtils.appendObject(sb, "ISCONCEDE", isConcede());
        XMLUtils.appendObject(sb, "GAME1RESULT", getGame1Result());
        XMLUtils.appendObject(sb, "GAME2RESULT", getGame2Result());
        XMLUtils.appendObject(sb, "MATCHLABEL", matchLabel);

        return sb;
    }

    public static boolean hasDuplicate(List<Match> matches) {
        boolean duplicateFound = false;
        for (Match dc : matches) {
            if (dc.isDuplicate()) {
                duplicateFound = true;
                break;
            }
        }

        return duplicateFound;
    }
}
