# project-part3
final project part 3

Make sure you have a package `projectview`

### STATES
 
To control the action of the simulator and the enabling of control features we need states.
 
In package projectview, open the enum `States`

```java
package projectview;
public enum States {
	AUTO_STEPPING, 
	NOTHING_LOADED,
	PROGRAM_HALTED,
	PROGRAM_LOADED_NOT_AUTOSTEPPING;
	
	public void enter() {}
}
```

We will show some of the power of Java enums. It localizes in one place a number of characteristics of the simulator. The order of layout of an enum is strange. The list of the individual constants must go first separated by commas, then a semicolon at the end of all of them. Each constant is a singleton object and the declaration of its data goes after the semicolon (each object has its own copy of the data) and common code of the whole abstract enum goes last. As shown above, the constants are AUTO_STEPPING, NOTHING_LOADED, PROGRAM_HALTED, PROGRAM_LOADED_NOT_AUTOSTEPPING.
 
Now we work on the part after the semicolon. We will use the following named constants to be clear about their significance:
 
```java
private static final int ASSEMBLE = 0;
private static final int CLEAR = 1;
private static final int LOAD = 2; 
private static final int RELOAD = 3;
private static final int RUN = 4;
private static final int RUNNING = 5;
private static final int STEP = 6;
private static final int CHANGE_JOB = 7; 
```
Next declare an array of boolean called `states` of length 8.
 
Next change the `enter` method above to be abstract: `public abstract void enter();` which will be overridden in each enum constant. Take out the `{}` above.
 
Next copy the following predicates (a predicate is a function that returns true or false):
 
```java
public boolean getAssembleFileActive() {
    return states[ASSEMBLE];
}
public boolean getClearActive() {
    return states[CLEAR];
}
public boolean getLoadFileActive() {
    return states[LOAD];
}
public boolean getReloadActive() {
    return states[RELOAD];
}
public boolean getRunningActive() {
    return states[RUNNING];
}
public boolean getRunPauseActive() {
    return states[RUN];
}
public boolean getStepActive() {
    return states[STEP];
}
public boolean getChangeJobActive() {
	return states[CHANGE_JOB];
}
``` 

Finally we go back to the enums constants and override the enter method. You should begin to see that the bottom part of the enum is like an abstract class with a possible mix of fields, constants, concrete methods, abstract methods and a constructor. Each enum constant such as NOTHING_LOADED describes a concrete subclass object of that abstract class and has to override any abstract methods. The constants have their own data and can override methods! We also use the constants as unique instances of that class.
 
Although we are not using it here, If the abstract bottom part were to have a field someField and a constructor (which is private by default and can only be private), say States(...) {someField = ...; }, then each enum constant would have to indicate what value to give to someField by writing it as part of its definition: NOTHING_LOADED(value), ...
 
In our States enum, each enum constant defines a different arrangement of boolean values.
 
Here is a table for the values in the enter method (where T = true and F = false)
 
```
0	1	2	3	4	5	6	7
F	F	F	F	T	T	F	F   (for AUTO_STEPPING)
T	F	T	F	F	F	F	T   (for NOTHING_LOADED)
T	T	T	T	F	F	F	T   (for PROGRAM_HALTED)
T	T	T	T	T	F	T	T   (for PROGRAM_LOADED_NOT_AUTOSTEPPING)
```

Here is how AUTO_STEPPING appears in the code, the others are similar, except the last constant ends with a semicolon instead of a comma
 
Instead of AUTO_STEPPING, put:
```java
AUTO_STEPPING {
  public void enter(){
    states[ASSEMBLE] = false;
    states[CLEAR] = false;
    states[LOAD] = false;
    states[RELOAD] = false;
    states[RUN] = true;
    states[RUNNING] = true;
    states[STEP] = false;
    states[CHANGE_JOB] = false;
  } 
},
``` 

### Completion of Code

We had a partial version of the Code class but it needs to be completed.

The imports are 

