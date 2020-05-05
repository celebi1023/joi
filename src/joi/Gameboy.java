package joi;

import java.io.*;

public class Gameboy {
	
	private Cpu cpu;

	public static void main(String[] args) {
		System.out.println("Hello world");
		Gameboy gb = new Gameboy("tetris.gb");
		gb.start();
	}
	
	public Gameboy(String fileName) {
		cpu = new Cpu(fileName);
	}
	
	public void start() {
		int stopper = 0;
		while(true) {
			cpu.run();
			stopper++;
			//if(stopper > 800) break;
		}
	}
}
