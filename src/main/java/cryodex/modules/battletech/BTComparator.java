package cryodex.modules.battletech;

import cryodex.Player;
import cryodex.modules.Tournament;
import cryodex.modules.TournamentComparator;

public class BTComparator extends TournamentComparator<Player> {

	public static enum CompareOptions {
		HEAD_TO_HEAD, KILL_POINTS, STRENGH_OF_SCHEDULE, AVERAGE_STRENGTH_OF_SCHEDULE, SCORE, RANDOM, NAME;
	}

	public static final CompareOptions[] uniqueCompare = { CompareOptions.NAME };
	public static final CompareOptions[] pairingCompare = {
			CompareOptions.SCORE, CompareOptions.KILL_POINTS};
	public static final CompareOptions[] rankingCompare = {
			CompareOptions.SCORE,
			CompareOptions.KILL_POINTS,
			CompareOptions.AVERAGE_STRENGTH_OF_SCHEDULE, CompareOptions.RANDOM };
	public static final CompareOptions[] rankingCompareNoHeadToHead = {
			CompareOptions.SCORE, CompareOptions.KILL_POINTS,
			CompareOptions.AVERAGE_STRENGTH_OF_SCHEDULE, CompareOptions.RANDOM };

	private final Tournament t;
	private final CompareOptions[] sortOrder;

	public BTComparator(Tournament t, CompareOptions[] sortOrder) {
		this.t = t;
		this.sortOrder = sortOrder;
	}

	@Override
	public int compare(Player o1, Player o2) {

		int result = 0;

		for (CompareOptions option : sortOrder) {
			if (result == 0) {
				result = compareOption(o1, o2, option);
			}
		}

		return result;
	}

	private int compareOption(Player p1, Player p2,
			CompareOptions option) {

		int result = 0;

		BTPlayer o1 = (BTPlayer) t.getModulePlayer(p1);
		BTPlayer o2 = (BTPlayer) t.getModulePlayer(p2);
		
		switch (option) {
		case SCORE:
			result = compareInt(o1.getScore(t), o2.getScore(t));
			break;
		case HEAD_TO_HEAD:
			if (o1.getName().equals(o2.getName())) {
				return 0;
			}
			result = o1.isHeadToHeadWinner(t) ? 1 : 0;
			if(result == 0){
				result = o2.isHeadToHeadWinner(t) ? -1 : 0;
			}
			break;
		case STRENGH_OF_SCHEDULE:
			// Not implemented
			break;
		case AVERAGE_STRENGTH_OF_SCHEDULE:
			result = compareDouble(o1.getAverageSoS(t), o2.getAverageSoS(t));
			break;
		case KILL_POINTS:
			result = compareInt(o1.getKillPoints(t),o2.getKillPoints(t));
			break;
		case RANDOM:
			String seedValue1 = o1.getSeedValue();
			String seedValue2 = o2.getSeedValue();

			try {
				Double d1 = Double.valueOf(seedValue1);
				Double d2 = Double.valueOf(seedValue2);

				result = d1.compareTo(d2);
			} catch (NumberFormatException e) {
				result = seedValue1.compareTo(seedValue2);
			}
			break;
		case NAME:
			result = o1.getName().compareTo(o2.getName());
			break;
		}

		return result;
	}
}