# project-part4
final project part 4

### See what you can uncomment in GUIMediator and anything left to uncomment in Model. You can even start running the GUIMediator to see if the GUI is starting to work. By the end of this part you will be able to uncomment everything that is not a comment and run and use the whole GUI.

The `pasm` files above all have errors to test your `FullAssembler` the should go in the `pasm` folder.

### TimerControl

Make a class `TimerControl` in `projectview` that imports `javax.swing.Timer`. The private fields are `static final int TICK = 500`, 
`boolean autoStepOn = false`, `Timer timer,` and `GUIMediator gui`. The constructor `public TimerControl(GUIMediator gm)` sets `gui` to `gm`.

Provide _getter_ and _setter_ methods for `autoStepOn`. Write a method `public void toggleAutoStep()` that sets `autoStepOn` to `!autoStepOn`
	
Write a method `void setPeriod(int period)` that calls `timer.setDelay(period)`

Write a method `public void start()` that instantiates `timer` as `new Timer(TICK, e -> {if(autoStepOn) gui.step();})` and calls `timer.start()`
	
Copy the class `WindowListenerFactory` from the repository code above. Note the comment in the code that this way to make it possible to incorporate lambda expressions into a non-functional interface was suggested by the Java 8 lead developer Brian Goetz.

### ProcessorViewPanel

Make a class `ProcessorViewPanel` in `projectview`. The imports are

```java
import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import project.Model;
```
thre
The private fields are `Model model`, and three JTextFields `acc`, `ip` and `base`, all set equal to a `new JTextField()`. The constructor `public ProcessorViewPanel(Model mdl)` sets the value of `model`. 

The method `public JComponent createProcessorDisplay()` is simple. Make a new JPanel and set the layout to `new GridLayout(1,0)`. Add the following 6 items to the panel in this order `new JLabel("Accumulator: ", JLabel.RIGHT)`, `acc`, `new JLabel("Instruction Pointer: ", JLabel.RIGHT)`, `ip`,  `new JLabel("Memory Base: ", JLabel.RIGHT)`, and `base`. Return the panel.

The method `public void update(String arg1)` does the following if `model != null`: `acc.setText("" + model.getAccum())`, `ip.setText("" + model.getInstrPtr())` and `base.setText("" + model.getMemBase())`.

This tester will give a quick check that the class works

```java
public static void main(String[] args) {
	Model model = new Model();
	ProcessorViewPanel panel = new ProcessorViewPanel(model);
	JFrame frame = new JFrame("TEST");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setSize(700, 60);
	frame.setLocationRelativeTo(null);
	frame.add(panel.createProcessorDisplay());
	frame.setVisible(true);
}
```

### ControlPanel

The class `ControlPanel` is in the package `projectview`. The imports are

```java
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
```

The private fields are `GUIMediator gui`, and four JButtons `stepButton`, `clearButton`, `runButton`, and `reloadButton`. `stepButton` is instantiated as `new JButton("Step")`. The other 3 buttons are also instantiated as new JButtons with the labels "Clear", "Run/Pause", and "Reload" in place of "Step".

The method `public JComponent createControlDisplay()` places the buttons in a JPanel and adds ActionListeners to the buttons. Make a JPanel and give it the layout `new GridLayout(1,0)`. Add the four JButton fields to the JPanel. Give the JButtons white backgrounds using `stepButton.setBackground(Color.WHITE)` and similarly for the other 3 buttons. Give the `stepButton` an ActionListener using `stepButton.addActionListener(e -> gui.step())`. Similarly give ActionListeners to the other buttons `e -> gui.clearJob()` for `clearButton`, `e -> gui.toggleAutoStep()` for `runButton`, and `e -> gui.reload()` for `reloadButton`.

Also add the JSlider using

```java
JSlider slider = new JSlider(5,1000);
slider.addChangeListener(e -> gui.setPeriod(slider.getValue()));
panel.add(slider); // panel is my name, change it if you used a different name
```

Finally return the panel ;

The update method depends on the `States`

```java
public void update(String arg1) {
	runButton.setEnabled(gui.getCurrentState().getRunPauseActive());
	stepButton.setEnabled(gui.getCurrentState().getStepActive());
	clearButton.setEnabled(gui.getCurrentState().getClearActive());
	reloadButton.setEnabled(gui.getCurrentState().getReloadActive());
}
```

