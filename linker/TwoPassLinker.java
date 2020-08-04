import java.util.*;
import java.lang.StringBuilder;
class Module {
	int mod_num;
	int mod_size;
	ArrayList<String> instructions;
	HashMap<String, String> definitions;
	LinkedHashMap<String, ArrayList<String>> usages;
	public Module(int mod_num) {
		this.mod_num = mod_num;
		mod_size = 0;
		instructions = new ArrayList<>(8);
		definitions = new HashMap<>(8);
		usages = new LinkedHashMap<>(8);
	}
}
public class TwoPassLinker {
	//static/global variables used throughout the program
	static Scanner input = new Scanner(System.in);


	//arraylist to hold the modules
	static ArrayList<Module> mods = new ArrayList<>(5);
	//hashmap that stores the absolute address usages of each symbol
	static LinkedHashMap<String, ArrayList<Integer>> global_usages = new LinkedHashMap<>(16);

	//data structures that hold definitions for symbols and each resolved instruction
	static LinkedHashMap<String, Integer> symbol_table = new LinkedHashMap<>(16);
	static ArrayList<String> memory_map = new ArrayList<>(16);
	
	//error hashmaps hold keys to what variables or instructions contain errors
	static HashMap<String, Boolean> global_are_multiply_defined = new HashMap<>(4);
	static HashMap<String, Boolean> global_are_out_of_bounds = new HashMap<>(4);

	static HashMap<Integer, String> not_defined_errors = new HashMap<>(4);
	static HashMap<Integer, Boolean> absolute_address_errors = new HashMap<>(4);
	static HashMap<Integer, Boolean> multiple_usages_errors = new HashMap<>(4);

	//regex used to split all input
	static String regex = "\\s";


	//main method
	public static void main(String args[]) {
		String code = new String();
		int[] mod_num_tuple = new int[2];
		//get input automatically handles empty strings and whitespace 
		code = getInput();
		mod_num_tuple = getFirstNumAndEnd(code);
		//only the mod num existed on the first line of code
		if (mod_num_tuple[1] == -1) {
			code = getInput();
		}
		//otherwise there's more to read on this line
		else {
			// System.out.println("You put some extra code in on the first line.");
			//use remaining code starting at tuple[1] at 'code'
			code = buildString(code.split(regex), mod_num_tuple[1]);
		}
		// System.out.println("Number of modules to read: "+mod_num_tuple[0]);
		for (int i = 0; i < mod_num_tuple[0]; i++) {
			Module cur_mod = new Module(i+1);
			code = readModule(code, cur_mod);
			mods.add(cur_mod);
		}
		passOne();
		passTwo();
	}



	//Error method

	public static void wrongFormat(){
			System.out.println("Sorry, your input does not match the spec. Please run this program again.");
			System.exit(1);
		}




	// Utility methods

	public static int getIndexOfNext(String[] line, int index) {
		// System.out.println("I'm checking this line for the next value starting at index: "+ index+"\nline length: "+line.length);
		if (line[0].matches("") && line.length == 1) {
			return -1;
		}
		for (int i = index; i < line.length; i++) {
			if (!line[i].matches("\\s")) {
				// System.out.println("The next character in the line_list is at: "+i);
				return i;
			}
		}
		return -1;
	}
	//overload
	public static int getIndexOfNext(String line) {
		return getIndexOfNext(line.split(regex), 0);
	}
	// returns a tuple consisting of the first num, and index of the next data point, if applicable, second value is -1 otherwise 
	public static int[] getFirstNumAndEnd(String line) {
		String[] line_list = line.split(regex);
		int[] tuple ={-1, -1};
		for (int i = 0; i < line_list.length; i++) {
			if (line_list[i].matches("[0-9]+")) {
				tuple[0] = Integer.valueOf(line_list[i]);
				tuple[1] = i+1;
				break;
			}
		}
		//advances tuple[1] to the index of the next non-whitespace character in the list
		tuple[1] = getIndexOfNext(line_list, tuple[1]);
		// System.out.println("Found value of "+tuple[0]+" as first value, and the rest of the code continues at "+tuple[1]);
		return tuple;
	}
	// input method used for getting all input
	// runs in loop until characters are given in string
	public static String getInput() {
		String user_input;
		String[] l;
		do {
			user_input = input.nextLine();
			l = user_input.split(regex);
		}
		while (getIndexOfNext(l, 0) == -1);
		return user_input;

	}
	public static String buildString(String[] sl, int index) {
		if (sl.length == 0) {
			return null;
		}
		StringBuilder newstring = new StringBuilder();
		for (int i = index; i < sl.length; i++) {
			if (!sl[i].matches("\\s")) {
				newstring.append(sl[i]+" ");
			}
		}
		return newstring.toString();
	}


