import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.*;

public class CGOL extends JPanel implements ActionListener {
    private static final int CELL_SIZE = 24;
    private static final Color CELL_COLOR = Color.decode("#2020E0"); // #FF90FF rosa
    private static final Color BG_COLOR = Color.decode("#F0F0F0");
    private static final Color LINE_COLOR = Color.decode("#A0A0A0");
    private static final int UPDATES_PER_SECOND = 8;
    private static final int MIN_CELLS_X = 6;
    private static final int MIN_CELLS_Y = 6;
    private static final int MAX_CELLS_X = 78;
    private static final int MAX_CELLS_Y = 42;
    private static final char ON = 'X';
    private static final char OFF = '.';

    private char[][] empty_matrix;

    private boolean running;
    private int cx;
    private int cy;
    private int width;
    private int height;
    private JFrame frame;

    private char[][] currmatrix;
    private char[][] updatedmatrix;

    private Timer timer;

    private boolean drawGrid = true;
    private boolean displaytooltips = false;

    public CGOL(JFrame f) {
        int[] sizes = inputSize();
		if(sizes == null) {
			System.exit(0);
		}

        running = false;
        cx = sizes[0];
        cy = sizes[1];
        width = cx*CELL_SIZE;
        height = cy*CELL_SIZE;

        empty_matrix = new char[cy+2][cx+2];
        currmatrix = new char[cy+2][cx+2];
        updatedmatrix = new char[cy+2][cx+2];
        for(int i = 0; i < cy+2; i++) {
            for(int j = 0; j < cx+2; j++) {
                empty_matrix[i][j] = OFF;
                currmatrix[i][j] = OFF;
            }
        }

        frame = f;

        timer = new Timer((int)(1000.0/UPDATES_PER_SECOND), this);

        setPreferredSize(new Dimension(width, height));
        setBackground(BG_COLOR);
        setFocusable(true);
        addKeyListener(new KeySensor());
        addMouseListener(new MouseSensor());

        timer.start();
    }

