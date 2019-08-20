package cryodex.modules;

public interface MatchValidator {
	
	boolean isMatchValid(Tournament t, Match m);
}
