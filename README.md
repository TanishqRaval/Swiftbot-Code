# Swiftbot-Code
SwiftBot Light Search Program
This Java-based program is designed to control a SwiftBot, a Raspberry Pi-powered robot, to autonomously navigate towards the brightest light source in its environment.
 The SwiftBot API is used to handle all robot interactions, including image capture, movement, and obstacle detection.
 The robot captures images and processes them by dividing the image into three sections: left, center, and right. It calculates the average light intensity in each section to determine the direction of the brightest light source.
 Based on this data, the robot moves toward the direction with the highest intensity. If no significant light source is found, the robot changes direction randomly.
 The project showcases several object-oriented programming (OOP) concepts like encapsulation, abstraction, and polymorphism, along with complex loops for data processing.
