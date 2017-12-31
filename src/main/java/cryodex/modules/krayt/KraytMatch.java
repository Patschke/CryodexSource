package cryodex.modules.krayt;

import cryodex.Player;
import cryodex.modules.Match;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class KraytMatch extends Match {

	public static KraytMatch copyMatch(Match m, String suffix) {
		KraytMatch copy = new KraytMatch(m.getPlayer1(), m.getPlayer2());
		copy.setConcede(m.isConcede());
		copy.setGame1Result(m.getGame1Result());
		copy.setPlayer1PointsDestroyed(m.getPlayer1Points());
		copy.setPlayer2PointsDestroyed(m.getPlayer2Points());
		copy.setSuffix(suffix);
		return copy;
	}

	private String suffix;
	private String subplayer1;
	private String subplayer2;
	
	public KraytMatch() {

	}

	public KraytMatch(Player player1, Player player2) {
		super(player1, player2);
	}

	public KraytMatch(Element matchElement) {

		super(matchElement);

		setSuffix(matchElement.getStringFromChild("SUFFIX"));
		setSubplayer1(matchElement.getStringFromChild("SUBPLAYER1"));
		setSubplayer2(matchElement.getStringFromChild("SUBPLAYER2"));
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	
	public String getSubplayer1() {
        return subplayer1;
    }

    public void setSubplayer1(String subplayer1) {
        this.subplayer1 = subplayer1;
    }

    public String getSubplayer2() {
        return subplayer2;
    }

    public void setSubplayer2(String subplayer2) {
        this.subplayer2 = subplayer2;
    }

    @Override
	public StringBuilder appendXML(StringBuilder sb) {
		XMLUtils.appendObject(sb, "SUFFIX", getSuffix());
		XMLUtils.appendObject(sb, "SUBPLAYER1", getSubplayer1());
		XMLUtils.appendObject(sb, "SUBPLAYER2", getSubplayer2());
		return super.appendXML(sb);
	}

}
