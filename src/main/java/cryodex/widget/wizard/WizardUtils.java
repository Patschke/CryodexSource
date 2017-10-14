package cryodex.widget.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cryodex.Player;
import cryodex.modules.Tournament;
import cryodex.modules.Tournament.InitialSeedingEnum;

public class WizardUtils {
    
    public static enum SplitOptions {
        GROUP,
        RANKING,
        RANDOM;
    }
    
    public static void createSplitTournament(SplitOptions splitOption){
        
        WizardOptions wizardOptions = TournamentWizard.getInstance().getWizardOptions();
        
        if (wizardOptions.getSplit() > 1) {
            Integer splitNum = wizardOptions.getSplit();

            List<WizardOptions> wizardOptionsList = new ArrayList<WizardOptions>();

            for (int i = 1; i <= splitNum; i++) {
                WizardOptions newWizardOption = new WizardOptions(wizardOptions);

                wizardOptionsList.add(newWizardOption);

                newWizardOption.setName(wizardOptions.getName() + " " + i);
                newWizardOption.setPlayerList(new ArrayList<Player>());
            }

            if (splitOption == SplitOptions.GROUP) {
                Map<String, List<Player>> playerMap = new HashMap<String, List<Player>>();

                // Add players to map
                for (Player p : wizardOptions.getPlayerList()) {
                    List<Player> playerList = playerMap.get(p.getGroupName());

                    if (playerList == null) {
                        playerList = new ArrayList<>();
                        String groupName = p.getGroupName() == null ? "" : p.getGroupName();
                        playerMap.put(groupName, playerList);
                    }

                    playerList.add(p);
                }

                int j = 0;
                for (String groupValue : playerMap.keySet()) {

                    List<Player> playerList = playerMap.get(groupValue);

                    while (playerList.isEmpty() == false) {

                        wizardOptionsList.get(j).getPlayerList().add(playerList.get(0));
                        j = j == splitNum - 1 ? 0 : j + 1;
                        playerList.remove(0);
                    }
                }

                //
                int first = 0;
                int last = wizardOptionsList.size() - 1;
                
                    while (first < last) {

                        if (wizardOptionsList.get(last).getPlayerList().size() % 2 == 0) {
                            last--;
                        } else {
                            if (wizardOptionsList.get(first).getPlayerList().size() % 2 == 1
                                    && wizardOptionsList.get(last).getPlayerList().size() % 2 == 1) {
                                Player p = wizardOptionsList.get(first).getPlayerList()
                                        .get(wizardOptionsList.get(first).getPlayerList().size() - 1);

                                wizardOptionsList.get(first).getPlayerList().remove(p);

                                wizardOptionsList.get(last).getPlayerList().add(p);
                            }
                            first++;
                        }
                    }
                
            } else if (splitOption == SplitOptions.RANKING) {
                List<Player> tempPlayers = WizardUtils.rankMergedPlayers(wizardOptions);

                int ppt = tempPlayers.size() / splitNum;
                int overage = tempPlayers.size() % splitNum;
                for (int j = 0; j < splitNum; j++) {
                    int count = j >= splitNum - overage ? ppt + 1 : ppt;
                    wizardOptionsList.get(j).getPlayerList().addAll(tempPlayers.subList(0, count));
                    tempPlayers = tempPlayers.subList(count, tempPlayers.size());
                }

                for (int i = 0; i < wizardOptionsList.size(); i++) {
                    // if (wizardOptionsList.get(i).getPlayerList().size() %
                    // 2 == 0) {
                    // continue;
                    // }

                    while (i + 1 < wizardOptionsList.size() && (wizardOptionsList.get(i).getPlayerList().size() % 2 == 1
                            || wizardOptionsList.get(i).getPlayerList().size() > wizardOptionsList.get(i + 1).getPlayerList().size())) {
                        Player t1 = wizardOptionsList.get(i).getPlayerList().get(wizardOptionsList.get(i).getPlayerList().size() - 1);
                        wizardOptionsList.get(i).getPlayerList().remove(t1);
                        List<Player> temp = new ArrayList<Player>();
                        temp.addAll(wizardOptionsList.get(i + 1).getPlayerList());
                        wizardOptionsList.get(i + 1).getPlayerList().clear();
                        wizardOptionsList.get(i + 1).getPlayerList().add(t1);
                        wizardOptionsList.get(i + 1).getPlayerList().addAll(temp);
                    }

                }
            } else if(splitOption == SplitOptions.RANDOM){
                List<Player> playerList = wizardOptions.getPlayerList();
                Collections.shuffle(playerList);
                int j = 0;
                while (playerList.isEmpty() == false) {

                    wizardOptionsList.get(j).getPlayerList().add(playerList.get(0));
                    j = j == splitNum - 1 ? 0 : j + 1;
                    playerList.remove(0);
                }

                //
                int first = 0;
                int last = wizardOptionsList.size() - 1;
                
                    while (first < last) {

                        if (wizardOptionsList.get(last).getPlayerList().size() % 2 == 0) {
                            last--;
                        } else {
                            if (wizardOptionsList.get(first).getPlayerList().size() % 2 == 1
                                    && wizardOptionsList.get(last).getPlayerList().size() % 2 == 1) {
                                Player p = wizardOptionsList.get(first).getPlayerList()
                                        .get(wizardOptionsList.get(first).getPlayerList().size() - 1);

                                wizardOptionsList.get(first).getPlayerList().remove(p);

                                wizardOptionsList.get(last).getPlayerList().add(p);
                            }
                            first++;
                        }
                    }
                
            }

            TournamentWizard.getInstance().setVisible(false);

            for (WizardOptions wo : wizardOptionsList) {
                if (wo.getInitialSeedingEnum() == InitialSeedingEnum.IN_ORDER) {
                    List<Player> tempList = WizardUtils.rankMergedPlayers(wo);
                    wo.setPlayerList(tempList);
                }
                wo.getModule().initializeTournament(wo);
            }
        }
    }
    
