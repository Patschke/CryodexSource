package cryodex.modules.etc;

import java.util.ArrayList;
import java.util.List;

import cryodex.modules.Match;
import cryodex.modules.Round;
import cryodex.modules.Tournament;
import cryodex.xml.XMLUtils.Element;

public class EtcRound extends Round {

	public EtcRound(Element roundElement, Tournament t) {
		super();
		
		setSingleElimination(roundElement
                .getBooleanFromChild("ISSINGLEELIMINATION"));

        Element matchElement = roundElement.getChild("MATCHES");

        List<Match> matches = new ArrayList<Match>();
        
        if (matchElement != null) {
            matches = new ArrayList<Match>();
            for (Element e : matchElement.getChildren()) {
                matches.add(new EtcMatch(e));
            }
        }
        
        setMatches(matches);
        
        setPanel(t.getRoundPanel(matches));
	}
}
