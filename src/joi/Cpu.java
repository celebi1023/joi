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
		System.out.println(Integer.toBinaryString(mmu.read(0xff40)));
		if((mmu.read(0xff40) << 6 % 2) == 1)
			System.exit(1);
		//for testing
		//breakpoint
		if(regs.getPC() - 1 == 0x343) {
			System.out.println("pc: " + Integer.toHexString(regs.getPC() - 1));
			System.out.println(Integer.toHexString(opCode));
			//System.out.println(Integer.toHexString(fetchWord()));
			//System.out.println(Integer.toHexString(fetchByte()));
			//System.out.println(Integer.toHexString(fetchByte()));
			System.out.println("AF: " + toWord(regs.getAF()));
			System.out.println("BC: " + toWord(regs.getBC()));
			System.out.println("DE: " + toWord(regs.getDE()));
			System.out.println("HL: " + toWord(regs.getHL()));
			System.out.println("SP: " + toWord(regs.getSP()));
			String flags = Integer.toBinaryString(regs.getF());
			while(flags.length() < 8)
				flags = '0' + flags;
			System.out.println("flags: " + flags.substring(0, 4));
			
			for(int i = 0x00; i < 0x30; i++) {
				System.out.println("pc: " + Integer.toHexString(i) + " ins: " + Integer.toHexString(mmu.read(i)));
			}
			
			String ff40 = Integer.toBinaryString(mmu.read(0xff40));
			while(ff40.length() < 8)
				ff40 = '0' + flags;
			System.out.println("ff40: " + ff40 + " " + Integer.toHexString(mmu.read(0xff40)));
			//while(true) {;}
			System.out.println(Integer.toHexString(mmu.read(0xcffd)));
			System.exit(0);
		}
		
		switch(opCode) {
		//nop
			case 0x00: return 4;
			
		//dec
			case 0x3d: {//dec a
				regs.setA(regs.subByte(regs.getA(), 1, true));
				return 4;
			}
			case 0x05: {//dec b
				regs.setB(regs.subByte(regs.getB(), 1, true)); 
				return 4;
			}
			case 0x0d: {//dec c
				regs.setC(regs.subByte(regs.getC(), 1, true));
				return 4;
			}
			case 0x15: {//dec d
				regs.setD(regs.subByte(regs.getD(), 1, true));
				return 4;
			}
			case 0x1d: {//dec e
				regs.setE(regs.subByte(regs.getE(), 1, true));
				return 4;
			}
			case 0x25: {//dec h
				regs.setH(regs.subByte(regs.getH(), 1, true));
				return 4;
			}
			case 0x2d: {//dec l
				regs.setL(regs.subByte(regs.getL(), 1, true));
				return 4;
			}
			case 0x0b: {//dec bc
				regs.setBC((regs.getBC() + 65536 - 1)%65536);
				return 8;
			}
			case 0x1b: {//dec be
				regs.setDE((regs.getDE() + 65536 - 1)%65536);
				return 8;
			}
			case 0x2b: {//dec hl
				regs.setHL((regs.getHL() + 65536 - 1)%65536);
				return 8;
			}
			case 0x3b: {//dec sp
				regs.setSP((regs.getSP() + 65536 - 1)%65536);
				return 8;
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
			
		//8-bit load to reg
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
			
		//16-bit load to reg
			case 0x01: {//ld bc, nn
				regs.setBC(fetchWord());
				return 12;
			}
			case 0x11: {//ld de, nn
				regs.setDE(fetchWord());
				return 12;
			}
			case 0x21: {//ld hl, nn
				regs.setHL(fetchWord());	
				return 12;
			}
			case 0x31: {//ld sp, nn
				regs.setSP(fetchWord());
				return 12;
			}
			
		//8-bit load to mem
			case 0x36: {//ld (hl), n
				mmu.write(regs.getHL(), fetchByte());
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
			case 0x70: {//ld (hl), b
				mmu.write(regs.getHL(), regs.getB());
				return 8;
			}
			case 0x71: {//ld (hl), c
				mmu.write(regs.getHL(), regs.getC());
				return 8;
			}
			case 0x72: {//ld (hl), d
				mmu.write(regs.getHL(), regs.getD());
				return 8;
			}
			case 0x73: {//ld (hl), e
				mmu.write(regs.getHL(), regs.getE());
				return 8;
			}
			case 0x74: {//ld (hl), h
				mmu.write(regs.getHL(), regs.getH());
				return 8;
			}
			case 0x75: {//ld (hl), l
				mmu.write(regs.getHL(), regs.getL());
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
			case 0x0a: {//ld a, (bc)
				regs.setA(mmu.read(regs.getBC()));
				return 8;
			}
			case 0x1a: {//ld a, (de)
				regs.setA(mmu.read(regs.getDE()));
				return 8;
			}
			case 0xfa: {//ld a, (nn)
				regs.setA(mmu.read(fetchWord()));
				return 16;
			}
			case 0x2a: {//ldi a, (hl)
				regs.setA(mmu.read(regs.getHL()));
				regs.setHL(regs.getHL() + 1);
				return 8;
			}
			case 0x7e: {//ld A, (HL)
				regs.setA(mmu.read(regs.getHL()));
				return 8;
			}
			case 0x46: {//ld B, (hl)
				regs.setB(mmu.read(regs.getHL()));
				return 8;
			}
			case 0x4e: {//ld C, (hl)
				regs.setC(mmu.read(regs.getHL()));
				return 8;
			}
			case 0x56: {//ld D, (hl)
				regs.setD(mmu.read(regs.getHL()));
				return 8;
			}
			case 0x5e: {//ld E, (hl)
				regs.setE(mmu.read(regs.getHL()));
				return 8;
			}
			case 0x66: {//ld H, (hl)
				regs.setH(mmu.read(regs.getHL()));
				return 8;
			}
			case 0x6e: {//ld L, (hl)
				regs.setL(mmu.read(regs.getHL()));
				return 8;
			}
			case 0xF0: {
				regs.setA(mmu.read(0xFF00 + fetchByte()));
				return 12;
			}
			
		//ADC
			case 0x8f: {//adc a
				regs.setA(regs.addByte(regs.getA(), regs.getA() + regs.getCarryInt()));
				return 4;
			}
			case 0x88: {//adc b
				regs.setA(regs.addByte(regs.getA(), regs.getB() + regs.getCarryInt()));
				return 4;
			}
			case 0x89: {//adc c
				regs.setA(regs.addByte(regs.getA(), regs.getC() + regs.getCarryInt()));
				return 4;
			}
			case 0x8a: {//adc d
				regs.setA(regs.addByte(regs.getA(), regs.getD() + regs.getCarryInt()));
				return 4;
			}
			case 0x8b: {//adc e
				regs.setA(regs.addByte(regs.getA(), regs.getE() + regs.getCarryInt()));
				return 4;
			}
			case 0x8c: {//adc h
				regs.setA(regs.addByte(regs.getA(), regs.getH() + regs.getCarryInt()));
				return 4;
			}
			case 0x8d: {//adc l
				regs.setA(regs.addByte(regs.getA(), regs.getL() + regs.getCarryInt()));
				return 4;
			}
			case 0x8e: {//adc (hl)
				regs.setA(regs.addByte(regs.getA(), mmu.read(regs.getHL()) + regs.getCarryInt()));
				return 8;
			}
			case 0xce: {//adc a
				regs.setA(regs.addByte(regs.getA(), fetchByte() + regs.getCarryInt()));
				return 8;
			}
			
		//compare
			case 0xbf: { //cp A, A
				regs.subByte(regs.getA(), regs.getA(), false);
				return 4;
			}
			case 0xb8: { //cp A, B
				regs.subByte(regs.getA(), regs.getB(), false);
				return 4;
			}
			case 0xb9: { //cp A, C
				regs.subByte(regs.getA(), regs.getC(), false);
				return 4;
			}
			case 0xba: { //cp A, D
				regs.subByte(regs.getA(), regs.getD(), false);
				return 4;
			}
			case 0xbb: { //cp A, E
				regs.subByte(regs.getA(), regs.getE(), false);
				return 4;
			}
			case 0xbc: { //cp A, H
				regs.subByte(regs.getA(), regs.getH(), false);
				return 4;
			}
			case 0xbd: { //cp A, L
				regs.subByte(regs.getA(), regs.getL(), false);
				return 4;
			}
			case 0xbe: { //cp A, (hl)
				regs.subByte(regs.getA(), mmu.read(regs.getHL()), false);
				return 8;
			}
			case 0xfe: { //cp A, #
				regs.subByte(regs.getA(), fetchByte(), false);
				return 8;
			}
		//sub
			case 0x97: { //sub a
				regs.setA(regs.subByte(regs.getA(), regs.getA(), false));
				return 4;
			}
			case 0x90: { //sub b
				regs.setA(regs.subByte(regs.getA(), regs.getB(), false));
				return 4;
			}
			case 0x91: { //sub c
				regs.setA(regs.subByte(regs.getA(), regs.getC(), false));
				return 4;
			}
			case 0x92: { //sub d
				regs.setA(regs.subByte(regs.getA(), regs.getD(), false));
				return 4;
			}
			case 0x93: { //sub e
				regs.setA(regs.subByte(regs.getA(), regs.getE(), false));
				return 4;
			}
			case 0x94: { //sub h
				regs.setA(regs.subByte(regs.getA(), regs.getH(), false));
				return 4;
			}
			case 0x95: { //sub l
				regs.setA(regs.subByte(regs.getA(), regs.getL(), false));
				return 4;
			}
			case 0x96: { //sub (hl)
				regs.setA(regs.subByte(regs.getA(), mmu.read(regs.getHL()), false));
				return 8;
			}
			case 0xd6: { //sub #
				regs.setA(regs.subByte(regs.getA(), fetchByte(), false));
				return 8;
			}
		//add byte
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
		//add word
			case 0x09: {//add hl, bc
				regs.setHL(regs.addWord(regs.getHL(), regs.getBC()));
				return 8;
			}
			case 0x19: {//add hl, de
				regs.setHL(regs.addWord(regs.getHL(), regs.getDE()));
				return 8;
			}
			case 0x29: {//add hl, hl
				regs.setHL(regs.addWord(regs.getHL(), regs.getHL()));
				return 8;
			}
			case 0x39: {//add hl, sp
				regs.setHL(regs.addWord(regs.getHL(), regs.getSP()));
				return 8;
			}
		//or
			case 0xb7: {//or A
				regs.setA(regs.or(regs.getA(), regs.getA()));
				return 4;
			}
			case 0xb0: {//or B
				regs.setA(regs.or(regs.getA(), regs.getB()));
				return 4;
			}
			case 0xb1: {//or C
				regs.setA(regs.or(regs.getA(), regs.getC()));
				return 4;
			}
			case 0xb2: {//or D
				regs.setA(regs.or(regs.getA(), regs.getD()));
				return 4;
			}
			case 0xb3: {//or E
				regs.setA(regs.or(regs.getA(), regs.getE()));
				return 4;
			}
			case 0xb4: {//or H
				regs.setA(regs.or(regs.getA(), regs.getH()));
				return 4;
			}
			case 0xb5: {//or L
				regs.setA(regs.or(regs.getA(), regs.getL()));
				return 4;
			}
			case 0xb6: {//or (hl)
				regs.setA(regs.or(regs.getA(), mmu.read(regs.getHL())));
				return 8;
			}
			case 0xf6: {//or #
				regs.setA(regs.or(regs.getA(), fetchByte()));
				return 8;
			}
		//xor
			case 0xaf: {//xor a
				regs.setA(regs.xor(regs.getA(), regs.getA()));
				return 4;
			}
			case 0xa8: {//xor b
				regs.setA(regs.xor(regs.getA(), regs.getB()));
				return 4;
			}
			case 0xa9: {//xor c
				regs.setA(regs.xor(regs.getA(), regs.getC()));
				return 4;
			}
			case 0xaa: {//xor d
				regs.setA(regs.xor(regs.getA(), regs.getD()));
				return 4;
			}
			case 0xab: {//xor e
				regs.setA(regs.xor(regs.getA(), regs.getE()));
				return 4;
			}
			case 0xac: {//xor h
				regs.setA(regs.xor(regs.getA(), regs.getH()));
				return 4;
			}
			case 0xad: {//xor l
				regs.setA(regs.xor(regs.getA(), regs.getL()));
				return 4;
			}
			case 0xae: {//xor (HL)
				regs.setA(regs.xor(regs.getA(), mmu.read(regs.getHL())));
				return 8;
			}
			case 0xee: {//xor #
				regs.setA(regs.xor(regs.getA(), fetchByte()));
				return 8;
			}
		//and 
			case 0xa7: {//and a
				regs.setA(regs.and(regs.getA(), regs.getA()));
				return 4;
			}
			case 0xa0: {//and b
				regs.setA(regs.and(regs.getA(), regs.getB()));
				return 4;
			}
			case 0xa1: {//and c
				regs.setA(regs.and(regs.getA(), regs.getC()));
				return 4;
			}
			case 0xa2: {//and d
				regs.setA(regs.and(regs.getA(), regs.getD()));
				return 4;
			}
			case 0xa3: {//and e
				regs.setA(regs.and(regs.getA(), regs.getE()));
				return 4;
			}
			case 0xa4: {//and h
				regs.setA(regs.and(regs.getA(), regs.getH()));
				return 4;
			}
			case 0xa5: {//and l
				regs.setA(regs.and(regs.getA(), regs.getL()));
				return 4;
			}
			case 0xa6: {//and hl
				regs.setA(regs.and(regs.getA(), mmu.read(regs.getHL())));
				return 8;
			}
			case 0xe6: {//and #
				regs.setA(regs.and(regs.getA(), fetchByte()));
				return 8;
			}
		//rotate
			case 0x17: {
				regs.setA(regs.rotateByteLeftCarry(regs.getA()));
				return 4;
			}
		//compliment
			case 0x2f: {//cpl
				regs.setA(regs.complByte(regs.getA()));
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
			
			case 0xc2: {//jp nz, nn
				jump(fetchWord(), !regs.getZero());
				return 12;
			}
			case 0xca: {//jp z, nn
				jump(fetchWord(), regs.getZero());
				return 12;
			}
			case 0xd2: {//jp nc, nn
				jump(fetchWord(), !regs.getCarry());
				return 12;
			}
			case 0xda: {//jp c, nn
				jump(fetchWord(), regs.getCarry());
				return 12;
			}
			
		//jst
			case 0xc7:{
				pushWord(regs.getPC());
				jump(0x00, true);
				return 32;
			}
			case 0xcf:{
				pushWord(regs.getPC());
				jump(0x08, true);
				return 32;
			}
			case 0xd7:{
				pushWord(regs.getPC());
				jump(0x10, true);
				return 32;
			}
			case 0xdf:{
				pushWord(regs.getPC());
				jump(0x18, true);
				return 32;
			}
			case 0xe7:{
				pushWord(regs.getPC());
				jump(0x20, true);
				return 32;
			}
			case 0xef:{
				pushWord(regs.getPC());
				jump(0x28, true);
				return 32;
			}
			case 0xf7:{
				pushWord(regs.getPC());
				jump(0x30, true);
				return 32;
			}
			case 0xff:{
				pushWord(regs.getPC());
				jump(0x38, true);
				return 32;
			}
			
		//push
			case 0xf5: { //push af
				pushWord(regs.getAF());
				return 16;
			}
			case 0xc5: {//push bc
				pushWord(regs.getBC());
				return 16;
			}
			case 0xd5: {//push de
				pushWord(regs.getDE());
				return 16;
			}
			case 0xe5: {//push hl
				pushWord(regs.getHL());
				return 16;
			}
			
		//pop
			case 0xf1: {//pop af
				regs.setAF(popWord());
				return 12;
			}
			case 0xc1: {//pop bc
				regs.setBC(popWord());
				return 12;
			}
			case 0xd1: {//pop de
				regs.setDE(popWord());
				return 12;
			}
			case 0xe1: {//pop hl
				regs.setHL(popWord());
				return 12;
			}
		//call
			case 0xcd: {
				call(fetchWord(), true);
				return 12;
			}
		//return
			case 0xc9: { //ret
				ret(true);
				return 8;
			}
			case 0xc0: { //ret nz
				ret(!regs.getZero());
				//System.out.println("pc: " + regs.getPC());
				return 8;
			}
			case 0xc8: { //ret z
				ret(regs.getZero());
				return 8;
			}
			case 0xd0: { //ret nc
				ret(!regs.getCarry());
				return 8;
			}
			case 0xd8: { //ret c
				ret(regs.getCarry());
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
				//swap
					case 0x37:{ //swap a
						regs.setA(regs.swapByte(regs.getA()));
						return 8;
					}
					case 0x30:{ //swap b
						regs.setB(regs.swapByte(regs.getB()));
						return 8;
					}
					case 0x31:{ //swap c
						regs.setC(regs.swapByte(regs.getC()));
						return 8;
					}
					case 0x32:{ //swap d
						regs.setD(regs.swapByte(regs.getD()));
						return 8;
					}
					case 0x33:{ //swap e
						regs.setE(regs.swapByte(regs.getE()));
						return 8;
					}
					case 0x34:{ //swap h
						regs.setH(regs.swapByte(regs.getH()));
						return 8;
					}
					case 0x35:{ //swap l
						regs.setL(regs.swapByte(regs.getL()));
						return 8;
					}
					case 0x36:{ //swap (hl)
						mmu.write(regs.getHL(), regs.swapByte(mmu.read(regs.getHL())));
						return 16;
					}
				//reset
					case 0x87:{ //res A
						regs.setA(regs.reset(regs.getA(), 0));
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
		//Enable interrupt
			case 0xfb: {
				//TODO
				return 4;
			}
		//Disable interrupt 
			case 0xf3: {
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
