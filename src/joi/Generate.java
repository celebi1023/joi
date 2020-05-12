package joi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Generate {

	public static void main(String[] args) {
		try {
			Scanner sc = new Scanner(new File("instructions.txt"));
			FileWriter fr = new FileWriter(new File("result.txt"));
			int first = 0;
			int second = 0;
			while(sc.hasNext()) {
				String opcode = sc.next();
				if(opcode.equals("RLC") || opcode.equals("RRC") || opcode.equals("RL") || 
						opcode.equals("RR") || opcode.equals("SLA") || opcode.equals("SRA") || 
						opcode.equals("SWAP") || opcode.equals("SRL")) {
					String toMod = sc.next();
					sc.next();
					String cycles = sc.next();
					fr.write("case 0x" + Integer.toHexString(first) + Integer.toHexString(second));
					fr.write(":{ //" + opcode.toLowerCase() + " " + toMod.toLowerCase() + "\n");
					String set = toMod.equals("(HL)") ? "mmu.write(regs.getHL(), " : "regs.set" + toMod + "(";
					String val = "regs." + opcode.toLowerCase() + "(";
					val = toMod.equals("(HL)") ? val + "mmu.read(regs.getHL())))" : val + "regs.get" + toMod + "()))";
					fr.write("\t" + set + val + ";\n");
					fr.write("\treturn " + cycles + ";\n");
					fr.write("}\n");
				}
				else if(opcode.equals("BIT") || opcode.equals("RES") || opcode.equals("SET")) {
					String jumble = sc.next();
					char index = jumble.charAt(0);
					String toMod = jumble.substring(2);
					sc.next();
					String cycles = sc.next();
					fr.write("case 0x" + Integer.toHexString(first) + Integer.toHexString(second));
					fr.write(":{ //" + opcode.toLowerCase() + " " + index + ", " + toMod.toLowerCase() + "\n");
					String set = toMod.equals("(HL)") ? "mmu.write(regs.getHL(), " : "regs.set" + toMod + "(";
					set = opcode.equals("BIT") ? "" : set;
					String val = toMod.equals("(HL)") ? "mmu.read(regs.getHL())" + ", " + index + ")" : "regs.get" + toMod + "(), " + index + ")";
					val = opcode.equals("BIT") ? val : val + ')';
					fr.write("\t" + set + "regs." + opcode.toLowerCase() + "(" + val + ";\n");
					fr.write("\treturn " + cycles + ";\n");
					fr.write("}\n");
				}
				else {
					System.err.println("unrecognized instruction!!");
					System.exit(1);
				}
				for(int i = 0; i < 4; i++)
					sc.next();
				second++;
				if(second == 16) {
					first++;
					second = 0;
				}
				
			}
			sc.close();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
