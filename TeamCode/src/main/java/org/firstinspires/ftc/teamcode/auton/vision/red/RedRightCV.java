/*
 * Copyright (c) 2019 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.firstinspires.ftc.teamcode.auton.vision.red;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;

import org.firstinspires.ftc.teamcode.hardware.Robot2;
import org.firstinspires.ftc.teamcode.test.barcode.TestBarcodeRedRightPipeline;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvInternalCamera;
import org.openftc.easyopencv.OpenCvPipeline;

@Autonomous (name = "RedRightCV", group = "Barcode")
public class RedRightCV extends LinearOpMode {
    Robot2 prbot = new Robot2();
    OpenCvCamera webcam;

    TestBarcodeRedRightPipeline pipeline = new TestBarcodeRedRightPipeline(telemetry);

    int wait = 500;

    @Override
    public void runOpMode() {
        //initialize robot hardware
        prbot.init(hardwareMap, telemetry);

        prbot.drivetrain.setPower(.4);
        prbot.drivetrain.setTelemetry(telemetry);
        prbot.drivetrain.useBrake(true);
        prbot.outtake.neutralPosition();

        prbot.lift.init();
        prbot.lift.setTelemetry(telemetry);
        prbot.lift.useEncoders(true);
        prbot.lift.useBrake(true);
        prbot.lift.setMaxPower(.3);

        //FOR THE WEBCAM
        /*
         * Instantiate an OpenCvCamera object for the camera we'll be using.
         * Webcam stream goes to RC phone
         */
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "webcam 1"), cameraMonitorViewId);

        webcam.setPipeline(pipeline);

        /*
         * Open the connection to the camera device. New in v1.4.0 is the ability
         * to open the camera asynchronously which allows faster init time, and
         * better behavior when pressing stop during init (i.e. less of a chance
         * of tripping the stuck watchdog)
         */
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                /*
                 * Tell the webcam to start streaming images to us! Note that you must make sure
                 * the resolution you specify is supported by the camera. If it is not, an exception
                 * will be thrown.
                 *
                 * Keep in mind that the SDK's UVC driver (what OpenCvWebcam uses under the hood) only
                 * supports streaming from the webcam in the uncompressed YUV image format. This means
                 * that the maximum resolution you can stream at and still get up to 30FPS is 480p (640x480).
                 * Streaming at e.g. 720p will limit you to up to 10FPS and so on and so forth.
                 *
                 * Also, we specify the rotation that the webcam is used in. This is so that the image
                 * from the camera sensor can be rotated such that it is always displayed with the image upright.
                 * For a front facing camera, rotation is defined assuming the user is looking at the screen.
                 * For a rear facing camera or a webcam, rotation is defined assuming the camera is facing
                 * away from the user.
                 */
                //320px x 340px
                webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT);

                /*
                 * Specify the image processing pipeline we wish to invoke upon receipt
                 * of a frame from the camera. Note that switching pipelines on-the-fly
                 * (while a streaming session is in flight) *IS* supported.
                 */

                webcam.setPipeline(pipeline);
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("errorCode", errorCode);
            }
        });
        // Tell telemetry to update faster than the default 250ms period :)
        telemetry.setMsTransmissionInterval(20);

        telemetry.addLine("Waiting for start");
        telemetry.update();

        //Wait for the user to press start on the Driver Station

        waitForStart();


        //Manages Telemetry and stopping the stream
        while (opModeIsActive()) {
            /*
             * Send some stats to the telemetry
//             */
//            telemetry.addData("Frame Count", webcam.getFrameCount());
//            telemetry.addData("FPS", String.format("%.2f", webcam.getFps()));
//            telemetry.addData("Total frame time ms", webcam.getTotalFrameTimeMs());
//            telemetry.addData("Pipeline time ms", webcam.getPipelineTimeMs());
//            telemetry.addData("Overhead time ms", webcam.getOverheadTimeMs());
//            telemetry.addData("Theoretical max FPS", webcam.getCurrentPipelineMaxFps());

//            telemetry.addData("avg1", pipeline.getAvg1());
//            telemetry.addData("avg2", pipeline.getAvg2());
//            telemetry.addData("avg3", pipeline.getAvg3());
            sleep(2000);
            telemetry.addData("Analysis", pipeline.getAnalysis());
            telemetry.update();

            switch (pipeline.getAnalysis()) {
                case LEFT:
                    //forward
                    prbot.drivetrain.backward(12);
                    sleep(wait);
                    //turn
                    prbot.drivetrain.pointTurnLeft();
                    sleep(wait);
                    //backward
                    prbot.drivetrain.backward(20);
                    sleep(wait);
                    //turn
                    prbot.drivetrain.pointTurnLeft();
                    sleep(wait);
                    // forward
                    prbot.drivetrain.forward(2);
                    sleep(wait);

                    //drop lift
                    //prbot.lift.setLevel(0);
                    //prbot.lift.updateLevel();
                    //sleep(wait);

                    prbot.outtake.backPosition();
                    sleep(2000);

                    prbot.outtake.neutralPosition();
                    //prbot.lift.setLevel(0);
                    //prbot.lift.updateLevel();
                    sleep(wait);

                    prbot.drivetrain.backward(2);
                    sleep(wait);

                    prbot.drivetrain.pointTurnLeft();
                    sleep(wait);

                    prbot.drivetrain.setPower(.9);
                    prbot.drivetrain.backward(60);
                    sleep(wait);
                    break;
                case CENTER:
                    //forward
                    prbot.drivetrain.backward(12);
                    sleep(wait);
                    //turn
                    prbot.drivetrain.pointTurnLeft();
                    sleep(wait);
                    //backward
                    prbot.drivetrain.backward(20);
                    sleep(wait);
                    //turn
                    prbot.drivetrain.pointTurnLeft();
                    sleep(wait);
                    // forward
                    prbot.drivetrain.forward(3);
                    sleep(wait);

                    //drop lift
                    prbot.lift.setLevel(1);
                    prbot.lift.updateLevel();
                    sleep(wait);

                    prbot.outtake.backPosition();
                    sleep(2000);

                    prbot.outtake.neutralPosition();
                    prbot.lift.setLevel(0);
                    prbot.lift.updateLevel();
                    sleep(wait);

                    prbot.drivetrain.backward(3);
                    sleep(wait);

                    prbot.drivetrain.pointTurnLeft();
                    sleep(wait);

                    prbot.drivetrain.setPower(.9);
                    prbot.drivetrain.backward(60);
                    sleep(wait);
                    break;
                case RIGHT:
                    //forward
                    prbot.drivetrain.backward(12);
                    sleep(wait);
                    //turn
                    prbot.drivetrain.pointTurnLeft();
                    sleep(wait);
                    //backward
                    prbot.drivetrain.backward(20);
                    sleep(wait);
                    //turn
                    prbot.drivetrain.pointTurnLeft();
                    sleep(wait);
                    // forward
                    prbot.drivetrain.forward(4);
                    sleep(wait);

                    //drop lift
                    prbot.lift.setLevel(2);
                    prbot.lift.updateLevel();
                    sleep(wait);

                    prbot.outtake.backPosition();
                    sleep(2000);

                    prbot.outtake.neutralPosition();
                    prbot.lift.setLevel(0);
                    prbot.lift.updateLevel();
                    sleep(wait);

                    prbot.drivetrain.backward(4);
                    sleep(wait);

                    prbot.drivetrain.pointTurnLeft();
                    sleep(wait);

                    prbot.drivetrain.setPower(.9);
                    prbot.drivetrain.backward(60);
                    sleep(wait);
                    break;
            }

            //reminder to use the KNO3 auto transitioner once this code is working
            webcam.stopStreaming();
            webcam.closeCameraDevice();
            break;
        }
    }
}