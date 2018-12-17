# Final Project--you should work in pairs if possible

This part is due November 28, 11:59pm

Whole project due date will be December 7 but extensions into exam week will be accepted if most of the work has been completed and committed to your Github repository.

AGAIN THIS PART IS DUE NOVEMBER 28 11:59pm

The purpose is to create a simulator for a very simple computer with a GUI to show the computer executing a program (including calculating factorial and doing sorts). Our project was originally inspired by a project called Pippin--from pp.210-214 of "The Analytical Engine, An Introduction to Computer Science Using the Internet" by Rick Decker and Stuart Hirshfield (B Publishing Company, 1998). However, many extensions have been made.

The computer has a Model that describes the CPU, the Instruction type that the CPU can execute. There are also Memories for code and for data, which the Model references. There will be a GUI and as the computer executes a series of instructions, the GUI changes its contents.

<h2>Update 1: Added some clarity and also mentioned an additional int[] getData() method.</h2>

Create two packages `project` and `projectview`. Make a class `Data` with a `public static final int DATA_SIZE` set to 2048 (that may be changed before the project is complete). In `Data`, the private fields are an `int[]` array called `data` of length `DATA_SIZE` and an int called `changedIndex`, initially -1. We need package private methods: a getter method for this array for JUnit testing, `int getData(int index)` and `void setData(int index, int value)` to read and write values from and to this array. In these methods throw `MemoryAccessException` if `index` is negative, or to too large (there are three exception classes in the repository, `MemoryAccessException` is one of them). Throw the unchecked Exception `MemoryAccessException` with the message 

>"Illegal access to data memory, index " + index  

Please see the files above dealing with Exceptions. 

Also provide a getter method for `changedIndex`. In the method `setData(int index, int value)`, assign `changedIndex` to `index` after `data[index]` is set to `value`. The `changedIndex` will be used by the GUI to colorize the location that is changed. Additionally, add another `int[] getData()` method that returns `data`. Note: we have two getData methods, one that returns an `int` and this one which returns an `int[]`.

Make a class `Model`. A lot goes on in this class, which will be built up in steps. In `Model`, make a nested class

```java
  static class CPU {
    ...
  }
```
The private `int` fields for the CPU class are `accumulator`, `instructionPointer`, `memoryBase`.

Also in `Model` put the _enum_ 

```java
static enum Mode {
	INDIRECT, DIRECT, IMMEDIATE;
	Mode next() {
		if (this==DIRECT) return IMMEDIATE;
		if (this==INDIRECT) return DIRECT;
		return null;
	}
}
```

<h2> Update 2: Added additional imports you need and added some clarity about the Maps and Sets being defined. </h2>

Once you have done this, put the following imports at the beginning, just after `package project`. You will need all of them.

```java
import static project.Model.Mode.*;
import static java.util.Map.entry;
import java.util.Map;
import java.util.Set;
```

Also in `Model` put the `static interface Instruction` that declares the method `void execute(int arg, Mode mode)` and the `static interface HaltCallBack` that declares the method `void halt()`. We are declaring these interfaces static so that they exist and are usable by the Model class. We will use them to define Instructions in the Model's value constructor.

Put in these constants. 

```java
public static final Map<Integer, String> MNEMONICS = Map.ofEntries(
	entry(0, "NOP"), entry(1, "LOD"), entry(2, "STO"), entry(3, "ADD"),
	entry(4, "SUB"), entry(5, "MUL"), entry(6, "DIV"), entry(7, "AND"),
	entry(8, "NOT"), entry(9, "CMPL"), entry(10, "CMPZ"), entry(11, "JUMP"),
	entry(12, "JMPZ"), entry(15, "HALT"));
// NOTE THERE IS A DELIBERATE GAP for 13 and 14
public static final Map<String, Integer> OPCODES = Map.ofEntries(
	entry("NOP", 0), entry("LOD", 1), entry("STO", 2), entry("ADD", 3),
	// ... you have to complete these entries. They reverse the mappings in MNEMONICS
public static final Set<String> NO_ARG_MNEMONICS = Set.of("NOP", "NOT", "HALT"); 
```

Note that `entry(...)` will not work if you did not add the `import static java.util.Map.entry`. 

Additionally, please read the comments and finish off the `OPCODES` Mapping as the comment mentions. It is worth saying that a 
MNEMONIC is just a String abbreviation describing some instruction to be executed on the CPU. For example, Instruction 0 will later be defined to be a NOP, or no operation instruction. Instruction 1 will later be defined to be a LOD, or load instruction, and so forth. 

We define these maps so we can easily go from an instruction's corresponding number (opcode) to the instruction's String name (mnemonic), and vise versa. 

The `NO_ARG_MNEMONICS` keeps a set of all instructions that do not take any arguments to execute. These are the NOP, NOT, and HALT instructions. This set may prove useful for us later on.

<h2> Update 3: Added clarity about where Job and States are being put package wise. Also mention need to update Job's currentPC if present.</h2>

