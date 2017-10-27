package cryodex.modules.etc;

import cryodex.Player;
import cryodex.modules.Match;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class EtcMatch extends Match {

	public static EtcMatch copyMatch(Match m, String suffix) {
		EtcMatch copy = new EtcMatch(m.getPlayer1(), m.getPlayer2());
		copy.setConcede(m.isConcede());
		copy.setGame1Result(m.getGame1Result());
		copy.setPlayer1PointsDestroyed(m.getPlayer1Points());
		copy.setPlayer2PointsDestroyed(m.getPlayer2Points());
		copy.setSuffix(suffix);
		return copy;
	}

	private String suffix;

	public EtcMatch() {

	}

	public EtcMatch(Player player1, Player player2) {
		super(player1, player2);
	}

	public EtcMatch(Element matchElement) {

		super(matchElement);

		setSuffix(matchElement.getStringFromChild("SUFFIX"));
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	@Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "SUFFIX", getSuffix());
		return super.appendXML(sb);
	}

}
