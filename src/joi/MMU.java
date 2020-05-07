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
	private int[][] tileSet;
	public int[][] tileMap0; //is this bad practice lol idk
	private int[][] returnTile; //used for getTile()
	public boolean pause; //strictly for testing@!!!!!!!!!!!!!!!!
	
	public MMU(String fileName) {
		memory = new byte[0x10000];
		openRom(fileName);
		tileSet = new int[1000][16]; //for now: starting from 8800, 1000 tiles x 16 bytes
		tileMap0 = new int[32][32]; //for now: starting from 
		returnTile = new int[8][8];
		pause = false;
	}
	
	public void write(int address, int val) {
		if(0x8000 <= address && address <= 0x9000) {//tileBank data, using tileSet 1 for now 
			int index1 = (address - 0x8000)/16;
			int index2 = (address - 0x8000)% 16;
			tileSet[index1][index2] = val % 256;
		}
		if(0x9800 <= address && address <= 0x9bff) {//tileMap #0 (just using this for now)
			int index1 = (address - 0x9800)/32;
			int index2 = (address - 0x8000)% 32;
			tileMap0[index1][index2] = val;
		}
		memory[address] = (byte) val;
	}
	
	public int read(int address) {
		return Byte.toUnsignedInt(memory[address]);
	}
	
	public int[][] getTile(int index) {
		//using returnTile to return
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
			//to fix later
			String startup = "BootRomMod.bin";
			boot = Files.readAllBytes(Paths.get("/Users/justi/joi/roms/" + startup));
			/*
			int begin = 0x00; int length = 20;
			for(int i = begin; i < begin + length; i++) {
				System.out.print(Integer.toHexString(Byte.toUnsignedInt(boot[i])) + " ");
			}
			*/
			/*
			for(int i = 0; i < boot.length; i++) {
				memory[i] = boot[i];
			}
			*/
			for(int i = 0; i < boot.length; i++) {
				write(i, Byte.toUnsignedInt(boot[i]));
			}
			System.out.println("Successfully loaded rom into memory");
			
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