    public int[] inputSize() {
        int[] res = new int[2];

		boolean cond = true;
		while(cond) {
			String in = JOptionPane.showInputDialog("Insert number of cells per row (width):");
			System.out.println(in);
			if(in == null) {
				return null;
			} else if(!isInteger(in)) {
				JOptionPane.showMessageDialog(this, "Please insert a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
			} else if(Integer.parseInt(in) < MIN_CELLS_X || Integer.parseInt(in) > MAX_CELLS_X) {
				JOptionPane.showMessageDialog(this, "The inserted value is not acceptable (Min "+MIN_CELLS_X+", Max "+MAX_CELLS_X+").", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				cond = false;
				res[0] = Integer.valueOf(in);
			}
		}

		cond = true;
		while(cond) {
			String in = JOptionPane.showInputDialog("Insert number of cells per column (height):");
			if(in == null) {
				return null;
			} else if(!isInteger(in)) {
				JOptionPane.showMessageDialog(this, "Please insert a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
			} else if(Integer.parseInt(in) < MIN_CELLS_Y || Integer.parseInt(in) > MAX_CELLS_Y) {
				JOptionPane.showMessageDialog(this, "The inserted value is not acceptable (Min "+MIN_CELLS_Y+", Max "+MAX_CELLS_Y+").", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				cond = false;
				res[1] = Integer.valueOf(in);
			}
		}

        return res;
    }

    public boolean isInteger(String s) {
        try{Integer.valueOf(s);} catch(Exception e) {return false;}
        return true;
    }

    public void updateEmpty() {
        empty_matrix = new char[cy+2][cx+2];
        for(int i = 0; i < cy+2; i++) {
            for(int j = 0; j < cx+2; j++) {
                empty_matrix[i][j] = OFF;
            }
        }
    }

    public void emptyUpdated() {
        updatedmatrix = new char[cy+2][];
        for(int i = 0; i < cy+2; i++) {
            updatedmatrix[i] = empty_matrix[i].clone();
        }
    }

    public void emptyCurrent() {
        currmatrix = new char[cy+2][];
        for(int i = 0; i < cy+2; i++) {
            currmatrix[i] = empty_matrix[i].clone();
        }
    }

    public void resize(int nx, int ny) {
        cx = nx;
        cy = ny;
        width = cx*CELL_SIZE;
        height = cy*CELL_SIZE;
        setPreferredSize(new Dimension(width, height));

        updateEmpty();
        emptyUpdated();
        emptyCurrent();

        frame.pack();
    }

    public void step() {
        emptyUpdated();

        int count;
        for(int i = 1; i < cy+1; i++) {
            for(int j = 1; j < cx+1; j++) {
                count = 0;
                for(int ky = -1; ky < 2; ky++) {
                    for(int kx = -1; kx < 2; kx++) {
                        char c = currmatrix[i+ky][j+kx];
                        if(c == ON && !(kx == 0 && ky == 0)) {
                            count++;
                        }
                    }
                }

                if(count == 3) {
                    updatedmatrix[i][j] = ON;
                } else if(count == 2) {
                    updatedmatrix[i][j] = currmatrix[i][j];
                } else {
                    updatedmatrix[i][j] = OFF;
                }
                
            }
        }

        // edges
        for(int j = 0; j < cx+2; j++) {
            updatedmatrix[0][j] = OFF;
            updatedmatrix[cy+1][j] = OFF;
        }
        for(int i = 0; i < cy+2; i++) {
            updatedmatrix[i][0] = OFF;
            updatedmatrix[i][cx+1] = OFF;
        }

        currmatrix = updatedmatrix;

        // printDebug();
    }

    public void loadSaved() throws IOException {
        String filename = JOptionPane.showInputDialog("Name of file to load:");
        File f = new File("./saved/"+filename+".cgol");
        if(!f.exists()) {
            JOptionPane.showMessageDialog(this, "The specified file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Scanner scan = new Scanner(f);
        int[] sizes = new int[2];
        String[] sizess = scan.nextLine().split(" ");
        sizes[0] = Integer.parseInt(sizess[0]);
        sizes[1] = Integer.parseInt(sizess[1]);

        resize(sizes[0], sizes[1]);

        char[] tmp;
        for(int i = 1; i < sizes[1]+1; i++) {
            tmp = scan.nextLine().toCharArray();
            for(int j = 1; j < sizes[0]+1; j++) {
                currmatrix[i][j] = tmp[j-1];
            }
        }

        scan.close();
    }
    
    public void saveCurrent() throws IOException {
        String filename = JOptionPane.showInputDialog("Insert name to save the file with:");
        if(filename == null) return;
        File f = new File("./saved/"+filename+".cgol");

        if(f.exists()) {
            int choice = JOptionPane.showOptionDialog(this, "A file with the same name is already present. Overwrite the existing file?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if(choice == JOptionPane.YES_OPTION) {
                f.delete();
            } else {
                return;
            }
        }

        File dir = new File("./saved");
        if(!dir.exists()) {
            dir.mkdir();
        }

        FileWriter w = null;
        f.createNewFile();
        w = new FileWriter(f);

        int[] corners = detectStructure();
        w.write((corners[2]-corners[0])+" "+(corners[3]-corners[1])+'\n');
        for(int i = corners[1]; i < corners[3]; i++) {
            for(int j = corners[0]; j < corners[2]; j++) {
                w.append(currmatrix[i][j]);
            }
            w.append('\n');
        }

        w.close();
    }

    public int[] detectStructure() {
        return new int[]{1,1,cx+2,cy+2};
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D gr = (Graphics2D)g;

        gr.setColor(BG_COLOR);
        gr.fillRect(0, 0, width, height);

        gr.setColor(CELL_COLOR);
        for(int i = 1; i < cy+1; i++) {
            for(int j = 1; j < cx+1; j++) {
                if(currmatrix[i][j] == ON) {
                    gr.fillRect((j-1)*CELL_SIZE, (i-1)*CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        if(drawGrid) {
            gr.setColor(LINE_COLOR);
            for(int j = 1; j < cx; j++) {
                gr.drawLine(j*CELL_SIZE, 0, j*CELL_SIZE, height);
            }
            for(int i = 1; i < cy; i++) {
                gr.drawLine(0, i*CELL_SIZE, width, i*CELL_SIZE);
            }
        }

        if(!running) {
            gr.setColor(Color.BLACK);
            int offset = (int)(1.3125*Math.min(cx, cy));
            int totsize = (int)(2.25*Math.min(cx, cy));
            int thickness = (int)(0.75*Math.min(cx, cy));
            gr.fillRect(width-offset-totsize, offset, thickness, totsize);
            gr.fillRect(width-offset-thickness, offset, thickness, totsize);
        }

        if(displaytooltips) {
            g.setFont(new Font(getFont().getName(), Font.PLAIN, 15));
            String[] tips = {
                "Q: Show/Hide these tooltips",
                "G: Show/Hide grid",
                "P: Pauses/Resumes the simulation",
                "C: Clears the simulation",
                "S: Saves the current state",
                "L: Loads a previously saved state",
                "R: Resizes the window",
                "T: Steps the simulation once"
            };

            for(int i = 0; i < tips.length; i++) {
                g.drawString(tips[i], 2, 12+i*16);
            }
        } else {
            g.drawString("Press Q for tooltips", 2, 10);
        }
    }

    public void printDebug() {
        for(int i = 0; i < cy+2; i++) {
            for(int j = 0; j < cx+2; j++) {
                System.out.print(currmatrix[i][j]);
            }
            System.out.println();
        }
        System.out.println('\n');
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if(running) {
            try {
                step();
            } catch(Exception e) {
                System.out.println("variables: "+cx+" "+cy+" ; currmatrix: "+currmatrix[0].length+" "+currmatrix.length+" ; empty_matrix: "+empty_matrix[0].length+" "+empty_matrix.length+" ; updatedmatrix: "+updatedmatrix[0].length+" "+updatedmatrix.length);
                throw e;
            }
        }
        repaint();
    }
    
    public class KeySensor extends KeyAdapter {
        boolean bfr;
        @Override
        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_Q:
                    displaytooltips = !displaytooltips;
                    break;

                case KeyEvent.VK_G:
                    drawGrid = !drawGrid;
                    repaint(); 
                    break;

                case KeyEvent.VK_P:
                    running = !running;
                    break;

                case KeyEvent.VK_C:
                    emptyCurrent();
                    break;

                case KeyEvent.VK_S:
                    bfr = running;
                    running = false;

                    try {
                        saveCurrent();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    running = bfr;
                    break;

                case KeyEvent.VK_L:
                    bfr = running;
                    running = false;

                    try {
                        loadSaved();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    running = bfr;
                    break;

                case KeyEvent.VK_R:
                    int[] sizes = inputSize();
                    if(sizes == null) return;
                    resize(sizes[0], sizes[1]);
                    break;

                case KeyEvent.VK_T:
                    step();
                    repaint();
                    break;
            }
        }
    }

    public class MouseSensor implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            int cellx = e.getX()/CELL_SIZE;
            int celly = e.getY()/CELL_SIZE;

            currmatrix[celly+1][cellx+1] = currmatrix[celly+1][cellx+1] == ON ? OFF : ON;
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
        
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Game of Life");
        f.add(new CGOL(f));
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }
}