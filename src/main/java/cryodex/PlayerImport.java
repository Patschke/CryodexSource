package cryodex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cryodex.modules.xwing.XWingPlayer;
import cryodex.modules.xwing.XWingPlayer.Faction;

public class PlayerImport {

	public static void importPlayers(){
		BufferedReader reader = null;
		try {
			JFileChooser fc = new JFileChooser();
			
			int returnVal = fc.showOpenDialog(Main.getInstance());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    
			    int response = JOptionPane.showConfirmDialog(Main.getInstance(), "Do you wish to import the players as active?", "Import as active?", JOptionPane.YES_NO_OPTION);
			    boolean active = response == JOptionPane.YES_OPTION;
			    
			    File file = fc.getSelectedFile();
			    
				reader = new BufferedReader(new FileReader(file));
				
				String line = reader.readLine();
				line = line.replace((char)8221, '"');
				Map<String, Integer> headerMap = new HashMap<String, Integer>();
				
				String[] headers = line.split(",");
				int i = 0;
				for(String s : headers){
					headerMap.put(s, i);
					i++;
				}
				
				Integer firstName = headerMap.get("\"First Name\"");
				Integer lastName = headerMap.get("\"Last Name\"");
				Integer name = headerMap.get("\"Name\"");
				Integer emailAddress = headerMap.get("\"Email Address\"");
                Integer group = headerMap.get("\"Group\"");
                Integer squad = headerMap.get("\"Squad\"");
                Integer factionColummn = headerMap.get("\"Faction\"");
				
				List<Player> players = new ArrayList<Player>();

				line = reader.readLine();
				
				while(line != null){
				    line = line.replace((char)8221, '"');
					String[] playerLine = line.split(",");
					
					String playerName = null;
					
					if(firstName != null && lastName != null){
						playerName = playerLine[firstName] + " " + playerLine[lastName];
					} else if(name != null){
						playerName = playerLine[name];
					}
					
					playerName = playerName.replace("\"", "");
					
					Player p = new Player(playerName);
					
					p.setEmail(getString(emailAddress, playerLine));
					p.setGroupName(getString(group, playerLine));
					p.setActive(active);
					
					XWingPlayer xp = new XWingPlayer(p);
					xp.setSquadId(getString(squad, playerLine));
					
					try {
                        Faction faction = Faction.valueOf(getString(factionColummn, playerLine));
                        xp.setFaction(faction);
                    } catch (Exception e) {
                        // Do nothing on failure
                    }
					
					p.getModuleInfo().add(xp);
					
					players.add(p);

					line = reader.readLine();
				}

				Main.getInstance().getRegisterPanel().importPlayers(players);
			}
		} catch (Exception e) {
			try {
				if(reader != null){
					reader.close();
				}
			} catch (IOException e1) {}
			e.printStackTrace();
		}
	}
	
	private static String getString(Integer column, String[] playerLine) {
	    String returnString = null;
	    
	    if(column != null && column < playerLine.length){
            returnString = playerLine[column];
            returnString = returnString.replace("\"", "");
        }
	    
	    return returnString;
    }
}
