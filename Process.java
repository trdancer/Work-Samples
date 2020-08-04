import java.util.HashMap;

public class Process implements Comparable<Process> {

	static int counter = 1;
	int size;
	int id;
	int total_residency_time;
	int total_evictions;
	int num_references;
	int previous_reference;
	
	double a;
	double b;
	double c;

	int num_page_faults;
	
	public Process (int size, int num_references, double a, double b, double c) {
		this.id = counter++;
		this.size = size;
		this.num_references = num_references;
		this.a = a;
		this.b = b;
		this.c = c;
		previous_reference = (111*this.id+this.size)% this.size;
	}
	public int compareTo(Process p) {
		if (this.id < p.id) {
			return -1;
		} 
		if (this.id > p.id) {
			return 1;
		}
		return 0;
	}
	public void stats() {
		double average_residency;
		if (total_evictions == 0) {
			System.out.println("Process "+id+" had "+num_page_faults+" and average residency is undefined.");
			return;
		}
		average_residency = (double) total_residency_time / (double) total_evictions;
		System.out.println("Process "+id+" had "+num_page_faults+" and "+average_residency+" average residency.");
	}
}