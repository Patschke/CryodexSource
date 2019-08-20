package cryodex.modules.xwing;

import java.util.ArrayList;
import java.util.List;

import cryodex.Player;
import cryodex.CryodexController.Modules;
import cryodex.modules.Match;
import cryodex.modules.MatchValidator;
import cryodex.modules.Tournament;

public class XWingMatchValidator implements MatchValidator {

	@Override
	public boolean isMatchValid(Tournament t, Match m) {
		return validateMatchPoints(m);
	}

	private boolean validateMatchPoints(Match m) {
		
		boolean result = true;
		
		if(m.getPlayer1() != null && m.getPlayer2() != null){
			if(m.getPlayer1Points() != null){
				XWingPlayer p = (XWingPlayer) m.getPlayer2().getModuleInfoByModule(Modules.XWING.getModule());
				List<Integer> ships = p.getShips();
				result = result && calculateScore(ships, m.getPlayer1Points());
			}
			if(m.getPlayer2Points() != null){
				XWingPlayer p = (XWingPlayer) m.getPlayer1().getModuleInfoByModule(Modules.XWING.getModule());
				List<Integer> ships = p.getShips();
				result = result && calculateScore(ships, m.getPlayer2Points());
			}
		}
		
		return result;
	}

	public static boolean calculateScore(List<Integer> ships, int expectedScore) {

		if (ships.isEmpty()) {
			return expectedScore == 0;
		}

		// Scoring all or none is always a valid score
		if (expectedScore == 0 || expectedScore == 200){
			return true;
		}
		
		List<Integer> newShipList = new ArrayList<Integer>();
		newShipList.addAll(ships);
		newShipList.remove(0);

		int shipValue = ships.get(0);
		int shipHalfValue = ships.get(0) / 2;

		// round up on half points
		if (shipHalfValue * 2 != shipValue) {
			shipHalfValue++;
		}

		if (expectedScore == 0) {
			System.out.println("Already zero, shouldn't have gotten here");
			return true;
		}

		if (expectedScore - shipValue == 0) {
			System.out.println("Full ship destroyed - " + shipValue + " of " + shipValue);
			return true;
		}

		if (expectedScore - shipHalfValue == 0) {
			System.out.println("Half ship destroyed - " + shipHalfValue + " of " + shipValue);
			return true;
		}

		if (calculateScore(newShipList, expectedScore)) {
			System.out.println("Ship not destroyed");
			return true;
		} else if (calculateScore(newShipList, expectedScore - shipHalfValue)) {
			System.out.println("Half ship destroyed - " + shipHalfValue + " of " + shipValue);
			return true;
		} else if (calculateScore(newShipList, expectedScore - shipValue)) {
			System.out.println("Full ship destroyed - " + shipValue + " of " + shipValue);
			return true;
		} else {
			return false;
		}
	}
}