### FilesMgr

The class `FilesMgr` goes in the package `projectview`. It is responsible for keeping track of where files are being stored, calling the `load` method in `Loader`, the `assemble` method in `Assembler` (or `FullAssembler`) and posting messages about the status of these operations.

The imports are 

```java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import project.Assembler;
import project.Job;
import project.Loader;
import project.Model;
```

The fields are

```java
private GUIMediator gui;
private Model model;
private String defaultDir; 
private String sourceDir; 
private String executableDir; 
private Properties properties = null;
private File[] currentlyExecutingFile = new File[4];
```
The constructor `public FilesMgr(GUIMediator gm)` sets the value of `gui`.

The methods are

```java
public void initialize()
private void locateDefaultDirectory()
void loadPropertiesFile()
public void assembleFile()
public void loadFile(Job job)
void finalLoad_Reload(Job job)
```

The method initialize() just calls `locateDefaultDirectory()` and `loadPropertiesFile()`.

The method locatDefaultDirectory() has the code

```java
//CODE TO DISCOVER THE ECLIPSE DEFAULT DIRECTORY:
//There will be a property file if the progra has been used for a while
//because it which will store the locations of the pasm and pexe files
//but we allow the possibility that it does not exist yet.
File temp = new File("propertyfile.txt");
if(!temp.exists()) {
	PrintWriter out; // make a file that we will delete later
	try {
		out = new PrintWriter(temp);
		out.close();
		defaultDir = temp.getAbsolutePath();
		temp.delete();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
} else {
	defaultDir = temp.getAbsolutePath();
}
// change to forward slashes, making it platform independent
defaultDir = defaultDir.replace('\\','/');
int lastSlash = defaultDir.lastIndexOf('/');
//remove the file name and keep the diretory path
defaultDir  = defaultDir.substring(0, lastSlash + 1);
```

The method `loadPropertiesFile` has the following code. It retrieves the directories for the pasm and pexe files that are stored in the file created by Java in one of the other methods. 

```java
try { // load properties file "propertyfile.txt", if it exists
	properties = new Properties();
	properties.load(new FileInputStream("propertyfile.txt"));
	sourceDir = properties.getProperty("SourceDirectory");
	executableDir = properties.getProperty("ExecutableDirectory");
	// CLEAN UP ANY ERRORS IN WHAT IS STORED:
	if (sourceDir == null || sourceDir.length() == 0 
			|| !new File(sourceDir).exists()) {
		sourceDir = defaultDir;
	}
	if (executableDir == null || executableDir.length() == 0 
			|| !new File(executableDir).exists()) {
		executableDir = defaultDir;
	}
} catch (Exception e) {
	// PROPERTIES FILE DID NOT EXIST
	sourceDir = defaultDir;
	executableDir = defaultDir;
}		

```

The `assemble` method used to assemble a file, which converts a `pasm` file to a `pexe` file. The code is log because it allows you to change and save the directory that you read the `pasm` files from. It then extracts the name of the `pasm` file chosen and uses it to make a name for the `pexe` file. Next is allows you to change and save the directory where the `pexe` files will be stored. Java used Properties and a properties file to retrieve and store persistent information like these preferred directories.

### Note that you must change `Assembler assembler = new Assembler()` to `Assembler assembler = new FullAssembler()` in the middle of this method in order to use your error-checking assembler.

