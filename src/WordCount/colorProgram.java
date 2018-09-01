package WordCount;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

class Color {
	int color;
	Position pos;
	
	Color(int c, Position p) {
		color = c;
		pos = p;
	}
	
	public int getRed() {
		return (color & 0x00ff0000) >> 16;
	}
	
	public int getGreen() {
		return (color & 0x0000ff00) >> 8;
	}
	
	public int getBlue() {
		return color & 0x000000ff;
	}
}

class Position {
	int x;
	int y;
	
	Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
}

class Direction {
	int startDir;
	double degree;
	int endDir;
	
	Direction(int s, double d, int e) {
		startDir = s;
		degree = d;
		endDir = e;
	}
}

public class colorProgram {
	private static ArrayList<Color> colorMap;
	private static Dimension imageDim;
	
	private static ArrayList<Color> parseImage() {
		File file= new File("file.jpg");
		BufferedImage image = null;
		ArrayList<Color> colors = new ArrayList<Color>();
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			System.out.println("error reading file... exiting");
			System.exit(1);
		}
		// Getting pixel color by position x and y
		int height = image.getHeight();
		int width = image.getWidth();
		imageDim = new Dimension(width, height);
		for(int i = 0; i < width; ++i) {
			for(int j = 0; j < height; ++j) {
				int clr =  image.getRGB(i, j);
				Color color = new Color(clr, new Position(i, j));
				colors.add(color);
			}
		}
		return colors;
	}
	
	private static Position averageOrange() {
		ArrayList<Position> positions = new ArrayList<Position>();
		
		// this is a very generous check to see if pixels are "orange"
		for(Color c: colorMap) {
			if((c.getRed() > 170) 
					&& (c.getGreen() > 40) 
					&& (c.getGreen() < 190) 
					&& (c.getBlue() < 60)) {
				positions.add(c.pos);
			}
		}
		
		int x = 0;
		int y = 0;
		int count = 0;
		for(Position pos: positions) {
			x += pos.x;
			y += pos.y;
			++count;
		}
		if(count == 0) { return new Position(0, 0); }
		
		return new Position(x / count, y / count);
	}
	
	private static Direction calcDirection(Position pos) {
		if(pos.x == 0) {
			if(pos.y == 0) {
				return new Direction(Constants.NORTH, -1, Constants.EAST);
			} else if (pos.y > 0) {
				return new Direction(Constants.NORTH, 0, Constants.EAST);
			} else {
				return new Direction(Constants.SOUTH, 0, Constants.EAST);
			}
		}
		double angleDeg = Math.abs(Math.toDegrees(Math.atan((double) pos.y/(double) pos.x)));
		if(pos.y >= 0 && pos.x >= 0) {
			return new Direction(Constants.EAST, angleDeg, Constants.NORTH);
		} else if(pos.y < 0 && pos.x >= 0) {
			return new Direction(Constants.EAST, angleDeg, Constants.SOUTH);
		} else if(pos.y >= 0 && pos.x < 0) {
			return new Direction(Constants.WEST, angleDeg, Constants.NORTH);
		} else{
			return new Direction(Constants.WEST, angleDeg, Constants.SOUTH);
		}
	}
	
	private static double calcPower(Position pos, Direction dir) {
		int xInt = imageDim.width/2;
		int yInt = imageDim.height/2;
		double degRad = Math.toRadians(dir.degree);
		double xInter, len;
		if(dir.degree == -1) {
			return 0;
		} else if(dir.startDir == Constants.NORTH || dir.startDir == Constants.SOUTH) {
			return Math.abs(pos.y / yInt);
		} else {
			xInter = yInt / Math.tan(degRad);
			if(xInter > xInt) {
				len = xInt / Math.cos(degRad);
			} else {
				len = xInter / Math.cos(degRad);
			}
			return Math.sqrt(Math.pow(pos.x, 2) + Math.pow(pos.y, 2)) / len;
		}
	}
	
	private static Position convertToQuad(Position pos) {
		double xdiff = pos.x - (imageDim.getWidth() / 2);	// finding x on Cart. grid
		double ydiff = (imageDim.getHeight() / 2) - pos.y;	// finding y on Cart. grid
		
		return new Position((int) xdiff, (int) ydiff);
	}
	
	public static void main(String args[]) {
		colorMap = parseImage();	
		Position pos = convertToQuad(averageOrange());
		Direction dir = calcDirection(pos);
		double power = calcPower(pos, dir);
		System.out.println("Direction: " + dir.startDir + " " + 
				Math.round(dir.degree * 10) / 10.0 + " " + 
				dir.endDir + "\n"
			+ "Power: " + power);
	}
}
