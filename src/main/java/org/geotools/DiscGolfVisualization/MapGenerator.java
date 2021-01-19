
package org.geotools.DiscGolfVisualization;

import java.util.*;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Prompts the user for a shapefile and displays the contents on the screen in a map frame.
 * <p>
 * This is the GeoTools Quickstart application used in documentationa and tutorials. *
 */
public class MapGenerator {

	// Data structures for disc golf course information
	private static ArrayList<Object[]> combinedData = new ArrayList<Object[]>();
	private static LinkedHashMap<Integer, Integer> yearOccurencesIterative = new LinkedHashMap<Integer, Integer>();
	private static LinkedHashMap<Integer, Integer> yearOccurencesTotaled = new LinkedHashMap<Integer, Integer>();

	// Data fields for calculations and image output
    private static double latitude, longitude;
    private static Integer dataPointYear, currentYear, totalCourses;
    private static Integer eightiesTotal, ninetiesTotal, milleniumTotal, tensTotal, twentiesTotal;
    
    // Color assignments for data points by decade
	private static Color colorRed = new Color(225, 0, 0);
	private static Color colorOrange = new Color(225, 165, 0);
	private static Color colorYellow = new Color(225, 225, 0);
	private static Color colorBlue = new Color(0, 176, 240);
	private static Color colorPink = new Color(225, 102, 204);
	
    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and displays its
     * contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception {

    	// Set shapefile data source
    	File shapeFile = new File("shapefile\\Counties__v17a_.shp");
        FileDataStore store = FileDataStoreFinder.getDataStore(shapeFile);
        SimpleFeatureSource featureSource = store.getFeatureSource();

        // Create a map content and add our shapefile to it
        MapContent map = new MapContent();        
        map.setTitle("Michigan Disc Golf");
        
        // Sets desired Viewport settings (region of map to be displayed as results populate)
        MapViewport mapView = new MapViewport();
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        mapView.setCoordinateReferenceSystem(crs);
        
        Extent crsExtent = crs.getDomainOfValidity();
        for (GeographicExtent element : crsExtent.getGeographicElements()) {
            if (element instanceof GeographicBoundingBox) {
                GeographicBoundingBox bounds = (GeographicBoundingBox) element;
                ReferencedEnvelope bbox = new ReferencedEnvelope(
                    bounds.getSouthBoundLatitude(),
                    bounds.getNorthBoundLatitude(),
                    bounds.getWestBoundLongitude(),
                    bounds.getEastBoundLongitude(),

                    DefaultGeographicCRS.WGS84
                );
                ReferencedEnvelope envelope = bbox.transform(crs, true);
                mapView.setBounds(envelope);
            }
        }
           
        // Colors interior portion of the map
        StyleBuilder styleBuilder = new StyleBuilder();
        PolygonSymbolizer symbolizer = styleBuilder.createPolygonSymbolizer(Color.LIGHT_GRAY, Color.DARK_GRAY, 0);
        
        symbolizer.getFill().setOpacity(styleBuilder.literalExpression(0.5));
        Style myStyle = styleBuilder.createStyle(symbolizer);
        Layer layer = new FeatureLayer(featureSource, myStyle);
        map.addLayer(layer); 
        
        // Initializing variables
        totalCourses = 0;
        dataPointYear = 1980;
        currentYear = 1980;
    	eightiesTotal = 0;
    	ninetiesTotal = 0;
    	milleniumTotal = 0;
    	tensTotal = 0;
    	twentiesTotal = 0;
        int fileOutputCounter = 2;
        int dataAccessor = 0;
        
        // Read course data from CSV and populate data structures
        readCSV("discgolf_mi.csv");
        populateyearOccurences();
        gifTiming();
        
        // Initial snapshot with no data points
        saveImage(map, "images/output1 (" + currentYear + ").png", 1000);
        
        // Generate snapshots for each year (one snapshot per data point OR one snapshot if no data points exist for given year)
        for (currentYear = 1980; currentYear <= 2020; currentYear++) {
        	        	
        	if (yearOccurencesTotaled.get(currentYear) == 0) {
                saveImage(map, "images/output" + fileOutputCounter + " (" + currentYear + ").png", 1000);
                fileOutputCounter++;
                continue;
        	}
        	
        	for (int numOccurences = 0; numOccurences < yearOccurencesTotaled.get(currentYear); numOccurences++) {
        		totalCourses++;
            	dataPointYear = (Integer)combinedData.get(dataAccessor)[0];
            	latitude = (double)combinedData.get(dataAccessor)[1];
            	longitude = (double)combinedData.get(dataAccessor)[2]; 
            	
            	// Keep track of number of courses for each year
            	yearOccurencesIterative.put(dataPointYear, yearOccurencesIterative.get(dataPointYear) + 1);
            	
            	// Keep track of number of courses for each decade
            	if (dataPointYear < 1990) {
                	eightiesTotal++;
                } else if (dataPointYear < 2000 ) {
                	ninetiesTotal++;
                } else if (dataPointYear < 2010 ) {
                	milleniumTotal++;
                } else if (dataPointYear < 2020 ) {
                	tensTotal++;
                } else {
                	twentiesTotal++;
                }
            	
            	// create layer with all appropriate data points
            	Layer pointLayer = createPoint(latitude, longitude);
                map.addLayer(pointLayer);
                saveImage(map, "images/output" + fileOutputCounter + " (" + currentYear + ").png", 1000); // this one is the best resolution / scale
                fileOutputCounter++;
            	dataAccessor++;	
        	}
        }
        JMapFrame.showMap(map);           
    }

