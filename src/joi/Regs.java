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
		return (x % 256);
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
	public void setCarry(boolean b) {carry = b;}
	
	public void setAF(int val) {
		a = val/256;
		f = val % 256;
	}
	
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
	public int getCarryInt() {return carry? 1 : 0;}
	
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
		else f = f & 0b10111111;
		if(half) f = f | 0b00100000;
		else f = f & 0b11011111;
		if(carry) f = f | 0b00010000;
		else f = f & 0b11101111;
	}
	
	public int subByte(int a, int b, boolean dec) {
		zero = (a % 256) == (b % 256);
		sub = true;
		half = (a % 16) < (b % 16);
		if(!dec) carry = a < b;
		updateF();
		return (a - b + 256) % 256;
	}
	
	public int sbc(int a, int b_) {
		int b = (b_ + getCarryInt() + 256) % 256;
		zero = (a - b) % 256 == 0;
		sub = true;
		half = (a % 16) > (b % 16);
		carry = a > b;
		updateF();
		return (a - b + 256) % 256;
	}
	
	public int addByte(int a, int b) {
		zero = (a + b) % 256 == 0;
		sub = false;
		half = (a % 16) + (b % 16) > 16;
		carry = ((a + 256) % 256) + ((b + 256) % 256) > 255;
		updateF();
		return (a + b) % 256;
	}
	
	public int addWord(int a_, int b_) { 
		int a = (a_ + 65536) % 65536;
		int b = (b_ + 65536) % 65536;
		sub = false;
		half = (a % 4096) + (b % 4096) > 4096;
		carry = a + b > 65536;
		updateF();
		return (a + b) % 65536;
	}
	
//rotates
	public int rr(int val) {
		val = (val + 256) % 256;
		int result = carry ? ((val >> 1) + (1 << 7)) : (val >> 1);
		sub = false;
		carry = false;
		carry = (val % 2) == 0;
		updateF();
		return result % 256;
	}
	
	public int rrc(int val) {
		val = (val + 256) % 256;
		int result = ((val) >> 1) + (val % 2)*(1 << 7);
		zero = result == 0;
		sub = false;
		half = false;
		carry = (val % 2) == 1;
		return result;
	}
	
	public int rl(int val) {
		val = (val + 256) % 256;
		int result = carry ? (val << 1) + 1 : val << 1;
		result = result % 256;
		zero = result == 0; //different docs say different things
		sub = false;
		half = false;
		carry = val > 127;
		updateF();
		return result;
	}
	
	public int rlc(int val) {
		val = (val + 256) % 256; 
		int result = ((val << 1) + ((val >> 7) % 2)) % 256;
		zero = result == 0; //different docs say different things
		sub = false;
		half = false;
		carry = ((val >> 7) % 2) == 1;
		updateF();
		return result;
	}
	
//shifts
	public int sla(int val) {
		val = (val + 256) % 256;
		int result = (val << 1) % 256;
		zero = result == 0;
		sub = false;
		half = false;
		carry = val > 127;
		return result;
	}
	
	public int sra(int val) {
		val = (val + 256) % 256;
		int result = (val >> 1) + ((val >> 7) & 1)*(1 << 7);
		zero = result == 0;
		sub = false;
		half = false;
		carry = (val % 2) == 1;
		return result;
	}
	
	public int srl(int val) {
		val = (val + 256) % 256;
		int result = (val >> 1);
		zero = result == 0;
		sub = false;
		half = false;
		carry = (val % 2) == 1;
		return result;
	}
	
	public void bit(int val, int bitIndex) {
		val = (val >> bitIndex) % 2;
		zero = val == 0;
		sub = false;
		half = true;
		updateF();
	}
	
	public int or(int a, int b) {
		zero = (a | b) == 0;
		sub = false;
		half = false;
		carry = false;
		updateF();
		return (a | b);
	}
	
	public int xor(int a, int b) {
		zero = (a ^ b) % 256 == 0;
		sub = false;
		half = false;
		carry = false;
		updateF();
		return (a ^ b);
	}
	
	public int and(int a, int b) {
		int val = ((a + 256)%256) & ((b + 256)%256);
		zero = (val == 0);
		sub = false;
		half = true;
		carry = false;
		updateF();
		return val;
	}
	
	public int complByte(int val) {
		sub = true;
		half = true;
		updateF();
		return 255 - ((val + 256)%256);
	}
	
	public int swap(int val) {
		int top = ((val + 256)%256)/16;
		int bot = ((val + 256)%256)%16;
		int result = bot * 16 + top;
		if(result == 0)
			zero = true;
		sub = false;
		half = false;
		carry = false;
		updateF();
		return result;
	}
	
	public int res(int val, int bit) {//only for bytes
		int toAnd = 0b11111111;
		int toSub = 1 << bit;
		toAnd -= toSub;
		updateF();
		return ((val + 256)%256) & toAnd;
	}
	
	public int set(int val, int bit) {
		int toOr = 1 << bit;
		val = (val + 256) % 256;
		return val | toOr;
	}
	
	public int daa(int val) {
		int result = val;
		if(!sub) {
			if(carry || result > 0x99) {
				result += 0x60;
				carry = true;
			}
			if(half || (result & 0x0f) > 0x09) {
				result += 0x06;
			}
		}
		else {
			if(carry) {
				result -= 0x60;
				carry = true;
			}
			if(half) {
				result -= 0x6;
			}
		}
		result = (result + 256) % 256;
		carry = result == 0;
		half = false;
		return result;
	}
}
