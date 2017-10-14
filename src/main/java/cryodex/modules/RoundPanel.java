package cryodex.modules;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public abstract class RoundPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public RoundPanel(BorderLayout borderLayout) {
        super(borderLayout);
    }

    public abstract void markInvalid();
    
    public abstract void resetGamePanels(boolean isTextOnly);
}
