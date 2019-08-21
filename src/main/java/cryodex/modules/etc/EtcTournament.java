package cryodex.modules.etc;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import cryodex.CryodexController.Modules;
import cryodex.Language;
import cryodex.Player;
import cryodex.modules.ExportController;
import cryodex.modules.Match;
import cryodex.modules.RoundPanel;
import cryodex.modules.Tournament;
import cryodex.modules.TournamentComparator;
import cryodex.modules.etc.export.EtcExportController;
import cryodex.modules.etc.gui.EtcRankingTable;
import cryodex.modules.etc.gui.EtcRoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class EtcTournament extends Tournament implements XMLObject {

    private EtcExportController exportController;
    private Integer playerCount = 6;

    public EtcTournament(Element tournamentElement) {
        super();
        setupTournamentGUI(new EtcRankingTable(this));
        playerCount = tournamentElement.getIntegerFromChild("PLAYERCOUNT");
        if(playerCount == null){
            playerCount = 6;
        }

        Element roundElement = tournamentElement.getChild("ROUNDS");
        for (Element e : roundElement.getChildren()) {
            getAllRounds().add(new EtcRound(e, this));
        }

        loadXML(tournamentElement);

    }

    public EtcTournament(WizardOptions wizardOptions) {
        super(wizardOptions);
        setupTournamentGUI(new EtcRankingTable(this));

        String playersPerTeamString = wizardOptions.getOption(Language.number_of_players_per_team);
        try{
            playerCount = Integer.parseInt(playersPerTeamString);
        } catch (Exception e) {
            playerCount = 6;
        }
    }

    @Override
    public Icon getIcon() {
        URL imgURL = EtcTournament.class.getResource("x.png");
        if (imgURL == null) {
            System.out.println("Failed to retrieve ETC Icon");
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
    }

    @Override
    public String getModuleName() {
        return Modules.ETC.getName();
    }

    @Override
    public RoundPanel getRoundPanel(List<Match> matches) {
        return new EtcRoundPanel(this, matches);
    }

    public EtcPlayer getModulePlayer(Player p) {
        return (EtcPlayer) p.getModuleInfoByModule(getModule());
    }

    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, getRankingComparator());

        int count = 0;
        for (Player p : playerList) {
            EtcPlayer xp = getModulePlayer(p);
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
        return new EtcComparator(this, EtcComparator.rankingCompare);
    }

    @Override
    public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
        return new EtcComparator(this, EtcComparator.rankingCompare);
    }

    @Override
    public TournamentComparator<Player> getPairingComparator() {
        return new EtcComparator(this, EtcComparator.pairingCompare);
    }

    @Override
    public int getPointsDefault() {
        return 200;
    }

    @Override
    public ExportController getExportController() {
        if(exportController == null){
            exportController = new EtcExportController();
        }
        return exportController;
    }

    public Integer getPlayerCount(){
        return playerCount;
    }

    @Override
    public List<Match> getRandomMatches(List<Player> playerList) {

        List<Match> matches = super.getRandomMatches(playerList);        

        List<Match> finalMatches = multiplyMatchesPerTeamPlayers(matches);

        return finalMatches;
    }
    
    @Override
    public List<Match> getOrderedMatches(List<Player> playerList) {

        List<Match> matches = super.getOrderedMatches(playerList);
        
        List<Match> finalMatches = multiplyMatchesPerTeamPlayers(matches);

        return finalMatches;
    }
    
    @Override
    protected List<Match> firstRoundPairings() {
        List<Match> matches = super.firstRoundPairings();

        List<Match> finalMatches = multiplyMatchesPerTeamPlayers(matches);

        return finalMatches; 
    }

    private List<Match> multiplyMatchesPerTeamPlayers(List<Match> matches){

        List<Match> finalMatches = new ArrayList<Match>();

        for(Match im :  matches){
            if(im.isBye()){
                finalMatches.add(im);
            } else {
                for(int i = 1 ; i <= getPlayerCount() ; i++){
                    EtcMatch fm = EtcMatch.copyMatch(im, String.valueOf(i));
                    finalMatches.add(fm);
                }
            }
        }

        return finalMatches;
    }

    @Override
    public Match getMatch(Player player1, Player player2) {
        return new EtcMatch(player1, player2);
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {
        super.appendXML(sb);
        XMLUtils.appendObject(sb, "PLAYERCOUNT", playerCount);
        return sb;
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
