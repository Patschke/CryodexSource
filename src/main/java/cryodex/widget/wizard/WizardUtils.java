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

            // Create new wizard option objects for each new tournament
            List<WizardOptions> wizardOptionsList = new ArrayList<WizardOptions>();

            for (int i = 1; i <= splitNum; i++) {
                WizardOptions newWizardOption = new WizardOptions(wizardOptions);

                wizardOptionsList.add(newWizardOption);

                newWizardOption.setModule(wizardOptions.getModule());
                newWizardOption.setName(wizardOptions.getName() + " " + i);
                newWizardOption.setPlayerList(new ArrayList<Player>());
            }

            // Both random and ranked just pop off a list
            if (splitOption == SplitOptions.RANDOM || splitOption == SplitOptions.RANKING) {
            	
            	List<Player> tempPlayers = WizardUtils.rankMergedPlayers(wizardOptions);
            	
            	// only difference is random being randomized
            	if(splitOption == SplitOptions.RANDOM){
            		Collections.shuffle(tempPlayers);
            	}
            	
            	for(int index = 0 ; index < wizardOptions.getSplitCountPerTournament().size() ; index++){
            		// Number of players for the tournament
            		int playersForTournament = wizardOptions.getSplitCountPerTournament().get(index);
            		
            		WizardOptions wizardOptionsForSplit = wizardOptionsList.get(index);
            		
            		// Pop X players off list
            		wizardOptionsForSplit.getPlayerList().addAll(tempPlayers.subList(0, playersForTournament));
            		tempPlayers = tempPlayers.subList(playersForTournament, tempPlayers.size());
            	}
            } else if (splitOption == SplitOptions.GROUP){
            	Map<String, List<Player>> playerMap = new HashMap<String, List<Player>>();

                // Add players to map of players by group
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

                    	// Check for full sub tournament
                    	int tournamentPlayerMax = wizardOptions.getSplitCountPerTournament().get(j);
                    	int tournamentPlayerCurrentSize = wizardOptionsList.get(j).getPlayerList().size();
                    	if (tournamentPlayerCurrentSize == tournamentPlayerMax){
                    		j = j == splitNum - 1 ? 0 : j + 1;
                    		continue;
                    	}
                    	
                        wizardOptionsList.get(j).getPlayerList().add(playerList.get(0));
                        j = j == splitNum - 1 ? 0 : j + 1;
                        playerList.remove(0);
                    }
                }
            }
            
            //Close wizard and go!
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

        // Creating the event starts the cache. We need to clear it now that all the rounds have been added.
        for (Player p : tempPlayers){
            mergeTournament.getModulePlayer(p).clearCache();
        }
        
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
