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
		pc = 0x0100;
		zero = true;
		sub = false;
		half = true;
		carry = true;
	}
	//set
	public void setA(int val)	{a = val;}
	public void setB(int val)	{b = val;}
	public void setC(int val)	{c = val;}
	public void setD(int val)	{d = val;}
	public void setE(int val)	{e = val;}
	public void setF(int val)	{f = val;}
	public void setH(int val)	{h = val;}
	public void setL(int val)	{l = val;}
	public void setSP(int val)	{sp = val;}
	public void setPC(int val)	{pc = val;}
	
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
	
	
}
