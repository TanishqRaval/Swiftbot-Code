import swiftbot.*;
import swiftbot.SwiftBotAPI;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class mainClass {

    public static int LeftHighest = 0;
    public static int RightHighest = 0;
    public static int CenterHighest = 0;
    public static double totalDistance;
    public static long startTime;
    public static int leftAverageIntensity;
    public static int centerAverageIntensity;
    public static int rightAverageIntensity;

    public static int lightDetectionCount = 0;
    public static int objectDetectionCount = 0;
    public static ArrayList<String> moveTowardsLight = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        SwiftBotAPI swiftBotAPI= new SwiftBotAPI();
        // Initialise class
        swiftBotAPI.enableButton(Button.B, () -> {
            System.out.println("The Java Program of the Swiftbot Light utilizes SwiftBotAPI to navigate SwiftBot" +
                    " roughly towards the brightest light source available in the environment." +
                    " It captures image, processes it to detect light intensity in different directions," +
                    " moves towards the brightest direction, and alerts the user if an obstacle is detected," +
                    " allowing them to display a log of execution upon the program completion.\n ");
        });

        // Initializing start time
        startTime = System.currentTimeMillis();
        //display the  menu
        menuDisplay();
        //assign the button A to start the program
        swiftBotAPI.enableButton(Button.A, () ->
                {

                    while (startProgram = true)
                    {
                        //this part of the code takes an image and saves it as jpg image
                        BufferedImage image = swiftBotAPI.takeStill(ImageSize.SQUARE_1080x1080);
                        try {
                            ImageIO.write(image, "jpg", new File("/home/pi/Documents/FirstImage.jpg"));
                        } catch (IOException e) {
                            System.out.println("Error while saving the image in the folder");
                        }
                        System.out.println("The image has been clicked and saved as FirstImage in the Documents in Swiftbot!\n");


                        // Process the captured image to decide the direction
                        int direction = imageProcessing(image);
                        //setting the lights green before movement
                        greenLights(swiftBotAPI);
                        //this part of the code sets the threshold to 10 for minimum value of light intensity
                        //if the light intensity is less than 10 then the swiftbot will move in random directions
                        //with blue underlights
                        if(leftAverageIntensity<10 && rightAverageIntensity<10 && centerAverageIntensity<10)
                        {
                            System.out.println("No light Source Found in the environment");
                            randomlyChange(swiftBotAPI);
                        }
                        //now the swiftbot should move at a slow speed for 0.5 second and then at the normal speed as mentioned in the requirements
                        swiftBotAPI.move(50, 50, 500);

                        // Move the bot in the direction of highest light
                        movementTowardHighestLight(swiftBotAPI, direction);

                        // The next par of the code Check for objects and move according to the requirements
                        if (obstacleDetection(swiftBotAPI)) {
                            // Object detected, stop execution
                            break;
                        }

                    }

                }
        );
        // After whole execution, ask the user if they want to display the log
        startProgram = false;
        swiftBotAPI.enableButton(Button.X, () -> {

            startProgram = false;
            swiftBotAPI.disableUnderlights();
            System.out.println("Do you want to display the log of execution? (Press 'Y' for Yes, 'X' for No\n");
            swiftBotAPI.enableButton(Button.Y, ()->{ //Y to display log, write to file and exit

                displayAndSaveLog();
                System.exit(0);
            });
            swiftBotAPI.disableButton(Button.X); //disable first so another functionality can be added
            swiftBotAPI.enableButton(Button.X, ()->{ //X again to write log to file and exit
                System.exit(0);
            });
        });
    }


    //code to turn on the green lights before moving the swiftbot
    public static void greenLights(SwiftBotAPI swiftBotAPI) {
        int[] colourToLightUp = {0, 0, 255};
        swiftBotAPI.fillUnderlights(colourToLightUp);
    }
    //method to display the menu
    public static void menuDisplay() {
        System.out.println("************************************************************************************");
        System.out.println("         SwiftBot Light Search Program       ");
        System.out.println("************************************************************************************");
        System.out.println("1. Press the Button A to Start The Program Program");
        System.out.println("2. Press the Button  X to Exit The Program");
        System.out.println("3. Press the button B for more information of the Program and how is it going work ");
        System.out.println("------------------------------------------------------------------------------------");

    }

    //method to process the image taken
    public static int imageProcessing(BufferedImage image) {
        // Convert the image to a pixel matrix using loops
        System.out.println("Processing the Image\n");
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] pixelMatrix = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelMatrix[y][x] = image.getRGB(x, y);

            }
        }


        // This part of the code Divide the pixel matrix into left, center, and right sections
        int columnWidth = width / 3;
        int[][] leftColumn = new int[height][columnWidth];
        int[][] centerColumn = new int[height][columnWidth];
        int[][] rightColumn = new int[height][width - 2 * columnWidth];
        // Copy pixels to respective columns
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x < columnWidth)
                    leftColumn[y][x] = pixelMatrix[y][x];
                else if (x >= columnWidth && x < 2 * columnWidth)
                    centerColumn[y][x - columnWidth] = pixelMatrix[y][x];
                else
                    rightColumn[y][x - 2 * columnWidth] = pixelMatrix[y][x];

            }
        }

        // Calculate average intensities for all of the sections
        leftAverageIntensity = calculateAverageIntensity(leftColumn);
        centerAverageIntensity = calculateAverageIntensity(centerColumn);
        rightAverageIntensity = calculateAverageIntensity(rightColumn);