    public static void createTournament(){
            TournamentWizard.getInstance().setVisible(false);
            WizardOptions wo = TournamentWizard.getInstance().getWizardOptions();
            wo.getModule().initializeTournament(wo);
    }
    
    public static List<Player> rankMergedPlayers(WizardOptions wizardOptions) {
        
    	WizardOptions mergeOptions = new WizardOptions();
    	mergeOptions.setName("");
    	mergeOptions.setPlayerList(wizardOptions.getPlayerList());
    	mergeOptions.setPoints(wizardOptions.getSelectedTournaments().get(0).getPoints());
    	mergeOptions.setSingleElimination(wizardOptions.isSingleElimination());
    	
    	Tournament mergeTournament = wizardOptions.getModule().createTournament(mergeOptions);
    	
        for (Tournament t : wizardOptions.getSelectedTournaments()) {
            mergeTournament.getAllRounds().addAll(t.getAllRounds());
        }

        List<Player> tempPlayers = new ArrayList<Player>();
        tempPlayers.addAll(wizardOptions.getPlayerList());
        Collections.sort(tempPlayers, mergeTournament.getRankingComparator());
        return tempPlayers;
    }
    
    public static Tournament getMergedTournament(WizardOptions wizardOptions) {
        
    	WizardOptions mergeOptions = new WizardOptions();
    	mergeOptions.setName("");
    	mergeOptions.setPlayerList(wizardOptions.getPlayerList());
    	mergeOptions.setPoints(wizardOptions.getSelectedTournaments().get(0).getPoints());
    	mergeOptions.setSingleElimination(wizardOptions.isSingleElimination());
    	
    	Tournament mergeTournament = wizardOptions.getModule().createTournament(mergeOptions);
    	
        for (Tournament t : wizardOptions.getSelectedTournaments()) {
            mergeTournament.getAllRounds().addAll(t.getAllRounds());
        }
        return mergeTournament;
    }
}