```java
import java.util.Map;
import static project.Model.Mode.*;
```
The fields and methods are 

```java
public static final int CODE_MAX = 1024;
private long[] code = new long[CODE_MAX];
public static Map<Model.Mode, Integer> MODE_NUMBER = Map.of(IMMEDIATE, 1, DIRECT, 2, INDIRECT, 3);
public static Map<Integer, Model.Mode> NUM_MODE = Map.of(1, IMMEDIATE, 2, DIRECT, 3, INDIRECT);
public void setCode(int index, int op, Model.Mode mode, int arg)
public int getOp(int i)
public Model.Mode getMode(int i)
public int getModeNumber(int i)
public int getArg(int i)
public void clear(int start, int end)
public String getHex(int i)
public String getText(int i)
```

Here is to code of `setCode`, which has a lot of bit-level manipulation

```java
// the opcode will use 29 bits, multiplying by
// 8 moves 3 bits to the left
long longOp = op*8;
// put the indirection level in those last 3 bits
int modeNum = 0;
if (mode != null) modeNum = MODE_NUMBER.get(mode);
longOp += modeNum;
long longArg = arg;
// move the opcode and indirLvl to the upper 32 bits
long OpAndArg = longOp << 32;
// if arg was negative, longArg will have 32 leading 1s,
// remove them:
longArg = longArg & 0x00000000FFFFFFFFL;
//join the upper 32 bits and the lower 32 bits
code[index] = OpAndArg | longArg;
```

Here is the code of `getOp` with more bit fiddling

```java
// move upper half to the lower half discarding lower half 
// and the 3 bit of the indirLvl
return (int)(code[i] >> 35);
```

Here is the code of `getMode` with more bit fiddling

```java
// move upper half to the lower half discarding lower half
// then get last 3 bits
int modeNum = (int)(code[i] >> 32)%8;
if(modeNum == 0) return null;
return NUM_MODE.get(modeNum);
```

Here is the code of `getModeNumber`

```java
// move upper half to the lower half discarding lower half
// then get last 3 bits
return (int)(code[i] >> 32)%8;
```

Here is the code of `getArg`

```java
// cut out upper half keeping lower half
return (int)(code[i] & 0x00000000FFFFFFFFL);
```

For the `clear` method write a for loop for `i` from `start` (inclusive) to `end` (exclusive) set `code[i]` to `0L`

The code for `getHex` is messy because a negative `arg` would start with "FFF..."

```java
int arg = getArg(i);
if(arg >= 0) {
	return Integer.toHexString(getOp(i)).toUpperCase() + " " 
	+ Integer.toHexString(getModeNumber(i)).toUpperCase() + " "
	+ Integer.toHexString(arg).toUpperCase();
}
return Integer.toHexString(getOp(i)).toUpperCase() + " " 
+ Integer.toHexString(getModeNumber(i)).toUpperCase() + " -"
+ Integer.toHexString(-arg).toUpperCase();
```

The code for `getText` is quite long also

```java
StringBuilder builder = new StringBuilder();
String mnem = Model.MNEMONICS.get(getOp(i));
builder.append(mnem);
int k = getModeNumber(i);
switch (k) {
case 0: 
	if(Model.NO_ARG_MNEMONICS.contains(mnem)) {
		builder.append(" ");
	} else {
		builder.append(" &");
	}
	break;
case 1: builder.append(" #"); break;
case 2: builder.append(" "); break;
case 3: builder.append(" @");
}
int arg = getArg(i);
if(arg >= 0) {
	builder.append(Integer.toHexString(arg).toUpperCase());
else {
	builder.append('-');
	builder.append(Integer.toHexString(-arg).toUpperCase());
}
return builder.toString();
```

### GUIMediator

In package `projectview`, make a class public class `GUIMediator`. Here are its imports

```java
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import project.Model;
import project.CodeAccessException;
import project.Data;
import project.DivideByZeroException;
```
in `GUIMediator` the fields are:

```java
	private Model model;
	private FilesMgr filesMgr;
	private TimerControl stepControl;
	private JFrame frame;	
	private CodeViewPanel codeViewPanel;
	private DataViewPanel dataViewPanel1;
	private DataViewPanel dataViewPanel2;
	private DataViewPanel dataViewPanel3;
	private ControlPanel controlPanel;
	private ProcessorViewPanel processorPanel;
	private MenuBarBuilder menuBuilder;
```
 
We will build the methods but have to keep returning to `GUIMediator` to uncomment lines as the GUI is developed. Put in all these methods:

```java
public Model getModel()
public void setModel(Model m)
public JFrame getFrame() { }
public void changeToJob(int i) { }
private void notifyObservers(String str) { }
public void clearJob() { }
public void makeReady(String s) { }
public void setCurrentState(States s) { }
public States getCurrentState () { }
public void toggleAutoStep() { }
public void reload() { }
public void assembleFile() { }
public void loadFile() { }
public void setPeriod(int value) { }
public void step() { }
public void execute() { }
  
// some complete methods:
private void createAndShowGUI() {
	stepControl = new TimerControl(this);
	filesMgr = new FilesMgr(this);
	filesMgr.initialize();
	codeViewPanel = new CodeViewPanel(model);
	dataViewPanel1 = new DataViewPanel(model, 0, 240);
	dataViewPanel2 = new DataViewPanel(model, 240, Data.DATA_SIZE/2);
	dataViewPanel3 = new DataViewPanel(model, Data.DATA_SIZE/2, Data.DATA_SIZE);
	controlPanel = new ControlPanel(this);
	processorPanel = new ProcessorViewPanel(model);
	menuBuilder = new MenuBarBuilder(this);
	frame = new JFrame("Simulator");

	JMenuBar bar = new JMenuBar();
	frame.setJMenuBar(bar);
	bar.add(menuBuilder.createFileMenu());
	bar.add(menuBuilder.createExecuteMenu());
	bar.add(menuBuilder.createJobsMenu());

	Container content = frame.getContentPane(); 
	content.setLayout(new BorderLayout(1,1));
	content.setBackground(Color.BLACK);
	frame.setSize(1200,600);
	frame.add(codeViewPanel.createCodeDisplay(), BorderLayout.LINE_START);
	frame.add(processorPanel.createProcessorDisplay(),BorderLayout.PAGE_START);
	JPanel center = new JPanel();
	center.setLayout(new GridLayout(1,3));
	center.add(dataViewPanel1.createMemoryDisplay());
	center.add(dataViewPanel2.createMemoryDisplay());
	center.add(dataViewPanel3.createMemoryDisplay());
	frame.add(center, BorderLayout.CENTER);
	frame.add(controlPanel.createControlDisplay(), BorderLayout.PAGE_END);
	frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	frame.addWindowListener(WindowListenerFactory.windowClosingFactory(e -> exit()));
	frame.setLocationRelativeTo(null);
	model.setCurrentState(States.NOTHING_LOADED);
	model.getCurrentState().enter();
	stepControl.start();
	notifyObservers("");
	frame.setVisible(true);
}

public void exit() { // method executed when user exits the program
  int decision = JOptionPane.showConfirmDialog(
    frame, "Do you really wish to exit?",
    "Confirmation", JOptionPane.YES_NO_OPTION);
  if (decision == JOptionPane.YES_OPTION) {
    System.exit(0);
  }
}
public static void main(String[] args) {
  javax.swing.SwingUtilities.invokeLater(new Runnable() {
    public void run() {
      GUIMediator organizer = new GUIMediator();
      Model model = new Model(
        () -> organizer.setCurrentState(States.PROGRAM_HALTED));
      organizer.setModel(model);
      organizer.createAndShowGUI();
    }
  });
}
```
 
You should comment out all the lines that don't compile because the classes are not ready yet. Complete the _getter_ and _setter_ for `model` and the _getter_ for `frame`.
 
