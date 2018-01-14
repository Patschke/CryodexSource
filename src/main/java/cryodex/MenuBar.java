package cryodex;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cryodex.export.PlayerExport;
import cryodex.widget.AboutPanel;
import cryodex.widget.TournamentMenu;

public class MenuBar extends JMenuBar {

    private static final long serialVersionUID = 1L;

    private JMenu fileMenu;
    private JMenu viewMenu;
    private TournamentMenu tournamentMenu;
    private JMenu helpMenu;

    private JCheckBoxMenuItem showTableNumbers;
    private JCheckBoxMenuItem showQuickFind;
	private JCheckBoxMenuItem hideCompleted;
	private JCheckBoxMenuItem showKillPoints;
	private JCheckBoxMenuItem onlyEnterPoints;


    private static MenuBar instance;

    public static MenuBar getInstance() {
        if (instance == null) {
            instance = new MenuBar();
            instance.resetMenuBar();
        }
        return instance;
    }

    private MenuBar() {

        this.add(getFileMenu());
        this.add(getViewMenu());
        this.add(getTournamentMenu().getMenu());

        this.add(getHelpMenu());
    }

    private TournamentMenu getTournamentMenu() {
		if(tournamentMenu == null){
			tournamentMenu = new TournamentMenu();
		}
		return tournamentMenu;
	}

	public JMenu getFileMenu() {
        if (fileMenu == null) {
            fileMenu = new JMenu("File");
            fileMenu.setMnemonic('F');

            JMenuItem loadSaveFile = new JMenuItem("Load from save file");
            loadSaveFile.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    CryodexController.loadFromSaveFile();
                }
            });

            JMenuItem chooseSaveLocation = new JMenuItem("Choose temporary save location");
            chooseSaveLocation.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    CryodexController.chooseSaveLocation();
                }
            });

            JMenuItem importPlayers = new JMenuItem("Import Players");
            importPlayers.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(Main.getInstance(),
                            "Import format is CSV with quotation marks. Legal columns are as follows.\n\n"
                                    + "\"Name\",\"First Name\",\"Last Name\",\"Email Address\",\"Group\",\"Squad\",\"Faction\"\n"
                                    + "\"Chris Brown\",\"Chris\",\"Brown\",\"chris.brown.spe@gmail.com\",\"Fort Wayne\",\"584074\",\"REBEL\"");

                    PlayerImport.importPlayers();
                }
            });

            JMenuItem exportPlayers = new JMenuItem("Export Players");
            exportPlayers.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    PlayerExport.exportPlayersDetail();
                }
            });

            JMenuItem exit = new JMenuItem("Exit");
            exit.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Main.getInstance().dispose();
                }
            });

            fileMenu.add(loadSaveFile);
            fileMenu.add(chooseSaveLocation);
            fileMenu.add(importPlayers);
            fileMenu.add(exportPlayers);
            fileMenu.add(exit);
        }

        return fileMenu;
    }

    public JMenu getViewMenu() {
        if (viewMenu == null) {
            viewMenu = new JMenu("View");
            viewMenu.setMnemonic('V');

            showTableNumbers = new JCheckBoxMenuItem("Show Table Numbers");
            showTableNumbers.setSelected(true);
            showTableNumbers.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    CryodexController.getOptions().setShowTableNumbers(showTableNumbers.isSelected());
                }
            });

            showQuickFind = new JCheckBoxMenuItem("Show Quick Table Search");
            showQuickFind.setSelected(false);
            showQuickFind.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    CryodexController.getOptions().setShowQuickFind(showQuickFind.isSelected());
                }
            });

            final JCheckBoxMenuItem showRegistrationPanel = new JCheckBoxMenuItem("Show Registration Panel");
            showRegistrationPanel.setSelected(true);
            showRegistrationPanel.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent arg0) {
                    Main.getInstance().getRegisterPane().remove(Main.getInstance().getRegisterPanel());
                    if (showRegistrationPanel.isSelected()) {
                        Main.getInstance().getRegisterPane().add(Main.getInstance().getRegisterPanel());
                    }

                    Main.getInstance().validate();
                    Main.getInstance().repaint();
                }
            });

            hideCompleted = new JCheckBoxMenuItem("Hide Completed Matches");
			hideCompleted.setSelected(CryodexController.getOptions().isHideCompleted());
			hideCompleted.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					CryodexController.getOptions().setHideCompleted(hideCompleted.isSelected());
				}
			});

			showKillPoints = new JCheckBoxMenuItem("Show Kill Points");
			showKillPoints.setSelected(CryodexController.getOptions().isShowKillPoints());
			showKillPoints.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					CryodexController.getOptions().setShowKillPoints(showKillPoints.isSelected());
				}
			});

			onlyEnterPoints = new JCheckBoxMenuItem("Only Enter Points");
			onlyEnterPoints.setSelected(CryodexController.getOptions().isEnterOnlyPoints());
			onlyEnterPoints.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					CryodexController.getOptions().setEnterOnlyPoints(onlyEnterPoints.isSelected());
				}
			});
			
			viewMenu.add(showQuickFind);
            viewMenu.add(showTableNumbers);
            viewMenu.add(showRegistrationPanel);
			viewMenu.add(hideCompleted);
			viewMenu.add(showKillPoints);
			viewMenu.add(onlyEnterPoints);
        }

        return viewMenu;
    }

    @Override
    public JMenu getHelpMenu() {
        if (helpMenu == null) {
            helpMenu = new JMenu("Help");
            helpMenu.setMnemonic('H');

            JMenuItem about = new JMenuItem("About");
            about.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    AboutPanel.showAboutPanel();
                }
            });
            JMenuItem whereIsSave = new JMenuItem("Where is my save file?");
            whereIsSave.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    File file = CryodexController.getSaveFile();

                    if (file.exists()) {
                        JOptionPane.showMessageDialog(Main.getInstance(),
                                "<html>Save file can be found at <b>" + file.getAbsolutePath() + "</b></html>");
                    } else {

                        File path = new File(CryodexController.getSavePath());

                        if (path.exists() == false) {
                            JOptionPane.showMessageDialog(Main.getInstance(),
                                    "Save location could not be determined. Check permissions to allow a Java application to save a file.");
                        } else if (file.exists() == false) {
                            JOptionPane.showMessageDialog(Main.getInstance(),
                                    "<html>A save file could not be found. It SHOULD be called <b>" + CryodexController.getSaveFilename()
                                            + "</b> and SHOULD be located in folder <b>" + path.getAbsolutePath() + "</b></html>");
                        }
                    }
                }
            });

            helpMenu.add(about);
            helpMenu.add(whereIsSave);
        }
        return helpMenu;
    }

    public void resetMenuBar() {

        showTableNumbers.setSelected(CryodexController.getOptions().isShowTableNumbers());
        showQuickFind.setSelected(CryodexController.getOptions().isShowQuickFind());
        hideCompleted.setSelected(CryodexController.getOptions().isHideCompleted());
        showKillPoints.setSelected(CryodexController.getOptions().isShowKillPoints());
        onlyEnterPoints.setSelected(CryodexController.getOptions().isEnterOnlyPoints());
    }

    public void updateTournamentOptions(CryodexOptions options) {
        options.setShowTableNumbers(showTableNumbers.isSelected());
        options.setShowQuickFind(showQuickFind.isSelected());
    }

}
