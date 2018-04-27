package cryodex;

import java.util.ArrayList;
import java.util.List;

import cryodex.CryodexController.Modules;
import cryodex.modules.Match;
import cryodex.modules.Module;
import cryodex.modules.ModulePlayer;
import cryodex.modules.Round;
import cryodex.modules.Tournament;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class Player implements Comparable<Player>, XMLObject {

	private String name;
	private String groupName;
	private String saveId;
	private String email;
    private boolean firstRoundBye = false;
	private List<ModulePlayer> moduleInfo;
	private boolean isActive = true;

	public Player() {
		this("");
	}

	public Player(String name) {
		this.name = name;

		moduleInfo = new ArrayList<ModulePlayer>();
	}

	public Player(Element e) {
		this.name = e.getStringFromChild("NAME");
		this.groupName = e.getStringFromChild("GROUPNAME");
		this.saveId = e.getStringFromChild("SAVEID");
		this.email = e.getStringFromChild("EMAIL");
        this.firstRoundBye = e.getBooleanFromChild("FIRSTROUNDBYE",false);
        this.isActive = e.getBooleanFromChild("ISACTIVE", true);

		Element moduleInfoElement = e.getChild("MODULE-INFO");

		moduleInfo = new ArrayList<ModulePlayer>();

		if (moduleInfoElement != null) {
			for (Element mp : moduleInfoElement.getChildren()) {
				String moduleName = mp.getStringFromChild("MODULE");
				Module m = Modules.getModuleByName(moduleName);
				if(m != null){
					moduleInfo.add(m.loadPlayer(this, mp));
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroupName() {
		return groupName == null ? "" : groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

    public boolean isFirstRoundBye() {
        return firstRoundBye;
    }

    public void setFirstRoundBye(boolean firstRoundBye) {
        this.firstRoundBye = firstRoundBye;
    }

	public String getSaveId() {
		return saveId;
	}

	public void setSaveId(String saveId) {
		this.saveId = saveId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public List<ModulePlayer> getModuleInfo() {
		return moduleInfo;
	}

	public void setModuleInfo(List<ModulePlayer> moduleInfo) {
		this.moduleInfo = moduleInfo;
	}

	public ModulePlayer getModuleInfoByModule(Module m) {
		String moduleName = Modules.getNameByModule(m);
		for (ModulePlayer mp : getModuleInfo()) {
			if (moduleName.equals(mp.getModuleName())) {
				return mp;
			}
		}

		ModulePlayer player = m.getNewModulePlayer(this);
		getModuleInfo().add(player);

		return player;
	}
	
	public int getByes(Tournament t) {
        int byes = 0;
        for (Match match : getMatches(t)) {
            if (match.isBye()) {
                byes++;
            }
        }
        return byes;
    }
	
    public List<Match> getMatches(Tournament t) {

        List<Match> matches = new ArrayList<Match>();

        if (t != null) {

            for (Round r : t.getAllRounds()) {
                if (r.isSingleElimination()) {
                    continue;
                }
                for (Match m : r.getMatches()) {
                    if (m.getPlayer1() == this || (m.getPlayer2() != null && m.getPlayer2() == this)) {
                        matches.add(m);
                    }
                }
            }

            // Recursively get dependent event matches
            for (Tournament dt : t.getDependentTournaments()) {
                matches.addAll(getMatches(dt));
            }
        }
        return matches;
    }
    
    public List<Match> getCompletedMatches(Tournament t) {

        List<Match> matches = new ArrayList<Match>();

        if (t != null) {

            rounds: for (Round r : t.getAllRounds()) {
                if (r.isSingleElimination()) {
                    continue;
                }
                for (Match m : r.getMatches()) {
                    if (m.getPlayer1() == this || (m.getPlayer2() != null && m.getPlayer2() == this)) {
                    	if(t.isMatchComplete(m)){
                    		matches.add(m);
                    	}
                        continue rounds;
                    }
                }
            }

            // Recursively get dependent event matches
            for (Tournament dt : t.getDependentTournaments()) {
                matches.addAll(getCompletedMatches(dt));
            }
        }
        return matches;
    }

	@Override
	public String toString() {
		return getName();
	}

	public String toXML() {
		StringBuilder sb = new StringBuilder();

		appendXML(sb);

		return sb.toString();
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		XMLUtils.appendObject(sb, "NAME", getName());
		XMLUtils.appendObject(sb, "ID", getSaveId());
		XMLUtils.appendObject(sb, "GROUPNAME", getGroupName());
		XMLUtils.appendObject(sb, "SAVEID", getSaveId());
		XMLUtils.appendObject(sb, "EMAIL", getEmail());
        XMLUtils.appendObject(sb, "FIRSTROUNDBYE", isFirstRoundBye());
        XMLUtils.appendObject(sb, "ISACTIVE", isActive());
		XMLUtils.appendList(sb, "MODULE-INFO", "MODULE-PLAYER", getModuleInfo());

		return sb;
	}

	@Override
	public int compareTo(Player arg0) {
		return this.getName().toUpperCase()
				.compareTo(arg0.getName().toUpperCase());
	}
}
