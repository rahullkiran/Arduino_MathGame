import org.firmata4j.I2CDevice;
import org.firmata4j.IODevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class Arduino_MathGame {
    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner sc = new Scanner(System.in);
        //Intialize Board
        String myUSB = "/dev/cu.usbserial-0001"; // Define USB Connection
        IODevice myGroveBoard = new FirmataDevice(myUSB); // Create a FirmataDevice object with a USB connection.
        myGroveBoard.start(); myGroveBoard.ensureInitializationIsDone(); // Start up the FirmataDevice object.

        //Intialize pins
        var myButton =  myGroveBoard.getPin(6);
        myButton.setMode(Pin.Mode.INPUT);
        var myPot = myGroveBoard.getPin(14);
        myPot.setMode(Pin.Mode.ANALOG);
        var myLed = myGroveBoard.getPin(4);
        myLed.setMode(Pin.Mode.OUTPUT);
        var myBuzzer = myGroveBoard.getPin(5);
        myBuzzer.setMode(Pin.Mode.OUTPUT);

        //OLED DISPLAY
        I2CDevice i2cObject = myGroveBoard.getI2CDevice((byte) 0x3C); // Use 0x3C for the Grove OLED
        SSD1306 theOledObject = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64); // 128x64 OLED SSD1515

        System.out.println("How many questions would you like?");
        int q = sc.nextInt();
        gameStart(myBuzzer, myButton, myLed, myPot, q, theOledObject);
    }


    //The game method
    public static void gameStart(Pin myBuzzer, Pin myButton, Pin myLed, Pin myPot, int q, SSD1306 theOledObject) {
        //Initialize variables
        System.out.println("START");
        Random random = new Random();
        theOledObject.init();
        int result = 0;

        for (int i = 0; i < q; i++) { //Loop iterates through questions
            int operation = random.nextInt(3);
            String op = " ";
            //Generates random question
            int num1 = random.nextInt(11);
            int num2 = random.nextInt(num1+1);
            int ans = 0;
            switch(operation){
                case 0: op = "+";
                ans = num1+num2;
                break;
                case 1: op = "-";
                ans = num1-num2;
                break;
                case 2: op = "x";
                ans = num1*num2;
                break;
            }
            theOledObject.clear();
            try{
                Thread.sleep(1500);
            }
            catch(Exception e){

            }

            //Displays question until answer is selected
            while(myButton.getValue()!=1) {
                theOledObject.getCanvas().drawString(0, 1, "Question " + (i + 1) + ": What is");
                theOledObject.getCanvas().drawString(0, 10, num1 + " " + op + " " + num2 + " ?");
                theOledObject.display();
                theOledObject.getCanvas().drawString(10,20,(int) myPot.getValue()/10 + " ");
            }

            //Check answer
            if(myPot.getValue()/10 == ans){ //Correct
                result++;
                try{
                    theOledObject.clear();
                    theOledObject.getCanvas().drawString(0, 1, "Correct!!!");
                    theOledObject.display();
                    myBuzzer.setValue(1);
                    Thread.sleep(3500);
                    myBuzzer.setValue(0);
                }
                catch(Exception e){
                    System.out.println("Exception!!!");
                }
            }
            else{ //Incorrect
                try{
                    theOledObject.clear();
                    theOledObject.getCanvas().drawString(0, 1, "Wrong!");
                    theOledObject.getCanvas().drawString(0, 10, "The answer is: " + ans);
                    theOledObject.display();
                    myLed.setValue(1);
                    Thread.sleep(3500);
                    myLed.setValue(0);
                }
                catch(Exception e){
                    System.out.println("Exception!!!");
                }
            }
        }

        //Display result
        theOledObject.clear();
        theOledObject.getCanvas().drawString(0, 1, "You got " + result + "/" + q);
        theOledObject.getCanvas().drawString(0, 10, "Questions right!!!");
        theOledObject.display();
    }
}
