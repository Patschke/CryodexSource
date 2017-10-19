package cryodex.modules.imperialassault;

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
import cryodex.modules.imperialassault.export.IAExportController;
import cryodex.modules.imperialassault.gui.IARankingTable;
import cryodex.modules.imperialassault.gui.IARoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils.Element;

public class IATournament extends Tournament implements XMLObject {

	IAExportController exportController;
	
	public IATournament(Element tournamentElement) {
		super();
		setupTournamentGUI(new IARankingTable(this));
		loadXML(tournamentElement);
	}

	public IATournament(WizardOptions wizardOptions) {
		super(wizardOptions);
		setupTournamentGUI(new IARankingTable(this));
	}
	
    @Override
    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, new IAComparator(this, IAComparator.rankingCompare));

        int count = 0;
        for (Player p : playerList) {
            IAPlayer xp = getIAPlayer(p);
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
        URL imgURL = IATournament.class.getResource("ia.png");
        if (imgURL == null) {
            System.out.println("Failed to retrieve Imperial Assault Icon");
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
    }

    @Override
    public String getModuleName() {
        return Modules.IA.getName();
    }

    @Override
    public RoundPanel getRoundPanel(List<Match> matches) {
        return new IARoundPanel(this, matches);
    }
    
    public IAPlayer getIAPlayer(Player p){
        return (IAPlayer) p.getModuleInfoByModule(getModule());
    }

	@Override
	public List<Match> getRandomMatches(List<Player> playerList) {
		return new IARandomMatchGeneration(this, playerList).generateMatches();
	}
    
    @Override
    public List<Match> getOrderedMatches(List<Player> playerList) {
        return null;
    }

	@Override
	public TournamentComparator<Player> getRankingComparator() {
		return new IAComparator(this, IAComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
		return new IAComparator(this, IAComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getPairingComparator() {
		return new IAComparator(this, IAComparator.pairingCompare);
	}

	@Override
	public int getPointsDefault() {
		return 100;
	}

	@Override
	public ExportController getExportController() {
		if(exportController == null){
			exportController = new IAExportController();
		}
		
		return exportController;
	}
}
