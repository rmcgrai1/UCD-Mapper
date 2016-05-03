package cont;
import io.IO;
import io.Mouse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.NumberFormatter;

import learning.AlgorithmTester;
import learning.Node;

import com.jogamp.opengl.awt.GLCanvas;

import fl.FileExt;
import gfx.GOGL;
import object.primitive.Updatable;
import time.Delta;


public class Controller extends JFrame implements WindowListener, ActionListener, MouseListener {	

	private static Controller instance;
	private boolean isRunning = true;
	int lastFpsTime;
	int fps;
	
	String chartTxt = "";
	int chartH = 0,
		chartFontSize = 9;
	
	PrintWriter outFile;
	String outString = "";
	
	private final byte
		N_NONE = -1,
		N_START = 0,
		N_END = 1;
	private byte selectingNode = N_NONE;
	
	private boolean isRandom = false;
	
	// Stuff
	JProgressBar progressBar;
	
		// Node
		JTextField startNodeField,
			endNodeField;
		JButton testButton, randomButton;
		
		// Options
		JFormattedTextField testNumField,
			trialNumField,
			randomnessField,
			predictionField;
		JCheckBox randomBox,
			qBox,
			sarsaBox,
			dijkstraBox,
			aStarBox;
		JLabel chart;
		
		JPanel pChart;
		JScrollPane pScrollChart;
	
    
	public static void main(String args[]) {
		getInstance().start();
	}
	
	public static Controller getInstance() {
		if(instance == null)
			instance = new Controller();
		return instance;
	}
        
	public static void println(String txt) {
		instance.outFile.println(txt);
		instance.chartTxt += txt+"<br>";

		instance.chartH += instance.chartFontSize+2;
		
		instance.chart.setText("<html>" + instance.chartTxt + "</html>");
		instance.chart.setSize(300,instance.chartH);

		instance.pChart.setPreferredSize(new Dimension(300,instance.chartH));
	}
	public static void print(String txt) {
		instance.outString += txt;
	}
	public static void endln() {
		println(instance.outString);
		instance.outString = "";
	}
	
