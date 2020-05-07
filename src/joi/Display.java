package joi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Display extends JPanel{
	
	MMU mmu;
	
	JFrame frame = null;
	
	BufferedImage canvas;
	Graphics g;
	int width = 170;
	int height = 150;
	boolean painting = false;
	
	Color[] colors = new Color[4];{
		colors[0]=Color.WHITE;
		colors[1]=Color.LIGHT_GRAY;
		colors[2]=Color.DARK_GRAY;
		colors[3]=Color.BLACK;
	}
	
    public Display(int width, int height, MMU m) {
    	canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		g = canvas.getGraphics();
		mmu = m;
    }
    
    public void clear(){
		g.setColor(Color.black);
		g.fillRect(0, 0, width, height);
	}
	
	public void setPixel(int x, int y, int color){
		g.setColor(colors[color]);
		g.fillRect(x, y, 1, 1);
	}
	
	public void refresh(){
		if(!painting){
			painting = true;
			repaint();
		}
	}
	
	public void render(byte[][] pattern, boolean draw){
		if(draw & !painting) {
			clear();
			for(int i = 0; i < pattern.length; i++) {
				for(int j = 0; j < pattern[0].length; j++) {
					setPixel(i,j, pattern[i][j]);
				}
			}
			g.setColor(Color.BLACK);
			refresh();
		}
	}
	
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(canvas, 0, 0, frame.getContentPane().getWidth(), frame.getContentPane().getHeight(), 0, 0, width, height, null);
		painting = false;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(canvas, null, null);
    }


    public void fillCanvas(Color c) {
        int color = c.getRGB();
        for (int x = 0; x < canvas.getWidth(); x++) {
            for (int y = 0; y < canvas.getHeight(); y++) {
                canvas.setRGB(x, y, color);
            }
        }
        repaint();
    }

    public void drawRect(Color c, int x1, int y1, int width, int height) {
        int color = c.getRGB();
        // Implement rectangle drawing
        for (int x = x1; x < x1 + width; x++) {
            for (int y = y1; y < y1 + height; y++) {
                canvas.setRGB(x, y, color);
            }
        }
        repaint();
    }
    
    
    public void showAsFrame(){
		if(frame == null){
			frame = new JFrame("NES Test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(this);
			frame.setResizable(false);;
			frame.pack();
			frame.setVisible(true);
		}
    }



}