```java
File source = null;
File outputExe = null;
JFileChooser chooser = new JFileChooser(sourceDir);
FileNameExtensionFilter filter = new FileNameExtensionFilter(
		"Pippin Source Files", "pasm");
chooser.setFileFilter(filter);
// CODE TO LOAD DESIRED FILE
int openOK = chooser.showOpenDialog(null);
if(openOK == JFileChooser.APPROVE_OPTION) {
	source = chooser.getSelectedFile();
}
if(source != null && source.exists()) {
	// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
	// WHICH WE WILL ALLOW TO BE DIFFERENT
	sourceDir = source.getAbsolutePath();
	sourceDir = sourceDir.replace('\\','/');
	int lastDot = sourceDir.lastIndexOf('.');
	String inName = source.getName();
	String outName = sourceDir.substring(0, lastDot + 1) + "pexe";			
	int lastSlash = sourceDir.lastIndexOf('/');
	sourceDir = sourceDir.substring(0, lastSlash + 1);
	outName = outName.substring(lastSlash+1); 
	filter = new FileNameExtensionFilter(
			"Pippin Executable Files", "pexe");
	if(executableDir.equals(defaultDir)) {
		chooser = new JFileChooser(sourceDir);
	} else {
		chooser = new JFileChooser(executableDir);
	}
	chooser.setFileFilter(filter);
	chooser.setSelectedFile(new File(outName));
	int saveOK = chooser.showSaveDialog(null);
	if(saveOK == JFileChooser.APPROVE_OPTION) {
		outputExe = chooser.getSelectedFile();
	}
	if(outputExe != null) {
		executableDir = outputExe.getAbsolutePath();
		executableDir = executableDir.replace('\\','/');
		lastSlash = executableDir.lastIndexOf('/');
		executableDir = executableDir.substring(0, lastSlash + 1);
		try { 
			properties.setProperty("SourceDirectory", sourceDir);
			properties.setProperty("ExecutableDirectory", executableDir);
			properties.store(new FileOutputStream("propertyfile.txt"), 
					"File locations");
		} catch (Exception e) {
			// Never seen this happen
			JOptionPane.showMessageDialog(
				gui.getFrame(), 
				"Problem with Java.\n" +
				"Error writing properties file",
				"Warning",
				JOptionPane.OK_OPTION);
		}
		TreeMap<Integer, String> errors = new TreeMap<>();
		// as soon as you have a FullAssembler compiled, change this to
		// Assembler assembler = new FullAssembler(); 
		// and start testing it
		Assembler assembler = new Assembler();
		int errorIndicator = assembler.assemble(source.getAbsolutePath(), 
				outputExe.getAbsolutePath(), errors);
		if (errorIndicator == 0){
			JOptionPane.showMessageDialog(
				gui.getFrame(), 
				"The source was assembled to an executable",
				"Success",
				JOptionPane.INFORMATION_MESSAGE);
		} else {
			StringBuilder sb = new StringBuilder(inName + " has one or more errors\n");
			for(Integer key : errors.keySet()) {
				sb.append(errors.get(key)); sb.append("\n");
			}
			JOptionPane.showMessageDialog(
				gui.getFrame(), 
				sb.toString(),
				"Source code error",
				JOptionPane.INFORMATION_MESSAGE);
		}
	} else {// outputExe still null
		JOptionPane.showMessageDialog(
			gui.getFrame(), 
			"The output file has problems.\n" +
			"Cannot assemble the program",
			"Warning",
			JOptionPane.OK_OPTION);
	}
} else {// source file does not exist
	JOptionPane.showMessageDialog(
		gui.getFrame(), 
		"The source file has problems.\n" +
		"Cannot assemble the program",
		"Warning",
		JOptionPane.OK_OPTION);				
}
```

The `loadFile` method does a lot of set-up to call the next method, finalLoad_Reload, which in turn calls the Loader. This method also allows you to change and remeber the directory where the `pexe` files are stored using Properties.

```java
int index = job.getId();
JFileChooser chooser = new JFileChooser(executableDir);
FileNameExtensionFilter filter = new FileNameExtensionFilter(
		"Pippin Executable Files", "pexe");
chooser.setFileFilter(filter);
// CODE TO LOAD DESIRED FILE
int openOK = chooser.showOpenDialog(null);

if(openOK == JFileChooser.APPROVE_OPTION) {
	currentlyExecutingFile[index] = chooser.getSelectedFile();
}
if(openOK == JFileChooser.CANCEL_OPTION) {
	currentlyExecutingFile[index] = null;
}

if(currentlyExecutingFile[index] != null && currentlyExecutingFile[index].exists()) {
	// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
	executableDir = currentlyExecutingFile[index].getAbsolutePath();
	executableDir = executableDir.replace('\\','/');
	int lastSlash = executableDir.lastIndexOf('/');
	executableDir = executableDir.substring(0, lastSlash + 1);
	try { 
		properties.setProperty("SourceDirectory", sourceDir);
		properties.setProperty("ExecutableDirectory", executableDir);
		properties.store(new FileOutputStream("propertyfile.txt"), 
				"File locations");
	} catch (Exception e) {
		// Never seen this happen
		JOptionPane.showMessageDialog(
			gui.getFrame(), 
			"Problem with Java.\n" +
			"Error writing properties file",
			"Warning",
			JOptionPane.OK_OPTION);
	}			
}
if(currentlyExecutingFile[index] != null) {
	finalLoad_ReloadStep(job);
} else {
	JOptionPane.showMessageDialog(
		gui.getFrame(),  
		"No file selected.\n" +
		"Cannot load the program",
		"Warning",
		JOptionPane.OK_OPTION);
}
```