We now have to add some support classes: Job (provided in the repository) and a tiny version of States, which will be expanded later. Put `States.java` in the package `projectview`. The class `Job` goes in project, and just stores all the information needed to suspend one program while another is running, and a long list of getters and setters and a `reset` method. 

If there are any occurences of currentPC in `Job` replace them with currentIP for the sake of consistency later on.

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

<h2> Update 4: Fixed some typos and added some clarity about instructions. The lambda expressions go in the VALUE CONSTRUCTOR</h2>

Give `Model` the _private_ fields `final Instruction[] INSTR` an array of length 16, `CPU cpu = new CPU()` and `Data dataMemory = new Data()`, `Code codeMemory= new Code()`, `HaltCallBack callBack`, `Job[] jobs = new Job[4];`, and `Job currentJob`. Note that the shell for the class `Code` is in the repository but is far from complete.

A LOT of work goes into the value constructor `public Model(HaltCallBack cb)` but first provide a no-argument constructor. 

```java
public Model() {
	this(() -> System.exit(0));
}
```

It will not compile until we put in the value constructor `public Model(HaltCallBack cb)`. The first line of this value constructor is `callBack = cb;` Then, still in the value constructor, we initialize the Job array, `jobs`, and `currentJob` field:

```java
for(int i = 0; i < jobs.length; i++) {
	jobs[i] = new Job();
	jobs[i].setId(i);
	jobs[i].setStartcodeIndex(i*Code.CODE_MAX/4);
	jobs[i].setStartmemoryIndex(i*Data.DATA_SIZE/4);
	jobs[i].getCurrentState().enter();
}
currentJob = jobs[0];
```

These 4 Job objects divide the code memory and data memory in to 4 equal parts.

Next we make all the instructions of our computer. Since Instruction is a _functional_ interface, we can use lambda expressions to define how the `void execute(int arg, Mode mode)` method behaves for each instruction. Note that our implementions of the execute behavior are recursive. 

The instructions are (in alphabetical order) ADD, AND, CMPL, CMPZ, DIV, HALT, JMPZ, JUMP, LOD, MUL, NOP, NOT, STO, SUB.

Here is a brief description of each of these instructions: 

* HALT, NOT, NOP take no argument (although we pass 0 to `execute`) and the `Mode` should be null, since it is ignored.
* CMPL, CMPZ, and STO only use DIRECT and INDIRECT Modes. The INDIRECT Mode uses the argument as a dataMemory address _but_ the value at that address is then used as the dataMemory address for the instruction itself. STO is a mnemonic for "Store in memory" and it sets the dataMemory at the index in the instruction to the current value in the accumulator of the CPU. CMPL, CMPZ examine the value in memory and modify the accumulator accordingly as described below.
* JUMP and JMPZ are jump instructions that change the `instructionPointer` and use the 3 Modes in slightly different ways as explained below as well as executing in a special way when the mode in null. When mode is null, we do execute what is best described as an ABSOLUTE mode for these two instructions. 
* The other 6 inststructions use all 3 non-null modes as we will describe.

We will give the complete lambda expression for ADD as an example, please make sure you have a good understanding of what it is doing.

Now, back in the value constructor of `Model`: we will index the INSTR array in hexadecimal 0x0, 0x1, ..., 0xF, skipping 0xD and 0xE, which corresond to 13 and 14 respectively.

Here is the NOP (no operation) instruction. Add it (and all other INSTRUCTIONS being defined via lambda expressions) to the value constructor of Model:

```java
//INSTRUCTION element for "NOP"
INSTR[0x0] = (arg, mode) -> {
	if(mode != null) throw new IllegalArgumentException(
			"Illegal Mode in NOP instruction");
	cpu.instructionPointer++;
};
```

The LOD (load accumulator from memory) is at `INSTR[0x1]`. Compare the description here to the logic in the ADD provided below. First throw `IllegalArgumentException("Illegal Mode in LOD instruction")` if `mode` is null. After that, if `mode != IMMEDIATE` call `INSTR[0x1].execute` with the arguments `dataMemory.getData(cpu.memoryBase + arg)` and `mode.next()`--note this a recursive call--else do 2 things: change `cpu.accumulator` to equal `arg` and increment the instruction pointer as above in NOP. 

The STO (store accumulator into memory) is at `INSTR[0x2]`. Throw `IllegalArgumentException("Illegal Mode in STO instruction")` if `mode` is null or IMMEDIATE. After that, if `mode != DIRECT` call `INSTR[0x2].execute` with the arguments `dataMemory.getData(cpu.memoryBase + arg)` and `mode.next()`, else do 2 things: set the `dataMemory` at index `cpu.memoryBase+arg` to the value `cpu.accumulator` and then increment the instruction pointer as above.

ADD is at `INSTR[0x3]` and adds a value to the `accumulator`. We provide it here for you to add to Model's value constructor:

```java
//INSTRUCTION entry for "ADD"
INSTR[0x3] = (arg, mode) -> {
	if(mode == null) {
		throw new IllegalArgumentException(
				"Illegal Mode in ADD instruction");
	}
	if(mode != IMMEDIATE) {
		INSTR[0x3].execute(
				dataMemory.getData(cpu.memoryBase + arg), mode.next());
	} else {
		cpu.accumulator += arg;
		cpu.instructionPointer++;
	}
};
```
SUB is at `INSTR[0x4]` and is the same as ADD except you have `-=arg` instead of `+=arg`. Also the recurisve call should be to `INSTR[0x4]` not `INSTR[0x3]`. Also update the error message thrown in the IllegalArgumentException appropriately.

MUL is at `INSTR[0x5]` and is the same as ADD except you have `*=arg` instead of `+=arg`. Similar story for the recursive call and IllegalArgumentException message as we changed in SUB.

DIV is at `INSTR[0x6]` and is the same as ADD except you have `/=arg` instead of `+=arg`. Similar story for the recursive call and IllegalArgumentException message. However, additionally before you divide by `arg`, you check for 0, so the `else` part begins with `if(arg == 0) {throw new DivideByZeroException("Divide by Zero");}`, where the exception is one of the files provided.

AND is at `INSTR[0x7]` and is a logical and, where 0 means false and anything elase means true. Throw an appropriate `IllegalArgumentException` if `mode` is null similar to the ones we've been writing in the previous instructions. In the recurisve block, make sure you call `INSTR[0x7]`. The major difference is in the `else` part. if `arg` is not zero _and_ `cpu.accumulator` is not zero, then set `cpu.accumulator` to 1, else set `cpu.accumulator to 0`. After that you still always increment the instruction pointer in the else block. 

NOT is at `INSTR[0x8]` and is logical negation. If `mode` is not null, there is an exception as in NOP. NOT has no recursive block, similar again to the NOP instruction. After the exception block, if `cpu.accumulator` is not 0, set it to 0, else set it to 1. Also increment the instruction pointer to finish off the NOT instruction. 

CMPL is at `INSTR[0x9]` and sets the accumulator to true (1) if the relevant memory location contains a value Less than 0. Throw `IllegalArgumentException("Illegal Mode in CMPL instruction")` if `mode` is null or IMMEDIATE. if `mode != DIRECT` call `INSTR[0x9].execute` with the arguments `dataMemory.getData(cpu.memoryBase + arg)` and `mode.next()`, else set `arg` be `dataMemory.getData(cpu.memoryBase + arg)` and set `cpu .accumulator` to 1 if `arg` is negative and otherwise set `cpu .accumulator` to 0. After that increment the instruction pointer as above.

CMPZ is at `INSTR[0xa]` and is very similar to CMPL. The difference is that the recursion must call `INSTR[0xa]` and we only set `cpu.accumulator` to 1 if `arg` is 0, for all other values `cpu.accumulator` is set to 0. As usual we increment the instruction pointer.

JUMP is at `INSTR[0xb]` and executes a jump by directly changing the instruction pointer. If `mode` is null, set `arg` be `dataMemory.getData(cpu.memoryBase + arg)` and change the `cpu.instructionPointer` to `arg + currentJob.getStartcodeIndex()`. Else if mode is not IMMEDIATE, call `INSTR[0xb].execute` with the arguments `dataMemory.getData(cpu.memoryBase + arg)` and `mode.next()`, else add `arg` to the `cpu.instructionPointer`.

JMPZ is at `INSTR[0xc]`. If `cpu.accumulator` is 0, do everything that the JUMP instruction does (except the recursion calls `INSTR[0xb]`. Otherwise (which is when `cpu.accumulator` is not zero), simply increment the instruction pointer as above.

The HALT instruction is simple and provided here for you:

```java
INSTR[0xf] = (arg, mode) -> {
	callBack.halt();
};	
```

`Model` will need many methods (many delegate to the fields) but here are those that are needed for the InstructorTester JUnit.

* `public int[] getData()` returns `dataMemory.getData()`
* `public int getInstrPtr()` returns `cpu.instructionPointer`
* `public int getAccum()` returns `cpu.accumulator`
* `public Instruction get(int i)` returns `INSTR[i]`
* `public void setData(int index, int value)` calls `dataMemory.setData(index, value)`
* `public int getData(int index)` returns `dataMemory.getData(index)`
* `public void setAccum(int accInit)` sets `cpu.accumulator` to `accInit`
* `public void setInstrPtr(int ipInit)` sets `cpu.instructionPointer` to `ipInit`
* `public void setMemBase(int offsetInit)` sets `cpu.memoryBase` to `offsetInit`
* `public Job getCurrentJob()` returns `currentJob`

NOW WRITE THE INSTRUCTION lambda expressions IN THE VALUE CONSTRUCTOR and test them with the InstructionTester. Make sure all tests are passed. The instructions must be properly implemented or you will not be able to execute simple assembly like programs later on. If you get stuck, reach out to a TA or the professor. We are more than happy to help you out.

There will be more documents coming shortly