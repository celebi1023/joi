package joi;

import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class MMU {
	// various MMU components: WRAM, VRAM, etc.
    //0000 - 3FFF From cartridge, usually a fixed bank
    //4000 - 7FFF From cartridge, switchable bank
    //8000 - 9FFF Vram, Only bank 0 in Non-CGB mode Switchable bank 0/1 in CGB mode
    //A000 - BFFF 8kb external ram
    //C000 - CFFF 4KB Work RAM (WRAM) bank 0	
    //D000 - DFFF 4KB Work RAM (WRAM) bank 1~N	Only bank 1 in Non-CGB mode Switchable bank 1~7 in CGB mode
    //E000 - FDFF Mirror of C000~DDFF (ECHO RAM)	Typically not used
    //FE00 - FE9F Sprite attribute table (OAM	
    //FEA0 - FEFF Not Usable	
    //FF00 - FF7F I/O Registers	
    //FF80 - FFFE High RAM (HRAM)	
    //FFFF - FFFF Interrupts Enable Register (IE)	
	private byte[] memory;
	private byte[] cart;
	
	public MMU(String fileName) {
		memory = new byte[0x10000];
		openRom(fileName);
	}
	
	private void openRom(String fileName) {
		try {
			//to fix later
			cart = Files.readAllBytes(Paths.get("/Users/justi/joi/roms/" + fileName));
			//for conversion reference
			int begin = 0x0100; int length = 20;
			for(int i = begin; i < begin + length; i++) {
				System.out.print(Integer.toHexString(Byte.toUnsignedInt(cart[i])) + " ");
			}
			for(int i = 0; i < cart.length; i++) {
				memory[i] = cart[i];
			}
			System.out.println("Successfully loaded rom into memory");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte read(int address) {
		return memory[address];
	}
	
	public void write(int address, byte val) {
		memory[address] = val;
	}
}
