package joi;

public class Cpu {
	private Regs regs;
	private MMU mmu;
	short writeAdddress;
	
	public Cpu(String fileName) {
		regs = new Regs();
		mmu = new MMU(fileName);
	}
	
	public byte fetch() {
		byte opCode = mmu.read(regs.getPC());
		regs.setPC(regs.getPC() + 1);
		return opCode;
	}
	
	public int fetchWord() {
		int low = Byte.toUnsignedInt(fetch());
		int high = Byte.toUnsignedInt(fetch());
		return high*16*16 + low;
	}
	
	public void jump(int address) {
		regs.setPC(address);
	}
	
	
	//returns number of cycles running the instruction took
	public int run() {
		int opCode = Byte.toUnsignedInt(fetch());
		System.out.print("Current ins: " + Integer.toHexString(opCode));
		System.out.println("\t   Current PC: " + Integer.toHexString(regs.getPC() - 1));
		switch(opCode) {
		//nop
			case 0x00: return 4;
			
		//xor
			case 0xaf: {
				byte result = (byte) (regs.getA() ^ regs.getA()); 
				regs.setA(result);
				//set flags
				return 4;
			}
			
		//jump	
			case 0xc3: jump(fetchWord());	return 16;
			
			
			default:
				System.out.println("Unrecognized instruction: " + Integer.toHexString(opCode));
				System.out.println("PC: 0x" + Integer.toHexString((regs.getPC()-1)));
				System.exit(1);
		}
		
		
		return 0;
	}
	

	
}
