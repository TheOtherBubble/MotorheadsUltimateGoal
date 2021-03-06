/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.Test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.teamcode.AutonDriving;
import org.firstinspires.ftc.teamcode.Hardware;

@Autonomous(name="Color Sensor Auton Test", group="Test")
//@Disabled
public class ColorSensorAutonTest extends AutonDriving {

    /* Declare OpMode members. */
    Hardware robot = new Hardware();   // Use a Pushbot's hardware
    private ElapsedTime     runtime = new ElapsedTime();

    static final double     FORWARD_SPEED = 0.4;

    private int leftAlpha;
    private int rightAlpha;

    boolean rightOverLine = false;
    boolean leftOverLine = false;

    boolean rightOverFirst;

    private boolean objectInVision = false;

    private static final String TFOD_MODEL_ASSET = "UltimateGoal.tflite";
    private static final String LABEL_FIRST_ELEMENT = "Quad";
    private static final String LABEL_SECOND_ELEMENT = "Single";

    private VuforiaLocalizer vuforia;

    private TFObjectDetector tfod;

    private String ringLabel = " ";

    public static final String VUFORIA_KEY =
            "AYy6NYn/////AAABmTW3q+TyLUMbg/IXWlIG3BkMMq0okH0hLmwj3CxhPhvUlEZHaOAmESqfePJ57KC2g6UdWLN7OYvc8ihGAZSUJ2JPWAsHQGv6GUAj4BlrMCjHvqhY0w3tV/Azw2wlPmls4FcUCRTzidzVEDy+dtxqQ7U5ZtiQhjBZetAcnLsCYb58dgwZEjTx2+36jiqcFYvS+FlNJBpbwmnPUyEEb32YBBZj4ra5jB0v4IW4wYYRKTNijAQKxco33VYSCbH0at99SqhXECURA55dtmmJxYpFlT/sMmj0iblOqoG/auapQmmyEEXt/T8hv9StyirabxhbVVSe7fPsAueiXOWVm0kCPO+KN/TyWYB9Hg/mSfnNu9i9";

    public ColorSensorAutonTest() {
    }

    @Override
    public void runOpMode() {

        robot.init(hardwareMap);

        waitForStart();

        normalDrive(0.29);

        do {

            rightOverLine = robot.rightColor.alpha() > 300;
            leftOverLine = robot.leftColor.alpha() > 300;

            if (rightOverLine) {
                rightOverFirst = true;
            }
            if (leftOverLine) {
                rightOverFirst = false;
            }

        } while (!(rightOverLine || leftOverLine) && opModeIsActive());

        normalDrive(0);

        if (rightOverFirst) {
            if (rightOverLine) {
                robot.fLMotor.setPower(0.05);
                robot.bLMotor.setPower(0.05);
                do {
                    leftOverLine = robot.leftColor.alpha() > 300;
                } while (!leftOverLine && opModeIsActive());
                normalDrive(0);
            }
            else {
                robot.fRMotor.setPower(-0.05);
                robot.bRMotor.setPower(-0.05);
                do {
                    rightOverLine = robot.rightColor.alpha() > 300;
                } while (!rightOverLine && opModeIsActive());
                normalDrive(0);
                robot.fLMotor.setPower(0.05);
                robot.bLMotor.setPower(0.05);
                do {
                    leftOverLine = robot.leftColor.alpha() > 300;
                } while (!leftOverLine && opModeIsActive());
                normalDrive(0);
            }
        }
        else {
            if (leftOverLine) {
                robot.fRMotor.setPower(0.05);
                robot.bRMotor.setPower(0.05);
                do {
                    rightOverLine = robot.rightColor.alpha() > 300;
                } while (!rightOverLine && opModeIsActive());
                normalDrive(0);
            }
            else {
                robot.fLMotor.setPower(-0.05);
                robot.bLMotor.setPower(-0.05);
                do {
                    leftOverLine = robot.leftColor.alpha() > 300;
                } while (!leftOverLine && opModeIsActive());
                normalDrive(0);
                robot.fRMotor.setPower(0.05);
                robot.bRMotor.setPower(0.05);
                do {
                    rightOverLine = robot.rightColor.alpha() > 300;
                } while (!rightOverLine && opModeIsActive());
                normalDrive(0);
            }
        }
        normalDrive(-0.2);
        sleep(700);
        normalDrive(0);
        telemetry.addLine("Goodbye!");
    }
    private void normalDrive(double power) {
        robot.fRMotor.setPower(power);
        robot.bRMotor.setPower(power);
        robot.fLMotor.setPower(power);
        robot.bLMotor.setPower(power);
    }
}