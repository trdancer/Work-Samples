public class Page {
	int cycle_loaded;
	int num;
	int process;
	int last_used;
	public Page(int num, int process, int cycle_loaded) {
		this.num = num;
		this.process = process;
		this.cycle_loaded = cycle_loaded;
		this.last_used = cycle_loaded;
	}
}