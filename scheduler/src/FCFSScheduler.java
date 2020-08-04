import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.LinkedList;
import java.io.*;
import java.lang.StringBuilder;
public class FCFSScheduler extends Scheduler {
	LinkedList<Process> ready_queue;
	ArrayList<Process> ready_buffer;
	int counter = 1;
	public FCFSScheduler(List<Process> processes, boolean v, String fn) {
		super(processes, v);
		try {
			writer = new BufferedWriter(new FileWriter(".."+sep+"out"+sep+"fcfs-output-"+fn+type));
		}
		catch (IOException e) {
			System.out.println("Could not create that file or directory");
			System.exit(1);
		}
		StringBuilder not_sorted = new StringBuilder("The original input was: "+num_processes+" ");
		for (Process p : original_processes) {
			not_sorted.append("("+p.arrival_time+" "+p.random_int_range+" "+p.total_cpu_time_needed+" "+p.io_multiplier+") ");
		}
		not_sorted.append("\n");
		write_file(writer, not_sorted.toString());
		this.not_arrived = new ArrayList<>();
		this.ready_queue = new LinkedList<Process>();
		this.blocked = new LinkedList<>();
		StringBuilder sorted_str = new StringBuilder("The (sorted) input was: "+num_processes+" ");
		Collections.<Process>sort(original_processes);
		//initialize beginning ready queue
		for (Process p : original_processes) {
			sorted_str.append("("+p.arrival_time+" "+p.random_int_range+" "+p.total_cpu_time_needed+" "+p.io_multiplier+") ");
			if (p.arrival_time != 0) {
				not_arrived.add(p);
			}
			else {
				p.state = 0;
				ready_queue.addFirst(p);
			}
		}
		sorted_str.append("\n");
		write_file(writer, sorted_str.toString());
		if (v) {
			write_file(writer, "\nThis detailed printout gives the state and remaining burst for each process\n");
			StringBuilder table_header = new StringBuilder(String.format("%18s"," "));
			for (int j = 0; j < num_processes; j++) {
				table_header.append(String.format("%13s %3d", "Process", j));
			}
			write_file(writer, table_header.toString());
			StringBuilder cycle_header = new StringBuilder(String.format("\n%s %5s:","Before Cycle", "0"));
			for (int i = 0; i < num_processes; i++) {
				cycle_header.append(String.format("%12s %3s ", "unstarted", "0"));
			}
			cycle_header.append("\n");
			write_file(writer, cycle_header.toString());
		}
	}
	public void execute() {
		// System.out.println("Executing FCFS.");
		// for (Process po : original_processes) {
		// 	po.print();
		// 	//if running process is null, choose next from ready queue
		// }
		while (true) {
			running_process = ready_queue.pollLast();
			if (running_process == null) {
				cycles_elapsed++;
				cpu_idle();
				checkArrivals();
			}
			else {
				break;
			}
		}
		int c = randomOS(running_process.random_int_range); 
		running_process.setToRunning(c);
		//loop until all processes have terminated
		while (finished_processes.size() < num_processes) {
			ready_buffer = new ArrayList<>();
			if (v) {
				write_status();
			}
			cycles_elapsed++;
			// System.out.println("Start of cycle "+cycles_elapsed);
			// print_ready();
			// print_blocked();
			//unblock processes
			//see if any processes have been unblocked after the previous cpu cycle
			updateBlocked();
			//run the current process for 1 cycle
			run();
			//get the next process to run from the ready queue 
			getNextReady();
			// enqueue any processes that have arrived at this cycle
			checkArrivals();
			// System.out.println("End of cycle:");
			// print_ready();
			// print_blocked();
			// System.out.println();
		}
		write_file(writer, "The scheduling alogrithm used was First Come First Served\n\n");
		Collections.<Process>sort(finished_processes);
		StringBuilder prw;
		int i = 0;
		for (Process p : finished_processes) {
			prw = new StringBuilder();
			prw.append("Process "+i+":\n");
			prw.append("\t(A, B, C, M) = ("+p.arrival_time+", "+p.random_int_range+", "+p.total_cpu_time_needed+", "+p.io_multiplier+")\n");
			prw.append("\tFinishing time: "+p.finish_time+"\n");
			int ta = p.finish_time - p.arrival_time;
			total_turnaround_time += ta;
			prw.append("\tTurnaround time: "+ta+"\n");
			prw.append("\tI/O wait time: "+p.total_io_time+"\n");
			total_wait_time += p.total_wait_time;
			prw.append("\tWaiting time: "+p.total_wait_time+"\n\n");
			write_file(writer, prw.toString());
			i++;
		}
		write_stats(writer);
		close_writer(writer);
		close_file();
	}
	void print_ready(){
		System.out.println("Processes in ready: ");
		for (Process p : ready_queue) {
			System.out.println(p.id);
		}
	}
	void print_blocked() {
		System.out.println("Processes blocked: ");
		for (Process p : blocked) {
			System.out.println(p.id);
		}
	}
	public void updateBlocked() {
		// Collections.<Process>sort(blocked);
		// System.out.println("In UpdateBlocked");
		ArrayList<Process> to_unblock = new ArrayList<>();
		if (blocked.size() != 0) {
			total_io_cycles++;
		}
		//loop through all processes
		for (Process p : blocked) {
			//subtract the a cycle from the current processes io burst time
			
			p.io_burst_time_remaining--;
			// System.out.println("Process "+p.id+" remaining io time: "+p.io_burst_time_remaining);
			//if the io burst time has gone to zero or below, unblock it
			if (p.io_burst_time_remaining <= 0) {
				// System.out.println("Process "+p.id+" has been unblocked");
				p.unblock();
				to_unblock.add(p);
				ready_buffer.add(p);
				// ready_queue.addFirst(p);
			}
		}
		for (Process tup : to_unblock) {
			blocked.remove(tup);
		}
	}
	public void run() {
		if (running_process == null) {
			return;
		}
		// System.out.println("in run()");
		running_process.run();
		total_cpu_usage++;
		// System.out.println("Running process "+running_process.id);
		if (running_process.remaining_total_time <= 0) {
			// System.out.println("Process terminated");
			running_process.terminate(cycles_elapsed);
			finished_processes.add(running_process);
			running_process = null;
			return;
		}
		if (running_process.cpu_burst_time_remaining <= 0) {
			// System.out.println("Process blocked");
			running_process.block();
			blocked.addFirst(running_process);
			running_process = null;
			return;
		}
	}
	void cpu_idle() {
		cpu_idle_cycles++;
		// System.out.println("Nothing to run, cpu idle");
	}
	public void checkArrivals() {
		// System.out.println("In checkArrivals");
		ArrayList<Process> to_remove = new ArrayList<>();
		for (Process p : not_arrived) {
			// System.out.println("Checking process "+p.id+" - it has state of "+p.state);
			if (p.arrival_time <= cycles_elapsed) {
				// System.out.println("Process "+p.id+" has arrived");
				p.arrive();
				ready_queue.addFirst(p);
				to_remove.add(p);
			}
		}
		for (Process trp : to_remove) {
			not_arrived.remove(trp);
		}
	}
	