In the `changeToJob` method of `GUIMediator` call `model.changeToJob(i)` (put this public void method in Model and we will fill it next). Still in `GUIMediator`, Make the local variable `States s` and set it to `model.getCurrentState()`. If `s` is not null, call `s.enter()` and call `notifyObservers("")`. That completes `changeToJob` in `GUIMediator`.

If you cloned the repository early, make the following CORRECTION to `Job.java`: change the field name `currentPC` to `currentIP` and change the _getter_ and _setter_ to

```java
public int getCurrentIP() {
	return currentIP;
}
public void setCurrentIP(int currentIP) {
	this.currentIP = currentIP;
}

//also in the method reset() change currentPC to currentIP
```

Go to `Model` and work on the `public void changeToJob(int i)` we just added. In `Model`'s `changeToJob(i)` method, if `i` is not 0 through 3, throw `IllegalArgumentException`. This exception should never happen since the call will only come from a specific part of the GUI and a correct input will always be provided--if there is a bad input, there is a problem somewhere else in the GUI so we let the program crash. Continuing in `changeToJob`, use `setCurrentAcc` and `setCurrentIP` on `currentJob` to put the values of `cpu.accumulator` and `cpu.instructionPointer` into the `currentAcc` and `currentIP` fields of `currentJob`. Then set `currentJob` to `jobs[i]` and change `cpu.accumulator`, `cpu.instructionPointer` and `cpu.memoryBase` to the `currentAcc`, `currentIP`, and `startMemoryIndex` of the new `currentJob` using `currentJob`'s getter methods.
 
In the class `Data` add a method void `clearData(int start, int end)`, which sets `data[i]` to zero for `start <= i < end` and also resets `changedIndex` to -1.
 
For `GUIMediator`, the `notifyObservers` method calls update(str) on `codeViewPanel`, `dataViewPanel1`, `dataViewPanel2`, `dataViewPanel3`, `controlPanel`, `processorPanel`. All these lines will be commented out until the classes have been built.

Write the method `public void clearJob()` as follows. First set `int codeSize` to `model.getCurrentJob().getCodeSize();` Call the following 3 methods on model: `clearJob()`, `setCurrentState(States.NOTHING_LOADED)`, `getCurrentState().enter()`. Then call `notifyObservers("Clear " + codeSize)`;

For the method `public void makeReady(String s)`, first call `stepControl.setAutoStepOn(false)`, then call the following 2 methods on `model`: `setCurrentState(States.PROGRAM_LOADED_NOT_AUTOSTEPPING)` and `getCurrentState().enter()`. Finally call `notifyObservers(s)`

The method `public void setCurrentState(States s)` starts by checking if `s == States.PROGRAM_HALTED`, in which case `stepControl.setAutoStepOn(false)` is called. After that `setCurrentState(s)` and `getCurrentState().enter()`
are called on `model`. Fianlly call notifyObservers("")

The method `public States getCurrentState ()` returns is a delegate emthod, it returns `model.getCurrentState()`.

The mthod `public void toggleAutoStep()`  calls `stepControl.toggleAutoStep()`, then if `stepControl.isAutoStepOn()` call
`model.setCurrentState(States.AUTO_STEPPING)`, else call `model.setCurrentState(States.PROGRAM_LOADED_NOT_AUTOSTEPPING)`. End with
model.getCurrentState().enter() and notifyObservers("")

For `public void reload()` call stepControl.setAutoStepOn(false), then call clearJob(), and 	`filesMgr.finalLoad_ReloadStep(model.getCurrentJob())`.

`public void assembleFile()` is a delegate method, it calls `assembleFile()` on `filesMgr`.

`public void loadFile() is a delegate method, it calls `loadFile(model.getCurrentJob())` on `filesMgr`.

`public void setPeriod(int value) is a delegate method, it calls `setPeriod(value)` on `stepControl`.

The method `step()` is quite long. If `model.getCurrentState()` is _not_ equal to (use !=) `States.PROGRAM_HALTED` and _not_ equal to
`States.NOTHING_LOADED` write