    // Exports current viewport display and specified graphics object as formatted image file
    static void saveImage(final MapContent map, final String file, final int imageWidth) {
        GTRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(map);

        Rectangle imageBounds = null;
        ReferencedEnvelope mapBounds = null;
                        
        try {
        	// Pulling bounds from viewport object defined in main method
        	mapBounds = map.getViewport().getBounds();
            
            double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
            imageBounds = new Rectangle(
                    0, 0, imageWidth, (int) Math.round(imageWidth * heightToWidth));

        } catch (Exception e) {
            // failed to access map layers
            throw new RuntimeException(e);
        }

        BufferedImage image = new BufferedImage(imageBounds.width, imageBounds.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = createGraphics(image, imageBounds);

        try {
        	renderer.paint(graphics, imageBounds, mapBounds);
        	
            File fileToSave = new File(file);
            ImageIO.write(image, "png", fileToSave);            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    // Returns a graphics object containing all string elements for image text overlay
    static Graphics2D createGraphics(BufferedImage image, Rectangle imageBounds) {
    	
        Graphics2D graphics = image.createGraphics();
        
        graphics.setPaint(Color.BLACK);
        graphics.fill(imageBounds);
        
        // Improve text output readability
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        // Drawing text on image output    
        Font header1 = new Font ("Helvetica", Font.BOLD, 83);
        Font header2 = new Font ("Helvetica", Font.BOLD, 70);
        Font header3 = new Font ("Helvetica", Font.PLAIN, 43);
        Font largeDetails = new Font ("Helvetica", Font.BOLD, 35);
        Font timelineHeader = new Font ("Helvetica", Font.BOLD, 32);
        Font smallDetailsBold = new Font ("Helvetica", Font.BOLD, 24);
        Font smallDetailsPlain = new Font ("Helvetica", Font.PLAIN, 22);
        Font sources = new Font ("Helvetica", Font.ITALIC, 20);
        graphics.setColor(Color.WHITE);
        
        graphics.setFont(header1);
        graphics.drawString("Disc Golf Courses", 260, 100);
	    graphics.setFont(header2);
	    graphics.drawString("State of Michigan", 398, 175);
	    graphics.setFont(header3);
	    graphics.drawString("Running Total Visualization", 454, 235);        
        graphics.setFont(timelineHeader);
        graphics.drawString("Growth by Decade", 18, 580);
        
        int positionX = 18;
        int positionY = 615;
        int columnCounter = 0;
        
        // might need to use a set for all years actually contined in hashmap
        for (int yearsRange = 1980; yearsRange < 2021; yearsRange++) {
            graphics.setColor(Color.WHITE);
            graphics.setFont(smallDetailsBold);
            graphics.drawString(yearsRange + ": ", positionX, positionY);
            graphics.setFont(smallDetailsPlain);
            graphics.drawString(yearOccurencesIterative.get(yearsRange).toString(), (positionX + 69), positionY);
            
            columnCounter++;
            positionY += 35;
                        
            if (columnCounter % 10 == 0) {
            	positionX += 102;
            	positionY = 615;
            }
            
            // draw disc icons and running total decade count
            int xOffset = 102;
            int yOffsetBullet = 991;
            int yOffsetNumber = 979;
            graphics.setFont(header2);
            graphics.setColor(colorRed);
            graphics.drawString("•", 20, yOffsetBullet);
            graphics.setFont(timelineHeader);
            graphics.drawString(eightiesTotal.toString(), 55, yOffsetNumber);

            graphics.setFont(header2);
            graphics.setColor(colorOrange);
            graphics.drawString("•", 20 + xOffset, yOffsetBullet);
            graphics.setFont(timelineHeader);
            graphics.drawString(ninetiesTotal.toString(), 55 + xOffset, yOffsetNumber);
            
            graphics.setFont(header2);
            graphics.setColor(colorYellow);
            graphics.drawString("•", 20 + (xOffset * 2), yOffsetBullet);
            graphics.setFont(timelineHeader);
            graphics.drawString(milleniumTotal.toString(), 55 + (xOffset * 2), yOffsetNumber);
            
            graphics.setFont(header2);
            graphics.setColor(colorBlue);
            graphics.drawString("•", 20 + (xOffset * 3), yOffsetBullet);
            graphics.setFont(timelineHeader);
            graphics.drawString(tensTotal.toString(), 55 + (xOffset * 3), yOffsetNumber);
            
            graphics.setFont(header2);
            graphics.setColor(colorPink);
            graphics.drawString("•", 20 + (xOffset * 4), yOffsetBullet);
            graphics.setFont(timelineHeader);
            graphics.drawString(twentiesTotal.toString(), 55 + (xOffset * 4), yOffsetNumber);
        }
        
        graphics.setColor(Color.WHITE);
        graphics.setFont(smallDetailsBold);
        graphics.drawString("____________________________________", 18, 940);
        graphics.setFont(largeDetails);
        graphics.drawString("Total Courses: " + totalCourses, 18, 1025);
        graphics.drawString("Current Year: " + currentYear, 18, 1075);
        graphics.setFont(sources);
        graphics.drawString("*Course details current through January 2021", 18, 1115);
        graphics.drawString("*Data sourced from dgcoursereview.com", 18, 1140);
        
        return graphics;
    }
    
    // Returns a layer object containing a single specified point
    static Layer createPoint(double latitude, double longitude) {
    	SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();    	

        builder.setName("MyFeatureType");
        builder.setCRS(DefaultGeographicCRS.WGS84);
        builder.add("location", Point.class);
        
        // building the type
        final SimpleFeatureType TYPE = builder.buildFeatureType();
        
        // consolidating features
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);        
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(latitude, longitude));
        featureBuilder.add(point);
        SimpleFeature feature = featureBuilder.buildFeature(null);
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);        
        featureCollection.add(feature);
        
        // Assign appropriate color by decade
        Style style;        
        if (dataPointYear < 1990) {
        	// Red
            style = SLD.createPointStyle("Circle", Color.BLACK, colorRed, 1.0f, 8.0f);
        } else if (dataPointYear < 2000 ) {
        	// Orange
            style = SLD.createPointStyle("Circle", Color.BLACK, colorOrange, 1.0f, 8.0f);
        } else if (dataPointYear < 2010 ) {
        	// Yellow
            style = SLD.createPointStyle("Circle", Color.BLACK, colorYellow, 1.0f, 8.0f);
        } else if (dataPointYear < 2020 ) {
        	// Blue
            style = SLD.createPointStyle("Circle", Color.BLACK, colorBlue, 1.0f, 8.0f);
        } else {
        	// Pink
            style = SLD.createPointStyle("Circle", Color.BLACK, colorPink, 1.0f, 8.0f);
        }        
        
        Layer layer = new FeatureLayer(featureCollection, style);
        return layer;
    }
    
