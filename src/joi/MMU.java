package joi;

import java.io.IOException;
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
	private byte[] boot;
	private byte[] cart;
	private int[][] tileSet0;
	private int[][] tileSet1;
	public int[][] tileMap0; 
	private int[][] returnTile; //used for getTile()
	private int[] returnTileLine;
	public boolean pause; //strictly for testing@!!!!!!!!!!!!!!!!
	private boolean ime;
	
	public MMU(String fileName) {
		memory = new byte[0x10000];
		tileSet0 = new int[1000][16]; //for now: starting from 8800, 1000 tiles x 16 bytes
		tileSet1 = new int[1000][16];
		tileMap0 = new int[32][32]; //for now: starting from 
		tileMap0 = new int[32][32];	
		returnTile = new int[8][8];
		returnTileLine = new int[8];
		ime = false;
		openRom(fileName);
		pause = false;
	}
	
	public void enableIME() {ime = true;}
	public void disableIME() {ime = false;}
	public boolean getIME() {return ime;}
	
	public void write(int address, int val) {
		if(address == 0xff05) {
			System.out.println("loading into 0xff05: " + val);
			//pause = true;
		}
		if(0x0000 <= address && address < 0x8000) {
			return; //can't write to rom
		}
		if(0x8000 <= address && address < 0x9000) { 
			int index1 = (address - 0x8000)/16;
			int index2 = (address - 0x8000)% 16;
			tileSet0[index1][index2] = val % 256;
		}
		if(0x8800 <= address && address < 0x9000) {
			int index1 = (address - 0x8800)/16 + 128;
			int index2 = (address - 0x8800)% 16;
			tileSet1[index1][index2] = val % 256;
		}
		if(0x9000 <= address && address < 0x9800) {
			int index1 = (address - 0x9000)/16;
			int index2 = (address - 0x9000)% 16;
			tileSet1[index1][index2] = val % 256;
		}
		if(0x9800 <= address && address <= 0x9bff) {//tileMap #0 
			int index1 = (address - 0x9800)/32;
			int index2 = (address - 0x9800)% 32;
			tileMap0[index1][index2] = val % 256;
		}
		if(0x9c00 <= address && address <= 0x9fff) {//tileMap #0 (just using this for now)
			int index1 = (address - 0x9c00)/32;
			int index2 = (address - 0x9c00)% 32;
			tileMap0[index1][index2] = val % 256;
		}
		if(address == 0xff46) {
			for(int i = 0; i < 160; i++)
				write(0xfe00 + i, read((val << 8) + i));
		}
		
		memory[address] = (byte) val;
	}
	
	public int read(int address) {
		if(Byte.toUnsignedInt(memory[0xff50]) != 1 && address < 0x0100)
			return Byte.toUnsignedInt(boot[address]);
		
		if(address == 0xff00) { //joypad, will return 1111 for now
			//gonna do this for now
			return ((Byte.toUnsignedInt(memory[address])) | 0b11001111); //TODO
		}
		
		return Byte.toUnsignedInt(memory[address]);
	}

	public int[][] getTile(int index) {//used for sprites
		//using returnTile to return
		int[][]tileSet = tileSet0; //i think having sprites using tileSet0 directly should be ok
		//System.out.println(Integer.toBinaryString(read(0xff40)));
		for(int i = 0; i < 8; i++) {
			String first = Integer.toBinaryString(tileSet[index][2*i]);
			String second = Integer.toBinaryString(tileSet[index][2*i + 1]);
			while(first.length() < 8)
				first = '0' + first;
			while(second.length() < 8)
				second = '0' + second;
			for(int j = 0; j < 8; j++) {
				int firstBit = (first.charAt(j) == '1') ? 1 : 0;
				int secondBit = (second.charAt(j) == '1') ? 1 : 0;
				returnTile[i][j] = firstBit + secondBit * 2;
			}
		}
		return returnTile;
	}
	
	public int[] getTileLine(int index, int line) {
		int[][]tileSet = (read(0xff40)/16% 2 == 1) ? tileSet0 : tileSet1;
		String first = Integer.toBinaryString(tileSet[index][2*line]);
		String second = Integer.toBinaryString(tileSet[index][2*line + 1]);
		while(first.length() < 8)
			first = '0' + first;
		while(second.length() < 8)
			second = '0' + second;
		for(int j = 0; j < 8; j++) {
			int firstBit = (first.charAt(j) == '1') ? 1 : 0;
			int secondBit = (second.charAt(j) == '1') ? 1 : 0;
			returnTileLine[j] = firstBit + secondBit * 2;
		}
		return returnTileLine;
	}
	
	private void openRom(String fileName) {
		try {
			//absolute path to fix later
			//cartridge
			
			cart = Files.readAllBytes(Paths.get("./roms/" + fileName));
			for(int i = 0; i < 0x8000; i++) {
				memory[i] = cart[i];
			}
			for(int i = 0; i < cart.length; i++) {
				write(i, Byte.toUnsignedInt(cart[i]));
			}
			System.out.println("Successfully loaded cartridge into memory");
			
			
			//commented out for testing
			String startup = "DMG_ROM.bin";
			//startup = "BootRomMod.bin";
			boot = Files.readAllBytes(Paths.get("./roms/" + startup));
			System.out.println("Successfully loaded bootrom into memory");

			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
