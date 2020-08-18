package joi;

import java.util.ArrayList;
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
	Display monitor;
	int mode; //which stage the ppu is in
	int cycles;
	int scanline;
	
	int test1;
	double time1;
	double time2;
	
	int[][]windowBuffer;
	
	public PPU(MMU m) {
		mmu = m;
		monitor = new Display(160, 144, m);
		mode = modeOAM; 
		windowBuffer = new int[144][160];
		
		test1 = 0;
		time1 = 0;
		time2 = 0;
	}

	public void step(int cycleIncrease) {
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
						//vblank interrupt
						if((mmu.read(0xffff) % 2) == 1) {
							mmu.write(0xff0f, (mmu.read(0xff0f) | 0b00000001));
						}
						mode = modeVBLANK;
						
						//UPDATE SCREEN -------------------------------
						updateSprites();
						//window buffer is ready
						monitor.render(windowBuffer, true);
						monitor.showAsFrame();
						time2 = time1;
						time1 = System.nanoTime()/1000000000.0;
						System.out.println(time1 - time2);
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
		//render background
		int x = mmu.read(scrollX);
		int y = mmu.read(scrollY) + scanline;
		if(x >= 256 || y >= 256)
			return;
		int[] currentTileLine = mmu.getTileLine(mmu.tileMap0[y/8][x/8], y % 8);
		for(int i = 0; i < 160; i++) {
			if((x + i) % 8 == 0) {
				currentTileLine = mmu.getTileLine(mmu.tileMap0[y/8][(x + i)/8], y % 8);
			}
			int pixel = currentTileLine[(x + i) % 8];
			int palette = mmu.read(0xff47);
			windowBuffer[scanline][i] = ((palette >> (2 * pixel + 1)) % 2) * 2 + ((palette >> (2 * pixel)) % 2);
		}
	}
	
	private void updateSprites() {
		//first make a pass of priority 1
		for(int i = 0; i < 40; i++) {
			int y = mmu.read(0xfe00 + 4*i);
			int x = mmu.read(0xfe00 + 4*i + 1);
			if(y != 0 && x != 0) {
				y = y - 16;
				x = x - 8;
				int id = mmu.read(0xfe00 + 4*i + 2);
				int flags = mmu.read(0xfe00 + 4*i + 3);
				if(((flags >> 7) & 1) == 1) {
					boolean flipY = ((flags >> 6) & 1) == 1;
					boolean flipX = ((flags >> 5) & 1) == 1;
					int startY = flipY ? 8 : 0;
					int incY = flipY ? -1 : 1;
					int startX = flipX ? 8 : 0;
					int incX = flipX ? -1 : 1;
					boolean paletteFlag = ((flags >> 4) & 1) == 1;
					int palette = paletteFlag ? mmu.read(0xff49) : mmu.read(0xff48);
					int[][]currentTile = mmu.getTile(id);
					for(int iterY = startY; (iterY < 8 && iterY >= 0); iterY += incY) {
						for(int iterX = startX; (iterX < 8 && iterX >= 0); iterX += incX) {
							if(0 <= (y + iterY) && (y + iterY) < 145 && 0 <= (x + iterX) && (x + iterX) < 161 && windowBuffer[y + iterY][x + iterX] == 0) {
								int pixel = currentTile[iterY][iterX];
								int color = ((palette >> (2 * pixel + 1)) % 2) * 2 + ((palette >> (2 * pixel)) % 2);
								if(pixel != 0 && (y + iterY) < 145 && (x + iterX) < 161)
									windowBuffer[y + iterY][x + iterX] = color;
							}
						}
					}
				}
			}
		}
		//next make a pass of priority 0
		for(int i = 0; i < 40; i++) {
			int y = mmu.read(0xfe00 + 4*i);
			int x = mmu.read(0xfe00 + 4*i + 1);
			if(y != 0 && x != 0) {
				y = y - 16;
				x = x - 8;
				int id = mmu.read(0xfe00 + 4*i + 2);
				int flags = mmu.read(0xfe00 + 4*i + 3);
				if(((flags >> 7) & 1) == 0) {
					boolean flipY = ((flags >> 6) & 1) == 1;
					boolean flipX = ((flags >> 5) & 1) == 1;
					int startY = flipY ? 8 : 0;
					int incY = flipY ? -1 : 1;
					int startX = flipX ? 8 : 0;
					int incX = flipX ? -1 : 1;
					boolean paletteFlag = ((flags >> 4) & 1) == 1;
					int palette = paletteFlag ? mmu.read(0xff49) : mmu.read(0xff48);
					int[][]currentTile = mmu.getTile(id);
					for(int iterY = startY; (iterY < 8 && iterY >= 0); iterY += incY) {
						for(int iterX = startX; (iterX < 8 && iterX >= 0); iterX += incX) {
							if(0 <= (y + iterY) && (y + iterY) < 145 && 0 <= (x + iterX) && (x + iterX) < 161) {
								int pixel = currentTile[iterY][iterX];
								int color = ((palette >> (2 * pixel + 1)) % 2) * 2 + ((palette >> (2 * pixel)) % 2);
								if(pixel != 0 && (y + iterY) < 145 && (x + iterX) < 161)
									windowBuffer[y + iterY][x + iterX] = color;
							}
						}
					}
				}
			}
		}
	}
	
}
