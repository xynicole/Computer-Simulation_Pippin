# project-part2
final project part 2

Source files for the machine that we are simulating have the following instruction formats:

```
NOP
LOD  argument1
STO  argument2
ADD  argument1
SUB  argument1
MUL  argument1
DIV  argument1
AND  argument1
NOT
CMPL  argument2
CMPZ  argument2
JUMP  argument3
JMPZ  argument3
HALT
```

where
* argument 1 can start with `#` or `@` followed by an int in hexadeciaml or be just an int in hexadeciaml
* argument 2 can start with `@` followed by an int in hexadeciaml or be just an int in hexadeciaml
* argument 3 can start with `#`, `@`, or `&` followed by an int in hexadeciaml or be just an int in hexadeciaml

The character `#` indicates IMMEDIATE mode, `@` indicates INDIRECT mode, `&` indicates the special "null" mode of JUMP and JMPZ, and a number alone without a leading character indicates DIRECT mode.

### Assembler
The purpose of an Assembler is to convert a source code program into an executable file (which will consist of a list of lines with 3 numbers separated by spaces). Note that normally an executable is a compact binary form but we will use a readable format so it is easier to cheack for errors.

We will start with an assember for correct programs. Writing an error detecting/reporting program is harder and comes later. A complete program may have an optional data part.

```
CMPZ 0
SUB #1
JMPZ #F
CMPL 0
SUB #1
JMPZ #E
LOD 0
STO 1
LOD 0 
SUB #1
STO 0
CMPZ 0
SUB #1
JMPZ #6
LOD 0
MUL 1
JUMP #-9	
NOT
STO 1
HALT
DATA
0 8
```

The uppercase DATA indicates the beginning of data for the programming. If there is no data, the word DATA is also omitted.

The meaning of the program is outlined here:

```
CMPZ 0 compare memory location 0 with 0
SUB #1 only if it is 0, you get 1 in the accumulator so make the accumulator 0
JMPZ #F only if the accumulator is 0 jump to NOT, near the end
CMPL 0 check if the memory location 0 is negative.
SUB #1 if location 0 is negative, you get 1 in the accumulator so make the accumulator 0
JMPZ #E only if the accumulator is 0 jump to HALT, at the end
LOD 0 load the value in memory location 0 into the accumulator
STO 1 store the accumulator in memory location 1 (we do this on each loop iteration)
LOD 0 reload the value in memory location 0 into the accumulator
SUB #1 subtract 1 from the accumulator
STO 0 store the new value in memory location 0
CMPZ 0 compare the value in the memory at 0 with 0
SUB #1 if location 0 is 0, the accumulator has 1, so make it 0
JMPZ #6 if the accumulator is 0, memory location 0 contains 0 so jump to HALT
LOD 0 reload the accumulator with what was in memory location 0
MUL 1 multiply it by the value storing in memory location 1
JUMP #-9 go back to STO 1
NOT we can only get here from line 3 and it changes the accumulator from 0 to 1
STO 1 store that value of 1 in memory location 1
HALT stop
--------
0 8 preload memory location 0 with 8
```

This code computes factorial. The input is at data location 0 and the result is stored at data location 1. The input 0 8 will compute the factorial of 8. Input 0 -1 (any negative number) would give the value 0. Input 0 0 would give the value 1. After than 0 n would give the value factorial(n) for any positive integer n. However for any n > 12, the value will not be correct, since int cannot store very large numbers.

Make a class `Assembler` in package `project` and put in this method. This is will assemble correct programs only. We will give you almost all the code but demonstate how Java 8 Streams handle file input and output of text files. Part of the final project is to write a subclass that does error checking of the input file. That is described later.

The imports are:

```java
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static project.Model.Mode.*;
```

As descibed above, an input file may consist of lines of code, followed by "DATA", then followed by lines of address/data pairs. A sequence of lambda expression will detect all this but needs access to a flag indicating the change from code to data. Because lambda expressions create anonymous inner class objects, variables have to be fields of the outer class.