//printing the average intensity in each sections
        System.out.println("Left Average Intensity: " + leftAverageIntensity);
        System.out.println("Center Average Intensity: " + centerAverageIntensity);
        System.out.println("Right Average Intensity: " + rightAverageIntensity);


        // This part of the code Updates the values of the Highest intensity
        //if its changed or the course of program execution
        // To decide the direction which have the highest light intensity and return with a number in accordance to them
        // To decide the direction which has the highest light intensity and return with a number accordingly
        LeftHighest = Math.max(LeftHighest, leftAverageIntensity);
        CenterHighest = Math.max(CenterHighest, centerAverageIntensity);
        RightHighest = Math.max(RightHighest, rightAverageIntensity);

        // Determine the direction with the highest intensity
        if (LeftHighest >= CenterHighest && LeftHighest >= RightHighest)
        {

            return 0; // Left side
        } else if (CenterHighest >= LeftHighest && CenterHighest >= RightHighest)
        {
            return 1; // Center
        } else
        {
            return 2; // Right side
        }
    }


    //method to detect the object and run according to the requirements
    public static boolean obstacleDetection(SwiftBotAPI swiftBotAPI)
    {
        int threshold = 50; // Threshold for object detection in cm

        double distanceToObject = swiftBotAPI.useUltrasound();
        if (distanceToObject <= threshold) {
            System.out.println("Object detected at " + distanceToObject + " cm ahead.");
            objectDetectionCount++;
            // Notify the user about the object
            int[] colorToLightdown = {255, 0, 0}; // Red color
            swiftBotAPI.fillUnderlights(colorToLightdown);
            System.out.println("Please remove the object in front of the Swiftbot.");

            // Check after 10 seconds if the object has been removed
            try {
                Thread.sleep(10000); // Wait for 10 seconds
            } catch (InterruptedException e) {
                System.out.println("Error while putting swiftbot on sleep");
            }

            double distanceAfterWait = swiftBotAPI.useUltrasound();
            if (distanceAfterWait > threshold) {
                // Object removed, continue the search
                System.out.println("Object removed. Continuing the search.\n");
                swiftBotAPI.disableUnderlights();
            } else
            {
                // Object not removed, terminate the program
                System.out.println("Object not removed. Terminating the program.\n");
                swiftBotAPI.disableUnderlights();
                startProgram = false;
                System.exit(1);
                return true; //object detected
            }
            return false; // object not detected
        }


        double distanceToObjectTwo = 0;
        distanceToObjectTwo = swiftBotAPI.useUltrasound();
        if (distanceToObjectTwo >= 50) {
            int[] colourToLight = {0, 0, 255};
            swiftBotAPI.fillUnderlights(colourToLight);
            System.out.println("No object found,the program is now continuing\n");
            swiftBotAPI.move(100, 100, 500);
        } else {
            System.out.println("Object is not removed,the program is now stopping.\n");
        }
        return false; // No object detected
    }
    //this part of the code changes the direction randomly if no light source is found
    public static void randomlyChange(SwiftBotAPI swiftBotAPI)
    {
        int[] colorToLightdown = {0, 255, 0}; // Blue color
        swiftBotAPI.fillUnderlights(colorToLightdown);
        try {
            Thread.sleep(5000); // Wait for 5 seconds
        } catch (InterruptedException e) {
            System.out.println("Error while putting swiftbot on sleep");
        }
        Random rand = new Random();
        int randomDirection = rand.nextInt(2); // Generate a random number (0 or 1) for direction change
        if (randomDirection == 0)
        {

            System.out.println("Light intensity threshold not met. Changing direction 90 degrees to the left.");
            swiftBotAPI.move(0, 100, 712); // Turn left
            swiftBotAPI.disableUnderlights();
        }
        else {
            int[] colorToLight = {0, 255, 0}; // Blue color
            swiftBotAPI.fillUnderlights(colorToLight);
            System.out.println("Light intensity threshold not met. Changing direction 90 degrees to the right.");
            swiftBotAPI.move(100, 0, 712); // Turn right
            swiftBotAPI.disableUnderlights();

        }

    }

    public static void movementTowardHighestLight(SwiftBotAPI swiftBotAPI, int direction) {
        String movetowardLight = "";
        switch (direction) {
            case 0: // Left
                movetowardLight = "Left";
                swiftBotAPI.move(0, 100, 712); // Turn left
                swiftBotAPI.move(100,100,1500);
                lightDetectionCount++;
                System.out.println("The swiftbot is now moving towards left\n");
                break;
            case 1: // Center
                movetowardLight = "Straight";
                swiftBotAPI.move(100, 100, 1500);
                lightDetectionCount++;
                // Move forward
                System.out.println("The swiftbot is now moving towards straight\n");
                break;
            case 2: // Right
                movetowardLight = "Right";
                swiftBotAPI.move(100, 0, 712); // Turn right
                swiftBotAPI.move(100,100,1500);
                lightDetectionCount++;
                System.out.println("The swiftbot is now moving towards right\n");
                break;
            default:
                System.out.println("No valid direction found");
                break;
        }
        moveTowardsLight.add(movetowardLight);
    }


    public static int calculateAverageIntensity(int[][] column)
    {
        int sum = 0;
        int number = 0;
        for (int[] row : column) {
            for (int pixel : row) {
                int red = (pixel >> 16) & 0xff;
                int blue = pixel & 0xff;
                int green = (pixel >> 8) & 0xff;
                int intensity = (red + green + blue) / 3;
                sum += intensity;
                number++;
            }
        }
        return (int) sum / number;
    }
    public static void displayAndSaveLog()
    {
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000; // Convert to seconds
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("            Executing the log information              ");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Highest average intensity in left column  " + LeftHighest);
        System.out.println("Highest average intensity in center column : " + CenterHighest);
        System.out.println("Highest average intensity in right column : " + RightHighest);
        System.out.println("Number of times light detected during the whole program: " +lightDetectionCount );
        System.out.println("Number of times objects during the whole program: " +objectDetectionCount );
        System.out.println("Movements:");
        for (String movement : moveTowardsLight) {
            System.out.println(movement);
        }
        for (int i = 0; i < moveTowardsLight.size(); i++) {
            String movement = moveTowardsLight.get(i);
            if (moveTowardsLight.contains("Straight")) {
                totalDistance += 10; // Assuming 10 cm for straight movement
            } else if (moveTowardsLight.equals("Left") || moveTowardsLight.equals("Right")) {
                totalDistance += 10; // Assuming 10 cm for left/right movement
            }
        }
        System.out.println("Total distance travelled: " + totalDistance + " cm");
        System.out.println("Duration of execution: " + duration + " seconds");

        // Write log information to a text file
        try {
            FileWriter writer = new FileWriter("/home/pi/Documents/Log.txt");
            writer.write("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            writer.write("            Executing the log information              ");
            writer.write("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            writer.write("Executing the log information\n");
            writer.write("Highest average intensity in left column  " + LeftHighest + "\n");
            writer.write("Highest average intensity in center column : " + CenterHighest + "\n");
            writer.write("Highest average intensity in right column : " + RightHighest + "\n");
            writer.write("Number of times light detected during the whole program: " + lightDetectionCount + "\n");
            writer.write("Number of times object detected during the whole program: " + objectDetectionCount + "\n");
            writer.write("Movements:\n");
            for (String movement : moveTowardsLight) {
                writer.write(movement + "\n");
            }
            writer.write("Total distance travelled: " + totalDistance + " cm\n");
            writer.write("Duration of execution: " + duration + " seconds\n");
            writer.close();
            System.out.println("Log file saved successfully.");
            System.out.println("*************   END OF LOG DISPLAY   **************");


        } catch (IOException e) {
            System.out.println("Error writing log file: " + e.getMessage());
        }

    }

    public static boolean startProgram;

}