	//read input methods

	public static String readModule(String line, Module m) {
		// System.out.println("Reading module...\nline="+line);
		if (line == null || getIndexOfNext(line) == -1) {
			line = getInput();
		}
		line = readDefNum(line, m);
		if (line == null || getIndexOfNext(line) == -1) {
			line = getInput();
		}
		line = readUsages(line, m);
		if (line == null || getIndexOfNext(line) == -1) {
			line = getInput();
		}
		// System.out.println("After readUsages(): "+line);
		line = readInstructions(line, m);
		return line;
	}
	public static String readDefNum(String line, Module m) {
		// System.out.println("In readDefNum()\nLine ="+line);
		int[] num_defs_list = getFirstNumAndEnd(line);
		String definitions = new String();
		//check if only the num_defs exist on this line
		if (num_defs_list[0] == 0) {
			if (num_defs_list[1] == -1) {
				return null;
			}
			else {
				return buildString(line.split(regex), num_defs_list[1]);
			}
		}
		if (num_defs_list[1] == -1) {
			//if they don't, ask for more input
			definitions = getInput();
		}
		//otherwise definitions exist on this line
		else {
			//only have to check the substring where num_defs leaves off
			definitions = line.substring(num_defs_list[1], line.length());
		}
		//find definitions and return the remaining code after it is completed
		return getSymbolDefinitions(definitions, num_defs_list[0], m);
	}
	public static String getSymbolDefinitions(String line, int num_defs, Module m) {
		// System.out.println("In getSymbolDefinitions()");
		String[] line_list = line.split(regex);
		// int previous_symb_size = symbs_defs.size();
		// System.out.println("previous hashmap size: "+previous_symb_size);
		String symbol_name = new String();
		// String symbol_location = new String();
		int i = 0;
		boolean find_symbol = true;
		//continue looking for symbols until found all needed
		while (m.definitions.size() < num_defs) {
			//determine if gotten to the end of the string
			if (i == line_list.length) {
				line = getInput();
				line_list = line.split(regex);
				i = 0;
			}
			//if a symbol is found and we are supposed to find one, record that value
			if (line_list[i].matches("\\w+") && find_symbol) {
				symbol_name = line_list[i];
				// System.out.println("Found symbol: "+symbol_name);
				//toggle find symbol so we are now looking for a location
				find_symbol = false;
				//iterate i
				i++;
				//continue loop at top
				continue;
			}
			// look for a number
			if (line_list[i].matches("[0-9]+")) {
				//if a number is found but we are looking for a string, exit program for wrong formatting
				if (find_symbol){
					wrongFormat();
				}
				//wrap integer conversion in try/catch block in case an unforseen error occurs
				//if a number is found, capture it and add the memory offset of this module
				// System.out.println("Found a symbol location: "+ line_list[i]);
				m.definitions.put(symbol_name, line_list[i]);
				//toggle find_symbol to look for a symbol name now
				find_symbol = true;
			}
			i++;
			//iterate i after all is said and done in loop
		}
		//outside loop means all definitions have been found
		// System.out.println("I have found the required number of definitions");
		if (getIndexOfNext(line_list, i) == -1) {
			return null;
		}
		else {
			return buildString(line_list, i);
		}
	}
	public static String readUsages(String line, Module m) {
		// System.out.println("In readUsages()\nLine="+ line);
		int[] num_usages_list = getFirstNumAndEnd(line);
		//at this point num_usages is guaranteed to have a value
		//determine if more input is needed
		if (num_usages_list[0] == 0) {
			if (num_usages_list[1] == -1) {
				return null;
			}
			else {
				return buildString(line.split(regex), num_usages_list[1]);
			}
		}
		if (num_usages_list[1] == -1) {
			line = getInput();
		}
		else {
			line = line.substring(num_usages_list[1], line.length());
		}
		String[] line_list = line.split(regex);
		String symbol_name = new String();
		int current_location_usage = 0;
		boolean find_symbol = true;
		int i = getIndexOfNext(line_list, 1);
		ArrayList<String> symbol_location_array = new ArrayList<>(8);
		while (m.usages.size() < num_usages_list[0]) {
			if (i == line_list.length) {
				line = getInput();
				line_list = line.split(regex);
				i = 0;
				continue;
			}
			if (line_list[i].matches("\\w+") && find_symbol) {
				symbol_name = line_list[i];
				// System.out.println("Found symbol: "+line_list[i]);
				//toggle find symbol so we are now looking for a location
				find_symbol = false;
				//iterate i
				i++;
				//continue loop at top
				continue;
			}
			// look for a number
			if (line_list[i].matches("[0-9]+")) {
				// System.out.println("Found a number");
				//if a number is found but we are looking for a string, exit program for wrong formatting
				if (find_symbol){
					wrongFormat();
				}
				//set usage location to the value found at line_list[i]
				//value is stored as a relative address for easier calculations when adding it to the hashmap of symbols
				symbol_location_array.add(line_list[i]);
			}
			if (line_list[i].equals("-1")) {
				// System.out.print("List of usages to add: ");
				int n = symbol_location_array.size();
				///create temporary arraylist to store the usages listed for this symbol  
				ArrayList<String> temp = new ArrayList<>(n);
				for (int r = 0; r < n; r++) {
					temp.add(symbol_location_array.get(r));
				}
				m.usages.put(symbol_name, temp);
				//clear the location array storing the current symbol's usages
				symbol_location_array.clear();
				//toggle find_symbol to look for a symbol name now
				find_symbol = true;
				//incr i
				i++;
				continue;
			}
			i++;
		}
		// System.out.println("----i="+i);
		if (getIndexOfNext(line_list, i) == -1) {
			return null;
		}
		else {
			return buildString(line_list, i);
		}
	}