    public Controller()  { 
    	
    	try {
			outFile = new PrintWriter("output.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	addWindowListener(this);
    	
        // We'll ask the width and height by this 

    	//setSize(GOGL.SCREEN_WIDTH, GOGL.SCREEN_HEIGHT);
    	
    	int w, h, m, bH, pW, pH, subPH, padding = 15, side, top;
    	side = GOGL.getSideBorderSize();
    	top = GOGL.getTopBorderSize();
    		
    	m = 5;
    	bH = 20;
    	pW = 300;
    	pH = GOGL.getScreenHeight();
    	subPH = (pH-bH-3*m)/3;
    	w = 3*padding + pW + GOGL.getScreenWidth()+2*GOGL.getSideBorderSize();
    	h = 2*padding + GOGL.getScreenHeight()+side+top;
    	this.setBackground(new Color(255,255,255));
    	
    	JPanel 
    		p = new JPanel(),
    		pNodes = new JPanel(),
    		pOptions = new JPanel();
    	
    	pChart = new JPanel();
    	

    	setSize(w,h);
    	setResizable(false);
    	    	    	

    	GOGL.start3D(this);
    	
        GLCanvas canv = GOGL.getCanvas();
        canv.setSize(GOGL.getScreenWidth(),GOGL.getScreenHeight());
        p.add(canv);
        p.setBounds(2*padding+pW, padding, GOGL.getScreenWidth(),GOGL.getScreenHeight());
        p.setLayout(null);
        
    	p.addMouseListener(this);
    	canv.addMouseListener(this);
        
        setLayout(null);
        
        Border loweredBevel = BorderFactory.createLoweredBevelBorder();

        // Create Node Panel
        pNodes.setBounds(padding,padding,pW,subPH);
        pNodes.setBackground(null);
        pNodes.setBorder(loweredBevel);
        pNodes.setLayout(null);
        
        
		int padF = (int) (padding*.5),
			inW = (pW-2*padding)/2-padF;
        int lineH =15+m;
		int dX = padding, dY = padding;

	        startNodeField = addTextField(pNodes,"",dX,dY, pW-2*padding, 20);
	        	dY += lineH;
		    	
	    	addLabel(pNodes, "to", pW/2-8,dY);
		    	dY += lineH;
		    	
    		endNodeField = addTextField(pNodes,"",dX,dY, pW-2*padding, 20);		    	
		    	dY += lineH;
		    	
		    testButton = addButton(pNodes, "Test", pW*2/5+m,subPH-30-padding, pW - (padding+pW*2/5+m), 30);
	    	randomButton = addButton(pNodes, "Random", padding,subPH-30-padding, pW*2/5-2*padding, 30);
		    	
        add(pNodes);
        
        
        // Create Option Panel
        pOptions.setBounds(padding,padding+subPH+m,pW,subPH);
		pOptions.setBackground(null);
	    pOptions.setBorder(loweredBevel);
	    pOptions.setLayout(null);
		
	    	// Create Options
		    NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
			    formatter.setValueClass(Integer.class);
			    formatter.setMinimum(1);
			    formatter.setMaximum(10000);
		    NumberFormatter formatterWeight = new NumberFormatter(NumberFormat.getInstance());
			    formatterWeight.setValueClass(Double.class);
			    formatterWeight.setMinimum(0.);
			    formatterWeight.setMaximum(1.);
			    			
		dX = padding;
		dY = padding;
			addLabel(pOptions, "Test #: ", dX,dY);			    
		    testNumField = new JFormattedTextField(formatter);
		    	testNumField.setBounds(padding+pW/4,padding, pW/4-(int)(1.5f*padding), 20);
		    	testNumField.setText("100");
		    	testNumField.setToolTipText("Number of distinct tests to perform.");
		    	pOptions.add(testNumField);
		    	
		    	dY += lineH;
		    	
			addLabel(pOptions, "Max Trial #:", dX,dY);
	    	trialNumField = new JFormattedTextField(formatter);
		    	trialNumField.setBounds(padding+pW/4,padding + 20+m, pW/4-(int)(1.5f*padding), 20);
		    	trialNumField.setText("1000");
		    	trialNumField.setToolTipText("Maximum number of trials to perform each test.");
		    	pOptions.add(trialNumField);
		    	
		    	dY += lineH;
		    	
	    	randomBox = addCheckBox(pOptions, "Randomize Tests", dX,dY);
	    		randomBox.setSelected(true);
	    		dY += lineH;
	    			    		
    		addLabel(pOptions, "Pred. Weight:", dX,dY);
	    	predictionField = new JFormattedTextField(formatterWeight);
		    	predictionField.setBounds(padding+pW/4,dY, pW/4-(int)(1.5f*padding), 20);
		    	predictionField.setText(".95");
		    	predictionField.setToolTipText("Fractional weight of future predictions.");
		    	pOptions.add(predictionField);
		    	dY += lineH;

    		addLabel(pOptions, "Rand. Weight:", dX,dY);
	    	randomnessField = new JFormattedTextField(formatterWeight);
		    	randomnessField.setBounds(padding+pW/4,dY, pW/4-(int)(1.5f*padding), 20);
		    	randomnessField.setText(".4");
		    	randomnessField.setToolTipText("Fractional weight of randomness in exploration.");
		    	pOptions.add(randomnessField);
		    	dY += lineH;

	    		
	    dY = padding;
	    	qBox = addCheckBox(pOptions, "Q Learning", pW/2,dY);
	    		qBox.setSelected(true);
	    		dY += lineH;
	    	sarsaBox = addCheckBox(pOptions, "SARSA", pW/2,dY);
	    		sarsaBox.setSelected(true);
	    		dY += lineH;
	    	dijkstraBox = addCheckBox(pOptions, "Dijkstra", pW/2,dY);
	    		dijkstraBox.setSelected(true);
	    		dY += lineH;
	    	aStarBox = addCheckBox(pOptions, "A*", pW/2,dY);
	    		aStarBox.setSelected(true);
	    		dY += lineH;
	
    	add(pOptions);
        
        
        // Create Chart Panel
    	pChart.setLocation(0,0);
        pChart.setBackground(null);
        pChart.setBorder(loweredBevel);
        pChart.setLayout(null);
                
        	chart = addLabel(pChart, "", 0,0);
        	
        	Font f = chart.getFont();
        		f = new Font("Courier New", Font.PLAIN, chartFontSize);
        	chart.setFont(f);
        	
        	JScrollPane pScrollChart = new JScrollPane(pChart);
        	

        	pScrollChart.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        	pScrollChart.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        	pScrollChart.setBounds(padding,padding+2*(subPH+m),pW,subPH);
        
        add(pScrollChart);
        
        // Create Progress Bar
        progressBar = new JProgressBar(0,100);
	        progressBar.setBounds(padding,padding + pH-bH,pW,bH);
	        this.add(progressBar);
        
    	this.setVisible(true);

        
        add(p);
        
        IO.ini();
    }
        			
	private JLabel addLabel(JPanel panel, String text, int x, int y) {
		JLabel label = new JLabel(text);
		label.setBounds(x,y,text.length()*8, 20);
		panel.add(label);
		
		return label;
	}
	
	private JCheckBox addCheckBox(JPanel panel, String text, int x, int y) {
		JCheckBox checkBox = new JCheckBox();
			checkBox.setBounds(x,y, 20,20);
	    	panel.add(checkBox);
		
	    addLabel(panel, text, x+20,y);
	    
		return checkBox;
	}
	
	private JTextField addTextField(JPanel panel, String text, int x, int y, int w, int h) {
		JTextField field = new JTextField(text);
    		field.setBounds(x,y, w,h);
    		field.setLayout(null);
    		field.addMouseListener(this);
    		field.setEnabled(false);
    		panel.add(field);
    		
    	return field;
	}
	
	private JButton addButton(JPanel panel, String text, int x, int y, int w, int h) {
		JButton button = new JButton(text);
			button.setBounds(x,y, w,h);
	    	button.setLayout(null);
	    	button.addActionListener(this);
	    	panel.add(button);
	    	
	    return button;
	}

	public void start() {		
		Thread loop = new Thread() {
			public void run() {
				gameLoop();
	        }
	    };
	    loop.start();
    }
    
	public static void updateInstance() {
		instance.update();
	}
	
	public void update() {
		AlgorithmTester at = GOGL.getAT();
		
		isRandom = randomBox.isSelected();
		at.enableQLearning(qBox.isSelected());
		at.enableSARSA(sarsaBox.isSelected());
		at.enableDijkstra(dijkstraBox.isSelected());
		at.enableAStar(aStarBox.isSelected());
		if(isRandom) {
			fillNodeFields();
			
			startNodeField.setDisabledTextColor(Color.GRAY);
			endNodeField.setDisabledTextColor(Color.GRAY);
		}
		else {
			if(selectingNode != N_NONE) {
				Node nearest = GOGL.getNodeSystem().getNearestNode(Mouse.getX(),Mouse.getY());
				
				at.setHoverNode(nearest);
				
				if(selectingNode == N_START) {
					startNodeField.setText(nearest.getName());
					at.setStart(nearest);
				}
				else if(selectingNode == N_END) {
					
					endNodeField.setText(nearest.getName());
					at.setEnd(nearest);
				}
			}
			else
				at.setHoverNode(null);
			
			startNodeField.setDisabledTextColor(Color.BLACK);
			endNodeField.setDisabledTextColor(Color.BLACK);
		}
		at.setRandom(isRandom);
				
		if(isRandom) {
			Color light = new Color(240,240,240);
			startNodeField.setBackground(light);
			endNodeField.setBackground(light);
		}
		else {
			startNodeField.setBackground((selectingNode == N_START) ? Color.GREEN : Color.WHITE);
			endNodeField.setBackground((selectingNode == N_END) ? Color.GREEN : Color.WHITE);
		}
		
		progressBar.setValue((int) (at.getProgressFraction()*100));
	}
    
    //ANIMATING SCRIPTS
	public void gameLoop() {
		Delta.setTargetFPS(120); //60
	    long now,
			lastLoopTime = System.nanoTime(),
			runTime;
	    double delta;
	       
	    // keep looping round til the game ends
	    while(isRunning) {
	        // work out how long its been since the last update, this
	        // will be used to calculate how far the entities should
	        // move this loop
	    	now = System.nanoTime();
	        runTime = now - lastLoopTime;
	        lastLoopTime = now;
	        delta = 1. * runTime / ((long)(1000000000 / Delta.getTargetFPS()));
	          
	        // update the frame counter
	        lastFpsTime += runTime;
	        fps++;
	          
	        // update our FPS counter if a second has passed since
	        // we last recorded
	        if (lastFpsTime >= 1000000000) {
	            lastFpsTime = 0;
	            fps = 0;
	        }
	          
	        Delta.setDelta((float) delta);
	          
	        // draw everyting
	        GOGL.repaint();
	          
	        // we want each frame to take 10 milliseconds, to do this
	        // we've recorded when we started the frame. We add 10 milliseconds
	        // to this and then factor in the current time to give 
	        // us our final value to wait for
	        // remember this is in ms, whereas our lastLoopTime etc. vars are in ns.
	        try {
	        	long sleepTime;
	        	sleepTime = (lastLoopTime-System.nanoTime() + (long)(1000000000 / Delta.getTargetFPS()))/1000000;
	        	  
	        	if(sleepTime >= 0)
	        		Thread.sleep(sleepTime);
	        } catch(InterruptedException e) {}
	    }	       
	} 	

	public void windowClosing(WindowEvent arg0) {
		end();
	}

	public void windowActivated(WindowEvent arg0) {}
	public void windowClosed(WindowEvent arg0) {}
	public void windowDeactivated(WindowEvent arg0) {}
	public void windowDeiconified(WindowEvent arg0) {}
	public void windowIconified(WindowEvent arg0) {}
	public void windowOpened(WindowEvent arg0) {}
	
	private static void unload() {
		Updatable.unload();
		instance.outFile.close();
	}

	public static void end() {
		unload();
		System.exit(5);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		AlgorithmTester at = GOGL.getAT();
		if(e.getSource() == testButton)
			if(isRandom || !(at.getStartNode() == at.getEndNode() || at.getStartNode() == null || at.getEndNode() == null)) {
				chartH = 0;
				chartTxt = "";
				
				if(isRandom)
					at.randomNodes();
				
				at.setPredictionWeight(Double.parseDouble(predictionField.getText()));
				at.setRandomnessWeight(Double.parseDouble(randomnessField.getText()));
				at.start(Integer.parseInt(testNumField.getText().replace(",", "")), 
						Integer.parseInt(trialNumField.getText().replace(",", "")));
			}
		if(e.getSource() == randomButton) {
			at.randomNodes();
			fillNodeFields();
		}
	}
	
	public void fillNodeFields() {
		AlgorithmTester at = GOGL.getAT();
		Node start, end;
		start = at.getStartNode();
		end = at.getEndNode();
		if(start != null)
			startNodeField.setText(start.getName());
		if(end != null)
			endNodeField.setText(end.getName());
	}

	@Override
	public void mouseClicked(MouseEvent o) {		
		if(isRandom)
			return;
		else if(o.getSource() == startNodeField)
			selectingNode = N_START;
		else if(o.getSource() == endNodeField)
			selectingNode = N_END;
		else
			selectingNode = N_NONE;
	}

	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
}
