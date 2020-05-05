package joi;

public class Cpu {
	private Regs regs;
	private MMU mmu;
	short writeAdddress;
	
	public Cpu(String fileName) {
		regs = new Regs();
		mmu = new MMU(fileName);
	}
	
	public int fetchByte() {
		int opCode = mmu.read(regs.getPC());
		regs.setPC(regs.getPC() + 1);
		return opCode;
	}
	
	public int fetchWord() {
		int low = fetchByte();
		int high = fetchByte();
		return high*16*16 + low;
	}
	
	public void jump(int address, boolean condition) {
		if(condition)
			regs.setPC(address);
	}
	
	
	//returns number of cycles running the instruction took
	public int run() {
		int opCode = fetchByte();
		System.out.print("Current ins: " + Integer.toHexString(opCode));
		System.out.print("\t   Current PC: " + Integer.toHexString(regs.getPC() - 1));
		System.out.println("\tzsfcL " + regs.getZSHC());
		switch(opCode) {
		//nop
			case 0x00: return 4;
			
		//dec
			case 0x05: {
				regs.setB(regs.subByte(regs.getB(), 1)); 
				return 4;
			}
			
		//8-bit load //i guess loads don't trigger flags?
			case 0x06: {
				regs.setB(fetchByte()); 
				return 8;
			}
			case 0x0e: {
				regs.setC(fetchByte()); 
				return 8;
			}
			
		//16-bit load
			case 0x21: {
				regs.setHL(fetchWord());	
				return 12;
			}
			
		//load reg to mem
			case 0x32: {
				mmu.write(regs.getHL(), regs.getA());
				regs.setHL(regs.getHL() - 1);
				return 8;
			}
				
			
		//xor
			case 0xaf: {
				regs.setA(regs.setFlags(regs.getA() ^ regs.getA()));
				return 4;
			}
			
		//jump	
			case 0x20: {
				jump((byte) fetchByte() + regs.getPC(), !regs.getZero()); 
				return 8;
			}
			
			
			case 0xc3: jump(fetchWord(), true);	return 16; //i don't think flags are affected? not sure
			
			
			default:
				//System.out.println(Integer.toHexString(regs.getB()));
				System.out.println("Unrecognized instruction: " + Integer.toHexString(opCode));
				System.out.println("PC: 0x" + Integer.toHexString((regs.getPC()-1)));
				System.exit(1);
		}
		
		
		return 0;
	}
	

	
}