	public void getNextReady() {
		// System.out.println("In getNextReady");
		addReadyBuffer();
		if (running_process != null) {
			// System.out.println("A process is already running, not choosing the next process");
			return;
		}
		running_process = ready_queue.pollLast();
		if (running_process == null) {
			// System.out.println("No processes in the ready queue");
			cpu_idle();
			return;
		}
		// System.out.println("Next process to run: "+running_process.id);
		int c = randomOS(running_process.random_int_range);
		running_process.setToRunning(c);
		// if (running_process.remaining_total_time > running_process.cpu_burst_time_remaining) {
		// 	total_io_cycles+= running_process.cpu_burst_time_remaining;
		// }
	}
	public void write_status() {
		StringBuilder to_write = new StringBuilder(String.format("%s %5d:", "Before Cycle", counter));
		for (Process p : original_processes) {
			switch (p.state) {
				case -1:
					to_write.append(String.format("%12s %3s " ,"unstarted", "0"));
					break;
				case 0:
					p.total_wait_time++;
					to_write.append(String.format("%12s %3s ", "ready", "0"));
					break;
				case 1:
					to_write.append(String.format("%12s %3d ", "running", p.cpu_burst_time_remaining));
					break;
				case 2:
					// p.total_io_time++;
					to_write.append(String.format("%12s %3d ", "blocked", p.io_burst_time_remaining));
					break;
				case 3:
					to_write.append(String.format("%12s %3s ", "terminated", "0"));
					break;
			}
		}
		to_write.append("\n");
		write_file(writer, to_write.toString());
		counter++;
	}
	void addReadyBuffer() {
		Collections.<Process>sort(ready_buffer);
		for (Process p : ready_buffer) {
			ready_queue.addFirst(p);
		}
	}
}