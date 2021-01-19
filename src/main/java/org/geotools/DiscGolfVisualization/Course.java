
package org.geotools.DiscGolfVisualization;

public class Course {

	private String courseName, courseLocation, courseEstablished, courseCoordinates;
	
	public Course(String courseName, String courseLocation, String courseEstablished, String courseCoordinates) {
		this.courseName = courseName;
		this.courseLocation = courseLocation;
		this.courseEstablished = courseEstablished;
		this.courseCoordinates = courseCoordinates;
	}

	public String getCourseName() {
		return courseName;
	}

	public String getCourseLocation() {
		return courseLocation;
	}

	public String getCourseEstablished() {
		return courseEstablished;
	}

	public String getCourseCoordinates() {
		return courseCoordinates;
	}
	
	@Override
	public String toString() {
		return "\"" + courseName + "\",\"" + courseLocation + "\",\"" + courseEstablished + "\",\"" + courseCoordinates + "\"\n";
	}	
	
}
