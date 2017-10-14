package cryodex;

import cryodex.modules.Tournament;
import cryodex.xml.XMLObject;
import cryodex.xml.XMLUtils;
import cryodex.xml.XMLUtils.Element;

public class CryodexOptions implements XMLObject {
	private boolean showTableNumbers = true;
	private boolean showQuickFind = false;
	private boolean showKillPoints = true;
	private boolean enterOnlyPoints = true;
	private boolean hideCompletedMatches = false;

	public CryodexOptions() {

	}

	public CryodexOptions(Element e) {
		showTableNumbers = e.getBooleanFromChild("SHOWTABLENUMBERS", false);
		showQuickFind = e.getBooleanFromChild("SHOWQUICKFIND", false);
		showKillPoints = e.getBooleanFromChild("SHOWKILLPOINTS", true);
		enterOnlyPoints = e.getBooleanFromChild("ENTERONLYPOINTS", true);
		hideCompletedMatches = e.getBooleanFromChild("HIDECOMPLETED", false);
	}

	public boolean isShowTableNumbers() {
		return showTableNumbers;
	}

	public void setShowTableNumbers(boolean showTableNumbers) {
		this.showTableNumbers = showTableNumbers;
		updateTournamentVisuals();
	}

	public boolean isShowQuickFind() {
		return showQuickFind;
	}

	public void setShowQuickFind(boolean showQuickFind) {
		this.showQuickFind = showQuickFind;
		updateTournamentVisuals();
	}


	public boolean isHideCompleted() {
	    return hideCompletedMatches;
	}
	
	public void setHideCompleted(boolean hideCompletedMatches) {
	    this.hideCompletedMatches = hideCompletedMatches;
	    updateTournamentVisuals();
	}

	public boolean isShowKillPoints() {
		return showKillPoints;
	}

	public void setShowKillPoints(boolean showKillPoints) {
		this.showKillPoints = showKillPoints;
		updateTournamentVisuals();
	}

	public boolean isEnterOnlyPoints() {
		return enterOnlyPoints;
	}

	public void setEnterOnlyPoints(boolean enterOnlyPoints) {
		this.enterOnlyPoints = enterOnlyPoints;
		updateTournamentVisuals();
	}
	
	private void updateTournamentVisuals() {
		if (CryodexController.isLoading == false && CryodexController.getAllTournaments() != null) {
			for (Tournament t : CryodexController.getAllTournaments()) {
				t.updateVisualOptions();
			}
			CryodexController.saveData();
		}
	}

	@Override
	public StringBuilder appendXML(StringBuilder sb) {

		String moduleString = "";

		XMLUtils.appendObject(sb, "SHOWQUICKFIND", showQuickFind);
		XMLUtils.appendObject(sb, "SHOWTABLENUMBERS", showTableNumbers);
		XMLUtils.appendObject(sb, "NONVISIBLEMODULES", moduleString);
	    XMLUtils.appendObject(sb, "HIDECOMPLETED", hideCompletedMatches);
		XMLUtils.appendObject(sb, "SHOWKILLPOINTS", showKillPoints);
		XMLUtils.appendObject(sb, "ENTERONLYPOINTS", enterOnlyPoints);

		return sb;
	}
}