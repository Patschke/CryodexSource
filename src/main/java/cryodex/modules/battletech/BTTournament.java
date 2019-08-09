package cryodex.modules.battletech;

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
import cryodex.modules.battletech.export.BTExportController;
import cryodex.modules.battletech.gui.BTRankingTable;
import cryodex.modules.battletech.gui.BTRoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils.Element;

public class BTTournament extends Tournament implements XMLObject {

    private BTExportController exportController;

    public BTTournament(Element tournamentElement) {
        super();
        setupTournamentGUI(new BTRankingTable(this));
        loadXML(tournamentElement);
    }

    public BTTournament(WizardOptions wizardOptions) {
        super(wizardOptions);
        setupTournamentGUI(new BTRankingTable(this));
    }

    @Override
    public Icon getIcon() {
        URL imgURL = BTTournament.class.getResource("bt.png");
        if (imgURL == null) {
            System.out.println("Failed to retrieve BattleTech Icon");
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
    }

    @Override
    public String getModuleName() {
        return Modules.BETTLETECH.getName();
    }

    @Override
    public RoundPanel getRoundPanel(List<Match> matches) {
        return new BTRoundPanel(this, matches);
    }

    public BTPlayer getModulePlayer(Player p) {
        return (BTPlayer) p.getModuleInfoByModule(getModule());
    }

    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, getRankingComparator());

        int count = 0;
        for (Player p : playerList) {
            BTPlayer xp = getModulePlayer(p);
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
        return new BTComparator(this, BTComparator.rankingCompare);
    }

    @Override
    public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
        return new BTComparator(this, BTComparator.rankingCompareNoHeadToHead);
    }

    @Override
    public TournamentComparator<Player> getPairingComparator() {
        return new BTComparator(this, BTComparator.pairingCompare);
    }

    @Override
    public int getPointsDefault() {
        return 100;
    }

    @Override
    public ExportController getExportController() {
        if (exportController == null) {
            exportController = new BTExportController();
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
        if (m.getWinner(1) != null && player1Points != null && player2Points != null) {
            return true;
        }

        return false;
    }
}
