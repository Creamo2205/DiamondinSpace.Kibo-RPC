package jp.jaxa.iss.kibo.rpc.sampleapk;
//astrobee
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
//opencv
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
//android
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;
//java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
//zxing

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import static android.content.ContentValues.TAG;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {

    private Object NotFoundException;

    @Override
    protected void runPlan1(){
        // astrobee is undocked and the mission starts by using this command
        api.startMission();
        //example

        // astrobee is undocked and the mission starts
        moveToWrapper(11.21, -9.8, 4.65, 0, 0, -0.707, 0.707);

        //scan qrcode
        Bitmap qrcode = api.getBitmapNavCam();
        int[] intArray = new int[530*530];
        qrcode.getPixels(intArray, 0, qrcode.getWidth(), 0, 0, qrcode.getWidth(), qrcode.getHeight());

        LuminanceSource source = new RGBLuminanceSource(qrcode.getWidth(), qrcode.getHeight(),intArray);

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new QRCodeReader();

        String info = "";

        try {

            Result result = Reader.decode(binaryBitmap);
            info =  result.getMessage();
            Log.d("LOG-DEBUGGER", info);

        }
        catch (NotFoundException e) {
            Log.d(TAG, "Code Not Found");
            e.printStackTrace();
        }


        /* irradiate the laser */
        api.laserControl(true);

        // take snapshots
        api.takeSnapshot();

        // move to the rear of Bay7
        moveToWrapper(10.6, -8.0, 4.5, 0, 0, -0.707, 0.707);

        // Send mission completion
        api.reportMissionCompletion();

    }

    @Override
    protected void runPlan2(){
        // write here your plan 2
    }

    @Override
    protected void runPlan3(){
        // write here your plan 3
    }

    // You can add your method
    private void moveToWrapper(double pos_x, double pos_y, double pos_z,
                               double qua_x, double qua_y, double qua_z,
                               double qua_w){

        final int LOOP_MAX = 5;
        final Point point = new Point(pos_x, pos_y, pos_z);
        final Quaternion quaternion = new Quaternion((float)qua_x, (float)qua_y,
                (float)qua_z, (float)qua_w);

        Result result = api.moveTo(point, quaternion, true);

        int loopCounter = 0;
        while(!result.hasSucceeded() || loopCounter < LOOP_MAX){
            result = api.moveTo(point, quaternion, true);
            ++loopCounter;
        }
    }

}

