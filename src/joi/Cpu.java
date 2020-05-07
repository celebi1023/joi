package joi;

import java.util.Arrays;

public class Cpu {
	private Regs regs;
	private MMU mmu;
	short writeAdddress;
	
	public Cpu(MMU m) {
		regs = new Regs();
		mmu = m;
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
	
	public void pushByte(int val) {
		regs.setSP(regs.getSP() - 1);
		//System.out.println("sp: " + Integer.toHexString(regs.getSP()) + " val: " + Integer.toHexString(val));
		mmu.write(regs.getSP(), val);
	}
	
	public void pushWord(int val) {
		pushByte(val/256);
		pushByte(val%256);
	}
	
	public int popByte() {
		int result = mmu.read(regs.getSP());
		regs.setSP(regs.getSP() + 1);
		return result;
	}
	
	public int popWord() {
		int low = popByte();
		int high = popByte();
		return high * 256 + low;
	}
	
	public void call(int toJump, boolean condition) {
		if(condition) {
			pushWord(regs.getPC());
			regs.setPC(toJump);
		}
	}
	
	public void ret(boolean condition) {
		if(condition) {
			regs.setPC(popWord());
		}
	}
	
	
	//returns number of cycles running the instruction took
	public int step() {
		int opCode = fetchByte();
		/*
		System.out.print("Current ins: " + Integer.toHexString(opCode));
		System.out.print("\t   Current PC: " + Integer.toHexString(regs.getPC() - 1));
		System.out.print("\tzsfc: " + regs.getZSHC());
		System.out.println("\tdesired reg: " + Integer.toHexString(regs.getC()));
		*/
		
		//for testing
		
		if(regs.getPC() - 1 == 0x00fa) {
			//mmu.printBackground();
			//System.out.println("locked up");
			mmu.pause = true;
			//System.exit(1);
		}
		
		switch(opCode) {
		//nop
			case 0x00: return 4;
			
		//dec
			case 0x3d: {//dec a
				regs.setA(regs.subByte(regs.getA(), 1));
				return 4;
			}
			case 0x05: {//dec b
				regs.setB(regs.subByte(regs.getB(), 1)); 
				return 4;
			}
			case 0x0d: {//dec c
				regs.setC(regs.subByte(regs.getC(), 1));
				return 4;
			}
			case 0x15: {//dec d
				regs.setD(regs.subByte(regs.getD(), 1));
				return 4;
			}
			case 0x1d: {//dec e
				regs.setE(regs.subByte(regs.getE(), 1));
				return 4;
			}
			case 0x25: {//dec h
				regs.setH(regs.subByte(regs.getH(), 1));
				return 4;
			}
			case 0x2d: {//dec l
				regs.setL(regs.subByte(regs.getL(), 1));
				return 4;
			}
			
		//inc
			case 0x3c: {///inc a
				regs.setA(regs.addByte(regs.getA(), 1));
				return 4;
			}
			case 0x04: {///inc b
				regs.setB(regs.addByte(regs.getB(), 1));
				return 4;
			}
			case 0x0c: {///inc c
				regs.setC(regs.addByte(regs.getC(), 1));
				return 4;
			}
			case 0x14: {///inc d
				regs.setD(regs.addByte(regs.getD(), 1));
				return 4;
			}
			case 0x1c: {///inc e
				regs.setE(regs.addByte(regs.getE(), 1));
				return 4;
			}
			case 0x024: {///inc h
				regs.setH(regs.addByte(regs.getH(), 1));
				return 4;
			}
			case 0x2c: {///inc l
				regs.setL(regs.addByte(regs.getL(), 1));
				return 4;
			}
			case 0x34: {//inc (hl) 
				mmu.write(regs.getHL(), regs.addByte(mmu.read(regs.getHL()), 1)); //could be wrong, haven't tested
				return 12;	
			}
			case 0x03:{//inc bc
				regs.setBC(regs.getBC() + 1);
				return 8;
			}
			case 0x13:{//inc de
				regs.setDE(regs.getDE() + 1);
				return 8;
			}
			case 0x23: {//inc hl
				regs.setHL(regs.getHL() + 1);
				return 8;
			}
			case 0x33:{//inc sp
				regs.setSP(regs.getSP() + 1);
				return 8;
			}
			
		//8-bit load 
			case 0x3e: {
				regs.setA(fetchByte());
				return 8;
			}
			case 0x06: {//ld b, n
				regs.setB(fetchByte()); 
				return 8;
			}
			case 0x0e: {//ld c, n
				regs.setC(fetchByte()); 
				return 8;
			}
			case 0x16: {//ld d, n
				regs.setD(fetchByte()); 
				return 8;
			}
			case 0x1e: {//ld e, n
				regs.setE(fetchByte()); 
				return 8;
			}
			case 0x26: {//ld h, n
				regs.setH(fetchByte()); 
				return 8;
			}
			case 0x2e: {//ld l, n
				regs.setL(fetchByte()); 
				return 8;
			}
			
		//16-bit load
			case 0x11: {
				regs.setDE(fetchWord());
				return 12;
			}
			
			case 0x21: {
				regs.setHL(fetchWord());	
				return 12;
			}
			
			case 0x31: {
				regs.setSP(fetchWord());
				return 12;
			}
		//load reg into reg

			case 0x7f: {//ld A, A
				regs.setA(regs.getA());
				return 4;
			}
			case 0x78: {//ld A, B
				regs.setA(regs.getB());
				return 4;
			}
			case 0x79: {//ld A, C
				regs.setA(regs.getC());
				return 4;
			}
			case 0x7a: {//ld A, D
				regs.setA(regs.getD());
				return 4;
			}
			case 0x7b: {//ld A, E
				regs.setA(regs.getE());
				return 4;
			}
			case 0x7c: {//ld A, H
				regs.setA(regs.getH());
				return 4;
			}
			case 0x7d: {//ld A, L
				regs.setA(regs.getL());
				return 4;
			}
			case 0x47: {//ld B, A
				regs.setB(regs.getA());
				return 4;
			}
			case 0x40: {//ld B, B
				regs.setB(regs.getB());
				return 4;
			}
			case 0x41: {//ld B, C
				regs.setB(regs.getC());
				return 4;
			}
			case 0x42: {//ld B, D
				regs.setB(regs.getD());
				return 4;
			}
			case 0x43: {//ld B, E
				regs.setB(regs.getE());
				return 4;
			}
			case 0x44: {//ld B, H
				regs.setB(regs.getH());
				return 4;
			}
			case 0x45: {//ld B, L
				regs.setB(regs.getL());
				return 4;
			}
			case 0x4f: { //ld C, A
				regs.setC(regs.getA());
				return 4;
			}
			case 0x48: {//ld C, B
				regs.setC(regs.getB());
				return 4;
			}
			case 0x49: {//ld C, C
				regs.setC(regs.getC());
				return 4;
			}
			case 0x4a: {//ld C, D
				regs.setC(regs.getD());
				return 4;
			}
			case 0x4b: {//ld C, E
				regs.setC(regs.getE());
				return 4;
			}
			case 0x4c: {//ld C, H
				regs.setC(regs.getH());
				return 4;
			}
			case 0x4d: {//ld C, L
				regs.setC(regs.getL());
				return 4;
			}
			case 0x57: {//ld D, A
				regs.setD(regs.getA());
				return 4;
			}
			case 0x50: {//ld D, B
				regs.setD(regs.getB());
				return 4;
			}
			case 0x51: {//ld D, C
				regs.setD(regs.getC());
				return 4;
			}
			case 0x52: {//ld D, D
				regs.setD(regs.getD());
				return 4;
			}
			case 0x53: {//ld D, E
				regs.setD(regs.getE());
				return 4;
			}
			case 0x54: {//ld D, H
				regs.setD(regs.getH());
				return 4;
			}
			case 0x55: {//ld D, L
				regs.setD(regs.getL());
				return 4;
			}
			case 0x5f: {//ld E, A
				regs.setE(regs.getA());
				return 4;
			}
			case 0x58: {//ld E, B
				regs.setE(regs.getB());
				return 4;
			}
			case 0x59: {//ld E, C
				regs.setE(regs.getC());
				return 4;
			}
			case 0x5a: {//ld E, D
				regs.setE(regs.getD());
				return 4;
			}
			case 0x5b: {//ld E, E
				regs.setE(regs.getE());
				return 4;
			}
			case 0x5c: {//ld E, H
				regs.setE(regs.getH());
				return 4;
			}
			case 0x5d: {//ld E, L
				regs.setE(regs.getL());
				return 4;
			}
			case 0x67: {//ld H, A
				regs.setH(regs.getA());
				return 4;
			}
			case 0x60: {//ld H, B
				regs.setH(regs.getB());
				return 4;
			}
			case 0x61: {//ld H, C
				regs.setH(regs.getC());
				return 4;
			}
			case 0x62: {//ld H, D
				regs.setH(regs.getD());
				return 4;
			}
			case 0x63: {//ld H, E
				regs.setH(regs.getE());
				return 4;
			}
			case 0x64: {//ld H, H
				regs.setH(regs.getH());
				return 4;
			}
			case 0x65: {//ld H, L
				regs.setH(regs.getL());
				return 4;
			}
			case 0x6f: {//ld L, A
				regs.setL(regs.getA());
				return 4;
			}
			case 0x68: {//ld L, B
				regs.setL(regs.getB());
				return 4;
			}
			case 0x69: {//ld L, C
				regs.setL(regs.getC());
				return 4;
			}
			case 0x6a: {//ld L, D
				regs.setL(regs.getD());
				return 4;
			}
			case 0x6b: {//ld L, E
				regs.setL(regs.getE());
				return 4;
			}
			case 0x6c: {//ld L, H
				regs.setL(regs.getH());
				return 4;
			}
			case 0x6d: {//ld L, L
				regs.setL(regs.getL());
				return 4;
			}
			
		//load reg to mem
			case 0x02: { //ld (bc), a
				mmu.write(regs.getBC(), regs.getA());
				return 8;
			}
			case 0x12: { //ld (de), a
				mmu.write(regs.getDE(), regs.getA());
				return 8;
			}
			case 0x22: { //LD(HDI) A
				mmu.write(regs.getHL(), regs.getA());
				regs.setHL(regs.getHL() + 1);
				return 8;
			}
			case 0x32: {
				mmu.write(regs.getHL(), regs.getA());
				regs.setHL(regs.getHL() - 1);
				return 8;
			}
			
			case 0x77: {//ld(hl), a
				mmu.write(regs.getHL(), regs.getA());
				return 8;
			}
			
			case 0xe2: {
				mmu.write(0xFF00 + regs.getC(), regs.getA());
				return 8;
			}
				
			case 0xE0: { //put a into FF00 + n
				mmu.write(0xFF00 + fetchByte(), regs.getA());
				return 12;
			}
			case 0xEA: {//ld (nn), A
				mmu.write(fetchWord(), regs.getA());
				return 16;
			}
			
		//load mem to reg
			case 0x1a: {
				regs.setA(mmu.read(regs.getDE()));
				return 8;
			}
			case 0x7e: {//ld A, (HL)
				regs.setA(mmu.read(regs.getHL()));
				return 8;
			}
			case 0xF0: {
				regs.setA(mmu.read(0xFF00 + fetchByte()));
				return 12;
			}
		//compare
			case 0xbf: { //cp A, A
				regs.subByte(regs.getA(), regs.getA());
				return 4;
			}
			case 0xb8: { //cp A, B
				regs.subByte(regs.getA(), regs.getB());
				return 4;
			}
			case 0xb9: { //cp A, C
				regs.subByte(regs.getA(), regs.getC());
				return 4;
			}
			case 0xba: { //cp A, D
				regs.subByte(regs.getA(), regs.getD());
				return 4;
			}
			case 0xbb: { //cp A, E
				regs.subByte(regs.getA(), regs.getE());
				return 4;
			}
			case 0xbc: { //cp A, H
				regs.subByte(regs.getA(), regs.getH());
				return 4;
			}
			case 0xbd: { //cp A, L
				regs.subByte(regs.getA(), regs.getL());
				return 4;
			}
			case 0xbe: { //cp A, (hl)
				regs.subByte(regs.getA(), mmu.read(regs.getHL()));
				return 8;
			}
			case 0xfe: { //cp A, #
				regs.subByte(regs.getA(), fetchByte());
				return 8;
			}
		//sub
			case 0x97: { //sub a
				regs.setA(regs.subByte(regs.getA(), regs.getA()));
				return 4;
			}
			case 0x90: { //sub b
				regs.setA(regs.subByte(regs.getA(), regs.getB()));
				return 4;
			}
			case 0x91: { //sub c
				regs.setA(regs.subByte(regs.getA(), regs.getC()));
				return 4;
			}
			case 0x92: { //sub d
				regs.setA(regs.subByte(regs.getA(), regs.getD()));
				return 4;
			}
			case 0x93: { //sub e
				regs.setA(regs.subByte(regs.getA(), regs.getE()));
				return 4;
			}
			case 0x94: { //sub h
				regs.setA(regs.subByte(regs.getA(), regs.getH()));
				return 4;
			}
			case 0x95: { //sub l
				regs.setA(regs.subByte(regs.getA(), regs.getL()));
				return 4;
			}
			case 0x96: { //sub (hl)
				regs.setA(regs.subByte(regs.getA(), mmu.read(regs.getHL())));
				return 8;
			}
			case 0xd6: { //sub #
				regs.setA(regs.subByte(regs.getA(), fetchByte()));
				return 8;
			}
		//add
			case 0x87: { //add a
				regs.setA(regs.addByte(regs.getA(), regs.getA()));
				return 4;
			}
			case 0x80: { //add b
				regs.setA(regs.addByte(regs.getA(), regs.getB()));
				return 4;
			}
			case 0x81: { //add c
				regs.setA(regs.addByte(regs.getA(), regs.getC()));
				return 4;
			}
			case 0x82: { //add d
				regs.setA(regs.addByte(regs.getA(), regs.getD()));
				return 4;
			}
			case 0x83: { //add e
				regs.setA(regs.addByte(regs.getA(), regs.getE()));
				return 4;
			}
			case 0x84: { //add h
				regs.setA(regs.addByte(regs.getA(), regs.getH()));
				return 4;
			}
			case 0x85: { //add l
				regs.setA(regs.addByte(regs.getA(), regs.getL()));
				return 4;
			}
			case 0x86: { //add (hl)
				regs.setA(regs.addByte(regs.getA(), mmu.read(regs.getHL())));
				return 8;
			}
			case 0xc6: { //add #
				regs.setA(regs.addByte(regs.getA(), fetchByte()));
				return 8;
			}
		//xor
			case 0xaf: {
				regs.setA(regs.setFlags(regs.getA() ^ regs.getA()));
				return 4;
			}
		//rotate
			case 0x17: {
				regs.setA(regs.rotateByteLeftCarry(regs.getA()));
				return 4;
			}
			
		//jump	
			case 0xc3: {//jp nn
				jump(fetchWord(), true); //i don't think flags are affected? not sure
				return 16;
			}
			case 0xe9: {//jr (HL)
				jump(regs.getHL(), true);
				return 4;
			}
			case 0x18: {//jr n
				jump((byte) fetchByte() + regs.getPC(), true);
				return 8;
			}
			case 0x20: {//jr nz, n
				jump((byte) fetchByte() + regs.getPC(), !regs.getZero()); 
				return 8;
			}
			case 0x28: {//jr z, n
				jump((byte) fetchByte() + regs.getPC(), regs.getZero()); 
				return 8; 
			}
			case 0x30: {//jr nc, n
				jump((byte) fetchByte() + regs.getPC(), !regs.getCarry()); 
				return 8;
			}
			case 0x38: {//jr c, n
				jump((byte) fetchByte() + regs.getPC(), regs.getCarry()); 
				return 8;
			}
			
		//push
			case 0xc5: {//push bc
				//System.out.println("preBC: " + Integer.toHexString(regs.getBC()));
				pushWord(regs.getBC());
				return 16;
			}
		//pop
			case 0xc1: {//pop bc
				regs.setBC(popWord());
				//System.out.println("afterBC: " + Integer.toHexString(regs.getBC()));
				return 12;
			}
		//call
			case 0xcd: {
				call(fetchWord(), true);
				return 12;
			}
		//return
			case 0xc9: {
				ret(true);
				return 8;
			}
			
		//cb
			case 0xcb:{
				int followIns = fetchByte();
				//System.out.println("follow cb: " + Integer.toHexString(followIns));
				switch(followIns) {
					case 0x07:{
						regs.setA(regs.rotateByteLeft(regs.getA()));
						return 8;
					}
					case 0x11:{//rl c
						regs.setC(regs.rotateByteLeftCarry(regs.getC()));
						return 8;
					}
					case 0x7c:{
						regs.checkByteBit(regs.getH(), 7);
						return 8;
					}
					default:{
						//System.out.println(toWord(regs.getSP()));
						System.out.println("AF: " + toWord(regs.getAF()));
						System.out.println("BC: " + toWord(regs.getBC()));
						System.out.println("DE: " + toWord(regs.getDE()));
						System.out.println("HL: " + toWord(regs.getHL()));
						System.out.println("Unrecognized cb instruction: " + Integer.toHexString(followIns));
						System.out.println("PC: 0x" + Integer.toHexString((regs.getPC()-1)));
						System.exit(1);
					}
				}
			}
			
		//Disable interrupt 
			case 0xF3: {
				//TODO
				return 4;
			}
			
			default:
				//System.out.println("carry : " + regs.getCarry() + " " + Integer.toBinaryString(regs.rotateByteRightCarry(0b10110111)) + " carry : " + regs.getCarry());
				//System.out.println(toWord(regs.getSP()));
				System.out.println("AF: " + toWord(regs.getAF()));
				System.out.println("BC: " + toWord(regs.getBC()));
				System.out.println("DE: " + toWord(regs.getDE()));
				System.out.println("HL: " + toWord(regs.getHL()));
				System.out.println("Unrecognized instruction: " + Integer.toHexString(opCode));
				System.out.println("PC: 0x" + Integer.toHexString((regs.getPC()-1)));
				
				System.out.println("ff40 :" + Integer.toHexString(mmu.read(0xff40)));
				System.exit(1);
				/*
				for(int i = 0x9800; i < 0x9bff; i++)
					System.out.println(Integer.toHexString(mmu.read(i)) + "   " + Integer.toHexString(i));
				System.out.println(Integer.toHexString(mmu.read(0x8014)) + "   " + Integer.toHexString(0x8014));
				System.out.println(Integer.toBinaryString(mmu.read(0xff40)));
				*/

		}
		
		return 0;
	}
	
	public String toWord(int val){ //only for printing
		String temp = Integer.toHexString(val);
		String result = "";
		for(int i = 0; i < 4-temp.length(); i++)
			result += '0';
		result += temp;
		return result;
	}
	
}
