package joi;

public class Gameboy{
	
	private Cpu cpu;
	private PPU ppu;
	private Interrupt interrupt;
	private Joypad joypad;
	//static boolean pause = false; //strictly for testing

	public static void main(String[] args) {
		Gameboy gb = new Gameboy("tetris.gb");
		gb.start();
	}
	
	public Gameboy(String fileName) {
		MMU memory = new MMU(fileName);
		Regs registers = new Regs();
		cpu = new Cpu(memory, registers);
		ppu = new PPU(memory);
		interrupt = new Interrupt(memory, registers);
		joypad = new Joypad(memory);
	}
	
	public void start() {
		while(true) {
			int cycleIncrease = cpu.step();
			ppu.step(cycleIncrease);
			//ppu.step(0);
			interrupt.step();
		}
	}
}
