package cryodex.modules.runewars;

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
import cryodex.modules.runewars.export.RunewarsExportController;
import cryodex.modules.runewars.gui.RunewarsRankingTable;
import cryodex.modules.runewars.gui.RunewarsRoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils.Element;

public class RunewarsTournament extends Tournament implements XMLObject {

	private RunewarsExportController exportController;
	
	public RunewarsTournament(Element tournamentElement) {
		super();
		setupTournamentGUI(new RunewarsRankingTable(this));
		loadXML(tournamentElement);
	}

	public RunewarsTournament(WizardOptions wizardOptions) {
		super(wizardOptions);
		setupTournamentGUI(new RunewarsRankingTable(this));
	}
	
    @Override
    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, new RunewarsComparator(this, RunewarsComparator.rankingCompare));

        int count = 0;
        for (Player p : playerList) {
            RunewarsPlayer xp = getRunewarsPlayer(p);
            if (xp.getScore(this) < minScore || count >= maxCount) {
                getPlayers().remove(p);
            } else {
                count++;
            }
        }

        resetRankingTable();
    }

    @Override
    public Icon getIcon() {
        URL imgURL = RunewarsTournament.class.getResource("rw.png");
        if (imgURL == null) {
            System.out.println("Failed to retrieve Runewars Icon");
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
    }

    @Override
    public String getModuleName() {
        return Modules.RUNEWARS.getName();
    }

    @Override
    public RoundPanel getRoundPanel(List<Match> matches) {
        return new RunewarsRoundPanel(this, matches);
    }
    
    public RunewarsPlayer getRunewarsPlayer(Player p){
        return (RunewarsPlayer) p.getModuleInfoByModule(getModule());
    }
    
	@Override
	public List<Match> getRandomMatches(List<Player> playerList) {
		return new RunewarsRandomMatchGeneration(this, playerList).generateMatches();
	}
    
    @Override
    public List<Match> getOrderedMatches(List<Player> playerList) {
        return null;
    }

	@Override
	public TournamentComparator<Player> getRankingComparator() {
		return new RunewarsComparator(this, RunewarsComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
		return new RunewarsComparator(this, RunewarsComparator.rankingCompareNoHeadToHead);
	}

	@Override
	public TournamentComparator<Player> getPairingComparator() {
		return new RunewarsComparator(this, RunewarsComparator.pairingCompare);
	}

	@Override
	public int getPointsDefault() {
		return 100;
	}

	@Override
	public ExportController getExportController() {
		if(exportController == null){
			exportController = new RunewarsExportController();
		}
		return exportController;
	}
}
