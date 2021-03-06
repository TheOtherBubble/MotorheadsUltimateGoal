package org.firstinspires.ftc.teamcode.Teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.AutonDriving;
import org.firstinspires.ftc.teamcode.Hardware;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;
import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;

//import org.firstinspires.ftc.teamcode.src.main.java.org.firstinspires.ftc.teamcode.DriveOnlyHardware;


@TeleOp(name="Teleop with launcher", group="Teleop")

//@Disabled

public class DriveTeleopWithLauncher extends OpMode {

    static final double     COUNTS_PER_MOTOR_REV    = 537.6 ;    // eg: TETRIX Motor Encoder
    static final double     DRIVE_GEAR_REDUCTION    = 0.4 ;     // This is < 1.0 if geared UP
    static final double     WHEEL_DIAMETER_INCHES   = 2.95276 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * Math.PI);
    static final double     DRIVE_SPEED             = 0.4;
    static final double     STRAFE_SPEED             = 0.7;
    static final double     TURN_SPEED              = 0.5;

    private ElapsedTime runtime = new ElapsedTime();
    private ElapsedTime driveTimer = new ElapsedTime();
    private boolean driveCheck = false;
    private final int VUFORIA_TIMEOUT_MS = 5000;
    private final float RED_GOAL_Y_SHIFT = -34;
    private final float BLUE_GOAL_Y_SHIFT = 34;

    private float vuforiaPosX = 0;
    private float vuforiaPosY = 0;
    private float vuforiaPosZ = 0;
    private float vuforiaAngX = 0;
    private float vuforiaAngY = 0;
    private float vuforiaAngZ = 0;


    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = BACK;
    private static final boolean PHONE_IS_PORTRAIT = false  ;

    /*
     * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
     * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
     * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
     * web site at https://developer.vuforia.com/license-manager.
     *
     * Vuforia license keys are always 380 characters long, and look as if they contain mostly
     * random data. As an example, here is a example of a fragment of a valid key:
     *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
     * Once you've obtained a license key, copy the string from the Vuforia web site
     * and paste it in to your code on the next line, between the double quotes.
     */
    private static final String VUFORIA_KEY =
            "AYy6NYn/////AAABmTW3q+TyLUMbg/IXWlIG3BkMMq0okH0hLmwj3CxhPhvUlEZHaOAmESqfePJ57KC2g6UdWLN7OYvc8ihGAZSUJ2JPWAsHQGv6GUAj4BlrMCjHvqhY0w3tV/Azw2wlPmls4FcUCRTzidzVEDy+dtxqQ7U5ZtiQhjBZetAcnLsCYb58dgwZEjTx2+36jiqcFYvS+FlNJBpbwmnPUyEEb32YBBZj4ra5jB0v4IW4wYYRKTNijAQKxco33VYSCbH0at99SqhXECURA55dtmmJxYpFlT/sMmj0iblOqoG/auapQmmyEEXt/T8hv9StyirabxhbVVSe7fPsAueiXOWVm0kCPO+KN/TyWYB9Hg/mSfnNu9i9";

    // Since ImageTarget trackables use mm to specifiy their dimensions, we must use mm for all the physical dimension.
    // We will define some constants and conversions here
    private static final float mmPerInch        = 25.4f;
    private static final float mmTargetHeight   = (6) * mmPerInch;          // the height of the center of the target image above the floor

    // Constants for perimeter targets
    private static final float halfField = 72 * mmPerInch;
    private static final float quadField  = 36 * mmPerInch;

    // Class Members
    private OpenGLMatrix lastLocation = null;
    private VuforiaLocalizer vuforia = null;

    /**
     * This is the webcam we are to use. As with other hardware devices such as motors and
     * servos, this device is identified using the robot configuration tool in the FTC application.
     */
    WebcamName webcamName = null;

    private boolean targetVisible = false;
    private float phoneXRotate    = 0;
    private float phoneYRotate    = 0;
    private float phoneZRotate    = 0;

    Hardware robot = new Hardware();

    private float drive = .6f;
    private float fullDrive = 1;
    //private float BRDrive = 1f;

    @Override
    public void init()
    {
        //Initialize the hardware variables.
        //The init() method of the hardware class does all the work here
        robot.init(hardwareMap);


    }

    @Override
    public void loop()

    {
        if(gamepad1.a)
        {
            if (robot.launcherServo.getPosition() < 1) {
                robot.launcherServo.setPosition(robot.launcherServo.getPosition() + 0.01);
            }
        }
        if (gamepad1.b)
        {
            if (robot.launcherServo.getPosition() > 0) {
                robot.launcherServo.setPosition(robot.launcherServo.getPosition() - 0.01);
            }
        }

        if(gamepad1.right_bumper)
        {
            robot.launcherMotor.setPower(1);
        }
        else if(gamepad1.left_bumper)
        {
            robot.launcherMotor.setPower(-1);
        }
        else
        {
            robot.launcherMotor.setPower(0);
        }

        if(gamepad1.y)
        {
            robot.intakeMotor.setPower(1);
        }
        else if(gamepad1.x)
        {
            robot.intakeMotor.setPower(-1);
        }
        else
        {
            robot.intakeMotor.setPower(0);
        }

        if(gamepad1.dpad_up)
        {
            ResetVuforiaValues();

            VuforiaShenanigans();

            telemetry.addData("PosX", vuforiaPosX);
            telemetry.addData("PosY", vuforiaPosY);
            telemetry.addData("PosZ", vuforiaPosZ);

            telemetry.addData("AngX", vuforiaAngX);
            telemetry.addData("AngY", vuforiaAngY);
            telemetry.addData("AngZ", vuforiaAngZ);
            telemetry.update();


            encoderDrive(DRIVE_SPEED, 'b', (int) vuforiaPosX - 10, 15);
            driveTimer.reset();
            driveCheck = false;

            if(vuforiaPosY < 0 && vuforiaPosY > RED_GOAL_Y_SHIFT)
            {
                encoderDrive(DRIVE_SPEED, 'r', Math.abs((int) vuforiaPosY - RED_GOAL_Y_SHIFT), 15);
            }
            else if(vuforiaPosY < 0 && vuforiaPosY < RED_GOAL_Y_SHIFT)
            {
                encoderDrive(DRIVE_SPEED, 'l', Math.abs((int) vuforiaPosY - RED_GOAL_Y_SHIFT), 15);
            }
            else if(vuforiaPosY > 0 && vuforiaPosY > BLUE_GOAL_Y_SHIFT)
            {
                encoderDrive(DRIVE_SPEED, 'r', Math.abs((int) vuforiaPosY - BLUE_GOAL_Y_SHIFT), 15);
            }
            else if(vuforiaPosY > 0 && vuforiaPosY < BLUE_GOAL_Y_SHIFT)
            {
                encoderDrive(DRIVE_SPEED, 'l', Math.abs((int) vuforiaPosY - BLUE_GOAL_Y_SHIFT), 15);
            }
        }

        mecanumMove();

    }

    public void mecanumMove()
    {
        //variables
        double r = Math.hypot(-gamepad1.left_stick_x, gamepad1.left_stick_y);
        double robotAngle = Math.atan2(gamepad1.left_stick_y, -gamepad1.left_stick_x) - Math.PI / 4;
        double rightX = gamepad1.right_stick_x;
        final double v1 = r * Math.cos(robotAngle) - rightX;
        final double v2 = r * Math.sin(robotAngle) + rightX;
        final double v3 = r * Math.sin(robotAngle) - rightX;
        final double v4 = r * Math.cos(robotAngle) + rightX;

        robot.fLMotor.setPower(-fullDrive * v1);
        robot.fRMotor.setPower(-fullDrive * v2);
        robot.bLMotor.setPower(-fullDrive * v3);
        robot.bRMotor.setPower(-fullDrive * v4);

//        telemetry.addData("fLPower", -drive * v1);
//        telemetry.addData("fRPower", -drive * v2);
//        telemetry.addData("bLPower", -drive * v3);
//        telemetry.addData("bRPower", -drive * v4);
    }

    public void VuforiaShenanigans() {
        /*
         * Retrieve the camera we are to use.
         */
        webcamName = hardwareMap.get(WebcamName.class, "Webcam 1");

        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         * We can pass Vuforia the handle to a camera preview resource (on the RC phone);
         * If no camera monitor is desired, use the parameter-less constructor instead (commented out below).
         */
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;

        /**
         * We also indicate which camera on the RC we wish to use.
         */
        parameters.cameraName = webcamName;

        // Make sure extended tracking is disabled for this example.
        parameters.useExtendedTracking = false;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Load the data sets for the trackable objects. These particular data
        // sets are stored in the 'assets' part of our application.
        VuforiaTrackables targetsUltimateGoal = this.vuforia.loadTrackablesFromAsset("UltimateGoal");
        VuforiaTrackable blueTowerGoalTarget = targetsUltimateGoal.get(0);
        blueTowerGoalTarget.setName("Blue Tower Goal Target");
        VuforiaTrackable redTowerGoalTarget = targetsUltimateGoal.get(1);
        redTowerGoalTarget.setName("Red Tower Goal Target");
        VuforiaTrackable redAllianceTarget = targetsUltimateGoal.get(2);
        redAllianceTarget.setName("Red Alliance Target");
        VuforiaTrackable blueAllianceTarget = targetsUltimateGoal.get(3);
        blueAllianceTarget.setName("Blue Alliance Target");
        VuforiaTrackable frontWallTarget = targetsUltimateGoal.get(4);
        frontWallTarget.setName("Front Wall Target");

        // For convenience, gather together all the trackable objects in one easily-iterable collection */
        List<VuforiaTrackable> allTrackables = new ArrayList<VuforiaTrackable>();
        allTrackables.addAll(targetsUltimateGoal);

        /**
         * In order for localization to work, we need to tell the system where each target is on the field, and
         * where the phone resides on the robot.  These specifications are in the form of <em>transformation matrices.</em>
         * Transformation matrices are a central, important concept in the math here involved in localization.
         * See <a href="https://en.wikipedia.org/wiki/Transformation_matrix">Transformation Matrix</a>
         * for detailed information. Commonly, you'll encounter transformation matrices as instances
         * of the {@link OpenGLMatrix} class.
         *
         * If you are standing in the Red Alliance Station looking towards the center of the field,
         *     - The X axis runs from your left to the right. (positive from the center to the right)
         *     - The Y axis runs from the Red Alliance Station towards the other side of the field
         *       where the Blue Alliance Station is. (Positive is from the center, towards the BlueAlliance station)
         *     - The Z axis runs from the floor, upwards towards the ceiling.  (Positive is above the floor)
         *
         * Before being transformed, each target image is conceptually located at the origin of the field's
         *  coordinate system (the center of the field), facing up.
         */

        //Set the position of the perimeter targets with relation to origin (center of field)
        redAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, -halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 180)));

        blueAllianceTarget.setLocation(OpenGLMatrix
                .translation(0, halfField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, 0)));
        frontWallTarget.setLocation(OpenGLMatrix
                .translation(-halfField, 0, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0 , 90)));

        // The tower goal targets are located a quarter field length from the ends of the back perimeter wall.
        blueTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0 , -90)));
        redTowerGoalTarget.setLocation(OpenGLMatrix
                .translation(halfField, -quadField, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, 90, 0, -90)));

        //
        // Create a transformation matrix describing where the phone is on the robot.
        //
        // NOTE !!!!  It's very important that you turn OFF your phone's Auto-Screen-Rotation option.
        // Lock it into Portrait for these numbers to work.
        //
        // Info:  The coordinate frame for the robot looks the same as the field.
        // The robot's "forward" direction is facing out along X axis, with the LEFT side facing out along the Y axis.
        // Z is UP on the robot.  This equates to a bearing angle of Zero degrees.
        //
        // The phone starts out lying flat, with the screen facing Up and with the physical top of the phone
        // pointing to the LEFT side of the Robot.
        // The two examples below assume that the camera is facing forward out the front of the robot.

        // We need to rotate the camera around it's long axis to bring the correct camera forward.
        if (CAMERA_CHOICE == BACK) {
            phoneYRotate = -90;
        } else {
            phoneYRotate = 90;
        }

        // Rotate the phone vertical about the X axis if it's in portrait mode
        if (PHONE_IS_PORTRAIT) {
            phoneXRotate = 90 ;
        }

        // Next, translate the camera lens to where it is on the robot.
        // In this example, it is centered (left to right), but forward of the middle of the robot, and above ground level.
        final float CAMERA_FORWARD_DISPLACEMENT  = 4.0f * mmPerInch;   // eg: Camera is 4 Inches in front of robot-center
        final float CAMERA_VERTICAL_DISPLACEMENT = 8.0f * mmPerInch;   // eg: Camera is 8 Inches above ground
        final float CAMERA_LEFT_DISPLACEMENT     = 0;     // eg: Camera is ON the robot's center line

        OpenGLMatrix robotFromCamera = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, YZX, DEGREES, phoneYRotate, phoneZRotate, phoneXRotate));

        /**  Let all the trackable listeners know where the phone is.  */
        for (VuforiaTrackable trackable : allTrackables) {
            ((VuforiaTrackableDefaultListener) trackable.getListener()).setPhoneInformation(robotFromCamera, parameters.cameraDirection);
        }

        // WARNING:
        // In this sample, we do not wait for PLAY to be pressed.  Target Tracking is started immediately when INIT is pressed.
        // This sequence is used to enable the new remote DS Camera Preview feature to be used with this sample.
        // CONSEQUENTLY do not put any driving commands in this loop.
        // To restore the normal opmode structure, just un-comment the following line:

        // waitForStart();

        // Note: To use the remote camera preview:
        // AFTER you hit Init on the Driver Station, use the "options menu" to select "Camera Stream"
        // Tap the preview window to receive a fresh image.

        targetsUltimateGoal.activate();
        runtime.reset();
        targetVisible = false;
        while (runtime.milliseconds() < VUFORIA_TIMEOUT_MS && !targetVisible) {

            // check all the trackable targets to see which one (if any) is visible.
            //targetVisible = false;
            for (VuforiaTrackable trackable : allTrackables) {
                if (((VuforiaTrackableDefaultListener)trackable.getListener()).isVisible()) {
                    telemetry.addData("Visible Target", trackable.getName());
                    targetVisible = true;

                    // getUpdatedRobotLocation() will return null if no new information is available since
                    // the last time that call was made, or if the trackable is not currently visible.
                    OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener)trackable.getListener()).getUpdatedRobotLocation();
                    if (robotLocationTransform != null) {
                        lastLocation = robotLocationTransform;
                    }
                    break;
                }
            }

            // Provide feedback as to where the robot is located (if we know).
            if (targetVisible) {
                // express position (translation) of robot in inches.
                VectorF translation = lastLocation.getTranslation();
                telemetry.addData("Pos (in)", "{X, Y, Z} = %.1f, %.1f, %.1f",
                        translation.get(0) / mmPerInch, translation.get(1) / mmPerInch, translation.get(2) / mmPerInch);

                vuforiaPosX = translation.get(0) / mmPerInch;
                vuforiaPosY = translation.get(1) / mmPerInch;
                vuforiaPosZ = translation.get(2) / mmPerInch;


                // express the rotation of the robot in degrees.
                Orientation rotation = Orientation.getOrientation(lastLocation, EXTRINSIC, XYZ, DEGREES);
                telemetry.addData("Rot (deg)", "{Roll, Pitch, Heading} = %.0f, %.0f, %.0f", rotation.firstAngle, rotation.secondAngle, rotation.thirdAngle);

                vuforiaAngX = rotation.firstAngle;
                vuforiaAngY = rotation.secondAngle;
                vuforiaAngZ = rotation.thirdAngle;
            }
            else {
                telemetry.addData("Visible Target", "none");
            }
            telemetry.update();
        }

        // Disable Tracking when we are done;
        //targetsUltimateGoal.deactivate();

    }

    public void ResetVuforiaValues()
    {
        vuforiaPosX = 0;
        vuforiaPosY = 0;
        vuforiaPosZ = 0;
        vuforiaAngX = 0;
        vuforiaAngY = 0;
        vuforiaAngZ = 0;
    }

    public void encoderDrive(double speed, char direction, double inches, double timeoutS) {

        robot.fLMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.fRMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.bLMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.bRMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.fLMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.fRMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.bLMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.bRMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        robot.fLMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.fRMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.bLMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.bRMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        int newFrontLeftTarget = 0;
        int newBackLeftTarget = 0;
        int newFrontRightTarget = 0;
        int newBackRightTarget = 0;

        int error = getError(speed);

        boolean directionIsTrue = true;

        // Ensure that the opmode is still active



            switch (direction) {
                case 'f':
                    newFrontLeftTarget = robot.fLMotor.getCurrentPosition() + (int)(inches * COUNTS_PER_INCH) - error;
                    newFrontRightTarget = robot.fRMotor.getCurrentPosition() + (int)(inches * COUNTS_PER_INCH)- error;
                    newBackLeftTarget = robot.bLMotor.getCurrentPosition() + (int)(inches * COUNTS_PER_INCH) - error;
                    newBackRightTarget = robot.bRMotor.getCurrentPosition() + (int)(inches * COUNTS_PER_INCH) - error;
                    robot.fLMotor.setTargetPosition(newFrontLeftTarget);
                    robot.fRMotor.setTargetPosition(newFrontRightTarget);
                    robot.bLMotor.setTargetPosition(newBackLeftTarget);
                    robot.bRMotor.setTargetPosition(newBackRightTarget);
                    break;
                case 'b':
                    newFrontLeftTarget = robot.fLMotor.getCurrentPosition() - (int)(inches * COUNTS_PER_INCH) + error;
                    newFrontRightTarget = robot.fRMotor.getCurrentPosition() - (int)(inches * COUNTS_PER_INCH) + error;
                    newBackLeftTarget = robot.bLMotor.getCurrentPosition() - (int)(inches * COUNTS_PER_INCH) + error;
                    newBackRightTarget = robot.bRMotor.getCurrentPosition() - (int)(inches * COUNTS_PER_INCH) + error;
                    robot.fLMotor.setTargetPosition(newFrontLeftTarget);
                    robot.fRMotor.setTargetPosition(newFrontRightTarget);
                    robot.bLMotor.setTargetPosition(newBackLeftTarget);
                    robot.bRMotor.setTargetPosition(newBackRightTarget);
                    break;
                case 'l':
                    newFrontLeftTarget = robot.fLMotor.getCurrentPosition() - (int)(inches * COUNTS_PER_INCH) + error;
                    newFrontRightTarget = robot.fRMotor.getCurrentPosition() + (int)(inches * COUNTS_PER_INCH) - error;
                    newBackLeftTarget = robot.bLMotor.getCurrentPosition() + (int)(inches * COUNTS_PER_INCH) - error;
                    newBackRightTarget = robot.bRMotor.getCurrentPosition() - (int)(inches * COUNTS_PER_INCH) + error;
                    robot.fLMotor.setTargetPosition(newFrontLeftTarget);
                    robot.fRMotor.setTargetPosition(newFrontRightTarget);
                    robot.bLMotor.setTargetPosition(newBackLeftTarget);
                    robot.bRMotor.setTargetPosition(newBackRightTarget);
                    break;
                case 'r':
                    newFrontLeftTarget = robot.fLMotor.getCurrentPosition() + (int)(inches * COUNTS_PER_INCH) - error;
                    newFrontRightTarget = robot.fRMotor.getCurrentPosition() - (int)(inches * COUNTS_PER_INCH) + error;
                    newBackLeftTarget = robot.bLMotor.getCurrentPosition() - (int)(inches * COUNTS_PER_INCH) + error;
                    newBackRightTarget = robot.bRMotor.getCurrentPosition() + (int)(inches * COUNTS_PER_INCH) - error;
                    robot.fLMotor.setTargetPosition(newFrontLeftTarget);
                    robot.fRMotor.setTargetPosition(newFrontRightTarget);
                    robot.bLMotor.setTargetPosition(newBackLeftTarget);
                    robot.bRMotor.setTargetPosition(newBackRightTarget);
                    break;
                default:
                    directionIsTrue = false;
            }
            // Determine new target position, and pass to motor controller

            // Turn On RUN_TO_POSITION
            robot.fLMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.fRMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.bLMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.bRMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            if (directionIsTrue) {
                robot.fLMotor.setPower(Math.abs(speed));
                robot.fRMotor.setPower(Math.abs(speed));
                robot.bLMotor.setPower(Math.abs(speed));
                robot.bRMotor.setPower(Math.abs(speed));
            }

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while ((directionIsTrue) &&
                    (runtime.seconds() < timeoutS) &&
                    (robot.fLMotor.isBusy() && robot.fRMotor.isBusy() && robot.bLMotor.isBusy() && robot.bRMotor.isBusy())) {

                // Display it for the driver.
//                telemetry.addData("Target Positions",  "Running to fL: %7d fR: %7d bL: %7d bR: %7d",
//                        newFrontLeftTarget,
//                        newFrontRightTarget,
//                        newBackLeftTarget,
//                        newBackRightTarget);
//                telemetry.addData("Current Positions",  "Running at fL: %7d fR: %7d bL: %7d bR: %7d",
//                        robot.fLMotor.getCurrentPosition(),
//                        robot.fRMotor.getCurrentPosition(),
//                        robot.bLMotor.getCurrentPosition(),
//                        robot.bRMotor.getCurrentPosition());
//                telemetry.update();

                if (Math.abs(newFrontLeftTarget - robot.fLMotor.getCurrentPosition()) < 100 ||
                        Math.abs(newFrontRightTarget - robot.fRMotor.getCurrentPosition()) < 50 ||
                        Math.abs(newBackLeftTarget - robot.bLMotor.getCurrentPosition()) < 100 ||
                        Math.abs(newBackRightTarget - robot.bRMotor.getCurrentPosition()) < 50) {
                    break;
                }
            }

            // Stop all motion;
            robot.fLMotor.setPower(0);
            robot.fRMotor.setPower(0);
            robot.bRMotor.setPower(0);
            robot.bLMotor.setPower(0);


            // Turn off RUN_TO_POSITIOn

        robot.fLMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.fRMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.bLMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.bRMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

            robot.fLMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.fRMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.bRMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.bLMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);




    }
    public int getError(double speed) { //me being stupid
        return (int) (speed * 200);
    }
}