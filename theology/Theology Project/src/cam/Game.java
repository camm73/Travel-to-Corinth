package cam;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import cam.entities.people.Commoner;
import cam.entities.people.Player;
import cam.graphics.Screen;
import cam.graphics.Sprite;
import cam.input.Keys;
import cam.level.Level;
import cam.level.MainLevel;
import cam.level.Map;
import cam.level.RandomLevel;
import cam.level.tile.Tile;

public class Game extends Canvas implements Runnable {

	private Thread thread;
	private JFrame frame;
	private boolean running = false;

	public static final int WIDTH = 260;
	public static final int HEIGHT = 180;
	public static final int SCALE = 3;
	public static final String title = "Travel to Corinth";

	private Screen screen;
	private Keys keys;
	private Level level;
	private Map map;
	private int fps;
	public static Player player;
	public boolean first = true;
	
	private int commonNum = 12;
	public static int statements;
	public static String commonerText = "";
	public static String commonerText2 = "";

	public BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	public int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

	public Game() {
		Dimension size = new Dimension(WIDTH * SCALE, HEIGHT * SCALE);
		setMinimumSize(size);
		setMaximumSize(size);
		setPreferredSize(size);

		frame = new JFrame(title);
		keys = new Keys();
		screen = new Screen(WIDTH, HEIGHT);
		level = Level.main;
		map = new Map();
		player = new Player(5, 20, keys);
		player.init(level);
		addKeyListener(keys);
		
		statements = commonNum;
	}

	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Display");
		thread.start();
	}

	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void update() {
		keys.update();
		player.update();
		level.update();
		
		if(first){
			addEntity();
			first = false;
		}
	}
	
	public void addEntity(){
		for(int i = 0; i < commonNum; i++){
			level.add(new Commoner(i*10 + 14, 20, i, false));
		}
	}

	public void render() {
		BufferStrategy bs = getBufferStrategy();

		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		screen.clear();
		int xScroll = player.x - screen.width / 2;
		int yScroll = player.y - screen.height / 2;
		level.render(xScroll, yScroll, screen);
		//System.out.println(xScroll + "   " + yScroll);
		player.render(screen);
		//System.out.println("x: "+ player.x + " y: "+ player.y);
		
		for(int i = 0; i < pixels.length; i++){
			pixels[i] = screen.pixels[i];
		}
		
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		g.setColor(Color.red);
		g.setFont(new Font("Arial", Font.BOLD, 18));
		g.drawString(new String(String.valueOf(fps)), 15, 25);
		g.drawString("Statements left: " + statements, (WIDTH*SCALE)/2, 25);
		g.setFont(new Font("Arial", Font.BOLD, 16));
		g.drawString(commonerText, 10, (HEIGHT*SCALE) - 35);
		g.drawString(commonerText2, 10, (HEIGHT*SCALE) - 15);
		
		map.render(g, level);
		
		g.dispose();
		bs.show();
	}

	@Override
	public void run() {
		long prev = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 60.0;
		double delta = 0;
		int frames = 0;
		int updates = 0;
		
		requestFocus();
		while (running) {
			long now = System.nanoTime();
			delta += (now-prev) / ns;
			prev = now;
			while(delta >= 1){
				update();
				updates++;
				delta--;
			}
			render();
			frames++;
			
			if(System.currentTimeMillis() - timer > 1000){
				timer += 1000;
				fps = frames;
				frame.setTitle(title + " UPS: " + updates);
				frames = 0;
				updates = 0;
			}
		}
		
		stop();
	}

	public void begin() {
		frame.setTitle(title);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		start();
	}

}
