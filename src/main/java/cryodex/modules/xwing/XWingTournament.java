package cryodex.modules.xwing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import cryodex.CryodexController.Modules;
import cryodex.Player;
import cryodex.modules.ExportController;
import cryodex.modules.Match;
import cryodex.modules.RoundPanel;
import cryodex.modules.Tournament;
import cryodex.modules.TournamentComparator;
import cryodex.modules.xwing.export.XWingExportController;
import cryodex.modules.xwing.gui.XWingRankingTable;
import cryodex.modules.xwing.gui.XWingRoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils.Element;

public class XWingTournament extends Tournament implements XMLObject {

	XWingExportController exportController;
	
	public XWingTournament(Element tournamentElement) {
		super();
		setupTournamentGUI(new XWingRankingTable(this));
		loadXML(tournamentElement);
	}

	public XWingTournament(WizardOptions wizardOptions) {
		super(wizardOptions);
		setupTournamentGUI(new XWingRankingTable(this));
	}

	@Override
	public Icon getIcon() {
		URL imgURL = XWingTournament.class.getResource("x.png");
		if (imgURL == null) {
			System.out.println("Failed to retrieve X-Wing Icon");
		}
		ImageIcon icon = new ImageIcon(imgURL);
		return icon;
	}

	@Override
	public String getModuleName() {
		return Modules.XWING.getName();
	}

	@Override
	public RoundPanel getRoundPanel(List<Match> matches) {
		return new XWingRoundPanel(this, matches);
	}

	public XWingPlayer getXWingPlayer(Player p) {
		return (XWingPlayer) p.getModuleInfoByModule(getModule());
	}

	public void massDropPlayers(int minScore, int maxCount) {

		List<Player> playerList = new ArrayList<Player>();
		playerList.addAll(getPlayers());

		Collections.sort(playerList, getRankingComparator());

		int count = 0;
		for (Player p : playerList) {
			XWingPlayer xp = getXWingPlayer(p);
			if (xp.getScore(this) < minScore || count >= maxCount) {
				getPlayers().remove(p);
			} else {
				count++;
			}
		}

		resetRankingTable();
	}

	@Override
	public List<Match> getRandomMatches(List<Player> playerList) {
		return new XWingRandomMatchGeneration(this, playerList).generateMatches();
	}
	
	@Override
    public List<Match> getOrderedMatches(List<Player> playerList) {
        return null;
    }

	@Override
	public TournamentComparator<Player> getRankingComparator() {
		return new XWingComparator(this, XWingComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
		return new XWingComparator(this, XWingComparator.rankingCompareNoHeadToHead);
	}

	@Override
	public TournamentComparator<Player> getPairingComparator() {
		return new XWingComparator(this, XWingComparator.pairingCompare);
	}

	@Override
	public int getPointsDefault() {
		return 100;
	}

	@Override
	public ExportController getExportController() {
		if(exportController == null){
			exportController = new XWingExportController();
		}
		return exportController;
	}
}
