# Disc Golf Visualization - State of Michigan Courses

This project was a multi-step venture intended to illustrate the rapid growth exhibited by the sport of disc golf in recent years, ultimately resulting in the following animation.

<img src="/images/_DiscGolfVisualization.gif?raw=true" width="1000px">

## Project Overview

### Data Scraping (Scraper.java & Course.java)
- Wrote Java program that utilized the Selenium framework to pull down name, location, coordinates, and year of establishment data for all disc golf courses in the state of Michigan from [dgcoursereview.com](https://www.dgcoursereview.com/).

### Map Plotting & Image Export (MapGenerator.java)
- Wrote Java program utilizing the GeoTools library to display a map of Michigan (shapefile), plot all courses based on their listed coordinates, display relevant graphics / data tracking details, and ultimately export results as a series of .png image files.
 
### Animated .gif Creation
- Leveraged gifTiming() method of MapGenerator.java to determine appropriate timing in milliseconds for each frame. This allowed the final output to display a single year of data per second and accurately reflect the rate of new course additions.
- Entered appropriate timing for each frame and produced final .gif output via [ezgif.com](https://ezgif.com/maker).