	public static String readInstructions(String line, Module m){
		// System.out.println("In readInstructions()\nLine="+line);
		int[] num_instructions_tuple = getFirstNumAndEnd(line);
		if (num_instructions_tuple[0] == 0) {
			if (num_instructions_tuple[1] == -1) {
				return null;
			}
			else {
				return buildString(line.split(regex), num_instructions_tuple[1]);
			}
		}
		if (num_instructions_tuple[1] == -1) {
			line = getInput();
		}
		else {
			line = line.substring(num_instructions_tuple[1], line.length());
		}
		m.mod_size = num_instructions_tuple[0];
		String[] line_list = line.split(regex);
		int i = 0;
		while (m.instructions.size() < num_instructions_tuple[0]) {
			if (i == line_list.length) {
				line = getInput();
				line_list = line.split(regex);
				i = 0;
				continue;
			}
			if (line_list[i].matches("[0-9]{5}")) {
				// System.out.println(line_list[i]);
				m.instructions.add(line_list[i]);
				//get type of memory address/value in middle 3 digits, LSD
				char type = line_list[i].charAt(4);
				// System.out.println("mem addr/value: "+instruct+" type: "+type);
				//check to ensure the last digit is a valid number for type of address/value
				if (type < 49 || type > 52) {
					wrongFormat();
				}
			}
			// else {
			// 	wrongFormat();
			// }
			i++;
		}
		return buildString(line_list, i);
	}
	

