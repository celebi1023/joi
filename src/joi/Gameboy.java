package joi;

import java.io.IOException;

public class Gameboy {
	
	private Cpu cpu;
	private PPU ppu;
	private Interrupt interrupt;
	//static boolean pause = false; //strictly for testing

	public static void main(String[] args) {
		System.out.println("Hello world");
		Gameboy gb = new Gameboy("drmario.gb");
		gb.start();
	}
	
	public Gameboy(String fileName) {
		MMU memory = new MMU(fileName);
		Regs registers = new Regs();
		cpu = new Cpu(memory, registers);
		ppu = new PPU(memory);
		interrupt = new Interrupt(memory, registers);
	}
	
	public void start() {
		int stopper = 0;
		while(true) {
			int cycleIncrease = cpu.step();
			ppu.step(cycleIncrease);
			//ppu.step(0);
			interrupt.step();
			
			stopper++;
			//if(stopper > 100) break;
		}
	}
}
