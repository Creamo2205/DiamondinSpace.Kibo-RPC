package jp.jaxa.iss.kibo.rpc.defaultapk;

//astrobee
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
//android
import android.graphics.Bitmap;
import android.util.Log;

//zxing
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {

    public String info = "";

    @Override
    protected void runPlan1(){
        //simulation scan qrcode
        simscanner();

        // astrobee is undocked and the mission starts by using this command
        api.startMission();
        //example

        // astrobee is undocked and the mission start
        moveToWrapper(11.21, -9.8, 4.79 , 0, 0, -0.707, 0.707);

        //scan qrcode
        scanner(api.getBitmapNavCam());

        api.sendDiscoveredQR(info);

        //TODO move to point A '

        /* irradiate the laser */
        api.laserControl(true);

        // take snapshots
        api.takeSnapshot();

        // move to the rear of Bay7
        moveToWrapper(10.6, -8.0, 4.5, 0, 0, -0.707, 0.707);

        // Send mission completion
        api.reportMissionCompletion();

    }

    public void simscanner(){
        Log.d("LOG-DEBUGGER","START FAKE SCANNER METHOD");
        Bitmap _qrcode = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);; //let me edit your code
        int[] _intArray = new int[_qrcode.getHeight() * _qrcode.getWidth()];
        _qrcode.getPixels(_intArray, 0, _qrcode.getWidth(), 0, 0, _qrcode.getWidth(), _qrcode.getHeight());
        String _info = " ";

        LuminanceSource _source = new RGBLuminanceSource(_qrcode.getWidth(), _qrcode.getHeight(), _intArray);
        BinaryBitmap _binaryBitmap = new BinaryBitmap(new HybridBinarizer(_source));

        Reader _reader = new QRCodeReader();
        try {
            Log.d("LOG-DEBUGGER", "DECODE STARTED");
            _info = _reader.decode(_binaryBitmap).getText();
            Log.d("LOG-DEBUGGER", "DECODE FINISHED");
            Log.d("LOG-DEBUGGER", "INFO IS " + _info);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        Log.d("LOG-DEBUGGER","FINISH FAKE SCANNER METHOD");
    }

    public void scanner(Bitmap qrcode){
        Log.d("LOG-DEBUGGER","START SCANNER METHOD");
        int[] intArray = new int[qrcode.getWidth()*qrcode.getHeight()];
        qrcode.getPixels(intArray, 0, qrcode.getWidth(), 0, 0, qrcode.getWidth(), qrcode.getHeight());

        LuminanceSource source = new RGBLuminanceSource(qrcode.getWidth(), qrcode.getHeight(),intArray);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new QRCodeReader();
        try {
            Log.d("LOG-DEBUGGER", "DECODE STARTED");
            info = reader.decode(binaryBitmap).getText();
            Log.d("LOG-DEBUGGER","DECODE FINISHED");
            Log.d("LOG-DEBUGGER","INFO IS "+info);
        }
        catch (NotFoundException e) {
            e.printStackTrace();
            Log.d("LOG-DEBUGGER","NFEerror"+e.toString());
        }
        catch (ChecksumException e) {
            e.printStackTrace();
            Log.d("LOG-DEBUGGER","CEerror"+e.toString());
        }
        catch (FormatException e) {
            e.printStackTrace();
            Log.d("LOG-DEBUGGER","FEerror"+e.toString());
        }
        Log.d("LOG-DEBUGGER","FINISH SCANNER METHOD");
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
