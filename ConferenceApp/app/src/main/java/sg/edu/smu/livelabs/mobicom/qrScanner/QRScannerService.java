package sg.edu.smu.livelabs.mobicom.qrScanner;

import android.content.Context;
import android.content.Intent;

import com.google.zxing.integration.android.IntentIntegrator;

import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.MainActivity;

/**
 * Created by john on 28/1/16.
 */
public class QRScannerService {
    private static final QRScannerService instance = new QRScannerService();
    public static QRScannerService getInstance(){return instance;}

    private Context context;
    private Action1<String> requestQRCallback;

    public void init(Context context){
        this.context = context;
    }

    // return pathFile
    public void requestScan(final  MainActivity mainActivity, final Action1<String> callback){
        requestQRCallback = callback;
        IntentIntegrator integrator = new IntentIntegrator(mainActivity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    public void returnFromScanner(MainActivity activity, Intent data){
        context = activity;
        if (requestQRCallback != null) {

            requestQRCallback.call(data.getStringExtra("SCAN_RESULT"));
        }
    }

//    public void showDialog(final Context context, String msg){
//        final Dialog dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.dialog_box_icebreaker);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//        Button closeBtn = (Button) dialog.findViewById(R.id.closeBtn);
//        EditText nameTV = (EditText) dialog.findViewById(R.id.name);
//        ImageView avatarIV = (ImageView) dialog.findViewById(R.id.avatar_image);
//        messageTV.setText(msg);
//
//        dialog.show();
//
//        closeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//    }

}
