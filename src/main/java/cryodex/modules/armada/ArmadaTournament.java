package cryodex.modules.armada;

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
import cryodex.modules.armada.export.ArmadaExportController;
import cryodex.modules.armada.gui.ArmadaRankingTable;
import cryodex.modules.armada.gui.ArmadaRoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils.Element;

public class ArmadaTournament extends Tournament implements XMLObject {

	private ArmadaExportController exportController;
	
	public ArmadaTournament(Element tournamentElement) {
		super();
		setupTournamentGUI(new ArmadaRankingTable(this));
		loadXML(tournamentElement);
	}

	public ArmadaTournament(WizardOptions wizardOptions) {
		super(wizardOptions);
		setupTournamentGUI(new ArmadaRankingTable(this));
	}
	
    @Override
    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, new ArmadaComparator(this, ArmadaComparator.rankingCompare));

        int count = 0;
        for (Player p : playerList) {
            ArmadaPlayer xp = getModulePlayer(p);
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
        URL imgURL = ArmadaTournament.class.getResource("a.png");
        if (imgURL == null) {
            System.out.println("Failed to retrieve Armada Icon");
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
    }

    @Override
    public String getModuleName() {
        return Modules.ARMADA.getName();
    }

    @Override
    public RoundPanel getRoundPanel(List<Match> matches) {
        return new ArmadaRoundPanel(this, matches);
    }
    
    public ArmadaPlayer getModulePlayer(Player p){
        return (ArmadaPlayer) p.getModuleInfoByModule(getModule());
    }

	@Override
	public TournamentComparator<Player> getRankingComparator() {
		return new ArmadaComparator(this, ArmadaComparator.rankingCompare);
	}

	@Override
	public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
		return new ArmadaComparator(this, ArmadaComparator.rankingCompareNoHeadToHead);
	}

	@Override
	public TournamentComparator<Player> getPairingComparator() {
		return new ArmadaComparator(this, ArmadaComparator.pairingCompare);
	}

	@Override
	public int getPointsDefault() {
		return 100;
	}

	@Override
	public ExportController getExportController() {
		if(exportController == null){
			exportController = new ArmadaExportController();
		}
		return exportController;
	}
}
