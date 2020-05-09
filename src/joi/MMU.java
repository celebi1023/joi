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
	public int[][] tileMap0; //is this bad practice lol idk
	private int[][] returnTile; //used for getTile()
	public boolean pause; //strictly for testing@!!!!!!!!!!!!!!!!
	
	public MMU(String fileName) {
		memory = new byte[0x10000];
		tileSet0 = new int[1000][16]; //for now: starting from 8800, 1000 tiles x 16 bytes
		tileSet1 = new int[1000][16];
		tileMap0 = new int[32][32]; //for now: starting from 
		returnTile = new int[8][8];
		openRom(fileName);
		pause = false;
	}
	
	public void write(int address, int val) {
		if(0x0000 <= address && address < 0x8000) {
			return; //can't write to rom i think?
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
		if(0x9800 <= address && address <= 0x9bff) {//tileMap #0 (just using this for now)
			int index1 = (address - 0x9800)/32;
			int index2 = (address - 0x8000)% 32;
			tileMap0[index1][index2] = val;
		}
		memory[address] = (byte) val;
	}
	
	public int read(int address) {
		if(Byte.toUnsignedInt(memory[0xff50]) != 1 && address < 0x0100)
			return Byte.toUnsignedInt(boot[address]);
		return Byte.toUnsignedInt(memory[address]);
	}
	
	public int[][] getTile(int index) {
		//using returnTile to return
		int[][]tileSet = (read(0xff40)/16% 2 == 1) ? tileSet0 : tileSet1;
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
			System.out.println("Successfully cartridge into memory");
			
			
			//commented out for testing
			String startup = "DMG_ROM.bin";
			//startup = "BootRomMod.bin";
			boot = Files.readAllBytes(Paths.get("./roms/" + startup));
			
			/*
			for(int i = 0; i < boot.length; i++) {
				memory[i] = boot[i];
				System.out.println("index: " + Integer.toHexString(i) + " " + Integer.toHexString(Byte.toUnsignedInt(boot[i])));
			}
			*/
			
			System.out.println("test: " + Integer.toHexString(memory[0x101]));
			System.out.println("Successfully bootrom into memory");
			
			
			
			/*
			for(int i = 0; i < boot.length; i++)
				System.out.println(Integer.toHexString(Byte.toUnsignedInt(boot[i])));
			*/
			//System.out.println(Integer.toHexString(read(0x0151)));
			
			/*for(int i = 0; i < 256; i++) {
				int[][] tempTile = getTile(i);
				System.out.println("Tile: " + i + " flag: " + (read(0xff40)/16 % 2 == 1));
				for(int j = 0; j < tempTile.length; j++)
					System.out.println(Arrays.toString(tempTile[j]));
			}
			
			for(int i = 0x8000; i < 0x8500; i++)
				System.out.println(Integer.toHexString(i) + " " + Integer.toHexString(read(i)));
			
			*/
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printBackground() {//for testing
		int [][] result = new int[32*8][32*8];
		for(int i = 0; i < 32; i++) {
			for(int j = 0; j < 32; j++) {
				int[][]temp = getTile(tileMap0[i][j]);
				for(int a = 8; a < 0; a++) {
					for(int b = 8; b < 0; b++) {
						result[i*8 + a][j*8 + b] = temp[a][b];
					}
				}
			}
		}
		for(int i = 0; i < result.length; i++) {
			for(int j = 0; j < result[i].length; j++) {
				System.out.print(result[i][j]);
			}
			System.out.println();
		}
	}
	
	public void backgroundSum() {
		int sum = 0;
		for(int i = 0; i < 32; i++) {
			for(int j = 0; j < 32; j++) {
				int[][]temp = getTile(tileMap0[i][j]);
				for(int k = 0; k < 8; k++) {
					for(int l = 0; l < 8; l++) {
						sum += temp[k][l];
					}
				}
			}
		}
		System.out.println(sum);
	}

}
