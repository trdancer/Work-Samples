// import java.io.FileNotFoundException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
public class SchedulerMain {
	static String sep = File.separator;
	static BufferedReader instream;
	static Process[] p_list;
	static String file_name;
	static String file_num;
	// static Scheduler[] schedulers = new Scheduler[4];
	public static void main(String args[]) {
		boolean verbosity = false;
		file_name = "";
		File input_file;
		int args_l = args.length;
		switch (args_l) {
			case 0: 
				System.out.println("Please run this program again using a file name as a runtime argument and/or a verbose flag.\nExample: \">>java SchedulerMain examplefile --verbose\"");
				System.exit(1);
				break;
			case 1:
				file_name = args[0];
				break;
			case 2:
				file_name = args[1];
				if (args[0].equals("-verbose") || args[0].equals("-v")) {
					verbosity = true;
					break;
				}
				else {
					System.out.println("Sorry, that flag is not valid, please try again using \'-verbose\' as the first command-line argument.");
					System.exit(1);
					break;
				}
		}
		try {
			input_file = new File("in"+sep+file_name);
			instream = new BufferedReader(new FileReader(input_file));
		}
		catch (IOException e) {
			System.out.println("Sorry, the file you specified does not exist in the current directory. Please try again.");
			System.exit(1);
		}
		String[] file_temp = file_name.split("-");
		file_num = file_temp[file_temp.length-1];
		parse_processes();
		ArrayList<Process> process_list = new ArrayList<>(Arrays.asList(p_list));
		FCFSScheduler fc_sch = new FCFSScheduler(process_list, verbosity, file_num);
		LCFSScheduler lc_sch = new LCFSScheduler(process_list, verbosity, file_num);
		HPRNScheduler hp_sch = new HPRNScheduler(process_list, verbosity, file_num);
		RRScheduler rr_sch = new RRScheduler(process_list, verbosity, file_num);
		Scheduler[] schedulers = {fc_sch, lc_sch, hp_sch, rr_sch};
		for (int i = 0; i < 4; i++) {
			run_scheduler(schedulers[i]);
		}
		System.out.println("Success");
		// System.out.println(my_sch.randomOS(5));
		// System.out.println(other_sch.randomOS(5));
	}
	static void parse_processes() {
		int abcm = 0;
		int num_read = 0;
		int arrival_time = 0;
		int random_int_range = 0;
		int total_cpu_time_needed = 0;
		int io_multiplier = 0;
		int[] line = parse_line();
		int num_processes = line[0];
		if (num_processes == 0) {
			no_processes();
		}
		Process[] pl = new Process[num_processes];
		int index = 1;
		while (num_read < num_processes) {
			if (index == line.length) {
				line = parse_line();
				index = 0;
				continue;
			}
			switch (abcm) {
				case 0:
					arrival_time = line[index++];
					abcm++;
					break;
				case 1:
					abcm++;
					random_int_range = line[index++];
					break;
				case 2:
					abcm++;
					total_cpu_time_needed = line[index++];
					break;
				case 3:
					abcm = 0;
					io_multiplier = line[index++];
					pl[num_read] = new Process(arrival_time, random_int_range, total_cpu_time_needed, io_multiplier);
					num_read++;
					break;
			}
		}
		p_list = pl;
	}
	public static void no_processes() {
		try {
			BufferedWriter outstream1 = new BufferedWriter(new FileWriter(".."+sep+"out"+sep+"fcfs-output-"+file_num));
			BufferedWriter outstream2 = new BufferedWriter(new FileWriter("lcfs-output-"+file_num));
			BufferedWriter outstream3 = new BufferedWriter(new FileWriter("hprn-output-"+file_num));
			BufferedWriter outstream4 = new BufferedWriter(new FileWriter("rr-output-"+file_num));
			String s = "The original input was: 0\n\nThere were no processes to run.\n";
			outstream1.write(s);
			outstream2.write(s);
			outstream3.write(s);
			outstream4.write(s);
			outstream1.close();
			outstream2.close();
			outstream3.close();
			outstream4.close();
			System.exit(0);
		}
		catch (IOException e) {
			System.exit(1);
		}
	}
	//parse line takes input from a file and parses it into an int array
	public static int[] parse_line() {
		String s = getInput();
		s = s.replace("(", "");
		s = s.replace(")", "");
		String[] sl = s.split("[^0-9]+");
		// for (String q : sl) {
		// 	System.out.println(q);
		// }
		int[] nums = new int[sl.length];
		for (int i = 0; i < sl.length; i++) {
			// System.out.println(sl[i]);
			nums[i] = Integer.valueOf(sl[i]);
		}
		return nums;
	}
	public static String getInput() {
		String file_input = null;
		while (true) {
			try {
				// System.out.println("Reading a line");
				file_input = instream.readLine();
				if (file_input == null) {
					System.out.println("Reached end of file");
					System.exit(1);
				}
				if (file_input.equals("") && file_input.length() == 0) {
					// System.out.println("Read an empty line");
					continue;
				}
				file_input = file_input.trim();
				break;
			}
			catch (IOException e) {
				System.exit(1);
			}
		}
		// System.out.println("Returned the value of "+file_input);
		return file_input;

	}
	static void run_scheduler(Scheduler s) {
		s.execute();
	}
}