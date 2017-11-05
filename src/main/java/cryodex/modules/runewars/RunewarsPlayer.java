package cryodex.modules.runewars;

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

public class RunewarsPlayer implements Comparable<ModulePlayer>, XMLObject, ModulePlayer {

	public static final int BYE_MOV = 70;
	public static final int MAX_MOV = 200;
	public static final int CONCEDE_MOV = 70;
	public static final int CONCEDE_WIN_SCORE = 8;
	public static final int CONCEDE_LOSE_SCORE = 0;

	/**
	 * This enum represents the table of match score to tournament points
	 */
	public enum ScoreTableEnum {
		THRESHOLD_6(0, 29, 6, 5), 
		THRESHOLD_7(30, 69, 7, 4), 
		THRESHOLD_8(70, 109, 8, 3), 
		THRESHOLD_9(110, 149, 9, 2), 
		THRESHOLD_10(150, 200, 10, 1);

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

			if (mov >= ScoreTableEnum.THRESHOLD_6.getLowerLimit()
					&& mov <= ScoreTableEnum.THRESHOLD_6.getUpperLimit()) {
				result = ScoreTableEnum.THRESHOLD_6;

			} else if (mov >= ScoreTableEnum.THRESHOLD_7.getLowerLimit()
					&& mov <= ScoreTableEnum.THRESHOLD_7.getUpperLimit()) {
				result = ScoreTableEnum.THRESHOLD_7;

			} else if (mov >= ScoreTableEnum.THRESHOLD_8.getLowerLimit()
					&& mov <= ScoreTableEnum.THRESHOLD_8.getUpperLimit()) {
				result = ScoreTableEnum.THRESHOLD_8;

			} else if (mov >= ScoreTableEnum.THRESHOLD_9.getLowerLimit()
					&& mov <= ScoreTableEnum.THRESHOLD_9.getUpperLimit()) {
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
	
    public static enum Faction {
        IMPERIAL, REBEL;
    }

    private Player player;
    private String seedValue;
    private String squadId;
    private Faction faction;

    public RunewarsPlayer(Player p) {
        player = p;
        seedValue = String.valueOf(Math.random());
    }

    public RunewarsPlayer(Player p, Element e) {
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
					if(match.isConcede()){
						matchScore = CONCEDE_LOSE_SCORE;
					} else {
						matchScore = ScoreTableEnum.getLoseScore(mov);	
					}
				}
				score += matchScore;
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
            if (m.isBye() == false && m.getWinner(1) != null) {
                if (m.getPlayer1() == this.getPlayer()) {
                    sos += ((RunewarsPlayer) m.getPlayer2().getModuleInfoByModule(t.getModule())).getAverageScore(t);
                    numOpponents++;
                } else {
                    sos += ((RunewarsPlayer) m.getPlayer1().getModuleInfoByModule(t.getModule())).getAverageScore(t);
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

    public int getWins(Tournament t) {
        int score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner(1) == this.getPlayer() || match.isBye()) {
                score++;
            }
        }
        return score;
    }

    public int getLosses(Tournament t) {
        int score = 0;
        for (Match match : getPlayer().getMatches(t)) {
            if (match.getWinner(1) != null && match.getWinner(1) != this.getPlayer()) {
                score++;
            }
        }
        return score;
    }

    public int getRank(Tournament t) {
        List<Player> players = new ArrayList<Player>();
        players.addAll(t.getPlayers());
        Collections.sort(players, new RunewarsComparator(t, RunewarsComparator.rankingCompare));

        for (int i = 0; i < players.size(); i++) {
            if (((RunewarsTournament) t).getModulePlayer(players.get(i)) == this) {
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
                    if ((m.getPlayer1() == this.getPlayer() || m.getPlayer2() == this.getPlayer()) && (m.getWinner(1) != null && m.getWinner(1) != this.getPlayer())) {
                        return r.getMatches().size() * 2;
                    }

                    if (r.getMatches().size() == 1 && m.getWinner(1) != null && m.getWinner(1) == this.getPlayer()) {
                        return 1;
                    }
                }
            }
        }

        return rank;
    }

    public int getMarginOfVictory(Tournament t) {

		Integer totalMov = 0;

		for (Match match : getPlayer().getMatches(t)) {
			totalMov += getMatchMOV(match);
		}
		return totalMov;
	}
    
    public int getMatchMOV(Match match){
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
    
    public int getWinnerMOV(Match match){
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

    /**
     * Returns true if the player has defeated every other person in their score group.
     * 
     * @param t
     * @return
     */
    public boolean isHeadToHeadWinner(Tournament t) {

        if (t != null) {
            int score = getScore(t);
            List<RunewarsPlayer> players = new ArrayList<RunewarsPlayer>();
            for (Player p : t.getPlayers()) {
                RunewarsPlayer xp = ((RunewarsTournament) t).getModulePlayer(p);
                if (xp != this && xp.getScore(t) == score) {
                    players.add(xp);
                }
            }

            if (players.isEmpty()) {
                return false;
            }

            playerLoop: for (RunewarsPlayer p : players) {
                for (Match m : p.getPlayer().getMatches(t)) {
                    if (m.getWinner(1) != null && m.getWinner(1) == this.getPlayer()) {
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
        return Modules.RUNEWARS.getName();
    }

    public String toXML() {
        StringBuilder sb = new StringBuilder();

        appendXML(sb);

        return sb.toString();
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {

        XMLUtils.appendObject(sb, "MODULE", Modules.RUNEWARS.getName());
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
