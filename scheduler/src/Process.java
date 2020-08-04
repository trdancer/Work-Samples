public class Process implements Comparable<Process> {
	static int counter = 0;
	int id;
	// -1 = not arrived, 0 = Ready, 1 = Running, 2 = Blocked, 3 = finished/terminated
	int state;
	//in cycles
	int total_cpu_time_needed;
	//how many cycles before finished
	int remaining_total_time;
	//what cycle this process arrived at
	int arrival_time;

	int random_int_range;
	int io_multiplier;

	//the remaining number of cycles this process has in the cpu
	int cpu_burst_time_remaining;

	//the remaining number of cycles this process has waiting for IO
	int io_burst_time_remaining;

	//the relative number of cycles this process has been waiting in 'ready' for
	int cur_time_in_ready;
	//the total number of cycles this process has been in ready for
	int total_wait_time;

	//what time this process finished in absolute cycles elapsed
	int finish_time;
	//total number of cycles this process was in io for
	int total_io_time;

	public Process(int arrival_time, int random_int_range, int total_cpu_time_needed, int io_multiplier) {
		id = counter++;
		this.total_cpu_time_needed = total_cpu_time_needed;
		this.finish_time = 0;
		this.total_io_time = 0;
		this.total_wait_time = 0;
		this.remaining_total_time = total_cpu_time_needed;
		this.arrival_time = arrival_time;
		this.random_int_range = random_int_range;
		this.io_multiplier = io_multiplier;
		// defualt process to not arrived state 
		this.state = -1;
		this.cur_time_in_ready = 0;
	}
	public Process(Process p) {
		id = p.id;
		state = p.state;
		total_cpu_time_needed = p.total_cpu_time_needed;
		remaining_total_time = p.remaining_total_time;
		arrival_time = p.arrival_time;

		random_int_range = p.random_int_range;
		io_multiplier = p.io_multiplier;

		cpu_burst_time_remaining = p.cpu_burst_time_remaining;

		io_burst_time_remaining = p.io_burst_time_remaining;

		cur_time_in_ready = p.cur_time_in_ready;
		total_wait_time = p.total_wait_time;

		finish_time = p.finish_time;
		total_io_time = p.total_io_time;
	}
	public int compareTo(Process p) {
		if (this.arrival_time < p.arrival_time) {
			return -1;
		}
		if (this.arrival_time > p.arrival_time) {
			return 1;
		}
		if (this.id < p.id) {
			return -1;
		}
		if (this.id > p.id) {
			return 1;
		}
		return 0;
	}
	int get_running_time() {
		return total_cpu_time_needed - remaining_total_time > 1 ? total_cpu_time_needed - remaining_total_time : 1;
	}
	void arrive() {
		state = 0;
		cpu_burst_time_remaining = 0;
		io_burst_time_remaining = 0;
		cur_time_in_ready = 0;
	}
	void block() {
		state = 2;
		cpu_burst_time_remaining = 0;
		cur_time_in_ready = 0;
	}
	void unblock() {
		state = 0;
		io_burst_time_remaining = 0;
		cur_time_in_ready = 0;
	}
	void run() {
		remaining_total_time--;
		cpu_burst_time_remaining--;
	}
	void setToRunning(int cpu) {
		// System.out.println("Cpu burst time: "+cpu);
		state = 1;
		cpu_burst_time_remaining = cpu;
		io_burst_time_remaining = cpu*io_multiplier;
		cur_time_in_ready = 0;
		if (remaining_total_time > cpu) {
			total_io_time+=io_burst_time_remaining;
		}
		// System.out.println("IO time: "+io_burst_time_remaining);
	}
	void setReady() {
		state = 0;
		cur_time_in_ready = 0;
	}
	void terminate(int finish_time) {
		this.finish_time = finish_time;
		state = 3;
	}
	void print() {
		System.out.println("ID: "+id);
		System.out.println("Arrival: "+arrival_time);
		System.out.println("RandomOS range: "+random_int_range);
		System.out.println("CPU time needed: "+total_cpu_time_needed);
		System.out.println("IO multiplier: "+io_multiplier);
	}
}