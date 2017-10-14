package cryodex.modules.etc.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cryodex.CryodexController;
import cryodex.Language;
import cryodex.Main;
import cryodex.modules.Tournament;
import cryodex.widget.ComponentUtils;
import cryodex.widget.wizard.TournamentWizard;
import cryodex.widget.wizard.WizardOptions;
import cryodex.widget.wizard.pages.Page;

public class MainPage implements Page {

	JTextField nameTextField;

	JPanel pagePanel;

	@Override
	public JPanel getPanel() {

		TournamentWizard.getInstance().setButtonVisibility(null, true, null);

		TournamentWizard.getInstance().setMinimumSize(new Dimension(450, 500));

		if (pagePanel == null) {
			JPanel namePanel = new JPanel(new BorderLayout());

			JLabel nameHeader = new JLabel("<HTML><H1>" + Language.name_event + "</H1></HTML>");

			nameTextField = new JTextField(10);

			namePanel.add(ComponentUtils.addToFlowLayout(nameHeader, FlowLayout.LEFT), BorderLayout.NORTH);
			namePanel.add(ComponentUtils.addToFlowLayout(nameTextField, FlowLayout.LEFT));

			pagePanel = ComponentUtils.addToFlowLayout(
					ComponentUtils.addToVerticalBorderLayout(null, namePanel, null), FlowLayout.CENTER);
		}
		return pagePanel;
	}

	@Override
	public void onNext() {

		WizardOptions wizardOptions = TournamentWizard.getInstance().getWizardOptions();

		wizardOptions.setName(nameTextField.getText());

		for (Tournament t : CryodexController.getAllTournaments()) {
			if (wizardOptions.getName().equals(t.getName())) {
				JOptionPane.showMessageDialog(Main.getInstance(),
						"Tournament name already used. Please pick something different.");
				return;
			}
		}

		TournamentWizard.getInstance().setCurrentPage(new PlayerSelectionPage());
	}

	@Override
	public void onPrevious() {
		// Do nothing
	}

	@Override
	public void onFinish() {
		// Do nothing
	}
}