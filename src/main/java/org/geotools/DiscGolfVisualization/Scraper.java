
package org.geotools.DiscGolfVisualization;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Scraper {
	
	private int counter, pageIterator, resultsIterator;
	private ArrayList<Course> courses = new ArrayList<Course>();
	private String courseName, courseLocation, courseEstablished, courseCoordinates, url;
		
	public Scraper() {	
		System.setProperty("webdriver.gecko.driver", "C:\\Users\\19894\\Drivers\\selenium\\geckodriver-v0.28.0-win64\\geckodriver.exe");		
		ProfilesIni profile = new ProfilesIni();
        FirefoxProfile seleniumProfile = profile.getProfile("seleniumProfile");
        FirefoxOptions options = new FirefoxOptions();
        options.setProfile(seleniumProfile);		
		WebDriver driver = new FirefoxDriver(options);
		
		WebDriverWait wait = new WebDriverWait(driver, 10);
		ArrayList<String> tabs = new ArrayList<String>();
		String openNewTab = Keys.chord(Keys.CONTROL, Keys.RETURN);

		counter = 0;
		pageIterator  = 1;
		resultsIterator = 2; // (first result is 2; each page has 20 results)		
		boolean completed = false;
		
		while (completed == false) {
			try {
				if (counter % 20 == 0) {					
					// MICHIGAN ONLY
					url = "https://www.dgcoursereview.com/browse.php?cname=&designer=&holes=0&length_min=&length_max=&holetype=0&"
							+ "coursetype=a%3A2%3A%7Bi%3A0%3Bs%3A1%3A%221%22%3Bi%3A1%3Bs%3A1%3A%222%22%3B%7D&terrain=a%3A3%3A%7Bi"
							+ "%3A0%3Bi%3A1%3Bi%3A1%3Bi%3A2%3Bi%3A2%3Bi%3A3%3B%7D&landscape=a%3A3%3A%7Bi%3A0%3Bi%3A1%3Bi%3A1%3Bi%"
							+ "3A2%3Bi%3A2%3Bi%3A3%3B%7D&teetype=0&mtees=&mpins=&cf=&num_reviews=&rating_min=&rating_max=&yem=&ye"
							+ "x=&country=1&state=27&city=&photos=&videos=&tourneys=&camping=&restrooms=&nopets=&private=1&paytop"
							+ "lay=1&on_bg=&extinct=&zipcode=&zip_distance=25&sort=name&order=ASC&page=&page=" + pageIterator;										
					driver.get(url);
				}
				
				// obtain details available from the search page
				courseName = driver.findElement(By.xpath("/html/body/div[4]/div[1]/div[7]/table/tbody/tr[" + resultsIterator + "]/td[1]/a")).getText();
				courseLocation = driver.findElement(By.xpath("/html/body/div[4]/div[1]/div[7]/table/tbody/tr[" + resultsIterator + "]/td[2]")).getText();
								
				// open new FireFox tab & switch
				driver.findElement(By.xpath("/html/body/div[4]/div[1]/div[7]/table/tbody/tr[" + resultsIterator + "]/td[1]/a")).sendKeys(openNewTab);
				tabs.addAll(driver.getWindowHandles());
				driver.switchTo().window(tabs.get(1));
					
				getDetailsFromCoursePage(driver, wait);			
				incrementPageIterators();
				courses.add(new Course(courseName, courseLocation, courseEstablished, courseCoordinates));
				System.out.println(courses.get(courses.size() - 1).toString());
				
				driver.close();
				driver.switchTo().window(tabs.get(0));			
				tabs.clear();
				counter++;	
			} catch (Exception e) {
				e.printStackTrace();
				writeCSV(courses);
				completed = true;
			}
		}	
		driver.close();	
	}
	
	public void getDetailsFromCoursePage(WebDriver driver, WebDriverWait wait)  {
		String xPathValue = "";
		
		// locates the first column of the "Location Details" box & determines whether a "Nearby Disc Shops" box is present (adjusts xPath accordingly).
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[4]/div[1]/div[9]/div[6]/div[1]/span[2]")));
			xPathValue = "6";
		} catch(Exception e) {
			e.printStackTrace();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[4]/div[1]/div[9]/div[7]/div[1]/span[2]")));
			xPathValue = "7";
		}
		
		courseEstablished = driver.findElement(By.xpath("/html/body/div[4]/div[1]/div[9]/div[4]/div[2]/span[1]")).getText();
		
		for (int i = 1; i < 10; i++) {
			if (driver.findElement(By.xpath("/html/body/div[4]/div[1]/div[9]/div[" + xPathValue + "]/div[" + i + "]/span[2]")).getText().equals("Latitude/Longitude:")) {

				// interact with "click to reveal" text
				try {
					driver.findElement(By.xpath("/html/body/div[4]/div[1]/div[9]/div[" + xPathValue + "]/div[" + i +"]/span[1]/a")).click();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
			    // wait for page to register click (adjust to control request rate)
			    try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			    courseCoordinates = driver.findElement(By.xpath("/html/body/div[4]/div[1]/div[9]/div[" + xPathValue + "]/div[" + i +"]/span[1]")).getText();
				break;				
			}
		}
	}
	
	public void writeCSV(ArrayList<Course> courses) {
		try (PrintWriter writer = new PrintWriter(new File("C:\\Users\\19894\\Java Projects\\DiscGolf\\discgolf_mi.csv"))) {
			for (Course course : courses) {
				writer.write(course.toString());
			}
			System.out.println("Course details written to CSV");
			
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void incrementPageIterators() {
		if (resultsIterator < 21) {
			resultsIterator++;
		} else {
			resultsIterator = 2;
			pageIterator++;
		}
	}
	
	public static void main(String[] args) {
		new Scraper();
	}

}