Give the `Assembler` class a private boolean field `readingCode` initially set to `true`. Next copy in this method, which has a few _TODOs_ for you to complete. To understand what the method does, examples of parts will be `[NOT], [HALT], [LOD, 7], [STO, @F2], [JUMP, &2F], [ADD, #-20]`. For correct code, parts will have length 1 or 2. For length 1, the return value is `Integer.toHexString(opcode).toUpperCase() + " 0 0"`. For length 2, the return value is `Integer.toHexString(opcode).toUpperCase() + " " + indirLvl + " " + parts[1]`, where `parts[1]` would have had the `#`, `@` or `&` removed if present. The calculation of `indirLvl` is shown in the code. 


```java
private String makeOutputCode(String[] parts) {
	int opcode = Model.OPCODES.get(parts[0]);
	if(parts.length == 1) return Integer.toHexString(opcode).toUpperCase() + " 0 0";
	else {
		Model.Mode mode = DIRECT; // the default mode is DIRECT
		if(parts[1].startsWith("#")) {
			mode = IMMEDIATE;
			parts[1] = parts[1].substring(1);
		}
		if(parts[1].startsWith("@")) {
		//TODO same as "#" case but mode = INDIRECT
		}
		if(parts[1].startsWith("&")) {
		//TODO same as "#" case but mode = null
		}
		int indirLvl = 0;
		if(mode != null) {
			indirLvl = Code.MODE_NUMBER.get(mode); // see note below
		}			
		return Integer.toHexString(opcode).toUpperCase() + " " + indirLvl + " " + parts[1];
	}
}
// NOTE: In the class Code, add the Map
// public static Map<Model.Mode, Integer> MODE_NUMBER = Map.of(IMMEDIATE, 1, DIRECT, 2, INDIRECT, 3);
```

Next the `assemble` method `public int assemble(String inputFileName, String outputFileName, TreeMap<Integer, String> errors)`. Note that normally we would have put `Map` as the type of `errors` but we want to enforce a TreeMap so that the key-set is stored in increasing order.

```java
// START OF METHOD
Map<Boolean, List<String>> lists = null;
try (Stream<String> lines = Files.lines(Paths.get(inputFileName))) {
	lists = lines
		.map(line -> line.trim()) //remove any spaces or tabs from the end (or start) of each line
		.filter(line -> line.length() > 0) // remove blank lines at end of file
		.peek(line -> {if(line.equals("DATA")) readingCode = false;}) // change flag if DATA appears
		.filter(line -> !line.equals("DATA")) // discard the "DATA" line from the stream
		.collect(Collectors.partitioningBy(line -> readingCode)); // store separately for 
		// readingCode = true and readingCode = false 
} catch (IOException e) {
	e.printStackTrace();
}
```

To see what just happened, can print with `System.out.println(lists.get(true))` and `System.out.println(lists.get(false))`

```java
// REST OF METHOD
try (PrintWriter output = new PrintWriter(outputFileName)){
	lists.get(true).stream() // get the code lines
	.map(line -> line.split("\\s+")) // split into an array of either one or two strings
	.map(this::makeOutputCode).forEach(output::println); // turn that into output format and save to the file 
	// note this:: is used in the method reference for an instance method in the class
	// for a static method it would be Assembler::methodName
	output.println(-1); // signal replacing "DATA" to separate code from data
	
	// now just save the data as-is since the file is assumed to be in correct format
	lists.get(false).stream().forEach(output::println);
} catch (FileNotFoundException e) {
	e.printStackTrace();
}		
return 0;
```

Here is a simple tester. Please make two folders _at the level of the whole Java Project (this is not in the project package)_ named `pasm` and `pexe`. Put the `merge.pasm` and `factorial.pasm` in the `pasm` folder. There will be a lot of tests and this will help. The files `mymerge.pexe` and `myfactorial8.pexe` are the expected outputs, though there is no reason the output you get will be different. 

```java
public static void main(String[] args) {
	TreeMap<Integer, String> errors = new TreeMap<>();
	Assembler test = new Assembler();
	test.assemble("pasm/merge.pasm", "pexe/merge.pexe", errors);		
}
```

### ERROR CHECKING ASSEMBLER 

This is hard and takes a while to get right--maybe days for some people. Note that part of the Final Project is to write another class `FullAssembler` which is a subclass of Assembler. Override the assemble method so that it does error checking (see later on this page). If errors are found the return value will be a line number where one of the reported errors occurs.

