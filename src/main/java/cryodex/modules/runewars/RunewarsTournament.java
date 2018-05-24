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
            RunewarsPlayer xp = getModulePlayer(p);
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
    
    public RunewarsPlayer getModulePlayer(Player p){
        return (RunewarsPlayer) p.getModuleInfoByModule(getModule());
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
	
    @Override
    public boolean isMatchComplete(Match m) {

        boolean isComplete = false;

        if (m.isBye()) {
            isComplete = true;
        } else if (m.getWinner(1) != null && m.getPlayer1Points() != null && m.getPlayer2Points() != null) {
            isComplete = true;
        }

        return isComplete;
    }

    @Override
    public boolean isValidResult(Match m) {
        Integer player1Points = m.getPlayer1Points() == null ? 0 : m.getPlayer1Points();
        Integer player2Points = m.getPlayer2Points() == null ? 0 : m.getPlayer2Points();

        // If there is no second player, it must be a bye
        if (m.getPlayer2() == null && m.isBye()) {
            return true;
        }

        // For single elimination we just look to make sure the correct
        // player is the winner according to points
        if ((m.getWinner(1) == m.getPlayer1() && player1Points >= player2Points)
                || (m.getWinner(1) == m.getPlayer2() && player2Points >= player1Points)
                || (player1Points == player2Points && m.getWinner(1) != null)) {
            return true;
        }

        return false;
    }
}
