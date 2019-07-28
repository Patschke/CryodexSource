package cryodex.widget.wizard.pages;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cryodex.Language;
import cryodex.Main;
import cryodex.modules.WizardController;
import cryodex.widget.ComponentUtils;
import cryodex.widget.SpringUtilities;
import cryodex.widget.wizard.TournamentWizard;

public class SplitTournamentPage implements Page {

	private JPanel pagePanel = null;
	private WizardController wizardController;

	private List<JSpinner> spinners;
	private JLabel countLabel;

	public SplitTournamentPage(WizardController wizardController) {
		this.wizardController = wizardController;
	}

	@Override
	public JPanel getPanel() {

		TournamentWizard.getInstance().setButtonVisibility(true, true, false);

		TournamentWizard.getInstance().setMinimumSize(new Dimension(450, 550));

		if (pagePanel == null) {
			pagePanel = new JPanel(new BorderLayout());

			JLabel header = new JLabel("<HTML><H3>" + Language.split_tournaments + "</H3></HTML>");

			JPanel listPanel = new JPanel(new SpringLayout());

			int count = 0;
			
			for(JSpinner spinner : getSpinners()){
				
				count++;
				
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(new JLabel(TournamentWizard.getInstance().getWizardOptions().getName() + " - " + count), BorderLayout.WEST);
				panel.add(spinner, BorderLayout.CENTER);
				
				listPanel.add(panel);
			}

			SpringUtilities.makeCompactGrid(listPanel, listPanel.getComponentCount(), 1, 0, 0, 0, 0);

			pagePanel.add(ComponentUtils.addToFlowLayout(header, FlowLayout.LEFT), BorderLayout.NORTH);
			pagePanel.add(ComponentUtils.addToFlowLayout(updateCountLabel(), FlowLayout.LEFT), BorderLayout.CENTER);
			pagePanel.add(ComponentUtils.addToFlowLayout(listPanel, FlowLayout.LEFT), BorderLayout.SOUTH);
		}
		return ComponentUtils.addToFlowLayout(pagePanel, FlowLayout.CENTER);
	}
	
	private JLabel updateCountLabel(){
		
		if(countLabel == null){
			countLabel = new JLabel("");
		}
		
		int totalCount = 0;
		for(JSpinner spinner : getSpinners()){
			int spinnerValue = (int) spinner.getValue();
			totalCount += spinnerValue;
		}
		
		int totalPlayerCount = TournamentWizard.getInstance().getWizardOptions().getPlayerList().size();
		
		String labelText = "Players allocated " + totalCount + "/" + totalPlayerCount;
		
		countLabel.setText(labelText);
		
		return countLabel;
	}

	private List<JSpinner> getSpinners() {
		if (spinners == null) {
			spinners = new ArrayList<JSpinner>();
			
			int splits = TournamentWizard.getInstance().getWizardOptions().getSplit();
			int playerCount = TournamentWizard.getInstance().getWizardOptions().getPlayerList().size();
			int playersPerTournament = playerCount/splits;
			
			if(playersPerTournament % 2 != 0){
				playersPerTournament++;
			}
			
			List<Integer> splitCounts = new ArrayList<Integer>();
			
			for(int count = 0 ; count < splits-1 ; count++){
				splitCounts.add(playersPerTournament);
			}
			int finalCount = playerCount - ((splits-1) * playersPerTournament);
			splitCounts.add(finalCount);
			
			for(Integer splitCount : splitCounts){
				SpinnerModel model = new SpinnerNumberModel(splitCount.intValue(), 0, playerCount, 1);
				model.addChangeListener(new ChangeListener() {
					
					@Override
					public void stateChanged(ChangeEvent e) {
						updateCountLabel();
					}
				});
				JSpinner spinner = new JSpinner(model);
				
				spinners.add(spinner);
			}
		}

		return spinners;
	}

	@Override
	public void onNext() {

		List<Integer> splitCountPerTournament = new ArrayList<Integer>();
		
		int totalCount = 0;
		for(JSpinner spinner : getSpinners()){
			int spinnerValue = (int) spinner.getValue();
			totalCount += spinnerValue;
			splitCountPerTournament.add(spinnerValue);
		}
		
		int totalPlayerCount = TournamentWizard.getInstance().getWizardOptions().getPlayerList().size();
		
		if(totalCount != totalPlayerCount){
            JOptionPane.showMessageDialog(Main.getInstance(),
                    "Please correct so that total is: " + totalPlayerCount);
            return;
		}
		
		TournamentWizard.getInstance().getWizardOptions().setSplitCountPerTournament(splitCountPerTournament);

		TournamentWizard.getInstance().setCurrentPage(wizardController.getAdditionalOptionsPage());

	}

	@Override
	public void onPrevious() {
		TournamentWizard.getInstance().goToPrevious();
	}

	@Override
	public void onFinish() {
		// Do nothing
	}
}