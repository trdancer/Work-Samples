import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
public class PagingMain {
	static BufferedReader random_reader;
	static HashMap<Integer, Page> frame_table;
	static ArrayList<Process> processes;
	static int finished_processes;
	static int machine_size;
	static int page_size;
	static int process_size;
	static int num_processes;
	static String algorithm;
	static int cycles;
	static int max_cycles;
	static int num_frames;
	static int quantum = 3;
	public static void main(String[] args) {
		random_reader = null;
		try {
			random_reader = new BufferedReader(new FileReader("random-numbers"));
		}
		catch (IOException e) {
			System.out.println("Could not open that file");
			System.exit(1);
		}
		int job_mix;
		int num_references;
		if (args.length != 6) {
			System.out.println("Please run this program with 6 command line arguments");
			System.exit(1);
		}
		machine_size = Integer.valueOf(args[0]);
		page_size = Integer.valueOf(args[1]);
		process_size = Integer.valueOf(args[2]);
		job_mix = Integer.valueOf(args[3]);
		num_references =Integer.valueOf(args[4]);
		algorithm = args[5];

		System.out.println("Machine size: "+machine_size);
		System.out.println("Page size: "+page_size);
		System.out.println("Process size: "+process_size);
		System.out.println("Job Mix: "+job_mix);
		System.out.println("Number of references per process: "+num_references);
		System.out.println("Replacement algorithm used: "+algorithm);
		System.out.println();
		num_frames = machine_size/page_size;
		processes = new ArrayList<>();
		finished_processes = 0;
		//initiate processes based on J (job mix)
		switch (job_mix) {
			case 1:
				num_processes = 1;
				Process temp = new Process(process_size, num_references, 1, 0, 0);
				processes.add(temp);
				break;
			case 2:
				for (int i = 0; i < 4; i++) {
					processes.add(new Process(process_size, num_references, 1, 0, 0));
				}
				num_processes = 4;
				break;
			case 3:
				for (int j = 0; j < 4; j++) {
					processes.add(new Process(process_size, num_references, 0, 0, 0));
				}
				num_processes = 4;
				break;
			case 4:
				processes.add(new Process(process_size, num_references, 0.75, 0.25, 0));
				processes.add(new Process(process_size, num_references, 0.75, 0, 0.25));
				processes.add(new Process(process_size, num_references, 0.75, 0.125, 0.125));
				processes.add(new Process(process_size, num_references, 0.5, 0.125, 0.125));
				num_processes = 4;
				break;
		}
		max_cycles = (num_processes * num_references); 
		// for (Process p : processes) {
		// 	System.out.println("Process "+p.id+" first reference: "+p.previous_reference);
		// 	System.out.println("\tNext reference: "+getNextVirtualAddress(p));

		// }
		//initiate the frames/memory as empty/null
		frame_table = new HashMap<>();
		for (int i = 0; i < num_frames; i++) {
			frame_table.put(i, null);
		}
		int index_of_next_process = 0;
		int addr = 0;
 		//loop until all processes done
		while (finished_processes != num_processes) {
			Process cur_process = processes.get(index_of_next_process);
			if (cur_process.num_references == 0) {
				if (index_of_next_process == num_processes-1){
					index_of_next_process = 0;
				}
				else {
					index_of_next_process++;
				}
				continue;
			}
			int cur_page_number;
			for (int q = 0; q < quantum; q++) {
				// System.out.println("***In cycle "+cycles+"***");
				// System.out.println("---FRAME TABLE---");
				// for (Map.Entry fr : frame_table.entrySet()) {
					// Page pa = (Page) fr.getValue();
					// String temp_s = (Page) pa == null ? " is empty" : String.format(" contains Page %d of Process %d", pa.num, pa.process);
					// System.out.println("Frame "+(Integer) fr.getKey() + temp_s);
				// } 
				if (q == 0) {
					addr = cur_process.previous_reference;
				}
				cur_page_number = getPageNumber(addr);
				// System.out.println("Process "+cur_process.id+" references virtual address "+addr+" in page "+cur_page_number);
				//search for page within frame table
				boolean loaded = false;
				for (int ft = num_frames-1; ft >= 0; ft--) {
					Page pg = frame_table.get(ft);
					if (pg == null) {
						continue;
					}
					// System.out.println("Checking against page "+pg.num+" of process "+pg.process);
					if (pg.num == cur_page_number && pg.process == cur_process.id) {
						// System.out.println("HIT: The page was loaded in frame "+ft);
						pg.last_used = cycles;
						loaded = true;
						break;
					}
				}
				//else page fault, load this page
				if (!loaded) {
					// System.out.println("A page fault has occured");
					Page new_page = new Page(cur_page_number, cur_process.id, cycles);
					cur_process.num_page_faults++;
					if (algorithm.equals("lru")) {
						lruReplace(new_page);
					}
					else if (algorithm.equals("fifo")) {
						fifoReplace(new_page);
					}
					else {
						randomReplace(new_page);
					}
				}
				addr = getNextVirtualAddress(cur_process);
				cycles++;
				cur_process.num_references--;
				if (cur_process.num_references == 0) {
					finished_processes++;
					break;
				}
			}
			if (index_of_next_process == num_processes-1){
				index_of_next_process = 0;
			}
			else {
				index_of_next_process++;
			}
 		}
 		try {
 			random_reader.close();
 		}
 		catch (IOException e) {
 			System.out.println("Could not close the reader.");
 		}
 		printStats();
	}
	static void lruReplace(Page pg) {
		//a page cannot be used in a cycle that cannot exist, since each process is guaranteed to terminate and the number of references is known
		//the total number of cycles/references that will occur is num_processes*num_references + 1
		//use this number as number to compare agianst to find the minimum last-used time, since last_used is measured in absolute cycles elapsed
		int latest_used_time = max_cycles+1;
		int frame_number = 0;
		//find the page with the latest use time
		for (int f = num_frames-1; f >= 0; f--) {
			Page cur_page = frame_table.get(f);
			if (cur_page == null) {
				// System.out.println("Frame "+f+" was free, loaded page "+pg.num+" of process "+pg.process);
				frame_table.replace(f, pg);
				return;
			}
			int temp = cur_page.last_used;
			if (temp < latest_used_time) {
				latest_used_time = temp;
				frame_number = f;
			}
		}
		//now frame_number contains the frame in which to place the new page and evict the current one
		evictPage(frame_number);
		frame_table.put(frame_number, pg);
	}
	static void fifoReplace(Page pg) {
		//a page cannot be loaded in a cycle that cannot exist, since each process is guaranteed to terminate and the number of references is known
		//the total number of cycles/references that will occur is num_processes*num_references + 1
		//use this number as number to compare agianst to find the minimum loaded time
		int earliest_load_time = max_cycles+1;
		int frame_number = 0;
		//find the page with the earliest load time
		for (int f = num_frames-1; f >= 0; f--) {
			Page cur_page = frame_table.get(f);
			if (cur_page == null) {
				// System.out.println("There was a free frame, loaded in frame "+f);
				frame_table.replace(f, pg);
				return;
			}
			int temp = cur_page.cycle_loaded;
			if (temp < earliest_load_time) {
				earliest_load_time = temp;
				frame_number = f;
			}
		}
		//now frame_number contains the frame in which to place the new page and evict the current one
		evictPage(frame_number);
		frame_table.put(frame_number, pg);
	}
	static void randomReplace(Page pg) {
		for (int f = num_frames-1; f >= 0; f--) {
			Page cur_page = frame_table.get(f);
			if (cur_page == null) {
				// System.out.println("There was a free frame, loaded in frame "+f);
				frame_table.replace(f, pg);
				return;
			}
		}
		int random_frame = (getNextRandom()+num_frames) % num_frames;
		evictPage(random_frame);
		frame_table.put(random_frame, pg);
	}
	static void evictPage(int frame_number) {
		Page evicted = frame_table.get(frame_number);
		Process evicted_process = processes.get(evicted.process-1);
		// System.out.println("Evicted Page "+evicted.num+" of Process "+evicted.process);
		evicted_process.total_residency_time += cycles - evicted.cycle_loaded;
		evicted_process.total_evictions++;
	}
	static int getNextRandom() {
		int r = 0;
		try {
			r = Integer.valueOf(random_reader.readLine());
			// System.out.println("\tUsed Random number: "+ r);
		}
		catch (IOException e) {
			System.exit(1);
		}
		return r;
	}
	static double getABC() {
		return getNextRandom() / ((double) Integer.MAX_VALUE + 1);
	}
	static int getRandomAddress() {
		return (getNextRandom()+process_size) % process_size;
	}
	static int getPageNumber(int virtual_address) {
		return virtual_address / page_size;
	}
	static int getNextVirtualAddress(Process p) {
		double y = getABC();
		if (y < p.a) {
			int temp = p.previous_reference;
			p.previous_reference = (temp+1+p.size) % p.size;
			return p.previous_reference;
		}
		if (y < p.a + p.b) {
			int temp = p.previous_reference;
			p.previous_reference = (temp-5+p.size) % p.size;
			return p.previous_reference;
		}
		if (y < p.a + p.b + p.c) {
			int temp = p.previous_reference;
			p.previous_reference = (temp+4+p.size) % p.size;
			return p.previous_reference;
		}
		else {
			p.previous_reference = getRandomAddress();
			return p.previous_reference;
		}
	}
	static void printStats() {
		int total_faults = 0;
		double total_evictions = 0.0;
		double overall_residency = 0.0;
		Collections.sort(processes);
		for (Process p : processes) {
			p.stats();
			total_faults+= p.num_page_faults;
			overall_residency += (double) p.total_residency_time;
			total_evictions += p.total_evictions;
		}
		if (total_evictions == 0.0) {
			System.out.println("The total number of faults is "+total_faults+" and the overall average residency time is undefined.");
		}
		double average_residency = overall_residency / total_evictions;
		System.out.println("The total number of faults is "+total_faults+" and the overall average residency time is "+ average_residency);
	}
}