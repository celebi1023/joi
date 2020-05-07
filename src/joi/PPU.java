package joi;

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
	
	public PPU(MMU m) {
		mmu = m;
		mode = modeOAM; //no idea
	}

	public void step(int cycleIncrease) { //cycle increase could be incorrect, gonna use it like this for now
		System.out.println("\tcycles: " + cycles + " scanline: " + scanline + " mode: " + mode);
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
	
}
