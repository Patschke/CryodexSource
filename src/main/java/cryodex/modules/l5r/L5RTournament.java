package cryodex.modules.l5r;

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
import cryodex.modules.l5r.export.L5RExportController;
import cryodex.modules.l5r.gui.L5RRankingTable;
import cryodex.modules.l5r.gui.L5RRoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils.Element;

public class L5RTournament extends Tournament implements XMLObject {

	private L5RExportController exportController;
	
	public L5RTournament(Element tournamentElement) {
		super();
		setupTournamentGUI(new L5RRankingTable(this));
		loadXML(tournamentElement);
	}

	public L5RTournament(WizardOptions wizardOptions) {
		super(wizardOptions);
		setupTournamentGUI(new L5RRankingTable(this));
	}

    @Override
    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, new L5RComparator(this, L5RComparator.rankingCompare));

        int count = 0;
        for (Player p : playerList) {
            L5RPlayer xp = getModulePlayer(p);
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
        URL imgURL = L5RTournament.class.getResource("l5r.png");
        if (imgURL == null) {
            System.out.println("Failed to retrieve L5R Icon");
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
    }

    @Override
    public String getModuleName() {
        return Modules.L5R.getName();
    }


    @Override
    public RoundPanel getRoundPanel(List<Match> matches) {
        return new L5RRoundPanel(this, matches);
    }

    public L5RPlayer getModulePlayer(Player p){
        return (L5RPlayer) p.getModuleInfoByModule(getModule());
    }

	@Override
	public TournamentComparator<Player> getRankingComparator() {
		return new L5RComparator(this, L5RComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
		return new L5RComparator(this, L5RComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getPairingComparator() {
		return new L5RComparator(this, L5RComparator.pairingCompare);
	}

	@Override
	public int getPointsDefault() {
		return 100;
	}

	@Override
	public ExportController getExportController() {
		if(exportController == null){
			exportController = new L5RExportController();
		}
		return exportController;
	}
}
