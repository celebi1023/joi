package joi;

public class Gameboy {
	
	private Cpu cpu;
	private PPU ppu;

	public static void main(String[] args) {
		System.out.println("Hello world");
		//Gameboy gb = new Gameboy("tetris.gb");
		//gb.start();
	}
	
	public Gameboy(String fileName) {
		MMU memory = new MMU(fileName);
		cpu = new Cpu(memory);
		ppu = new PPU(memory);
	}
	
	public void start() {
		int stopper = 0;
		while(true) {
			System.err.print("test");
			System.exit(1);
			int cycleIncrease = cpu.step();
			System.err.print("test");
			System.exit(1);
			ppu.step(cycleIncrease);
			stopper++;
			if(stopper > 100) break;
		}
	}
}