The method `finalLoad_Reload` is called by the `loadFile` above and the "Reload" button in the `ControlPanel`. It is just there to call the Loader for the current Job. When the Loader is successful, it returns the number of lines of code as a String. If there was failure to load the program because an exception occurred, that will signalled by a return String with more information, which this method shows using a MessageDialog.  

```java
gui.clearJob();
String str = Loader.load(model, currentlyExecutingFile[job.getId()], 
		job.getStartcodeIndex(), job.getStartmemoryIndex());
try {
	int len = Integer.parseInt(str);
	job.setCodeSize(len);
	gui.makeReady("Load Code");
} catch (NumberFormatException e ) {
	JOptionPane.showMessageDialog(
		gui.getFrame(),  
		"The file being selected has problems.\n" +
		str + "\n" +
		"Cannot load the program",
		"Warning",
		JOptionPane.OK_OPTION);
}
```

### MenuBarBuilder

We still have to set up the MenuBar at the top of the GUI. Here is the code with a number of "TODO" lines.

In package `projectview` make a class `MenuBarBuilder`. The imports are

```java
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
```

The private fields are `GUIMediator gui`, `JMenuItem assemble = new JMenuItem("Assemble Source...")` and 7 more JMenuItems: `load` with the text "Load Program...", `exit` with the text "Exit", `go` with the text "Go", job0 with the text "Job 0", `job1` with the text "Job 1", `job2` with the text "Job 2", and `job3` with the text "Job 3",

The constructor `public MenuBarBuilder(GUIMediator gm)` sets the value of `gui` to `gm`.

The method `public JMenu createFileMenu()` does the following. NOTE the accelerators using ALT and CRTL are for Windows. They will work as accelerators in Mac with the standard key combinations also. IF some accelerator oes nto work on Mac, please just select a different letter.

```java
JMenu menu = new JMenu("File");
menu.setMnemonic(KeyEvent.VK_F); // so you can use ALT-F to open the File menu

assemble.setMnemonic(KeyEvent.VK_M); // so ALT-F, ALT-M to call the assembler
assemble.setAccelerator(
	KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
	//so you can use CTRL-M to call the assembler
assemble.addActionListener(e -> gui.assembleFile());
menu.add(assemble);
		
// TODO 
// Write the same 4 instructions for load. 
// Use VK_L for the accelerator
// change the lambda expression to e -> gui.loadFile()

menu.addSeparator();
		
// TODO 
// Write the same 4 instructions for exit. 
// Use VK_E for the accelerator
// change the lambda expression to e -> gui.exit()

return menu;
```

The method `public JMenu createExecuteMenu()` is a simpler version of `createFileMenu`. It makes JMenu menu = new JMenu("Execute") with accelerator key `VK_X`. It only adds the JMenuItem `go` with accelerator `VK_G`. I has the lambda expression `e -> gui.execute()`. At the end return `menu`.

Here is the method `public JMenu createJobsMenu()` with TODOs

```java
JMenu menu = new JMenu("Change Job");
menu.setMnemonic(KeyEvent.VK_J);

job0.setMnemonic(KeyEvent.VK_0);
job0.setAccelerator(KeyStroke.getKeyStroke(
	KeyEvent.VK_0, ActionEvent.CTRL_MASK));
job0.addActionListener(e -> gui.changeToJob(0));
menu.add(job0);

// TODO add 3 more jobs, changing all the 0s to 1, 2, and 3 in turn

return menu
```

It is important to include this `update` method

```java
public void update(String arg1) {
	assemble.setEnabled(gui.getCurrentState().getAssembleFileActive());
	load.setEnabled(gui.getCurrentState().getLoadFileActive());
	go.setEnabled(gui.getCurrentState().getStepActive());
	job0.setEnabled(gui.getCurrentState().getChangeJobActive());
	job1.setEnabled(gui.getCurrentState().getChangeJobActive());
	job2.setEnabled(gui.getCurrentState().getChangeJobActive());
	job3.setEnabled(gui.getCurrentState().getChangeJobActive());
}
```

