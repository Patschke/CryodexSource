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
import cryodex.modules.Round;
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

    private XWingExportController exportController;

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

    public XWingPlayer getModulePlayer(Player p) {
        return (XWingPlayer) p.getModuleInfoByModule(getModule());
    }

    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, getRankingComparator());

        int count = 0;
        for (Player p : playerList) {
            XWingPlayer xp = getModulePlayer(p);
            if (xp.getScore(this) < minScore || count >= maxCount) {
                getPlayers().remove(p);
            } else {
                count++;
            }
        }

        triggerDeepChange();
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
        if (exportController == null) {
            exportController = new XWingExportController();
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

        if(player1Points > this.getRoundPoints(this.getRoundNumber(this.getRoundOfMatch(m)))){
        	return false;
        }
        
        if(player2Points > this.getRoundPoints(this.getRoundNumber(this.getRoundOfMatch(m)))){
        	return false;
        }
        
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