```java 
	try {
		model.step();
	} catch (MemoryAccessException e) {
		JOptionPane.showMessageDialog(frame, 
		"Illegal access to data from line " + model.getInstrPtr() + "\n"
		+ "Exception message: " + e.getMessage(),
		"Run time error",
		JOptionPane.OK_OPTION);
	} catch (CodeAccessException e) {
		// copy the previous catch details but cahnge "data" to "code"
	} catch(NullPointerException e) {
		// copy the previous catch, changing "Illegal access to data" to "NullPointerException"  
	} catch(IllegalArgumentException e) {
		// copy the previous catch, changing "Illegal access to data" to "Program Error"
	} catch(DivideByZeroException e) {
		// copy the previous catch, changing "Illegal access to data" to "Divide by zero"
	}
	notifyObservers("");
}
```

The method `execute()` is similar but wuth 2 changes. Change the "if" at the beginning to "while" and (very important) move the notifyObservers("") OUTSIDE of and AFTER the end of the while loop. 

Back to `Model`. We will finish all the methods, even if some may not compile yet.

```java
// done previously
public int[] getData() returns dataMemory.getData()
public int getInstrPtr() returns cpu.instructionPointer
public int getAccum() returns cpu.accumulator
public Instruction get(int i) returns INSTR[i]
public void setData(int index, int value) calls dataMemory.setData(index, value)
public int getData(int index) returns dataMemory.getData(index)
public void setAccum(int accInit) sets cpu.accumulator to accInit
public void setInstrPtr(int ipInit) sets cpu.instructionPointer to ipInit
public void setMemBase(int offsetInit) sets cpu.memoryBase to offsetInit
public Job getCurrentJob() returns currentJob
public void changeToJob(int i) described above
public Code getCodeMemory() returns codeMemory
public int getChangedIndex() returns dataMemory.getChangedIndex()
public void setCode(int index, int op, Mode mode, int arg) calls codeMemory.setCode(index, op, mode, arg)
public String getHex(int i) returns codeMemory.getHex(i)
public String getText(int i) returns codeMemory.getText(i)
public int getOp(int i) returns codeMemory.getOp(i)
public Mode getMode(int i) returns codeMemory.getMode(i)
public int getArg(int i) returns codeMemory.getArg(i)
public int getMemBase() returns cpu.memoryBase
public States getCurrentState() returns currentJob.getCurrentState()
public void step() discussed below
public void clearJob() discussed below
public void setCurrentState(States state) calls currentJob.setCurrentState(state)
```

The `clearJob` method in `Model` just zeros out the parts of data and code memory corresponding to a Job, puts the cpu in an initial state for that Job. So first call `dataMemory.clearData` to clear data memory from `currentJob.getStartmemoryIndex()` to `currentJob.getStartmemoryIndex()+Data.DATA_SIZE/4`. It then calls `codeMemory.clear()` to clear code memory from `currentJob.getStartcodeIndex()` to `currentJob.getStartcodeIndex()+currentJob.getCodeSize()`. Then set the `cpu.accumulator` to 0 and the `cpu.instructionPointer` to `currentJob.getStartcodeIndex()`. Finally set `cpu.memoryBase` to `currentJob.getStartmemoryIndex()`and call `currentJob.reset()`

