package cryodex.modules.swlcg;

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
import cryodex.modules.swlcg.export.SWLCGExportController;
import cryodex.modules.swlcg.gui.SWLCGRankingTable;
import cryodex.modules.swlcg.gui.SWLCGRoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils.Element;

public class SWLCGTournament extends Tournament implements XMLObject {

	private SWLCGExportController exportController;
	
	public SWLCGTournament(Element tournamentElement) {
		super();
		setupTournamentGUI(new SWLCGRankingTable(this));
		loadXML(tournamentElement);
	}

	public SWLCGTournament(WizardOptions wizardOptions) {
		super(wizardOptions);
		setupTournamentGUI(new SWLCGRankingTable(this));
	}

    @Override
    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, new SWLCGComparator(this, SWLCGComparator.rankingCompare));

        int count = 0;
        for (Player p : playerList) {
            SWLCGPlayer xp = getModulePlayer(p);
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
        URL imgURL = SWLCGTournament.class.getResource("l.png");
        if (imgURL == null) {
            System.out.println("Failed to retrieve SWLCG Icon");
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
    }

    @Override
    public String getModuleName() {
        return Modules.SWLCG.getName();
    }


    @Override
    public RoundPanel getRoundPanel(List<Match> matches) {
        return new SWLCGRoundPanel(this, matches);
    }

    public SWLCGPlayer getModulePlayer(Player p){
        return (SWLCGPlayer) p.getModuleInfoByModule(getModule());
    }

	@Override
	public TournamentComparator<Player> getRankingComparator() {
		return new SWLCGComparator(this, SWLCGComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
		return new SWLCGComparator(this, SWLCGComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getPairingComparator() {
		return new SWLCGComparator(this, SWLCGComparator.pairingCompare);
	}

	@Override
	public int getPointsDefault() {
		return 100;
	}

	@Override
	public ExportController getExportController() {
		if(exportController == null){
			exportController = new SWLCGExportController();
		}
		return exportController;
	}
	
	@Override
	public boolean isMatchComplete(Match m) {
		if(m.getGame1Result() != null && m.getGame2Result() != null){
			return true;
		}
		
		if(m.isBye()){
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isValidResult(Match m) {
		if(m.getGame1Result() != null && m.getGame2Result() != null){
			return true;
		}
		
		if(m.isBye() && m.getPlayer2() == null){
			return true;
		}
		
		return false;
	}
}
