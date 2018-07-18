package cryodex.modules.legion;

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
import cryodex.modules.legion.export.LegionExportController;
import cryodex.modules.legion.gui.LegionRankingTable;
import cryodex.modules.legion.gui.LegionRoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils.Element;

public class LegionTournament extends Tournament implements XMLObject {

	private LegionExportController exportController;
	
	public LegionTournament(Element tournamentElement) {
		super();
		setupTournamentGUI(new LegionRankingTable(this));
		loadXML(tournamentElement);
	}

	public LegionTournament(WizardOptions wizardOptions) {
		super(wizardOptions);
		setupTournamentGUI(new LegionRankingTable(this));
	}

    @Override
    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, new LegionComparator(this, LegionComparator.rankingCompare));

        int count = 0;
        for (Player p : playerList) {
            LegionPlayer xp = getModulePlayer(p);
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
        URL imgURL = LegionTournament.class.getResource("l.png");
        if (imgURL == null) {
            System.out.println("Failed to retrieve Legion Icon");
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
    }

    @Override
    public String getModuleName() {
        return Modules.LEGION.getName();
    }


    @Override
    public RoundPanel getRoundPanel(List<Match> matches) {
        return new LegionRoundPanel(this, matches);
    }

    public LegionPlayer getModulePlayer(Player p){
        return (LegionPlayer) p.getModuleInfoByModule(getModule());
    }

	@Override
	public TournamentComparator<Player> getRankingComparator() {
		return new LegionComparator(this, LegionComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
		return new LegionComparator(this, LegionComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getPairingComparator() {
		return new LegionComparator(this, LegionComparator.pairingCompare);
	}

	@Override
	public int getPointsDefault() {
		return 100;
	}

	@Override
	public ExportController getExportController() {
		if(exportController == null){
			exportController = new LegionExportController();
		}
		return exportController;
	}
	
    @Override
    public boolean isMatchComplete(Match m) {

        boolean isComplete = false;

        if (m.isBye()) {
            isComplete = true;
        } else if (m.getWinner(1) != null) {
            isComplete = true;
        }

        return isComplete;
    }

    @Override
    public boolean isValidResult(Match m) {
        return isMatchComplete(m);
    }
}
