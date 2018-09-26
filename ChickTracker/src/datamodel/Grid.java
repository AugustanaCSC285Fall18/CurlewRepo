package datamodel;

import java.util.ArrayList;

public class Grid {
	
	private double pixelColumnWidth;
	private double pixelRowWidth;
	private double gridHeight;
	private double gridWidth;
	private ArrayList<Double> columnNumbers;
	private ArrayList<Double> rowNumbers;

	public Grid(double xPixelsPerCm, double yPixelsPerCm, int numRows, int numColumns) {
		setPixelColumnWidth((gridWidth/numColumns) * xPixelsPerCm);
		setPixelRowWidth((gridHeight/numRows) * yPixelsPerCm);
		
		
		
	}

	
	
	
	
	
	public double getPixelColumnWidth() {
		return pixelColumnWidth;
	}

	public void setPixelColumnWidth(double pixelColumnWidth) {
		this.pixelColumnWidth = pixelColumnWidth;
	}

	public double getPixelRowWidth() {
		return pixelRowWidth;
	}

	public void setPixelRowWidth(double pixelRowWidth) {
		this.pixelRowWidth = pixelRowWidth;
	}

	public double getGridHeight() {
		return gridHeight;
	}

	public void setGridHeight(double gridHeight) {
		this.gridHeight = gridHeight;
	}

	public double getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(double gridWidth) {
		this.gridWidth = gridWidth;
	}

}