    // Reads CSV course information for iterative map updating
    static void readCSV(String inputFile) {
    	try {
			File file = new File(inputFile);
			FileInputStream input = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			
			String line = "";
			String latitude = "";
			String longitude = "";
			String established = "";
			
			while ((line = reader.readLine()) != null) {
				String[] output = line.split(",");
				established = output[output.length - 3];
				latitude = output[output.length - 1];
				latitude = latitude.substring(0, latitude.length() - 1);
				longitude = output[output.length - 2];
				longitude = longitude.substring(1, longitude.length() - 1);
				
				Object[] data = new Object[] {Integer.parseInt(established), Double.parseDouble(latitude), Double.parseDouble(longitude)};

				combinedData.add(data);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    // Populates LinkedHashMap data structures with appropriate course data
    static void populateyearOccurences() {
        
    	// populate LinkedHashMap that tracks courses iteratively as program runs (initializes all values at zero)
    	for (int yearsRange = 1980; yearsRange < 2021; yearsRange++) {
        	yearOccurencesIterative.put(yearsRange, 0);
        	yearOccurencesTotaled.put(yearsRange, 0);
        }
        
    	// populate LinkedHashMap that shows total number of courses for each year
        for (Object[] course : combinedData) {
        	// Keep track of number of courses for each year
        	yearOccurencesTotaled.put((Integer)course[0], yearOccurencesTotaled.get((Integer)course[0]) + 1);
        }
    }
    
    // Calculates display timing in milliseconds for each year and outputs details to console (useful for ezgif.com)
    static void gifTiming() {
    	
    	String outputString = "";
    	int year = 1980;
    	double msPerYear = 100.0;
    	double msPerFrame = 0.0;
    	
    	for (year = 1980; year < 2021; year++) {
    		if (yearOccurencesTotaled.get(year) == 0) {
    			msPerFrame = msPerYear;
    		} else {
    			msPerFrame = Math.round(msPerYear / (double)yearOccurencesTotaled.get(year));
    		}
    		outputString += year + ": " + msPerFrame + " ms\n" + "FrameCount: " + yearOccurencesTotaled.get(year) + "\n\n";
        }    	
    	System.out.println(outputString);
    }

}