	// Actual linker methods -- resolve definitions, addresses, and usage locations
	public static void passOne() {
		int n = mods.size();
		Module cur_mod;
		int cur_offset = 0;
		//check if symbols are multiply defined or if they are declared outside of the module limits
		for (int i = 0; i < n; i++) {
			cur_mod = mods.get(i);
			HashMap<String, String> cur_definitions = mods.get(i).definitions;
			//loop through each symbol defined in this module
			for (Map.Entry symbol : cur_definitions.entrySet()) {
				// replaced = false;
				// out_of_bounds = false;
				String cur_symbol_name = (String) symbol.getKey();
				Integer cur_symbol_def = Integer.valueOf((String) symbol.getValue());
				int absolute_address = cur_symbol_def + cur_offset;
				if (cur_symbol_def > cur_mod.mod_size-1) {
					absolute_address = (cur_mod.mod_size)-1 + cur_offset;
					global_are_out_of_bounds.put(cur_symbol_name, true);
				}
				if (symbol_table.containsKey(cur_symbol_name)) {
					symbol_table.replace(cur_symbol_name, absolute_address);
					global_are_multiply_defined.put(cur_symbol_name, true);
				}
				else {
					symbol_table.put(cur_symbol_name, absolute_address);
				}
				} //end of iteration through the current module's definitions 
			cur_offset += cur_mod.mod_size;
		} //end of iteration through each module
		System.out.print("Symbol Table:");
		for (Map.Entry e : symbol_table.entrySet()) {
			String name = (String) e.getKey();
			String out_of_bounds_error = new String();
			String replaced_error = new String();
			if (global_are_multiply_defined.containsKey(name)) {
				replaced_error = " Error this symbol was defined multiple times. Using most recent definition";
			}
			if (global_are_out_of_bounds.containsKey(name)) {
				out_of_bounds_error = " Error this symbol was defined out of bounds of this module.";
			}
			System.out.print("\n"+name+":\t"+ ((Integer) e.getValue())+ replaced_error + out_of_bounds_error);
		}

	}
	public static void passTwo() {
		int n = mods.size();
		int cur_offset = 0;
		ArrayList<String> not_used_errors = new ArrayList<>(4);
		//iterate over each module
		System.out.print("\nMemory Map:");
		for (int i = 0; i < n; i++) {
			for (Map.Entry e : mods.get(i).usages.entrySet()) {
				ArrayList e_list = (ArrayList) e.getValue();
				String e_string = (String) e.getKey();
				ArrayList<Integer> temp = new ArrayList<>(e_list.size());
				//relocate usage locations to absolute addresses
				for (int h = 0; h < e_list.size(); h++) {
					Integer abs_loc = Integer.valueOf((String) e_list.get(h))+cur_offset;
					temp.add(abs_loc);
				}
				//if this symbol doesn't exist in the global usages map, add it and continue with next symbol
				if (global_usages.get(e_string) == null) {
					global_usages.put(e_string, temp);
				}
				//otherwise add these usages to symbols usage list
				else {
					//loop through each usage for this symbol
					for (int q = 0; q < temp.size(); q++) {
						int x = temp.get(q);
						//only add this absolute location if it doesn't already exist for this symbol
						if (!global_usages.get(e_string).contains(x)) {
							global_usages.get(e_string).add(x);
						}
					}
				}
				//check for any repeat usage absolute locations in the global usages
				for (int f = 0; f < temp.size(); f++) {
					//a_loc is the absolute location usage of the current symbol  
					Integer a_loc = (Integer) temp.get(f);
					//check the global usages list for any other symbols that use this location
					for (Map.Entry global_entry : global_usages.entrySet()) {
						String g_string = (String) global_entry.getKey();
						//do not check if the current symbol is the same as the current one
						if (g_string.equals(e_string)) {
							continue;
						}
						ArrayList g_list = (ArrayList) global_entry.getValue();
						//check if the global usage contains the same absolute address
						if (g_list.contains(a_loc)) {
							//remove the usage location from that symbol in global
							global_usages.get(g_string).remove(a_loc);
							multiple_usages_errors.put(a_loc, true);
						}
					} //finish iterating thgough each entry in global
				} // finish iterating through each address in temp
			} //finish adding these usages to global usages

			ArrayList<String> cur_instructions_list = mods.get(i).instructions;
			int m = cur_instructions_list.size();


			//iterate through each instruction usage in this module
			for (int j = 0; j < m; j++) {
				String cur_instruction = cur_instructions_list.get(j);
				//create an integer representation of this current address/value
				int address = Integer.valueOf(cur_instructions_list.get(j).substring(1, 4));
				char c = cur_instruction.charAt(4);
				switch(c) {
					//ascii value of 2, absolute address referenced
					case 50 : 
						//check if the absolute address is greater than machine size. Flag it as an error if it is
						if (address > 299) {
							absolute_address_errors.put(j+cur_offset, true);
							//set address to largest possible value
							address = 299;
						}
						break;
					//ascii value 3, relative address referenced
					case 51 :
						//add the module's offset to this address
						address += cur_offset;
						break;
					//ascii value 4, external symbol referenced
					case 52 :
						//find the symbol that this usage refers to
						String this_symbol = new String();
						for (Map.Entry ent : global_usages.entrySet()) {
							ArrayList ent_list = (ArrayList) ent.getValue();
							if (ent_list.contains(cur_offset+j)) {
								this_symbol = (String) ent.getKey();
								break;
							}
						}
						if (!symbol_table.containsKey(this_symbol)) {
							// System.out.println("At instruction#"+(cur_offset+j)+". The symbol at this location was not defined.");
							not_defined_errors.put(cur_offset+j, this_symbol);
							address = 111;
						}
						else {
							address = symbol_table.get(this_symbol);
						}
						break;
					default :
						break;
				}
				//set the instructions 3 middle digits to addresses new value
				String resolved_address = cur_instruction.substring(0, 1) + String.format("%03d", address);
				memory_map.add(resolved_address);	
			}
			//add the mod size to the current offset
			cur_offset +=mods.get(i).mod_size;
		}
		int mem_size = memory_map.size();

		for (int xyz = 0; xyz < mem_size; xyz++) {
			String abs_error = "";
			String mult_use = "";
			String not_def = "";
			if (absolute_address_errors.containsKey(xyz)) {
				abs_error = " Error: this instruction references an absolute address larger than the machine size. Using largest legal value.";
			}
			if (not_defined_errors.containsKey(xyz)) {
				not_def = " Error:"+not_defined_errors.get(xyz)+ "  is not defined. Using value 111";
			}
			if (multiple_usages_errors.containsKey(xyz)) {
				mult_use = " Error: multiple symbols listed as used at this instruction. Using latest value.";
			}
			System.out.print("\n"+xyz+"\t"+memory_map.get(xyz)+abs_error+not_def+mult_use);
		}
		System.out.println();

		for (String s : symbol_table.keySet()) {
			if (!global_usages.containsKey(s)) {
				int mod_belongs = 0;
				for (int mod_n = 0; mod_n < mods.size(); mod_n++) {
					if (mods.get(mod_n).definitions.containsKey(s)) {
						mod_belongs = mod_n;
						break;
					}
				}

				System.out.println("Warning: "+s+ " is defined in module "+mod_belongs+" but never used");
			}
		}
	}
}