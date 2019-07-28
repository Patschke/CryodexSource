package cryodex.widget;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class WelcomePage extends JPanel {

	private static final String content = "<HTML><h1>Welcome to Cryodex!!!</h1><br><b>Basic Instructions</b><br>1. Add players in the registration panel<br>2. Select create tournament from the tournament menu<HTML>";
	private static final String saveProblem = "<br><br><b>Save Problem</b><br>If the warning bar at the bottom says you're not protected, there was a problem saving the tournament. This is usually a folder permissions issue.<br> - Go to the file menu and select a temporary save location<br> - Make sure you pick a location that isn't a protected folder so Java can save to it.<br> - The next time you start Cryodex, you'll have to load that save file.";
	private static final String somethingFailed = "<br><br><b>Something Went Wrong</b><br>If something goes wrong, feel free to message me on facebook or send me an email. I can't fix it if you don't tell me what's wrong, and you might get lucky and I can fix it on the fly. Drop a message in the Cryodex facebook page and maybe another user can help if you need immediate assistance and I'm not available.";
	
	private static final long serialVersionUID = 1L;

	public WelcomePage() {
		super(new BorderLayout());
		addContentPanel();
	}
	
	private void addContentPanel(){
		JLabel contentLabel = new JLabel(content + saveProblem + somethingFailed);
		
		add(contentLabel, BorderLayout.NORTH);
		add(new JLabel(), BorderLayout.CENTER);
	}
}
