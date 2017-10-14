package cryodex.modules;

import java.util.ArrayList;
import java.util.List;

import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class Round implements XMLObject {
	private List<Match> matches;
	private RoundPanel panel;
	private Boolean isSingleElimination = false;

	public Round() {

	}

	public Round(Element roundElement, Tournament t) {
		this.isSingleElimination = roundElement.getBooleanFromChild("ISSINGLEELIMINATION");

		Element matchElement = roundElement.getChild("MATCHES");

		if (matchElement != null) {
			matches = new ArrayList<Match>();
			for (Element e : matchElement.getChildren()) {
				matches.add(new Match(e));
			}
		}

		this.panel = t.getRoundPanel(matches);
	}

	public Round(List<Match> matches, Tournament t) {
		this.matches = matches;
		this.panel = t.getRoundPanel(matches);
	}

	public List<Match> getMatches() {
		return matches;
	}

	public void setMatches(List<Match> matches) {
		this.matches = matches;
	}

	public RoundPanel getPanel() {
		return panel;
	}

	public void setPanel(RoundPanel panel) {
		this.panel = panel;
	}

	public void setSingleElimination(boolean isSingleElimination) {
		this.isSingleElimination = isSingleElimination;
	}

	public boolean isSingleElimination() {
		return isSingleElimination == null ? false : isSingleElimination;
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "ISSINGLEELIMINATION", isSingleElimination());
		XMLUtils.appendList(sb, "MATCHES", "MATCH", getMatches());

		return sb;
	}

	/**
	 * This function loops through each match and determines if the match is
	 * complete or not. It then returns a list of the incomplete matches to be
	 * displayed to the user. If all matches are complete, it will return a null
	 * string.
	 * 
	 * @param tournament
	 * @return a String of incomplete matches. Null if none.
	 */
	public String isComplete(Tournament tournament) {
		String result = null;
		for (Match m : getMatches()) {
			if (tournament.isMatchComplete(m) == false) {
				if (result == null) {
					result = m.toString();
				} else {
					result += ", " + m.toString();
				}
			}
		}
		return result;
	}


	/**
	 * This function loops through each match and determines if the match has
	 * a valid result. It then returns a list of the invalid matches to be
	 * displayed to the user. If all matches are valid, it will return a null
	 * string.
	 * 
	 * @param tournament
	 * @return a String of invalid matches. Null if none.
	 */
	public String isValid(Tournament tournament) {
		String result = null;
		for (Match m : getMatches()) {
			if (tournament.isValidResult(m) == false) {
				if (result == null) {
					result = m.toString();
				} else {
					result += ", " + m.toString();
				}
				break;
			}
		}

		panel.markInvalid();

		return result;
	}
}
