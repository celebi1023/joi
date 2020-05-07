package joi;

public class Regs {
	private int a;
	private int b;
	private int c;
	private int d;
	private int e;
	private int f;
	private int h;
	private int l;
	private int sp;
	private int pc;
	private boolean zero;
	private boolean sub;
	private boolean half;
	private boolean carry;
	
	public Regs() {
		a = 0x01;
		b = 0x00;
		c = 0x13;
		d = 0x00;
		e = 0xD8;
		f = 0xB0;
		h = 0x01;
		l = 0x4D;
		sp = 0xFFFE;
		pc = 0x0000;
		zero = true;
		sub = false;
		half = true;
		carry = true;
	}
	//set
	private int val(int x) {
		if(x < 0) return 256 + x;
		return x;
	}
	public void setA(int val)	{a = val(val);}
	public void setB(int val)	{b = val(val);}
	public void setC(int val)	{c = val(val);}
	public void setD(int val)	{d = val(val);}
	public void setE(int val)	{e = val(val);}
	public void setF(int val)	{f = val(val);}
	public void setH(int val)	{h = val(val);}
	public void setL(int val)	{l = val(val);}
	public void setSP(int val)	{sp = val;}
	public void setPC(int val)	{pc = val;}
	public void setCarry(boolean b) {carry = b;} //just for testing
	
	public void setBC(int val) {
		b = val/256;
		c = val % 256;
	}
	
	public void setDE(int val) {
		d = val/256;
		e = val % 256;
	}
	
	public void setHL(int val) {
		h = val/256;
		l = val % 256;
	}
	
	//get
	public int getA() {return a;}
	public int getB() {return b;}
	public int getC() {return c;}
	public int getD() {return d;}
	public int getE() {return e;}
	public int getF() {return f;}
	public int getH() {return h;}
	public int getL() {return l;}
	public int getSP() {return sp;}
	public int getPC() {return pc;}
	public boolean getZero() {return zero;}
	public boolean getSub() {return sub;}
	public boolean getHalf() {return half;}
	public boolean getCarry() {return carry;}
	
	public int getAF() {return a * 256 + f;}
	public int getBC() {return b * 256 + c;}
	public int getDE() {return d * 256 + e;}
	public int getHL() {return h * 256 + l;}
	
	public String getZSHC()	{
		String output = "";
		output += zero ? 1 : 0;
		output += sub ? 1 : 0;
		output += half ? 1 : 0;
		output += carry ? 1 : 0;
		return output;
	}
	
	//ALU in some sense
	private void updateF() {
		if(zero) f = f | 0b10000000;
		else f = f & 0b01111111;
		if(sub) f = f | 0b01000000;
		else f = f & 0b10000000;
		if(half) f = f | 0b00100000;
		else f = f & 0b11011111;
		if(carry) f = f | 0b00010000;
		else f = f & 0b11101111;
	}
	
	public int setFlags(int val) {
		zero = val % 256 == 0;
		sub = false;
		half = false;
		carry = false;
		updateF();
		return val;
	}
	
	public int subByte(int a, int b) {
		zero = (a % 256) == (b % 256);
		sub = true;
		half = (a % 16) < (b % 16);
		carry = a < b;
		updateF();
		return (a - b + 256) % 256;
	}
	
	/*
	public int subWord(int a, int b) {
		zero = a == b;
		sub = true;
		half = (a % 256) < (b % 256);
		carry = a < b;
		updateF();
		return (a - b + 256*256) % 256*256; 
	}
	*/
	
	public int addByte(int a, int b) {
		zero = a + b % 256 == 0;
		sub = false;
		half = (a % 16) + (b % 16) > 16; //to be checked
		carry = (a + 256 % 256) + (b + 256 % 256) > 255; //iffy, to be checked maybe
		return a + b % 256;
	}
	
	public int rotateByteLeft(int val) {
		int lastbit = (val % 2 == 0) ? 0b11111110 : 0b11111111;
		int result = (val << 1) & lastbit;
		half = false;
		sub = false;
		carry = val > 127; //old bit 7 was 1
		System.out.println("result: " + result);
		return result % 256;
	}
	
	public int rotateByteLeftCarry(int val) {
		zero = !carry && val > 2;
		sub = false;
		half = false;
		int result = carry ? (val << 1) + 1 : val << 1;
		carry = val > 127;
		return result % 256;
	}
	
	public int rotateByteRightCarry(int val) {
		zero = !carry && val/2 == 0;
		sub = false;
		half = false;
		int result = carry ? (val >> 1) + 128 : val >> 1;
		carry = (val % 2) == 1;
		return result % 256;
	}
	
	public void checkByteBit(int val, int bitIndex) {
		val = (val >> bitIndex) % 2;
		zero = val == 0;
		sub = false;
		half = true;
		//no carry then?
	}
}
