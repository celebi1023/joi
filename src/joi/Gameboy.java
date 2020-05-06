package joi;

public class Gameboy {
	
	private Cpu cpu;

	public static void main(String[] args) {
		System.out.println("Hello world");
		Gameboy gb = new Gameboy("tetris.gb");
		gb.start();
	}
	
	public Gameboy(String fileName) {
		MMU memory = new MMU(fileName);
		cpu = new Cpu(memory);
	}
	
	public void start() {
		int stopper = 0;
		while(true) {
			cpu.step();
			stopper++;
			//if(stopper > 100) break;
		}
	}
}
