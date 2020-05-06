package joi;

public class PPU {
	//using the javascript tutorial's convention but shouldn't matter
	public static final int modeOAM = 2;
	public static final int modeVRAM = 3;
	public static final int modeHBLANK = 0;
	public static final int modeVBLANK = 1;
	
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
				if(mode >= 456) {
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
	}
	
}