### Running and Testing

You can start running the program and testing the correct and incorrect programs.

The files such as `100rt.pexe` are supposed to run with a run-time exception and should pop up a DialogMessage.

### Here are the errors reported by our program. You may find some other errors or skip some and you may be giving different messages.

```
03e.pasm has one or more errors
Error on line 32: illegal mnemonic

04e.pasm has one or more errors
Error on line 34: data address is not a hex number

05e.pasm has one or more errors
Error on line 15. Illegal blank line in the source file
Error on line 21. Illegal blank line in the source file

06e.pasm has one or more errors
Error on line 15. Illegal blank line in the source file
Error on line 21. Line starts with illegal white space

07e.pasm has one or more errors
Error on line 15. Line starts with illegal white space
Error on line 20. Illegal blank line in the source file

08e.pasm has one or more errors
Error on line 15. Line starts with illegal white space
Error on line 20. Illegal blank line in the source file

09e.pasm has one or more errors
Error on line 33. Illegal blank line in the source file

10e.pasm has one or more errors
Error on line 19: data value is not a hex number
Error on line 20: data address is not a hex number
Error on line 21: data value is not a hex number
Error on line 22: data value is not a hex number
Error on line 23: data address is not a hex number
Error on line 24: data value is not a hex number
Error on line 25: data value is not a hex number
Error on line 26: data address is not a hex number
Error on line 27: data format does not consist of two numbers
Error on line 28: data address is not a hex number
Error on line 29: data address is not a hex number
Error on line 30: data format does not consist of two numbers
Error on line 31: data format does not consist of two numbers
Error on line 34. Illegal blank line in the source file

11e.pasm has one or more errors
Error on line 30: illegal mnemonic
Error on line 31: illegal mnemonic
Error on line 32: illegal mnemonic
Error on line 33: illegal mnemonic
Error on line 35: illegal mnemonic

12e.pasm has one or more errors
Error on line 9: mnemonic must be upper case
Error on line 30: illegal mnemonic
Error on line 34. Illegal blank line in the source file

13e.pasm has one or more errors
Error on line 26: this mnemonic cannot take arguments
Error on line 30: illegal mnemonic

14e.pasm has one or more errors
Error on line 30: illegal mnemonic

15e.pasm has one or more errors
Error on line 8: mnemonic must be upper case
Error on line 27: instruction does not allow immediate addressing

16e.pasm has one or more errors
Error on line 8: illegal mnemonic
Error on line 29: this mnemonic cannot take arguments

17e.pasm has one or more errors
Error on line 5: this mnemonic has too many arguments: E2 1
Error on line 8: this mnemonic is missing an argument
Error on line 19. Illegal blank line in the source file
Error on line 26. Line starts with illegal white space
Error on line 30: this mnemonic cannot take arguments
Error on line 31. Line starts with illegal white space
Error on line 34. Illegal blank line in the source file

18e.pasm has one or more errors
Error on line 5: this mnemonic has too many arguments: # E2
Error on line 28: instruction does not allow immediate addressing

19e.pasm has one or more errors
Error on line 11: this mnemonic has too many arguments: 0 12
Error on line 30. Line starts with illegal white space

20e.pasm has one or more errors
Error on line 10: argument is not a hex number: -1G

21e.pasm has one or more errors
Error on line 32: data address is not a hex number

22e.pasm has one or more errors
Error on line 32: data value is not a hex number

23e.pasm has one or more errors
Error on line 32: data value is not a hex number

24e.pasm has one or more errors
Error on line 12: argument is not a hex number: 100000000
Error on line 31: data format does not consist of two numbers
Error on line 32: data value is not a hex number

25e.pasm has one or more errors
Error on line 33: data value is not a hex number
```

### Here are messages from running the "10?rt" files

```
Illegal access to data from line 11
Exception message: Illegal access to data memory, index -1

Divide by zero from line 10
Exception message: Divide by Zero

Illegal access to code from line -1
Exception message: Illegal access outside of executing code

Divide by zero from line 16
Exception message: Divide by Zero

Divide by zero from line 16
Exception message: Divide by Zero
```

