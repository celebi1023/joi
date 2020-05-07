package joi;

import java.util.Arrays;

public class PPU {
	public static final int modeOAM = 2;
	public static final int modeVRAM = 3;
	public static final int modeHBLANK = 0;
	public static final int modeVBLANK = 1;
	
	//registers
	public static final int lcdc = 0xff40; //lcd control
	public static final int stat = 0xff41; //lcdc status
	public static final int scrollY = 0xff42;
	public static final int scrollX = 0xff43; 
	public static final int ly = 0xff44; //lcdc y-coordinate
	public static final int lyc = 0xff45; //ly compare
	public static final int dma = 0xff46; //dma transfer and start address
	public static final int bgp = 0xff47; //bg & window palette data
	public static final int obp0 = 0xff48; //object palette 0 data
	public static final int obp1 = 0xff49; //object palette 1 data 
	public static final int wy = 0xff4a; //window y position
	public static final int wx = 0xff4b; //window x position
	public static final int ie = 0xffff; //interrupt enable 
	
	
	MMU mmu;
	int mode; //which stage the ppu is in
	int cycles;
	int scanline;
	
	int[][]windowBuffer;
	
	public PPU(MMU m) {
		mmu = m;
		mode = modeOAM; //no idea
		windowBuffer = new int[144][160];
	}

	public void step(int cycleIncrease) { //cycle increase could be incorrect, gonna use it like this for now
		//System.out.println("\t\t\t\t\t\t\t\t\tcycles: " + cycles + " scanline: " + scanline + " mode: " + mode);
		cycles += cycleIncrease;
		switch(mode) {
			case modeOAM: {
				if(cycles >= 80) {
					cycles = 0;
					mode = modeVRAM;
				}
				break;
			}
			case modeVRAM: {
				if(cycles >= 172) {
					cycles = 0;
					mode = modeHBLANK;
					//render line on frame buffer
					renderLine();
				}
				break;
			}
			case modeHBLANK: {
				if(cycles >= 204) {
					cycles = 0;
					scanline++;
					if(scanline == 143) {
						mode = modeVBLANK;
						//update frame (or maybe wait until vblank to update? idk)
						/*
						for(int i = 0; i < windowBuffer.length; i++)
							System.out.println(Arrays.toString(windowBuffer[i]));
						System.out.println();
						*/
						//mmu.printBackground();
						/*
						for(int i = 0; i < mmu.getTile(1).length; i++){
							System.out.println(Arrays.toString(mmu.getTile(1)[i]));
						}
						for(int i = 0; i < mmu.tileMap0.length; i++)
							System.out.println(Arrays.toString(mmu.tileMap0[i]));
						System.out.println();
						mmu.backgroundSum();
						*/
						if(mmu.pause) {
							/*
							System.out.println("success");
							for(int i = 0; i < mmu.getTile(1).length; i++){
								System.out.println(Arrays.toString(mmu.getTile(1)[i]));
							}
							for(int i = 0; i < mmu.tileMap0.length; i++)
								System.out.println(Arrays.toString(mmu.tileMap0[i]));
							System.out.println();
							mmu.backgroundSum();
							
							//work
							
							scanline = 0;
							while(scanline < 144) {
								renderLine();
								scanline++;
								for(int i = 0; i < windowBuffer.length; i++)
									System.out.println(Arrays.toString(windowBuffer[i]));
								System.out.println();
							}
							*/
							System.out.println("exiting");
							System.exit(1);
						}
						//UPDATE SCREEN -------------------------------
						//window buffer is ready
						
					}
					else {
						mode = modeOAM;
					}
				}
				break;
			}
			case modeVBLANK: {
				if(cycles >= 456) {
					cycles = 0;
					scanline++;
					if(scanline > 153) {
						mode = modeOAM;
						scanline = 0;
					}
				}
				break;
			}
		}
		//update ff44
		mmu.write(ly, scanline);
	}
	
	private void renderLine() {
		int x = mmu.read(scrollX);
		int y = mmu.read(scrollY) + scanline;
		int[][] currentTile = mmu.getTile(mmu.tileMap0[y/8][x/8]);
		for(int i = 0; i < 160; i++) {
			if((x + i) % 8 == 0) {
				currentTile = mmu.getTile(mmu.tileMap0[y/8][(x + i)/8]);
			}
			windowBuffer[scanline][i] = currentTile[y % 8][(x + i) % 8];
		}
	}
	
}