This Full Assembler is arguably the hardest part of this final project. It might be a good idea to save it for last, getting all the gui working (which is coming very soon) and then coming back to it at the end. 
 
The easiest way is to read the file into an ArrayList<String> using a Scanner and let "try/catch with resources" close the file for you.
	
```java
try(Scanner in = new Scanner(new File(inputFileName))) {
// ...
}
```

After the try/catch scan this ArrayList from start to finish (often more than once) until you find an error. Error messages are stored as values in the `errors` TreeMap, with the line number as the key. If no errors are found, you can call super.assemble with the same parameters to write the output file. It is certainly possible but more intricate to do all the error checking in one pass, and 2 passes are easier than one. Even with 2 passes it is easier to save the output in a list and only write it to the output after checking that all the code is correct.
 
### ADDITIONAL HINTS AND SUGGESTIONS:

What we mean by two passes is this. Make two separate ArrayList<String>, one for your code lines and one for the data lines in the pasm file.
 
On the first pass, you can read in each line and try to separate them into the correct ArrayList. Any line that comes before the DATA word should be thrown in the ArrayList of code lines, and anything after it should be thrown in the ArrayList of data. During this initial first pass, you can additionally look for any blank line errors and any errors associated with an invalid or duplicate DATA line. You can also check for illegal whitespace in this initial pass, adding in the trimmed lines to be considered for the second pass. 
 
In the second pass, you will iterate over the code ArrayList, and for each line additionally try to find and report any errors specific to code. For instance, illegal mnemonics, mnemonics not all in uppercase, and so forth (see the list below). Then, you will iterate over the data ArrayList and for each line you will try to find and report any errors specific to data, which are also laid out below.
 
If there are no errors found, only then do you call `super.assemble` to actually assemble the pasm file into a pexe file. Otherwise, if any errors exist in the file, you will not assemble the pasm file but instead print out the list of errors to the user, specifying what lines caused the pasm file to fail to assemble. 
 
### Tips for Keeping Track of correct line number

You should have a lineNumber value equal to zero initially. As you look at a line for errors in a pass, increment the lineNumber by one. If you are doing two passes, be sure to reset the lineNumber to zero before the second pass. It may be a good idea to add the blank lines from the first pass, and if it is blank just add one to the lineNumber and continue checking for errors on the next line. Another thing to keep in mind is that when you switch from checking code to checking data, if you did not add the data delimeter to the data ArrayList, then you need to add 1 to lineNumber before you start iterating over the data searching for errors, to account for the DATA line which you may have removed. 
 
There is flexibility on the actual error messages but any error messages that you find before ending the Assembler should be entered to the map errors. Also the return value should be the line number of any one of those errors.
 
Here is a list of errors that could be used:
 
At the top of the code (`errors` is the TreeMap parameter)
 
`if(errors == null) throw new IllegalArgumentException("Coding error: the error map is null")`;
 
If `n` is the first of an illegal group of blank lines (see 1 below), then use
`errors.put(n, "Illegal blank line in the source file, starting at " + n);` 
 
`errors.put(n, "Line starts with illegal white space on line " + n);` // see 2 below
 
`errors.put(n, "Line does not have DATA in upper case on line " + n);` // see 3 below
 
`errors.put(-1, "Unable to open the source file");` // that is in the catch (FileNotFoundException)
The return value is -1 in this case.
 
`errors.put((i+1), "Error on line " + (i+1) + ": illegal mnemonic")` // see 4 below

`errors.put((i+1), "Error on line " + (i+1) + ": mnemonic must be upper case");` // see 4 below
 
`errors.put((i+1), "Error on line " + (i+1) + ": this mnemonic cannot take arguments");` // see 5 below
 
`errors.put((i+1), "Error on line " + (i+1) + ": this mnemonic has too many arguments");` // see 5 below
 
`errors.put((i+1), "Error on line " + (i+1) + ": this mnemonic is missing an argument");` // see 5 below
 
// also the errors for NumberFormatException described above.
 
When trying to write the output file we can have
 
} catch (FileNotFoundException e) {
	errors.put(-1, "Error: Unable to write the assembled program to the output file");
	retVal = -1;
} catch (IOException e) {
	errors.put(-1,"Unexplained IO Exception");
	retVal = -1;
}
 
