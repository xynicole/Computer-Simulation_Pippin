package projectview;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class ControlPanel {
	private GUIMediator gui;
	private JButton stepButton = new JButton("Step");
	private JButton clearButton = new JButton("Clear");
	private JButton runButton = new JButton("Run/Pause");
	private JButton reloadButton = new JButton("Reload");
	
	public ControlPanel(GUIMediator gui) {
		this.gui = gui;
	}


	public JComponent createControlDisplay() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1,0));
		
		stepButton.setBackground(Color.WHITE);
		stepButton.addActionListener(e -> gui.step());
		panel.add(stepButton);
		
		clearButton.setBackground(Color.WHITE);
		clearButton.addActionListener(e -> gui.clearJob());
		panel.add(clearButton);
		
		runButton.setBackground(Color.WHITE);
		runButton.addActionListener(e -> gui.toggleAutoStep());
		panel.add(runButton);
		
		reloadButton.setBackground(Color.WHITE);
		reloadButton.addActionListener(e -> gui.reload());
		panel.add(reloadButton);
		
		JSlider slider = new JSlider(5,1000);
		slider.addChangeListener(e -> gui.setPeriod(slider.getValue()));
		panel.add(slider); // panel is my name, change it if you used a different name
		
		return panel;
	}
	
	
	public void update(String arg1) {
		runButton.setEnabled(gui.getCurrentState().getRunPauseActive());
		stepButton.setEnabled(gui.getCurrentState().getStepActive());
		clearButton.setEnabled(gui.getCurrentState().getClearActive());
		reloadButton.setEnabled(gui.getCurrentState().getReloadActive());
	}

}
