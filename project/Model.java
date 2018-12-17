package project;
import static project.Model.Mode.*;
import static java.util.Map.entry;
import java.util.Map;
import java.util.Set;

import projectview.States;

import static java.util.Map.entry;

public class Model {
	
	private final Instruction[] INSTR = new Instruction[16];
	private CPU cpu = new CPU();
	private Data dataMemory = new Data();
	private Code codeMemory= new Code();
	private HaltCallBack callBack;
	private Job[] jobs = new Job[4];
	private Job currentJob;
	
	public Model() {
		this(() -> System.exit(0));
	}
	
	public Model(HaltCallBack cb) {
		callBack = cb;
		for(int i = 0; i < jobs.length; i++) {
			jobs[i] = new Job();
			jobs[i].setId(i);
			jobs[i].setStartcodeIndex(i*Code.CODE_MAX/4);
			jobs[i].setStartmemoryIndex(i*Data.DATA_SIZE/4);
			jobs[i].getCurrentState().enter();
		}
		currentJob = jobs[0];
		
		//INSTRUCTION element for "NOP"
		INSTR[0x0] = (arg, mode) -> {
			if(mode != null) throw new IllegalArgumentException(
					"Illegal Mode in NOP instruction");
			cpu.instructionPointer++;
		};
		
		//INSTRUCTION element for "LOD"
		INSTR[0x1] = (arg, mode) -> {
			if(mode == null) {
				throw new IllegalArgumentException(
						"Illegal Mode in LOD instruction");
			}
			if(mode != IMMEDIATE) {
				INSTR[0x1].execute(
						dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				cpu.accumulator = arg;
				cpu.instructionPointer++;
			}
		};
		
		//INSTRUCTION element for "STO"
		INSTR[0x2] = (arg, mode) -> {
			if(mode == null|| mode == IMMEDIATE ) {
				throw new IllegalArgumentException(
						"Illegal Mode in STO instruction");
			}
			if(mode != DIRECT) {
				INSTR[0x2].execute(
						dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				//set the dataMemory at index cpu.memoryBase+arg to the value cpu.accumulator
				dataMemory.setData(cpu.memoryBase+arg,cpu.accumulator );
				cpu.instructionPointer++;
			}
		};
		
		
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
		
		//INSTRUCTION entry for "SUB"
		INSTR[0x4] = (arg, mode) -> {
			if(mode == null) {
				throw new IllegalArgumentException(
						"Illegal Mode in SUB instruction");
			}
			if(mode != IMMEDIATE) {
				INSTR[0x4].execute(
						dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				cpu.accumulator -=arg;
				cpu.instructionPointer++;
			}
		};
		
		
		
		//INSTRUCTION entry for "MUL"
		INSTR[0x5] = (arg, mode) -> {
			if(mode == null) {
				throw new IllegalArgumentException(
						"Illegal Mode in MUL instruction");
			}
			if(mode != IMMEDIATE) {
				INSTR[0x5].execute(
						dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				cpu.accumulator *=arg;
				cpu.instructionPointer++;
			}
		};
		
		//INSTRUCTION entry for "DIV"
		INSTR[0x6] = (arg, mode) -> {
			if(mode == null) {
				throw new IllegalArgumentException(
						"Illegal Mode in DIV instruction");
			}
			if(mode != IMMEDIATE) {
				INSTR[0x6].execute(
						dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				if(arg == 0) {throw new DivideByZeroException("Divide by Zero");}
				cpu.accumulator /=arg;
				cpu.instructionPointer++;
			}
		};
		
		//INSTRUCTION entry for "AND"
		INSTR[0x7] = (arg, mode) -> {
			if(mode == null) {
				throw new IllegalArgumentException(
						"Illegal Mode in AND instruction");
			}
			if(mode != IMMEDIATE) {
				INSTR[0x7].execute(
						dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				if(arg != 0 && cpu.accumulator != 0) {
					cpu.accumulator = 1;
				}else {
					cpu.accumulator = 0;
				
				}
				cpu.instructionPointer++;
			}
		};
		
		//INSTRUCTION entry for "NOT"
		INSTR[0x8] = (arg, mode) -> {
			if(mode != null) throw new IllegalArgumentException(
								"Illegal Mode in NOT instruction");
			if(cpu.accumulator != 0) {
				cpu.accumulator = 0;
								
			}else {
				cpu.accumulator = 1;
			}
				cpu.instructionPointer++;
		};
				
		//INSTRUCTION entry for "CMPL"
		INSTR[0x9] = (arg, mode) -> {
			if(mode == null || mode == IMMEDIATE) {
				throw new IllegalArgumentException(
						"Illegal Mode in CMPL instruction");
			}
			if(mode != DIRECT) {
				INSTR[0x9].execute(
						dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				arg = dataMemory.getData(cpu.memoryBase + arg);
					if(arg < 0 ) {
						cpu .accumulator = 1;
				}else {
					cpu .accumulator = 0;
				}
				cpu.instructionPointer++;
			}
		};
		
		//INSTRUCTION entry for "CMPZ"
		INSTR[0xa] = (arg, mode) -> {
			if(mode == null || mode == IMMEDIATE) {
				throw new IllegalArgumentException(
						"Illegal Mode in CMPZ instruction");
			}
			if(mode != DIRECT) {
				INSTR[0xa].execute(
						dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				arg = dataMemory.getData(cpu.memoryBase + arg);
					if(arg == 0 ) {
						cpu .accumulator = 1;
				}else {
					cpu .accumulator = 0;
				}
				cpu.instructionPointer++;
			}
		};
		//INSTRUCTION entry for "JUMP"
		INSTR[0xb] = (arg, mode) -> {
			if(mode == null) {
				arg = dataMemory.getData(cpu.memoryBase + arg);
				cpu.instructionPointer = arg + currentJob.getStartcodeIndex();
			}
			else if (mode !=  IMMEDIATE) {
				INSTR[0xb].execute(
						dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				cpu.instructionPointer += arg;
			}
		};
		//INSTRUCTION entry for "JMPZ"
		INSTR[0xc] = (arg, mode) -> {
			if(cpu.accumulator == 0) {
				if(mode == null) {
					arg = dataMemory.getData(cpu.memoryBase + arg);
					cpu.instructionPointer = arg + currentJob.getStartcodeIndex();
				}
				else if (mode !=  IMMEDIATE) {
					INSTR[0xc].execute(
							dataMemory.getData(cpu.memoryBase + arg), mode.next());
				} else {
					cpu.instructionPointer += arg;
				}
			
			} else {
				cpu.instructionPointer++;
			}
		};

		//INSTRUCTION entry for "HALT"
		INSTR[0xf] = (arg, mode) -> {
			callBack.halt();
		};	

		
		
	}
	static class CPU {
		  	private int accumulator;
			private int instructionPointer;
			private int memoryBase;
		  }
	  
	static enum Mode {
			INDIRECT, DIRECT, IMMEDIATE;
			Mode next() {
				if (this==DIRECT) return IMMEDIATE;
				if (this==INDIRECT) return DIRECT;
				return null;
			}
		}
	  
	  
	  static interface Instruction{
		  void execute(int arg, Mode mode);
	  }
	  
	  public static interface HaltCallBack{
		  void halt();
	  }

	  public static final Map<Integer, String> MNEMONICS = Map.ofEntries(
				entry(0, "NOP"), entry(1, "LOD"), entry(2, "STO"), entry(3, "ADD"),
				entry(4, "SUB"), entry(5, "MUL"), entry(6, "DIV"), entry(7, "AND"),
				entry(8, "NOT"), entry(9, "CMPL"), entry(10, "CMPZ"), entry(11, "JUMP"),
				entry(12, "JMPZ"), entry(15, "HALT"));
			// NOTE THERE IS A DELIBERATE GAP for 13 and 14
	  public static final Map<String, Integer> OPCODES = Map.ofEntries(
				entry("NOP", 0), entry("LOD", 1), entry("STO", 2), entry("ADD", 3),
				entry("SUB", 4), entry("MUL", 5), entry("DIV", 6), entry("AND", 7),
				entry("NOT", 8), entry("CMPL", 9), entry("CMPZ", 10), entry("JUMP", 11),
				entry("JMPZ", 12), entry("HALT", 15));
				// ... you have to complete these entries. They reverse the mappings in MNEMONICS
	  public static final Set<String> NO_ARG_MNEMONICS = Set.of("NOP", "NOT", "HALT");
	  public static final Set<String> IND_MNEMONICS = Set.of("STO", "CMPL", "CMPZ"); 
	  public static final Set<String> JMP_MNEMONICS = Set.of("JUMP", "JMPZ"); 
	  
				
		
	  public int[] getData() {
		  return dataMemory.getData();
	  }
	  public int getInstrPtr() {
		  return cpu.instructionPointer;
	  }
	  public int getAccum() {
		  return cpu.accumulator;
	  }
	  public Instruction get(int i) {
		  return INSTR[i];
	  }
	  public void setData(int index, int value) {
		  dataMemory.setData(index, value);
	  }
	  public int getData(int index) {
		  return dataMemory.getData(index);
	  }
	  public void setAccum(int accInit) {
		  cpu.accumulator = accInit;
	  }
	  public void setInstrPtr(int ipInit) {
		  cpu.instructionPointer = ipInit;
	  }
	  public void setMemBase(int offsetInit) {
		  cpu.memoryBase = offsetInit;
	  }
	  public Job getCurrentJob() {
		  return currentJob;
	  }
	  
	  public void changeToJob(int i) {
		  if(i < 0 || i > 3) {
			  throw new IllegalArgumentException("Cannot be not 0 through 3");		  
		  }
		  
		  currentJob.setCurrentAcc(cpu.accumulator);
		  currentJob.setcurrentIP(cpu.instructionPointer);
		  currentJob = jobs[i];
		  //cpu.accumulator(currentJob.getCurrentAcc());
		  //cpu.instructionPointer(currentJob.getcurrentIP());
		  //cpu.memoryBase(currentJob.getStartmemoryIndex());
		  
		  cpu.accumulator = currentJob.getCurrentAcc();
		  cpu.instructionPointer = currentJob.getcurrentIP();
		  cpu.memoryBase = currentJob.getStartmemoryIndex();
		  
		  
	  }
	  
	  public Code getCodeMemory() { 
		  return codeMemory;
		  
	  }
	  public int getChangedIndex() {
		  return dataMemory.getChangedIndex();
	  }
	  public void setCode(int index, int op, Mode mode, int arg) { 
		  codeMemory.setCode(index, op, mode, arg);
		  
	  }
	  public String getHex(int i) { 
		  return codeMemory.getHex(i);
		  
	  }
	  public String getText(int i) { 
		  return codeMemory.getText(i);
		  
	  }
	  public int getOp(int i) { 
		  return codeMemory.getOp(i);
		  
	  }
	  public Mode getMode(int i) { 
		  return codeMemory.getMode(i);
		  
	  }
	  public int getArg(int i) { 
		  return codeMemory.getArg(i);
		  
	  }
	  public int getMemBase() { 
		  return cpu.memoryBase;
		  
	  }
	  public States getCurrentState() {
		  return currentJob.getCurrentState();
		  
	  }
	  public void step() {
		  try {
			  int ip = cpu.instructionPointer;
			  if(ip < currentJob.getStartcodeIndex() || ip >= currentJob.getStartcodeIndex()+currentJob.getCodeSize() ) {
				  throw new CodeAccessException("Illegal Access, instructionPointer out of Bounds");
			  }
			  int opcode = getOp(ip);
			  Mode mode = getMode(ip);
			  int arg = getArg(ip);
			  get(opcode).execute(arg, mode);
			  
		  }catch(Exception e) {
			  callBack.halt();
			  throw e;
		  }
	  }
	  
	  public void clearJob() {
		  dataMemory.clearData(currentJob.getStartmemoryIndex(), currentJob.getStartmemoryIndex()+Data.DATA_SIZE/4);
		  codeMemory.clear(currentJob.getStartcodeIndex(), currentJob.getStartcodeIndex()+currentJob.getCodeSize());
		  cpu.accumulator = 0;
		  cpu.instructionPointer = currentJob.getStartcodeIndex();
		  cpu.memoryBase = currentJob.getStartmemoryIndex();
		  currentJob.reset();
	  }
	  
	  public void setCurrentState(States state) {
		  currentJob.setCurrentState(state);
	  }
	  
}
