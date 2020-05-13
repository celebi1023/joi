package joi;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Joypad implements KeyListener{
	MMU mmu;
	
	public Joypad(MMU m) {
		mmu = m;
	}
	
	public void keyPressed(KeyEvent e) {
		System.out.println("pressed~!");
		//System.exit(1);
		int key = e.getKeyCode();
		switch(key){
			case KeyEvent.VK_DOWN:{//down
				mmu.setKey(0, 1, 3);
				break;
			}
			case KeyEvent.VK_S:{//start
				mmu.setKey(0, 0, 3);
				break;
			}
			case KeyEvent.VK_UP:{//up
				mmu.setKey(0, 1, 2);
				break;
			}
			case KeyEvent.VK_A:{//select
				mmu.setKey(0, 0, 2);
				break;
			}
			case KeyEvent.VK_LEFT:{//left
				mmu.setKey(0, 1, 1);
				break;
			}
			case KeyEvent.VK_X:{//b
				mmu.setKey(0, 0, 1);
				break;
			}
			case KeyEvent.VK_RIGHT:{//right
				mmu.setKey(0, 1, 0);
				break;
			}
			case KeyEvent.VK_Z:{//a
				mmu.setKey(0, 0, 0);
				break;
			}
			default:
				return;
		}
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		//System.exit(1);
		int key = e.getKeyCode();
		switch(key){
			case KeyEvent.VK_DOWN:{//down
				mmu.setKey(1, 1, 3);
				break;
			}
			case KeyEvent.VK_S:{//start
				mmu.setKey(1, 0, 3);
				break;
			}
			case KeyEvent.VK_UP:{//up
				mmu.setKey(1, 1, 2);
				break;
			}
			case KeyEvent.VK_A:{//select
				mmu.setKey(1, 0, 2);
				break;
			}
			case KeyEvent.VK_LEFT:{//left
				mmu.setKey(1, 1, 1);
				//System.exit(1);
				break;
			}
			case KeyEvent.VK_X:{//b
				mmu.setKey(1, 0, 1);
				//System.exit(1);
				break;
			}
			case KeyEvent.VK_RIGHT:{//right
				mmu.setKey(1, 1, 0);
				break;
			}
			case KeyEvent.VK_Z:{//a
				mmu.setKey(1, 0, 0);
				break;
			}
			default:
				return;
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {
		//System.exit(1);
	}
}
