package cryodex.modules.destiny;

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
import cryodex.modules.destiny.export.DestinyExportController;
import cryodex.modules.destiny.gui.DestinyRankingTable;
import cryodex.modules.destiny.gui.DestinyRoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils.Element;

public class DestinyTournament extends Tournament implements XMLObject {

	private DestinyExportController exportController;
	
	public DestinyTournament(Element tournamentElement) {
		super();
		setupTournamentGUI(new DestinyRankingTable(this));
		loadXML(tournamentElement);
	}

	public DestinyTournament(WizardOptions wizardOptions) {
		super(wizardOptions);
		setupTournamentGUI(new DestinyRankingTable(this));
	}

    @Override
    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, new DestinyComparator(this, DestinyComparator.rankingCompare));

        int count = 0;
        for (Player p : playerList) {
            DestinyPlayer xp = getModulePlayer(p);
            if (xp.getScore(this) < minScore || count >= maxCount) {
                getPlayers().remove(p);
            } else {
                count++;
            }
        }

        triggerDeepChange();
    }

    @Override
    public Icon getIcon() {
        URL imgURL = DestinyTournament.class.getResource("d.png");
        if (imgURL == null) {
            System.out.println("Failed to retrieve Destiny Icon");
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
    }

    @Override
    public String getModuleName() {
        return Modules.DESTINY.getName();
    }


    @Override
    public RoundPanel getRoundPanel(List<Match> matches) {
        return new DestinyRoundPanel(this, matches);
    }

    public DestinyPlayer getModulePlayer(Player p){
        return (DestinyPlayer) p.getModuleInfoByModule(getModule());
    }

	@Override
	public TournamentComparator<Player> getRankingComparator() {
		return new DestinyComparator(this, DestinyComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
		return new DestinyComparator(this, DestinyComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getPairingComparator() {
		return new DestinyComparator(this, DestinyComparator.pairingCompare);
	}

	@Override
	public int getPointsDefault() {
		return 100;
	}

	@Override
	public ExportController getExportController() {
		if(exportController == null){
			exportController = new DestinyExportController();
		}
		return exportController;
	}
}