`step()` has a try-catch block. The catch is for any `Exception e`. The handler for exception `e` first calls `callBack.halt()` and then has `throw e;` (this means we throw the same exception to the caller of `step()`. Note that the reason the `step()` method does not have to say it throws Exception is that all the possible exceptions from the rest of the body of the try block are unchecked exceptions.

In the try part of the `step()` method get the instruction pointer `ip` from the `cpu`. throw `CodeAccessException` with a message if `ip` is not between `currentJob.getStartcodeIndex()` INCLUSIVE and `currentJob.getStartcodeIndex()+currentJob.getCodeSize()` EXCLUSIVE.
Note about the if block where you throw the exception in `step()`: Be careful about when you negate the following statement. The negation of (startIndex <= ip < endIndex) will be `(startIndex > ip || ip >= endIndex)`. Continuing after the throw, get the `opcode`, `mode` and `arg` from code using `getOp(ip)`, `getMode(ip)` and `getArg(ip)`. Execute the instruction using `get(opcode).execute(arg, mode)`

### Loader

The purpose of the `Loader` is to read a `pexe` file and put the code and data into appropriate locations in `codeMemory` and `dataMemory`. Make a class `Loader` in package `project`. Give us a `public static String load(Model model, File file, int codeOffset, int memoryOffset)`

Make a variable `int codeSize` set to 0. if `model` or `file` is null return null. Start a try-with-resources `try (Scanner input = new Scanner(file))`. Make a variable `boolean incode` set to `true` then while `input.hasNextLine()` do the following: Make a variable `String line` set to `input.nextLine()` then make a variable `Scanner parser` set to `new Scanner(line)`. Make `int op` set to parser.nextInt(16). Make `int indirLvl` and `int arg` both set to 0. 

If `incode` and `op == -1` change `incode` to `false`

else if `incode` do these 6 steps, set `indirLvl` to `parser.nextInt(16)` and `arg` to `parser.nextInt(16)`. Make the variable `Model.Mode mode` set to `null`. If `indirLvl > 0` set `mode` to `Code.NUM_MODE.get(indirLvl)`. Call `model.setCode(codeOffset+codeSize, op, mode, arg)` and then increment `codeSize` (`codeSize++`)

else (we are in data) make the variable `int value` set to `parser.nextInt(16)` and call `model.setData(memoryOffset + op, value)`. Call `parser.close()` before the end of the loop.

That is the end of the while loop and we next call `parser.close()` and return `"" + codeSize`

Here are the catch blocks:

catch for `FileNotFoundException e` return `"File " + file.getName() + " Not Found"`
catch for `CodeAccessException e` return `"Code Memory Access Exception " + e.getMessage()`
catch for `MemoryAccessException e` return `"Data Memory Access Exception " + e.getMessage()`
catch for `NoSuchElementException e` call `e.printStackTrace()`then return `"From Scanner: NoSuchElementException"`

A tester is the following:

```java
public static void main(String[] args) {
	Model model = new Model();
	String s = Loader.load(model, new File("out.pexe"),16,32);
	for(int i = 16; i < 16+Integer.parseInt(s); i++) {
		System.out.println(model.getCodeMemory().getText(i));			
	}
	System.out.println("--");
	System.out.println("4FF " + Integer.toHexString(model.getData(0x20+0x4FF)).toUpperCase());
	System.out.println("0 " + Integer.toHexString(model.getData(0x20)).toUpperCase());
	System.out.println("10 -" + Integer.toHexString(-model.getData(0x20+0x10)).toUpperCase());
}
```

### GUI Components

Copy the class `DataViewPanel` from the files above into `projectview`.  Copy all the `pasm` and `pexe` files and move the `pasm` files to the pasm folder and teh `pexe` files to the `pexe` folder.  

After we write the `Loader` class, we can run `DataViewPanel` to get a sense of what parts of the GUI look like.

Next make a class `CodeViewPanel` in `projectview`. The imports of are the same `DataViewPanel` _plus_ `java.awt.Dimension` and `project.Code`. The private fields are `Model model, Code code, JScrollPane scroller, JTextField[] codeHex = new JTextField[Code.CODE_MAX], JTextField[] codeText = new JTextField[Code.CODE_MAX], int previousColor = -1`. The constructor `public CodeViewPanel(Model mdl)` simply sets `model` to `mdl`. The method `public JComponent createCodeDisplay()` is similar to `createDataDisplay` in `DataViewPanel`. The panels are `panel, innerPanel, numPanel, textPanel, hexPanel,` each set to a `new JPanel()`. Here is a small difference: `panel.setPreferredSize(new Dimension(300,150));` which is intended to remove a horizontal scroll bar in the final GUI. The width (300) may need to be reduced on other computers. Compare all of the following with similar operations in the other class. Set the layouts of `panel` and `innerPanel` to `new BorderLayout()`. Define

```java
Border border = BorderFactory.createTitledBorder(
	BorderFactory.createLineBorder(Color.BLACK), "Code Memory View",
	TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
panel.setBorder(border);
```
Set the layouts of `numPanel, textPanel`, and `hexPanel` to `new GridLayout(0,1)`. In a for loop for `i` from 0 (inclusive) to Code.CODE_MAX (excusive) do the following `numPanel.add(new JLabel(i+": ", JLabel.RIGHT));`, `codeText[i]` and `codeHex[i]` are set to `new JTextField(10)`, then `textPanel.add(codeText[i])` and `hexPanel.add(codeHex[i])`. Add `numPanel` to `innerPanel` at position `BorderLayout.LINE_START`, add `textPanel` to `innerPanel` at position `BorderLayout.CENTER)` and add `hexPanel` to `innerPanel` at position `BorderLayout.LINE_END`. Set `scroller` to `new JScrollPane(innerPanel)`. Add `scroller` to `panel`. Return `panel`. 
 
Here is the `update` method that has been debugged over years, so we'll just give it to you

```java
public void update(String arg1) {
	if(arg1 != null && arg1.equals("Load Code")) {
		code = model.getCodeMemory();
		int offset = model.getCurrentJob().getStartcodeIndex();
		for(int i = offset; 
				i < offset + model.getCurrentJob().getCodeSize(); i++) {
			codeText[i].setText(code.getText(i));
			codeHex[i].setText(code.getHex(i));
		}	
		previousColor = model.getInstrPtr();			
		codeHex[previousColor].setBackground(Color.YELLOW);
		codeText[previousColor].setBackground(Color.YELLOW);
	} else if(arg1 != null && arg1 instanceof String && ((String)arg1).startsWith("Clear")) {
		int offset = model.getCurrentJob().getStartcodeIndex();
		int codeSize = Integer.parseInt(((String)arg1).substring(6).trim());
		for(int i = offset; 
			i < offset + codeSize; i++) {
			codeText[i].setText("");
			codeHex[i].setText("");
		}	
		if(previousColor >= 0 && previousColor < Code.CODE_MAX) {
			codeText[previousColor].setBackground(Color.WHITE);
			codeHex[previousColor].setBackground(Color.WHITE);
		}
		previousColor = -1;
	}		
	if(this.previousColor >= 0 && previousColor < Code.CODE_MAX) {
		codeText[previousColor].setBackground(Color.WHITE);
		codeHex[previousColor].setBackground(Color.WHITE);
	}
	previousColor = model.getInstrPtr();
	if(this.previousColor >= 0 && previousColor < Code.CODE_MAX) {
		codeText[previousColor].setBackground(Color.YELLOW);
		codeHex[previousColor].setBackground(Color.YELLOW);
	} 
	if(scroller != null && code != null && model!= null) {
		JScrollBar bar= scroller.getVerticalScrollBar();
		int pc = model.getInstrPtr();
		if(pc > 0 && pc < Code.CODE_MAX && codeHex[pc] != null) {
			Rectangle bounds = codeHex[pc].getBounds();
			bar.setValue(Math.max(0, bounds.y - 15*bounds.height));
		}
	}
}
```

Here is a test main method

```java
public static void main(String[] args) {
	Model model = new Model();
	CodeViewPanel panel = new CodeViewPanel(model);
	JFrame frame = new JFrame("TEST");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setSize(400, 700);
	frame.setLocationRelativeTo(null);
	frame.add(panel.createCodeDisplay());
	frame.setVisible(true);
	int size = Integer.parseInt(Loader.load(model, new File("pexe/merge.pexe"), 0, 0));
	model.getCurrentJob().setCodeSize(size);
	panel.update("Load Code");
}
```


