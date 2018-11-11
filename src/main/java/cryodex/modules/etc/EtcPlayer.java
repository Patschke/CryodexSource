package cryodex.modules.etc;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.Match;
import cryodex.modules.ModulePlayer;
import cryodex.modules.Round;
import cryodex.modules.Tournament;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class EtcPlayer extends ModulePlayer {

    public static enum Faction {
        IMPERIAL, REBEL, SCUM;
    }

    private String squadId;
    private Faction faction;

    public EtcPlayer(Player p) {
        super(p);
    }

    public EtcPlayer(Player p, Element e) {
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

        for (int i = 0; i < t.getRoundCount(); i++) {
            score += getRoundScore(i, t);
        }

        putPlayerStatisticInteger(t, "Score", score);

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
//                    if (etcT.getPlayerCount() == 6) {
//                        score += 3;
//                    } else if (etcT.getPlayerCount() == 3) {
//                        score += 2;
//                    }
                       score += etcT.getPlayerCount();
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
            if (completeGameCount == 6) {
                score = 1;
            } else if (completeGameCount == 3) {
                score = 0;
            }

        }

        if (score == 6) {
            score = 5;
        }

        return score;
    }

    public int getMarginOfVictory(Tournament t) {

        Integer movPoints = getPlayerStatisticInteger(t, "MOV");

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
//                if (etcT.getPlayerCount() == 6) {
//                    movPoints += 600;
//                } else if (etcT.getPlayerCount() == 3) {
//                    movPoints += 150 + 150 + 50;
//                }
                movPoints += etcT.getPlayerCount() * 300;
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

    /**
     * Returns true if the player has defeated every other person in their score group.
     * 
     * @param t
     * @return
     */
    @Override
    public boolean isHeadToHeadWinner(Tournament t) {

        return false;
    }

    @Override
    public String getModuleName() {
        return Modules.ETC.getName();
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {

        super.appendXML(sb);

        XMLUtils.appendObject(sb, "SQUADID", getSquadId());
        XMLUtils.appendObject(sb, "FACTION", getFaction());

        return sb;
    }
}
