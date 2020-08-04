import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
public abstract class Scheduler {
	static String sep = File.separator;
	int cycles_elapsed = 0;
	int num_processes = 0;
	int cpu_idle_cycles = 0;
	int cur_cpu_burst_time = 0;
	int running_time = 0;
	int total_cpu_usage = 0;
	int total_io_cycles = 0;
	int total_turnaround_time;
	int total_wait_time;
	Process running_process;
	ArrayList<Process> original_processes;
	List<Process> not_arrived;
	LinkedList<Process> blocked;
	ArrayList<Process> finished_processes = new ArrayList<>();
	LinkedList<Process> ready_queue;
	boolean v = false;
	String type;
	BufferedReader instream = null;
	BufferedWriter writer = null;
	public Scheduler(List<Process> p, boolean v) {
		this.original_processes = new ArrayList<>();
		for (Process pr : p) {
			this.original_processes.add(new Process(pr));
		}
		this.num_processes = original_processes.size();
		this.v = v;
		type = v ? "-detailed" : "";
		cycles_elapsed = 0;
		try {
			File rn_file = new File("random-numbers");
			instream = new BufferedReader(new FileReader(rn_file));
		}
		catch (IOException e) {
			System.exit(1);
		}
	}
	public int randomOS(int u) {
		try {
			int r = Integer.valueOf(instream.readLine());
			// if (v) {
			// 	// System.out.println("Picked random int "+ r);
			// }
			int d = r % u;
			// System.out.println(d);
			return 1 + d;
		}
		catch (IOException e) {
			System.exit(1);
		}
		return -1;
	}
	public void close_file() {
		try {
			instream.close();
		}
		catch (IOException e) {
			System.exit(1);
		}
	}
	public void write_file(BufferedWriter f, String s) {
		try {
			f.write(s);
		}
		catch (IOException e) {
			System.out.println("Failed to write to the file");
			System.exit(1);
		}
	}
	public void close_writer(BufferedWriter w) {
		try {
			w.close();
		}
		catch (IOException e) {
			System.exit(1);
		}

	}
	public void write_stats(BufferedWriter w) {
		write_file(w, "Summary Data:\n");
		StringBuilder s = new StringBuilder("\tFinishing time: "+cycles_elapsed+"\n");
		s.append(String.format("\tCPU utilization: %.6f\n", ((float)total_cpu_usage/(float)cycles_elapsed)));
		s.append(String.format("\tI/O utilization: %.6f\n", ((float)total_io_cycles/(float)cycles_elapsed)));
		s.append(String.format("\tThroughput: %.6f processes per hundred cycles\n",(100.0*((float)num_processes/(float)cycles_elapsed))));
		s.append(String.format("\tAverage turnaround time: %.6f\n", (float)total_turnaround_time/(float)num_processes));
		s.append(String.format("\tAverage waiting time: %.6f\n", (float)total_wait_time/(float)num_processes));
		write_file(w, s.toString());
	}
	public abstract void execute();

}