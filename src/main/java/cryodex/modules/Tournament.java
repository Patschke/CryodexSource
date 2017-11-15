package cryodex.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import cryodex.CryodexController;
import cryodex.CryodexController.Modules;
import cryodex.Main;
import cryodex.Player;
import cryodex.widget.wizard.WizardOptions;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public abstract class Tournament implements XMLObject {

    public enum InitialSeedingEnum {
        RANDOM, BY_GROUP, IN_ORDER;
    }

    private final List<Round> rounds;
    private List<Player> players;
    private InitialSeedingEnum seedingEnum;
    private TournamentGUI tournamentGUI;
    private String name;
    private List<Integer> points;
    private boolean startAsSingleElimination = false;
    private List<String> dependentTournaments;
    private Module module;
    private boolean isRandomPairing = true;

    public Tournament() {
        this(null);
    }

    public Tournament(WizardOptions wizardOptions) {

        // Initialize
        this.players = new ArrayList<>();
        this.rounds = new ArrayList<>();
        this.dependentTournaments = new ArrayList<>();
        this.seedingEnum = InitialSeedingEnum.RANDOM;

        // Default Values
        this.name = "";
        this.seedingEnum = null;
        this.points = null;
        this.startAsSingleElimination = false;
        this.isRandomPairing = true;

        if (wizardOptions != null) {
            // Load values
            this.name = wizardOptions.getName();
            this.seedingEnum = wizardOptions.getInitialSeedingEnum();
            this.points = wizardOptions.getPoints();
            this.startAsSingleElimination = wizardOptions.isSingleElimination();
            this.isRandomPairing = wizardOptions.isRandomPairing();

            if (wizardOptions.getPlayerList() != null) {
                this.players.addAll(wizardOptions.getPlayerList());
            }
        }

        module = Modules.getModuleByName(getModuleName());
    }

    public void loadXML(Element tournamentElement) {

        String playerIDs = tournamentElement.getStringFromChild("PLAYERS");

        for (String s : playerIDs.split(",")) {
            Player p = CryodexController.getPlayerByID(s);
            if (p != null) {
                getPlayers().add(p);
            }
        }

        if (rounds == null || rounds.isEmpty()) {
            Element roundElement = tournamentElement.getChild("ROUNDS");
            for (Element e : roundElement.getChildren()) {
                rounds.add(new Round(e, this));
            }
        }

        Element dependentElement = tournamentElement.getChild("DEPENDENTTOURNAMENTS");
        if (dependentElement != null) {
            for (Element e : dependentElement.getChildren()) {
                dependentTournaments.add(e.getData());
            }
        }

        name = tournamentElement.getStringFromChild("NAME");
        String seeding = tournamentElement.getStringFromChild("SEEDING");
        if (seeding != null && seeding.isEmpty() == false) {
            seedingEnum = InitialSeedingEnum.valueOf(seeding);
        } else {
            seedingEnum = InitialSeedingEnum.RANDOM;
        }

        String pointsString = tournamentElement.getStringFromChild("POINTS");

        if (pointsString != null && pointsString.isEmpty() == false) {
            points = new ArrayList<Integer>();
            for (String s : pointsString.split(",")) {
                points.add(new Integer(s));
            }
        }

        isRandomPairing = tournamentElement.getBooleanFromChild("ISRANDOMPAIRING", true);

        int counter = 1;
        for (Round r : rounds) {
            if (r.isSingleElimination()) {
                getTournamentGUI().getRoundTabbedPane().addSingleEliminationTab(r.getMatches().size() * 2, r.getPanel());
            } else {
                getTournamentGUI().getRoundTabbedPane().addSwissTab(counter, r.getPanel());
                counter++;
            }

        }

        triggerDeepChange();
    }

    public void setupTournamentGUI(RankingTable rankingTable) {
        tournamentGUI = new TournamentGUI(rankingTable);
    }

    public int getRoundNumber(Round round) {
        int count = 0;
        for (Round r : rounds) {
            count++;
            if (r == round) {
                return count;
            }
        }

        return 0;
    }

    public Round getRound(int i) {
        if (rounds == null) {
            return null;
        } else {
            return rounds.get(i);
        }
    }

    public Round getSelectedRound() {
        if (rounds == null) {
            return null;
        } else {
            return getAllRounds().get(getTournamentGUI().getRoundTabbedPane().getSelectedIndex());
        }
    }

    public List<Round> getAllRounds() {
        return rounds;
    }

    public int getRoundCount() {
        if (rounds == null) {
            return 0;
        } else {
            return rounds.size();
        }
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Set<Player> getAllPlayers() {
        // TreeSets and Head To Head comparisons can have problems.
        // Do not use them together.
        Set<Player> allPlayers = new TreeSet<Player>(getRankingNoHeadToHeadComparator());

        for (Round r : getAllRounds()) {
            for (Match m : r.getMatches()) {
                if (m.isBye()) {
                    allPlayers.add(m.getPlayer1());
                } else {
                    allPlayers.add(m.getPlayer1());
                    if (m.getPlayer2() != null) {
                        allPlayers.add(m.getPlayer2());
                    }
                }
            }
        }

        allPlayers.addAll(players);

        return allPlayers;
    }

    public TournamentGUI getTournamentGUI() {
        return tournamentGUI;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getPoints() {
        return points;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void updateVisualOptions() {
        if (CryodexController.isLoading == false) {
            for (Round r : getAllRounds()) {
                r.getPanel().resetGamePanels(true);
            }
        }
    }

    public boolean generateNextRound() {

        // All matches must have a result filled in
        String incompleteMatches = getLatestRound().isComplete(this);
        if (incompleteMatches != null) {
            JOptionPane.showMessageDialog(Main.getInstance(),
                    "<HTML>Current round is not complete. Please complete all matches before continuing<br>" + incompleteMatches);
            return false;
        }

        // Single elimination checks
        if (getLatestRound().isSingleElimination()) {
            // If there was only one match then there is no reason to create a
            // new round.
            if (getLatestRound().getMatches().size() == 1) {
                JOptionPane.showMessageDialog(Main.getInstance(), "Final tournament complete. No more rounds will be generated.");
                return false;
            }

            String invalidMatches = getLatestRound().isValid(this);
            if (invalidMatches != null) {
                JOptionPane.showMessageDialog(Main.getInstance(),
                        "<HTML>At least one tournamnt result is not correct.\n" + "-Check if points are backwards or a draw has been set.\n"
                                + "-Draws are not allowed in single elimination rounds.\n" + "--If a draw occurs, the player with initiative wins.\n"
                                + "--This can be set by going to the view menu and deselect 'Enter Only Points'<br>" + invalidMatches);
                return false;
            }

            generateSingleEliminationMatches(getLatestRound().getMatches().size());
        } else {
            // Regular swiss round checks
            String invalidMatches = getLatestRound().isValid(this);
            if (invalidMatches != null) {
                JOptionPane.showMessageDialog(Main.getInstance(),
                        "<HTML>At least one tournamnt result is not correct. Check if points are backwards or a result should be a modified win or tie.<br>"
                                + invalidMatches);
                return false;
            }

            generateRound(getAllRounds().size() + 1);
        }
        return true;
    }

    public void cancelRound(int roundNumber) {
        if (rounds.size() >= roundNumber) {
            // If we are generating a past round. Clear all existing rounds that
            // will be erased.
            while (rounds.size() >= roundNumber) {
                int index = rounds.size() - 1;
                Round roundToRemove = rounds.get(index);
                for (Match m : roundToRemove.getMatches()) {
                    m.clear();
                }
                rounds.remove(roundToRemove);

                getTournamentGUI().getRoundTabbedPane().remove(index);
            }
        }
    }

    public void generateRound(int roundNumber) {

        // if trying to skip a round...stop it
        if (roundNumber > rounds.size() + 1) {
            throw new IllegalArgumentException();
        }

        cancelRound(roundNumber);

        List<Match> matches;

        boolean hasDependentTournaments = dependentTournaments != null && !dependentTournaments.isEmpty();

        if (roundNumber == 1 && !hasDependentTournaments) {
            matches = firstRoundPairings();
        } else {
            matches = getMatches(getPlayers());
        }

        Round r = new Round(matches, this);
        rounds.add(r);
        if (roundNumber == 1 && startAsSingleElimination && (matches.size() == 1 || matches.size() == 2 || matches.size() == 4 || matches.size() == 8
                || matches.size() == 16 || matches.size() == 32)) {
            r.setSingleElimination(true);
            getTournamentGUI().getRoundTabbedPane().addSingleEliminationTab(r.getMatches().size() * 2, r.getPanel());
        } else {
            getTournamentGUI().getRoundTabbedPane().addSwissTab(roundNumber, r.getPanel());
        }

        triggerDeepChange();
    }

    protected List<Match> firstRoundPairings() {
        List<Match> matches;
        List<Player> nonPairedPlayers = new ArrayList<>();
        nonPairedPlayers.addAll(getPlayers());

        List<Player> firstRoundByePlayers = new ArrayList<>();
        for (Player p : nonPairedPlayers) {
            if (p.isFirstRoundBye()) {
                firstRoundByePlayers.add(p);
            }
        }
        nonPairedPlayers.removeAll(firstRoundByePlayers);

        switch (seedingEnum) {
        case IN_ORDER:
            matches = initialSeedingInOrder(nonPairedPlayers);
            break;
        case BY_GROUP:
            matches = initialSeedingByGroup(nonPairedPlayers);
            break;
        default:
            matches = initialSeedingRandom(nonPairedPlayers);
            break;
        }

        for (Player p : firstRoundByePlayers) {
            matches.add(getMatch(p, null));
        }
        return matches;
    }

    protected List<Match> initialSeedingByGroup(List<Player> players) {
        List<Match> matches = new ArrayList<>();
        Map<String, List<Player>> playerMap = new HashMap<>();

        // Add players to map
        for (Player p : players) {
            List<Player> playerList = playerMap.get(p.getGroupName());
            if (playerList == null) {
                playerList = new ArrayList<>();
                String groupName = p.getGroupName();
                playerMap.put(groupName, playerList);
            }
            playerList.add(p);
        }

        // Shuffle up the lists
        List<String> seedValues = new ArrayList<>(playerMap.keySet());
        Collections.shuffle(seedValues);

        // Shuffle each group list
        for (List<Player> list : playerMap.values()) {
            Collections.shuffle(list);
            System.out.println(list.get(0).getGroupName() + " has " + list.size());
        }

        Player p1;
        Player p2 = null;
        while (!playerMap.isEmpty()) {
            String biggestGroup = "";
            int playersInGroup = -1;

            for (Map.Entry<String, List<Player>> entry : playerMap.entrySet()) {
                if (entry.getValue().size() > playersInGroup || biggestGroup.equals("")) {
                    biggestGroup = entry.getKey();
                    playersInGroup = entry.getValue().size();
                }
            }

            p1 = playerMap.get(biggestGroup).get(0);
            playerMap.get(biggestGroup).remove(p1);
            if (playerMap.get(biggestGroup).isEmpty()) {
                playerMap.remove(biggestGroup);
            }

            // ready player two
            List<String> keys = new ArrayList<>(playerMap.keySet());
            Collections.shuffle(keys);
            for (String key : keys) {
                if (biggestGroup.equals("") || (!biggestGroup.equals(key))) {
                    p2 = playerMap.get(key).get(0);
                }
            }

            // can't find a player 2, take one from own team
            if (p2 == null) {
                for (Map.Entry<String, List<Player>> entry : playerMap.entrySet()) {
                    p2 = entry.getValue().get(0);
                }
            }

            if (p2 != null) {
                String groupName = p2.getGroupName();
                playerMap.get(groupName).remove(p2);
                if (playerMap.get(groupName).isEmpty()) {
                    playerMap.remove(groupName);
                }
            }
            matches.add(getMatch(p1, p2));
            p2 = null;
        }
        return matches;
    }

    protected List<Match> initialSeedingRandom(List<Player> players) {
        List<Match> matches = new ArrayList<>();
        Collections.shuffle(players);

        while (!players.isEmpty()) {
            Player player1 = players.get(0);
            Player player2 = players.get(players.size() - 1);
            players.remove(player1);
            if (player1 == player2) {
                player2 = null;
            } else {
                players.remove(player2);
            }

            Match match = getMatch(player1, player2);
            matches.add(match);
        }
        return matches;
    }

    protected List<Match> initialSeedingInOrder(List<Player> players) {
        List<Match> matches = new ArrayList<>();
        while (!players.isEmpty()) {
            Player player1 = players.get(0);
            Player player2 = null;
            players.remove(0);
            if (!players.isEmpty()) {
                player2 = players.get(0);
                players.remove(0);
            }

            Match match = getMatch(player1, player2);
            matches.add(match);
        }
        return matches;
    }

    private List<Match> getMatches(List<Player> userList) {
        List<Match> matches = new ArrayList<Match>();

        List<Player> tempList = new ArrayList<Player>();
        tempList.addAll(userList);
        Collections.sort(tempList, getPairingComparator());

        Match byeMatch = null;
        // Setup the bye match if necessary
        // The player to get the bye is the lowest ranked player who has not had
        // a bye yet or who has the fewest byes
        if (tempList.size() % 2 == 1) {
            Player byeUser = null;
            int byUserCounter = 1;
            int minByes = 0;
            try {
                while (byeUser == null || byeUser.getByes(this) > minByes
                        || (byeUser.getMatches(this) != null && byeUser.getMatches(this).get(byeUser.getMatches(this).size() - 1).isBye())) {
                    if (byUserCounter > tempList.size()) {
                        minByes++;
                        byUserCounter = 1;
                    }
                    byeUser = tempList.get(tempList.size() - byUserCounter);

                    byUserCounter++;

                }
            } catch (ArrayIndexOutOfBoundsException e) {
                byeUser = tempList.get(tempList.size() - 1);
            }
            byeMatch = getMatch(byeUser, null);
            tempList.remove(byeUser);
        }

        if (isRandomPairing) {
            matches = getRandomMatches(tempList);
        } else {
            matches = getOrderedMatches(tempList);
        }

        if (Match.hasDuplicate(matches)) {
            JOptionPane.showMessageDialog(Main.getInstance(), "Unable to resolve duplicate matches. Please review for best course of action.");
        }

        // Add the bye match at the end
        if (byeMatch != null) {
            matches.add(byeMatch);
        }

        return matches;
    }

    public List<Match> getOrderedMatches(List<Player> playerList) {
        OrderedMatchGeneration generator = new OrderedMatchGeneration(this, playerList);
        return generator.generateMatches();
    }

    public List<Match> getRandomMatches(List<Player> playerList) {
        RandomMatchGeneration generator = new RandomMatchGeneration(this, playerList);
        return generator.generateMatches();
    }

    public void generateSingleEliminationMatches(int cutSize) {

        List<Match> matches = new ArrayList<>();

        List<Match> matchesCorrected = new ArrayList<Match>();

        if (getLatestRound().isSingleElimination()) {
            List<Match> lastRoundMatches = getLatestRound().getMatches();

            for (int index = 0; index < lastRoundMatches.size(); index = index + 2) {
                Match newMatch = getMatch(lastRoundMatches.get(index).getWinner(1), lastRoundMatches.get(index + 1).getWinner(1));
                matches.add(newMatch);
            }

            matchesCorrected = matches;
        } else {
            List<Player> tempList = new ArrayList<>();
            tempList.addAll(getPlayers());
            Collections.sort(tempList, getRankingComparator());
            tempList = tempList.subList(0, cutSize);

            while (tempList.isEmpty() == false) {
                Player player1 = tempList.get(0);
                Player player2 = tempList.get(tempList.size() - 1);
                tempList.remove(player1);
                if (player1 == player2) {
                    player2 = null;
                } else {
                    tempList.remove(player2);
                }

                Match match = getMatch(player1, player2);
                matches.add(match);
            }

            switch (matches.size()) {
            case 4:
                matchesCorrected.add(matches.get(0));
                matchesCorrected.add(matches.get(3));
                matchesCorrected.add(matches.get(2));
                matchesCorrected.add(matches.get(1));
                break;
            case 8:
                matchesCorrected.add(matches.get(0));
                matchesCorrected.add(matches.get(7));
                matchesCorrected.add(matches.get(4));
                matchesCorrected.add(matches.get(3));
                matchesCorrected.add(matches.get(2));
                matchesCorrected.add(matches.get(5));
                matchesCorrected.add(matches.get(6));
                matchesCorrected.add(matches.get(1));
                break;
            default:
                matchesCorrected = matches;
            }
        }

        Round r = new Round(matchesCorrected, this);
        r.setSingleElimination(true);
        rounds.add(r);
        getTournamentGUI().getRoundTabbedPane().addSingleEliminationTab(cutSize, r.getPanel());

        CryodexController.saveData();
    }

    public StringBuilder appendXML(StringBuilder sb) {

        String playerString = "";
        String seperator = "";
        for (Player p : players) {
            playerString += seperator + p.getSaveId();
            seperator = ",";
        }

        XMLUtils.appendObject(sb, "PLAYERS", playerString);

        XMLUtils.appendList(sb, "ROUNDS", "ROUND", getAllRounds());

        String pointsString = "";
        seperator = "";
        if (points != null) {
            for (Integer p : points) {
                pointsString += seperator + p;
                seperator = ",";
            }
        }

        XMLUtils.appendObject(sb, "ISRANDOMPAIRING", isRandomPairing);
        XMLUtils.appendObject(sb, "POINTS", pointsString);
        XMLUtils.appendObject(sb, "NAME", name);
        XMLUtils.appendObject(sb, "MODULE", getModuleName());
        XMLUtils.appendObject(sb, "SEEDING", seedingEnum);
        XMLUtils.appendStringList(sb, "DEPENDENTTOURNAMENTS", "DEPENDENT", dependentTournaments);

        return sb;
    }

    public void startTournament() {
        generateRound(1);
    }

    public void addPlayer(Player p) {
        getPlayers().add(p);
        triggerDeepChange();
    }

    public void dropPlayer(Player p) {

        getPlayers().remove(p);
        triggerDeepChange();
    }

    public void massDropPlayers(List<Player> playersToDrop) {

        for (Player p : playersToDrop) {
            dropPlayer(p);
        }

        triggerDeepChange();
    }

    public abstract void massDropPlayers(int minScore, int maxCount);

    private void resetRankingTable() {
        getTournamentGUI().getRankingTable().setPlayers(getAllPlayers());
    }

    public int getRoundPoints(int roundNumber) {

        int tournamentPoints = getPointsDefault();

        try {
            if (getPoints() != null && getPoints().isEmpty() == false) {

                tournamentPoints = getPoints().size() >= roundNumber ? getPoints().get(roundNumber - 1) : getPoints().get(getPoints().size() - 1);
            }
        } catch (Exception e) {
            // If a problem occurs then stick to 100. Lets be honest, it's rarely anything else.
            e.printStackTrace();
        }

        return tournamentPoints;
    }

    public void addDependentTournaments(List<Tournament> tournaments) {
        for (Tournament t : tournaments) {
            dependentTournaments.add(t.getName());
        }
    }

    public abstract int getPointsDefault();

    public List<Tournament> getDependentTournaments() {

        if (dependentTournaments == null || dependentTournaments.isEmpty()) {
            return new ArrayList<Tournament>();
        }

        List<Tournament> dependentList = new ArrayList<Tournament>();

        for (Tournament t : CryodexController.getAllTournaments()) {
            if (t instanceof Tournament && dependentTournaments.contains(t.getName())) {
                dependentList.add(t);
            }
        }

        return dependentList;
    }

    public InitialSeedingEnum getSeedingEnum() {
        return seedingEnum;
    }

    public void triggerChange() {
        CryodexController.saveData();
        getTournamentGUI().getRankingTable().resetPlayers();
    }

    public void triggerDeepChange() {
        CryodexController.saveData();
        resetRankingTable();
    }

    public Module getModule() {
        return module;
    }

    public abstract Icon getIcon();

    public abstract String getModuleName();

    public abstract RoundPanel getRoundPanel(List<Match> matches);

    public abstract TournamentComparator<Player> getRankingComparator();

    public abstract TournamentComparator<Player> getRankingNoHeadToHeadComparator();

    public abstract TournamentComparator<Player> getPairingComparator();

    public abstract ExportController getExportController();

    public Round getLatestRound() {
        if (rounds == null || rounds.isEmpty()) {
            return null;
        } else {
            return rounds.get(rounds.size() - 1);
        }
    }

    public boolean isMatchComplete(Match m) {

        boolean winnerChosen = m.getWinner(1) != null;

        return m.isBye() || winnerChosen;
    }

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

    public Match getMatch(Player player1, Player player2) {
        return new Match(player1, player2);
    }

    public abstract ModulePlayer getModulePlayer(Player p);
}
