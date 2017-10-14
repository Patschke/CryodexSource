package cryodex.modules.swlcg.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import cryodex.CryodexController;
import cryodex.Language;
import cryodex.Player;
import cryodex.modules.Tournament;
import cryodex.modules.swlcg.SWLCGPlayer;
import cryodex.modules.swlcg.SWLCGTournament;
import cryodex.widget.ComponentUtils;
import cryodex.widget.SpringUtilities;
import cryodex.widget.wizard.TournamentWizard;
import cryodex.widget.wizard.WizardOptions;
import cryodex.widget.wizard.WizardUtils;
import cryodex.widget.wizard.pages.Page;

public class ProgressionCutPage implements Page {

    private JPanel pagePanel = null;
    private final Map<SWLCGTournament, JCheckBox> checkBoxMap = new HashMap<SWLCGTournament, JCheckBox>();
    private JLabel maxPlayersLabel = null;
    private JLabel minPointsLabel = null;
    private JTextField maxPlayersTF = null;
    private JTextField minPointsTF = null;

    @Override
    public JPanel getPanel() {

        TournamentWizard.getInstance().setButtonVisibility(true, true, false);

        TournamentWizard.getInstance().setMinimumSize(new Dimension(450, 500));

        if (pagePanel == null) {
            pagePanel = new JPanel(new BorderLayout());

            JLabel header = new JLabel("<HTML><H3>" + Language.select_tournaments + "</H3></HTML>");

            JPanel listPanel = new JPanel(new SpringLayout());

            for (Tournament t : CryodexController.getAllTournaments()) {
                JCheckBox cb = new JCheckBox(t.getName());
                if (t instanceof SWLCGTournament) {
                    checkBoxMap.put((SWLCGTournament) t, cb);
                }

                listPanel.add(cb);
            }

            SpringUtilities.makeCompactGrid(listPanel, listPanel.getComponentCount(), 1, 0, 0, 0, 0);

            JLabel playersFromLabel = new JLabel("<HTML><H3>" + Language.additional_information + "</H3></HTML>");

            maxPlayersLabel = new JLabel(Language.max_players);
            minPointsLabel = new JLabel(Language.min_points);

            maxPlayersTF = new JTextField(3);
            minPointsTF = new JTextField(3);

            JPanel maxPlayer = ComponentUtils.addToHorizontalBorderLayout(maxPlayersLabel, maxPlayersTF, null);
            JPanel minPoints = ComponentUtils.addToHorizontalBorderLayout(minPointsLabel, minPointsTF, null);

            JPanel infoPanel = ComponentUtils.addToVerticalBorderLayout(playersFromLabel, maxPlayer, minPoints);

            pagePanel.add(ComponentUtils.addToFlowLayout(header, FlowLayout.LEFT), BorderLayout.NORTH);
            pagePanel.add(ComponentUtils.addToFlowLayout(listPanel, FlowLayout.LEFT), BorderLayout.CENTER);
            pagePanel.add(ComponentUtils.addToFlowLayout(infoPanel, FlowLayout.CENTER), BorderLayout.SOUTH);
        }
        return ComponentUtils.addToFlowLayout(pagePanel, FlowLayout.CENTER);
    }

    @Override
    public void onNext() {
        WizardOptions wizardOptions = TournamentWizard.getInstance().getWizardOptions();
        List<Tournament> tournamentList = new ArrayList<Tournament>();
        Set<Player> playerList = new TreeSet<Player>();
        Integer playerCount = null;
        Integer minPoints = null;

        try {
            playerCount = Integer.parseInt(maxPlayersTF.getText());
        } catch (NumberFormatException e) {
            // Leave it as null
        }

        try {
            minPoints = Integer.parseInt(minPointsTF.getText());
        } catch (NumberFormatException e) {
            // Leave it as null
        }

        for (SWLCGTournament t : checkBoxMap.keySet()) {
            if (checkBoxMap.get(t).isSelected()) {
                tournamentList.add(t);
                playerList.addAll(t.getPlayers());
            }
        }
        
        if(tournamentList.isEmpty()){
            return;
        }

        wizardOptions.setPlayerList(new ArrayList<Player>(playerList));
        wizardOptions.setSelectedTournaments(tournamentList);

        List<Player> rankedPlayers = WizardUtils.rankMergedPlayers(wizardOptions);
        SWLCGTournament mergedTournament = (SWLCGTournament) WizardUtils.getMergedTournament(wizardOptions);

        List<Player> playersToAdd = new ArrayList<Player>();

        for (Player p : rankedPlayers) {
        	
        	SWLCGPlayer xp = mergedTournament.getSWLCGPlayer(p);
        	
            if (playerCount != null && playersToAdd.size() >= playerCount) {
                break;
            }

            if (minPoints != null && xp.getScore(mergedTournament) < minPoints) {
                continue;
            }
            
            playersToAdd.add(p);
        }

        wizardOptions.setPlayerList(playersToAdd);

        TournamentWizard.getInstance().setCurrentPage(new AdditionalOptionsPage());
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