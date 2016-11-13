package sg.edu.smu.livelabs.mobicom;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.flyco.animation.SlideEnter.SlideTopEnter;
import com.flyco.animation.SlideExit.SlideBottomExit;
import com.flyco.dialog.listener.OnBtnClickL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.functions.Action0;
import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.views.MyDialog;

/**
 * Created by smu on 21/1/16.
 */
public class UIHelper {
    private Typeface normalFont;
    private Typeface boldFont;
    private Typeface italicFont;
    private Typeface exo2LightFont;
    private Typeface boldExoFont;
    private Context context;
    private MyDialog alertDialog;
    private MaterialDialog progressDialog;
    private boolean alertDialogCancellable;
    private static UIHelper instance;

    public static UIHelper getInstance(){
        if (instance == null){
            instance = new UIHelper();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public void setTypeface(TextView... textViews) {
        for (TextView textView : textViews) {
            textView.setTypeface(getNormalFont());
        }
    }

    public void setBoldTypeface(TextView textView) {
        textView.setTypeface(getBoldFont());
    }

    public void setItalicTypeface(TextView textView) {
        textView.setTypeface(getItalicFont());
    }

    public void setExo2BoldTypeFace(TextView... textViews) {
        for (TextView textView : textViews) {
            textView.setTypeface(getBoldExoFont());
        }
    }

    public void setExo2LightTypeFace(TextView... textViews) {
        for (TextView textView : textViews) {
            textView.setTypeface(getExo2LightFont());
        }
    }

    public void setExo2TypeFace(TextView textView, boolean bold) {
        if (textView != null) {
            if (bold) {
                textView.setTypeface(getBoldExoFont());
            } else {
                textView.setTypeface(getExo2LightFont());
            }
        }
    }

    public Typeface getExo2LightFont() {
        if (exo2LightFont == null) {
            exo2LightFont = Typeface.createFromAsset(context.getAssets(),
                    "fonts/Exo2-Light.ttf");
        }
        return exo2LightFont;
    }

    public Typeface getNormalFont() {
        if (normalFont == null) {
            normalFont = Typeface.createFromAsset(context.getAssets(),
                    "fonts/Calibri.ttf");
        }
        return this.normalFont;
    }

    public Typeface getBoldFont() {
        if (boldFont == null) {
            boldFont = Typeface.createFromAsset(context.getAssets(),
                    "fonts/Calibri_Bold.ttf");
        }
        return this.boldFont;
    }

    public Typeface getItalicFont() {
        if (italicFont == null) {
            italicFont = Typeface.createFromAsset(context.getAssets(),
                    "fonts/Calibri_Italic.ttf");
        }
        return this.italicFont;
    }

    public Typeface getBoldExoFont() {
        if (boldExoFont == null) {
            boldExoFont = Typeface.createFromAsset(context.getAssets(), "fonts/Exo2.0-Bold.otf");
        }
        return boldExoFont;
    }


    public  void applyExoFont(View view) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            if (tv.getTypeface() == null || !tv.getTypeface().isBold()) {
                setExo2TypeFace(tv, false);
            } else {
                setExo2TypeFace(tv, true);
            }
        } else {
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0, count = group.getChildCount(); i < count; i++) {
                    View childView = group.getChildAt(i);
                    applyExoFont(childView);
                }
            }
        }
    }

    public  void applyNormalFont(View view) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            if (tv.getTypeface() == null || !tv.getTypeface().isBold()) {
                setTypeface(tv);
            } else {
                setBoldTypeface(tv);
            }
        } else {
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0, count = group.getChildCount(); i < count; i++) {
                    View childView = group.getChildAt(i);
                    applyNormalFont(childView);
                }
            }
        }
    }

    public void forceClosePopup(){
        if (alertDialog != null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }

    }

    public void showAlert(Context context, String message) {
        showAlert(context, context.getString(R.string.app_name), message, true);
    }

    public void showAlert(Context context, String message, boolean cancelable) {
        showAlert(context, context.getString(R.string.app_name), message, cancelable);
    }

    public void showAlert(final Context context, final String title, final String message, final boolean cancelable) {
        try {
            if (alertDialog != null && alertDialog.isShowing() && !alertDialogCancellable) {
                Handler h = new Handler(Looper.getMainLooper());
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showAlert(context, title, message, cancelable);
                    }
                }, 500);
            } else {
                dismissAlertDialog();
                alertDialogCancellable = cancelable;
                alertDialog = new MyDialog(context)
                        .style(MyDialog.STYLE_TWO)
                        .btnNum(1)
                        .title(title)
                        .content(message)
                        .btnText("OK")
                        .showAnim(SlideTopEnter.class.newInstance())
                        .dismissAnim(SlideBottomExit.class.newInstance());
                alertDialog.setCanceledOnTouchOutside(cancelable);
                alertDialog.show();
                alertDialog.setOnBtnClickL(new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        alertDialog.dismiss();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(App.APP_TAG, e.toString());
        }
    }

    public void showAlert(final Context context, final String title, final String message, final boolean cancelable, final Action0 action) {
        try {
            if (alertDialog != null && alertDialog.isShowing() && !alertDialogCancellable) {
                Handler h = new Handler(Looper.getMainLooper());
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showAlert(context, title, message, cancelable);
                    }
                }, 500);
            } else {
                dismissAlertDialog();
                alertDialogCancellable = cancelable;
                alertDialog = new MyDialog(context)
                        .style(MyDialog.STYLE_TWO)
                        .btnNum(1)
                        .title(title)
                        .content(message)
                        .btnText("OK")
                        .showAnim(SlideTopEnter.class.newInstance())
                        .dismissAnim(SlideBottomExit.class.newInstance());
                alertDialog.setCanceledOnTouchOutside(cancelable);
                alertDialog.show();
                alertDialog.setOnBtnClickL(new OnBtnClickL() {
                    @Override
                    public void onBtnClick() {
                        alertDialog.dismiss();
                        action.call();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(App.APP_TAG, e.toString());
        }
    }

    public void showProgressDialog(Context context, String message, boolean cancelable) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setContent(message);
            progressDialog.setCancelable(cancelable);
        } else {
            try {
                progressDialog = new MaterialDialog.Builder(context)
                        .title(context.getString(R.string.app_name))
                        .content(message)
                        .typeface(getExo2LightFont(), getExo2LightFont())
                        .progress(true, 0)
                        .cancelable(cancelable)
                        .cancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                progressDialog = null;
                            }
                        })
                        .show();
            } catch (Exception e) {
                Log.e(App.APP_TAG, e.toString());
            }
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {}
            progressDialog = null;
        }
    }

    public void dismissAlertDialog() {
        if (alertDialog != null) {
            try {
                alertDialog.dismiss();
            } catch (Exception e) {}
            alertDialog = null;
        }
    }

    public void showConfirmAlert(Context context, String title, String message, String positiveText,
                                 String negativeText, final Action0 positiveAction, final Action0 negativeAction){
        dismissAlertDialog();
        try {
            alertDialog = new MyDialog(context)
                    .style(MyDialog.STYLE_TWO)
                    .title(title)
                    .content(message)
                    .showAnim(SlideTopEnter.class.newInstance())
                    .dismissAnim(SlideBottomExit.class.newInstance())
                    .btnText(negativeText, positiveText);
            alertDialog.setOnBtnClickL(new OnBtnClickL() {
                @Override
                public void onBtnClick() {
                    alertDialog.dismiss();
                    negativeAction.call();
                }
            }, new OnBtnClickL() {
                @Override
                public void onBtnClick() {
                    alertDialog.dismiss();
                    positiveAction.call();
                }
            });
            alertDialog.show();
        } catch (Exception e) {
            Log.e(App.APP_TAG, e.toString());
        }

    }

    public void showNoneAnimaConfirmAlert(Context context, String title, String message, String positiveText,
                                 String negativeText, final Action0 positiveAction, final Action0 negativeAction){
        dismissAlertDialog();
        try {
            alertDialog = new MyDialog(context)
                    .style(MyDialog.STYLE_TWO)
                    .title(title)
                    .content(message)
                    .showAnim(SlideTopEnter.class.newInstance())
                    .btnText(negativeText, positiveText);
            alertDialog.setOnBtnClickL(new OnBtnClickL() {
                @Override
                public void onBtnClick() {
                    alertDialog.dismiss();
                    negativeAction.call();
                }
            }, new OnBtnClickL() {
                @Override
                public void onBtnClick() {
                    alertDialog.dismiss();
                    positiveAction.call();
                }
            });
            alertDialog.show();
        } catch (Exception e) {
            Log.e(App.APP_TAG, e.toString());
        }

    }


    public void showAlertBeacon(final Context context, final String title, final String message, final boolean cancelable,
                                final Action0 skipAction, final Action0 openAction) {
        try {

            dismissAlertDialog();
            alertDialogCancellable = cancelable;
            alertDialog = new MyDialog(context)
                    .style(MyDialog.STYLE_TWO)
                    .title(title)
                    .content(message)
                    .btnText("Skip It", "OK")
                    .showAnim(SlideTopEnter.class.newInstance())
                    .dismissAnim(SlideBottomExit.class.newInstance());
            alertDialog.setCanceledOnTouchOutside(cancelable);

            alertDialog.setOnBtnClickL(new OnBtnClickL() {
                                           @Override
                                           public void onBtnClick() {
                                               alertDialog.dismiss();
                                               skipAction.call();
                                           }
                                       }, new OnBtnClickL() {
                                           @Override
                                           public void onBtnClick() {
                                               alertDialog.dismiss();
                                               openAction.call();
                                           }
                                       }
            );

            alertDialog.show();

        } catch (Exception e) {
            Log.e(App.APP_TAG, e.toString());
        }
    }

    public void showPaperBeacon(final Context context, final String url, final boolean cancelable,
                                final Action0 action) {
        try {

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);


            final ProgressDialog prDialog = new ProgressDialog(context);
            final WebView webView = new WebView(context);
            webView.loadUrl(url);
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    if (!prDialog.isShowing()) {
                        prDialog.setMessage("Loading in progress ...");
                        prDialog.show();
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if(prDialog!=null){
                        prDialog.dismiss();
                    }
                }
            });

            dialog.setView(webView);
            dialog.setCancelable(cancelable);
            dialog.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    action.call();
                }
            });

            dialog.show();

        } catch (Exception e) {
            Log.e(App.APP_TAG, e.toString());
        }
    }

    public void openPDF(final String url, final MainActivity mainActivity){
        mainActivity.checkAndRequirePermissions(
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                },
                new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            downloadAndOpenPDF(url, mainActivity);
                        }catch (Exception e){
                            String newUrl = "https://docs.google.com/gview?embedded=true&url="+url;
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newUrl));
                            mainActivity.startActivity(browserIntent);
                            showConfirmAlert(mainActivity,
                                    mainActivity.getString(R.string.app_name),
                                    mainActivity.getString(R.string.download_drive),
                                    "Yes, Please", "No, Thanks", new Action0() {
                                        @Override
                                        public void call() {
                                            try {
                                                mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.docs")));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.docs")));
                                            }
                                        }
                                    }, new Action0() {
                                        @Override
                                        public void call() {

                                        }
                                    });
                        }
                    }
                });
    }

    public void openEpub(final String url, final MainActivity mainActivity){
        mainActivity.checkAndRequirePermissions(
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                },
                new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {

                            downloadAndOpenEPUB(url, mainActivity);
//            Intent epubIntent = new Intent(Intent.ACTION_VIEW);
//            epubIntent.setDataAndType(Uri.parse(url), ".epub/application/epub+zip");
//            mainActivity.startActivity(epubIntent);
                        }catch (Exception e){
                            Intent epubIntent1;
                            PackageManager manager = mainActivity.getPackageManager();
                            try {
                                epubIntent1 = manager.getLaunchIntentForPackage("com.flyersoft.moonreader");
                                if (epubIntent1 == null)
                                    throw new PackageManager.NameNotFoundException();

                                epubIntent1.setAction(Intent.ACTION_VIEW);
                                epubIntent1.setData(Uri.parse(url));
                                mainActivity.startActivity(epubIntent1);
                            } catch (PackageManager.NameNotFoundException e1) {
                                showConfirmAlert(mainActivity,
                                        mainActivity.getString(R.string.app_name),
                                        mainActivity.getString(R.string.download_moonreader),
                                        "Yes, Please", "No, Thanks", new Action0() {
                                            @Override
                                            public void call() {
                                                try {
                                                    mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.flyersoft.moonreader")));
                                                } catch (android.content.ActivityNotFoundException anfe) {
                                                    mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.flyersoft.moonreader")));
                                                }
                                            }
                                        }, new Action0() {
                                            @Override
                                            public void call() {

                                            }
                                        });
                            }
                        }
                    }
                });

    }

    public void downloadAndOpenPDF(final String url, final  MainActivity mainActivity) {
        if(url == null) return;
        new Thread(new Runnable() {
            public void run() {
                File file = downloadFile(url, "pdf");
                if(file == null){
                    UIHelper.getInstance().showAlert(context, "Please try again.");
                }
                try {
                    Uri path = Uri.fromFile(file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(path, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mainActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    showConfirmAlert(mainActivity,
                            mainActivity.getString(R.string.app_name),
                            mainActivity.getString(R.string.download_moonreader),
                            "Yes, Please", "No, Thanks", new Action0() {
                                @Override
                                public void call() {
                                    try {
                                        mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.flyersoft.moonreader")));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.flyersoft.moonreader")));
                                    }
                                }
                            }, new Action0() {
                                @Override
                                public void call() {

                                }
                            });
                }
            }
        }).start();

    }

    public void downloadAndOpenEPUB(final String url, final  MainActivity mainActivity) {
        if(url == null) return;
        new Thread(new Runnable() {
            public void run() {
                File file = downloadFile(url, "epub");
                if(file == null){
                    UIHelper.getInstance().showAlert(context, "Please try again.");
                }
                try {
                    Uri path = Uri.fromFile(file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(path, "application/epub+zip");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mainActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    showConfirmAlert(mainActivity,
                            mainActivity.getString(R.string.app_name),
                            mainActivity.getString(R.string.download_moonreader),
                            "Yes, Please", "No, Thanks", new Action0() {
                                @Override
                                public void call() {
                                    try {
                                        mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.flyersoft.moonreader")));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.flyersoft.moonreader")));
                                    }
                                }
                            }, new Action0() {
                                @Override
                                public void call() {

                                }
                            });
                }
            }
        }).start();

    }

    public File downloadFile(String dwnload_file_path, String type) {
        File file = null;
        try {

            URL url = new URL(dwnload_file_path);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            // connect
            urlConnection.connect();

            String[] tmp = dwnload_file_path.split("/");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date today = new Date();
            String fileName = "paper"+ df.format(today) +"." + type;
            if(tmp[tmp.length-1].contains("." + type)){
                fileName = tmp[tmp.length-1];
            }
            // set the path where we want to save the file
            File SDCardRoot = Environment.getExternalStorageDirectory();
            // create a new file, to save the downloaded file
            file = new File(SDCardRoot, fileName);

            FileOutputStream fileOutput = new FileOutputStream(file);

            // Stream used for reading the data from the internet
            InputStream inputStream = urlConnection.getInputStream();

            // create a buffer...
            byte[] buffer = new byte[1024 * 1024];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
            }
            // close the output stream when complete //
            fileOutput.close();

        } catch (final MalformedURLException e) {
        } catch (final IOException e) {
        } catch (final Exception e) {
        }
        catch (OutOfMemoryError e){
            System.gc();
        }
        return file;
    }

}