The intention is that all errors are fed back to the GUI controller and then to the user.
 
### LIST OF ERRORS TO BE DISCOVERED
 
1) There can be no blank lines in the file EXCEPT at the end of the file. This means that if you locate a blank line in the file (use line.trim().length() == 0) then it is an error if you later find a non-blank line. The error should be reported for the line number of the FIRST blank line that is an error.  The test cases will include files with multiple blank lines at the end of the file, which are permitted and should be ignored. The trick to this is to have a boolean initially set to false that is changed to true when the first concurrence of a blank line is found. You should also store the lineNumber of this found blank line in some int variable. The error is only triggered when reading a later line that is not blank. If you have read a blank line (which you know from the boolean being true) and then find a non-blank line after it, this is not allowed. In other words every non-blank line is checked against the value of that boolean.
 
2) There can be no white space at the beginning of a non-blank line. Hence it is an error if the first char on the line is a space (== ' ') or a tab (== '\t')
 
3) Although some programs have no data, the ones that do have data need a single separator line "DATA". The word DATA must be in upper case. It CAN have white space _after_ the letters of DATA as in "DATA    "  so you should check if a line has line.trim().toUpperCase().equals("DATA") and `inCode` is still true. Tf that is true, we know the program is supposed to have the separator between code and data. However if we use the negated condition (!line.trim().equals("DATA")) we can recognize that DATA is not in upper case, which is an error. In addition if we find "DATA" and `inCode` is false, then there is a second separator, which is an error also.
 
4) If you are in code and you split the line into `parts` using `line.trim().split("\\s+")`, then parts[0] must be contained in `Model.OPCODES.keySet().contains(parts[0])`.  You also need to use the same trick that was used for DATA to be sure that the mnemonic, if present, is in upper case.
 
5) If you found a correct mnemonic (NOP, NOT, HALT, LOD, etc) then use `Model.NO_ARG_MNEMONICS.contains(...)`, to see if this instruction does or does not need an argument. If the mnemonic is in the no argument set, then parts must have length 1. Otherwise, parts must have length 2. Anything else is an error. 

6) Go back to `Model` and provide 2 more public Sets: `IND_MNEMONICS` that contains ("STO", "CMPL", "CMPZ"), and `JMP_MNEMONICS` ("JUMP", "JMPZ"). If the mnemonic is an instruction with an argument. First we must check for an addressing mode: look at the first character `parts[1].charAt(0)`. Note that it was also possible that `parts[1].charAt(0)` does not have an addressing mode character, so we skip this part of the cheching. However, that first character could be `#`, `@`, `&` , which must be checked: for `#` it is an error if the mnemonic is in `IND_MNEMONICS` and for `&` it is an error if the mnemonic is _not_ in `JMP_MNEMONICS`. All the mnemonics that take an argument can use the addressing mode `@` so there is no check for that one. After checking and if there are no errors the `#`, `@`, `&` is discarded by changing `parts[1]` to `parts[1].substring(1)`. Finally check that the remaining `parts[1]` is an int in hexadecimal. To check it is a number use something like this:
 
```java
try{
//... all the code to compute the correct flags
	int arg = Integer.parseInt(parts[1],16);
//.. the rest of setting up the opPart
} catch(NumberFormatException e) {
	errors.put((i+1), "Error on line " + (i+1) + 
			": argument is not a hex number");
	retVal = i + 1;				
} // At this point, i is the current index of the ArrayList of code
// so the line number is 1 larger than the index (index 0 corresponds to line 1)
```

7) When you are working through the lines of data parts has to have length 2 and you check for NumberFormatException from
 
`int address = Integer.parseInt(parts[0],16);`
 
and from
 
`int value = Integer.parseInt(parts[1],16);`

The error message will be something like
 
```java
} catch(NumberFormatException e) {
	errors.put((offset+i+1), "Error on line " + (offset+i+1) + 
		": data has non-numeric memory address");
	retVal = offset + i + 1;				
}
``` 
where offset is the number of lines of code before DATA. Here i is the index in the ArrayList of data lines.

As indicated near the beginning if retVal is not 0, we return it and the method ends. If it is 0, then we return `super.assemble(inputFileName, outputFileName, errors)`.
 

