package cryodex.modules.battletech;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Match;
import cryodex.modules.ModulePlayer;
import cryodex.modules.Tournament;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class BTPlayer extends ModulePlayer {

    public static enum Faction {
    	IMPERIAL, REBEL, SCUM, RESISTANCE, FIRST_ORDER, SEPARATISTS, REPUBLIC;
    }

    private String squadId;
    private Faction faction;

    public BTPlayer(Player p) {
        super(p);
    }

    public BTPlayer(Player p, Element e) {
        super(p, e);
        this.squadId = e.getStringFromChild("SQUADID");
        String factionString = e.getStringFromChild("FACTION");

        if (factionString != null && factionString.isEmpty() == false) {
            faction = Faction.valueOf(factionString);
        } else {
            faction = Faction.IMPERIAL;
        }
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

    public int getScore(Tournament t) {

        Integer score = getPlayerStatisticInteger(t, "Score");

        if (score != null) {
            return score;
        }

        score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner(1) == this.getPlayer()) {
                score += BTConstants.WIN_POINTS;
            } else if (match.isBye()) {
                score += BTConstants.BYE_POINTS;
            } else {
                score += BTConstants.LOSS_POINTS;
            }
        }

        putPlayerStatisticInteger(t, "Score", score);

        return score;
    }
    
    public int getKillPoints(Tournament t) {
        Integer killPoints = getPlayerStatisticInteger(t, "KP");

        if (killPoints != null) {
            return killPoints;
        }

        killPoints = 0;

        for (Match match : getPlayer().getMatches(t)) {

            if (match.isBye()) {
                killPoints += 200;
                continue;
            } else if (match.getWinner(1) == null) {
                continue;
            }

            boolean isPlayer1 = match.getPlayer1() == this.getPlayer();

            int player1Points = match.getPlayer1Points() == null ? 0 : match.getPlayer1Points();
            int player2Points = match.getPlayer2Points() == null ? 0 : match.getPlayer2Points();

            if(isPlayer1){
            	killPoints += player1Points;
            } else {
            	killPoints += player2Points;
            }
        }

        putPlayerStatisticInteger(t, "KP", killPoints);

        return killPoints;
        
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
        return Modules.BETTLETECH.getName();
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {

        super.appendXML(sb);
        
        XMLUtils.appendObject(sb, "SQUADID", getSquadId());
        XMLUtils.appendObject(sb, "FACTION", getFaction());

        return sb;
    }

}
