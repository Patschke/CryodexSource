package cryodex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
					headerMap.put(s.replace("\"", ""), i);
					i++;
				}
				
				Integer firstName = headerMap.get("First Name");
				Integer lastName = headerMap.get("Last Name");
				Integer name = headerMap.get("Name");
				Integer emailAddress = headerMap.get("Email Address");
                Integer group = headerMap.get("Group");
                Integer squad = headerMap.get("Squad");
                Integer factionColummn = headerMap.get("Faction");
				
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
					
					if(playerName == null || playerName.isEmpty()){
					    
					    JOptionPane.showMessageDialog(Main.getInstance(), "Could not find a name field. Make sure you have a header titled Name, First Name, or Last Name.", "Name not found!", JOptionPane.ERROR_MESSAGE);
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

	public static boolean importPlayersT3(int tournamentID){
		List<Player> players = new ArrayList<Player>();

		URL tournamentURL = null;
		try {
			tournamentURL = new URL("https://www.tabletoptournaments.net/t3_tournament_list.php?tid=" + String.valueOf(tournamentID));
		} catch (java.net.MalformedURLException ex) {
			return false;
		}
		try {
	        BufferedReader in = new BufferedReader(
		        new InputStreamReader(tournamentURL.openStream()));

		Pattern p = Pattern.compile("<tr><td>[0-9]*\\. <\\/td><td>(?<name>.*)<\\/td><td>.*<\\/td><td class=\"ctr\">.*<\\/td><td>(?<faction>.*)<\\/td><td class=\"ctr\">(?<verein>.*)<\\/td><td class=\"ctr\">(?<bezahlt>.*)<\\/td><\\/tr>");

        	String inputLine;
        	while ((inputLine = in.readLine()) != null){
			Matcher m = p.matcher(inputLine);
			if (m.matches()){
				Player player = getPlayer(m);
				players.add(player);
			}
		}
     		in.close();	
		} catch (java.io.IOException ex) {
			return false;
		}
		Main.getInstance().getRegisterPanel().importPlayers(players);
		return true;
	}

	private static Player getPlayer(Matcher m){
		Player p =  new Player(m.group("name").replaceAll("<.*;(?<a>.*)&.*>","$1"));
					
		p.setGroupName(m.group("verein"));
		p.setActive(m.group("bezahlt").equals("yes"));
		return p;
	}
}
