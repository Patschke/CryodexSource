package cryodex.modules.krayt;

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
import cryodex.modules.krayt.export.KraytExportController;
import cryodex.modules.krayt.gui.KraytRankingTable;
import cryodex.modules.krayt.gui.KraytRoundPanel;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class KraytTournament extends Tournament implements XMLObject {

    private KraytExportController exportController;
    private Integer playerCount = 4;

    public KraytTournament(Element tournamentElement) {
        super();
        setupTournamentGUI(new KraytRankingTable(this));
        playerCount = tournamentElement.getIntegerFromChild("PLAYERCOUNT");
        if(playerCount == null){
            playerCount = 4;
        }

        Element roundElement = tournamentElement.getChild("ROUNDS");
        for (Element e : roundElement.getChildren()) {
            getAllRounds().add(new KraytRound(e, this));
        }

        loadXML(tournamentElement);

    }

    public KraytTournament(WizardOptions wizardOptions) {
        super(wizardOptions);
        setupTournamentGUI(new KraytRankingTable(this));

        String playersPerTeamString = wizardOptions.getOption(Language.number_of_players_per_team);
        try{
            playerCount = Integer.parseInt(playersPerTeamString);
        } catch (Exception e) {
            playerCount = 4;
        }
    }

    @Override
    public Icon getIcon() {
        URL imgURL = KraytTournament.class.getResource("krayt.png");
        if (imgURL == null) {
            System.out.println("Failed to retrieve Krayt Icon");
        }
        ImageIcon icon = new ImageIcon(imgURL);
        return icon;
    }

    @Override
    public String getModuleName() {
        return Modules.KRAYT.getName();
    }

    @Override
    public RoundPanel getRoundPanel(List<Match> matches) {
        return new KraytRoundPanel(this, matches);
    }

    public KraytPlayer getModulePlayer(Player p) {
        return (KraytPlayer) p.getModuleInfoByModule(getModule());
    }

    public void massDropPlayers(int minScore, int maxCount) {

        List<Player> playerList = new ArrayList<Player>();
        playerList.addAll(getPlayers());

        Collections.sort(playerList, getRankingComparator());

        int count = 0;
        for (Player p : playerList) {
            KraytPlayer xp = getModulePlayer(p);
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
        return new KraytComparator(this, KraytComparator.rankingCompare);
    }

    @Override
    public TournamentComparator<Player> getRankingNoHeadToHeadComparator() {
        return new KraytComparator(this, KraytComparator.rankingCompareNoHeadToHead);
    }

    @Override
    public TournamentComparator<Player> getPairingComparator() {
        return new KraytComparator(this, KraytComparator.pairingCompare);
    }

    @Override
    public int getPointsDefault() {
        return 100;
    }

    @Override
    public ExportController getExportController() {
        if(exportController == null){
            exportController = new KraytExportController();
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
                    KraytMatch fm = KraytMatch.copyMatch(im, String.valueOf(i));
                    finalMatches.add(fm);
                }
            }
        }

        return finalMatches;
    }

    @Override
    public Match getMatch(Player player1, Player player2) {
        return new KraytMatch(player1, player2);
    }

    @Override
    public StringBuilder appendXML(StringBuilder sb) {
        super.appendXML(sb);
        XMLUtils.appendObject(sb, "PLAYERCOUNT", playerCount);
        return sb;
    }
}
