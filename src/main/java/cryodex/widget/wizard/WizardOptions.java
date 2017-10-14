package cryodex.widget.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cryodex.Player;
import cryodex.modules.Module;
import cryodex.modules.Tournament;
import cryodex.modules.Tournament.InitialSeedingEnum;

public class WizardOptions {

    private String name;
    private InitialSeedingEnum initialSeedingEnum;
    private List<Player> playerList;
    private List<Integer> points;
    private int split = 1;
    private boolean isMerge = false;
    private List<Tournament> selectedTournaments;
    private boolean isSingleElimination = false;
    private int minPoints = 0;
    private int maxPlayers = 0;
    private Module module = null;
    private Map<String,String> additionalOptions;

    public WizardOptions() {
    	additionalOptions = new HashMap<String, String>();
    }

    public WizardOptions(WizardOptions wizardOptions) {
        this.name = wizardOptions.getName();
        this.initialSeedingEnum = wizardOptions.getInitialSeedingEnum();
        this.points = wizardOptions.getPoints();
        this.isSingleElimination = wizardOptions.isSingleElimination();
        for(String option : wizardOptions.getAdditionalOptions().keySet()){
        	setOption(option, wizardOptions.getOption(option));
        }
    }

    public Map<String, String> getAdditionalOptions() {
		return additionalOptions;
	}
    
    public String getOption(String optionName){
    	return additionalOptions.get(optionName);
    }
    
    public void setOption(String optionName, String value){
    	additionalOptions.put(optionName, value);
    }

	public InitialSeedingEnum getInitialSeedingEnum() {
        return initialSeedingEnum;
    }

    public void setInitialSeedingEnum(InitialSeedingEnum initialSeedingEnum) {
        this.initialSeedingEnum = initialSeedingEnum;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public void setPoints(Integer points){
        this.points = new ArrayList<>();
        this.points.add(points);
    }
    
    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSplit() {
        return split;
    }

    public void setSplit(int split) {
        this.split = split;
    }

    public boolean isMerge() {
        return isMerge;
    }

    public void setMerge(boolean isMerge) {
        this.isMerge = isMerge;
    }

    public List<Tournament> getSelectedTournaments() {
        return selectedTournaments;
    }

    public void setSelectedTournaments(List<Tournament> selectedTournaments) {
        this.selectedTournaments = selectedTournaments;
    }

    public boolean isSingleElimination() {
        return isSingleElimination;
    }

    public void setSingleElimination(boolean isSingleElimination) {
        this.isSingleElimination = isSingleElimination;
    }

    public int getMinPoints() {
        return minPoints;
    }

    public void setMinPoints(int minPoints) {
        this.minPoints = minPoints;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public Module getModule(){
    	return module;
    }
    
    public void setModule(Module module){
    	this.module = module;
    }
}