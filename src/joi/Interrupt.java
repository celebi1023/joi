package joi;

public class Interrupt {
	private MMU mmu;
	private Regs regs;
	public Interrupt(MMU m, Regs r) {
		mmu = m;
		regs = r;
	}
	
	public int step() {//returns cycles
		if(mmu.getIME()) {
			//vblank interrupt
			if((mmu.read(0xffff) & mmu.read(0xff0f) & 0b00000001) == 1) {
				mmu.write(0xff0f, (mmu.read(0xff0f) & 0b11111110));
				pushWord(regs.getPC());
				regs.setPC(0x40);
				return 12;
			}
		}
		return 0;
	}
	
	public void pushByte(int val) {
		regs.setSP(regs.getSP() - 1);
		mmu.write(regs.getSP(), val);
	}
	
	public void pushWord(int val) {
		pushByte(val/256);
		pushByte(val%256);
	}
}
