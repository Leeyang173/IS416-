package sg.edu.smu.livelabs.mobicom.fileupload;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.MyActionSheetDialog;

/**
 * Created by smu on 28/1/16.
 */
public class UploadFileService {
    private static final UploadFileService instance = new UploadFileService();
    public static UploadFileService getInstance(){return instance;}

    private static final String IMAGES_FOLDER_PATH = "mobysis_image_store";
    private FileUploadApi fileUploadApi;
    private Context context;
    private File uploadingFile;
    private MaterialDialog fileUploadProgressDialog;
    private Action1<String> requestPhotoCallback;

    private boolean isUpload;
    private static final int MAX_IMAGE_SIZE = 512;
    public static String photoPlace;
    private Resources rs;

    public void init(Context context, FileUploadApi fileUploadApi){
        this.context = context;
        this.fileUploadApi = fileUploadApi;
    }

    // return pathFile
    public void requestPhoto(final  MainActivity mainActivity, final Action1<String> callback){
        requestUploadPhoto(mainActivity, callback);
        isUpload = false;
    }

    // return imageId
    public void uploadPhoto(final MainActivity activity, String fillPath, Bitmap bitmap, final Action1<String> callback) throws Exception {
        isUpload = true;
        requestPhotoCallback = callback;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        uploadingFile = new File(context.getFilesDir(), IMAGES_FOLDER_PATH);
        OutputStream outStream = new FileOutputStream(uploadingFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        outStream.flush();
        outStream.close();
        upload(activity);
    }

    //return imageId
    public void requestUploadPhoto(final MainActivity mainActivity, final Action1<String> callback) {
        if (mainActivity.isFinishing()) {
            return;
        }
        rs = mainActivity.getResources();
        context = mainActivity;
        requestPhotoCallback = callback;
        isUpload = true;
        mainActivity.checkAndRequirePermissions(
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                },
                new Action1<String>() {

                    @Override
                    public void call(String s) {
                        final MyActionSheetDialog sheetDialog = new MyActionSheetDialog(mainActivity, new String[]{"Take new photo", "Upload from gallery"}, null)
                                .cancelText("Cancel")
                                .isTitleShow(false);
                        sheetDialog.setOnOperItemClickL(new OnOperItemClickL() {
                            @Override
                            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                                switch (position) {
                                    case 0: //"Take new photo"
                                        photoPlace = "click";
                                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        if (takePictureIntent.resolveActivity(mainActivity.getPackageManager()) != null) {
                                            try {
                                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                                                        .format(new Date());
                                                String imageFileName = "JPEG_" + timeStamp + "_";
                                                File storageDir = Environment
                                                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                                uploadingFile = File.createTempFile(imageFileName, /* prefix */
                                                        ".jpg", /* suffix */
                                                        storageDir /* directory */
                                                );
                                                // Continue only if the File was successfully created
                                                if (uploadingFile != null) {
                                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                                            Uri.fromFile(uploadingFile));
                                                    mainActivity.startActivityForResult(takePictureIntent, MainActivity.REQUEST_IMAGE_CAPTURE);
                                                }
                                            } catch (IOException ex) {
                                                Log.d(App.APP_TAG, "Take photo cannot create file");
                                                // Error occurred while creating the File
                                            }

                                        }
                                        sheetDialog.dismiss();
                                        break;
                                    case 1: //"Upload from gallery"
                                        photoPlace = "library";
                                        Intent pictureActionIntent = new Intent(
                                                Intent.ACTION_GET_CONTENT, null);
                                        pictureActionIntent.setType("image/*");
                                        pictureActionIntent.putExtra("return-data", true);
                                        mainActivity.startActivityForResult(pictureActionIntent,
                                                MainActivity.REQUEST_IMAGE_GALLERY);
                                        sheetDialog.dismiss();
                                        break;
                                }
                            }
                        });

                        sheetDialog.show();
                    }
                });

    }

    public void saveImageToGallery(Context context, String title, Bitmap bitmap){
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            String imageFileName = title + "_" + timeStamp ;
            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, title);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            Uri url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            OutputStream fOut = cr.openOutputStream(url);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            UIHelper.getInstance().showAlert(context, rs.getString(R.string.saved_photo));
        } catch (Exception e) {
            UIHelper.getInstance().showAlert(context, rs.getString(R.string.error_occurred));
            Log.d(App.APP_TAG, e.toString());
        }
    }

    public String saveImageToStorage(Bitmap bitmap){
        try {
            File image = createImageFile();
            FileOutputStream fOut = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            return image.getAbsolutePath();
        } catch (Exception e) {
            Log.d(App.APP_TAG, e.toString());
        }
        return null;
    }

    public void downLoadFile(Context context, String url){
        try {
            File file = createImageFile();
            DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            Uri downloadUri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(
                    downloadUri);

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle("Demo")
                    .setDescription("Something useful. No, really.")
                    .setDestinationUri(Uri.fromFile(file));

            mgr.enqueue(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Bitmap getBitmap(String filePath){
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            opts.inDither = true;
            BitmapFactory.decodeFile(filePath, opts);
            int sourceHeight = opts.outHeight;
            int sourceWidth = opts.outWidth;
            opts.inSampleSize = 1;
            if (sourceHeight > sourceWidth){
                if (sourceWidth > MAX_IMAGE_SIZE){
                    opts.inSampleSize = Math.round(((float)sourceWidth)/MAX_IMAGE_SIZE);
                }
            } else {
                if (sourceHeight > MAX_IMAGE_SIZE){
                    opts.inSampleSize = Math.round(((float) sourceHeight) / MAX_IMAGE_SIZE);
                }
            }
            opts.inJustDecodeBounds = false;
            Bitmap source = BitmapFactory.decodeFile(filePath, opts);
            if (source == null){
                return null;
            }
            bitmap = source;
            ExifInterface exif = null;
            exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            if (orientation > 0){
                Matrix matrix = null;
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix = new Matrix();
                        matrix.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix = new Matrix();
                        matrix.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix = new Matrix();
                        matrix.postRotate(270);
                        break;
                    default:
                        break;
                }
                if (matrix != null){
                    bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                    if (!bitmap.sameAs(source)){
                        source.recycle();
                        source = null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch(OutOfMemoryError e){
            e.printStackTrace();
        }

        return bitmap;
    }

    public void uploadImageGallery(MainActivity activity, Intent data){
        context = activity;
        if (requestPhotoCallback != null) {
            try {
                Cursor cursor = activity.getContentResolver().query(data.getData(),
                        null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (idx > -1) {
                        String fileSrc = cursor.getString(idx);
                        File file = new File(fileSrc);
                        if (file.exists()) {
                            uploadingFile = file;
                            upload(activity);
                        } else {
                            UIHelper.getInstance().showAlert(activity, rs.getString(R.string.cannot_use_image));
                        }
                    } else {
                        Uri imageUri = data.getData();
                        // Will return "image:x*"
                        String wholeID = DocumentsContract.getDocumentId(imageUri);

                        // Split at colon, use second item in the array
                        String id = wholeID.split(":")[1];

                        String[] column = {MediaStore.Images.Media.DATA};

                        // where id is equal to
                        String sel = MediaStore.Images.Media._ID + "=?";

                        Cursor cursor2 = activity.getContentResolver().
                                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        column, sel, new String[]{id}, null);

                        String filePath = "";

                        int columnIndex = cursor2.getColumnIndex(column[0]);

                        if (cursor2.moveToFirst()) {
                            filePath = cursor2.getString(columnIndex);
                        }

                        cursor2.close();

                        File file = new File(filePath);
                        if (file.exists()) {
                            uploadingFile = file;
                            upload(activity);
                        } else {
                            UIHelper.getInstance().showAlert(activity, rs.getString(R.string.cannot_use_image));
                        }
                    }
                }
            } catch (Throwable t) {
                Log.d(App.APP_TAG, "Cannot upload image", t);
                UIHelper.getInstance().showAlert(activity, rs.getString(R.string.cannot_use_image));
            }
        }
    }

    //UPLOAD BASE64
    public void upload(final MainActivity activity){
        try{
            context = activity;
            if (!isUpload){
                dismissFileUploadProgressDialog();
                requestPhotoCallback.call(uploadingFile.getPath());
                return;
            }
            fileUploadProgressDialog = new MaterialDialog.Builder(context)
                    .title(rs.getString(R.string.app_name))
                    .content("Uploading photo...")
                    .progress(false, 100, true)
                    .cancelable(false)
                    .show();

            fileUploadProgressDialog.incrementProgress(10);
            Bitmap image = BitmapFactory.decodeFile(uploadingFile.getAbsolutePath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            fileUploadProgressDialog.incrementProgress(10);
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();
            fileUploadProgressDialog.incrementProgress(10);
            String imageStr = Base64.encodeToString(bytes, Base64.DEFAULT);
            fileUploadProgressDialog.incrementProgress(40);
            fileUploadApi.uploadFile1( imageStr, DatabaseService.getInstance().getMe().getUID())//TODO hard-code "10010412" testlivelabs4)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<FileUploadResponse>() {
                        @Override
                        public void call(FileUploadResponse fileUploadResponse) {
                            fileUploadProgressDialog.incrementProgress(10);
                            if ("success".equals(fileUploadResponse.status)) {
                                dismissFileUploadProgressDialog();
                                if (requestPhotoCallback != null) {
                                    requestPhotoCallback.call(fileUploadResponse.id);
                                }
                            } else {
                                dismissFileUploadProgressDialog();
                                UIHelper.getInstance().showAlert(context, activity.getResources().getString(R.string.cannot_upload_photo));
                            }
                            uploadingFile = null;
                            requestPhotoCallback = null;
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e(App.APP_TAG, "Cannot upload photo", throwable);
                            dismissFileUploadProgressDialog();
                            UIHelper.getInstance().showAlert(context, activity.getResources().getString(R.string.cannot_upload_photo));
                            uploadingFile = null;
                            requestPhotoCallback = null;
                        }
                    });
        }catch (Exception e){
            Log.e(App.APP_TAG, "UploadFileService.upload ", e );
            UIHelper.getInstance().showAlert(activity, rs.getString(R.string.cannot_use_image));
        }

    }


    //UPLOAD BINARY
//    public void upload(final MainActivity activity){
//        context = activity;
//        if (!isUpload){
//            dismissFileUploadProgressDialog();
//            requestPhotoCallback.call(uploadingFile.getPath());
//            return;
//        }
//        fileUploadProgressDialog = new MaterialDialog.Builder(context)
//                .title(rs.getString(R.string.app_name))
//                .content("Uploading photo...")
//                .progress(false, 100, true)
//                .cancelable(false)
//                .show();
//
//
//        fileUploadApi.uploadFile(new ProgressRequestBody(uploadingFile, new ProgressRequestBody.FileUploadProgressListener(){
//            @Override
//            public void onProgressUpdate(int percentage) {
//                fileUploadProgressDialog.setProgress(percentage);
//            }
//        }), Long.toString(DatabaseService.getInstance().getMe().getUID()))//TODO hard-code "10010412" testlivelabs4)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<FileUploadResponse>() {
//                    @Override
//                    public void call(FileUploadResponse fileUploadResponse) {
//                        if ("success".equals(fileUploadResponse.status)) {
//                            dismissFileUploadProgressDialog();
//                            if (requestPhotoCallback != null) {
//                                requestPhotoCallback.call(fileUploadResponse.id);
//                            }
//                        } else {
//                            dismissFileUploadProgressDialog();
//                            UIHelper.getInstance().showAlert(context, activity.getResources().getString(R.string.cannot_upload_photo));
//                        }
//                        uploadingFile = null;
//                        requestPhotoCallback = null;
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        Log.e(App.APP_TAG, "Cannot upload photo", throwable);
//                        dismissFileUploadProgressDialog();
//                        UIHelper.getInstance().showAlert(context, activity.getResources().getString(R.string.cannot_upload_photo));
//                        uploadingFile = null;
//                        requestPhotoCallback = null;
//                    }
//                });
//    }

    public void dismissFileUploadProgressDialog(){
        if(fileUploadProgressDialog != null && fileUploadProgressDialog.isShowing()){
            fileUploadProgressDialog.incrementProgress(50);
            fileUploadProgressDialog.dismiss();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(context.getFilesDir(), IMAGES_FOLDER_PATH);
        if (!storageDir.exists() && !storageDir.mkdir()) {
            throw new IOException("Failed to create storage dir: " + storageDir.getAbsolutePath());
        }
        File image = File.createTempFile(imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );

        Log.d(App.APP_TAG, image.getAbsolutePath().toString());
        return image;
    }

    public void saveImage(Context context, String title, Bitmap bitmap){
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            String imageFileName = title + "_" + timeStamp ;
            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, title);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            Uri url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            OutputStream fOut = cr.openOutputStream(url);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.saved_photo));
        } catch (Exception e) {
            UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.error_occurred));
            Log.d(App.APP_TAG, e.toString());
        }
    }

    public void loadImage(final ImageView imageView, final int drawableDefault,
                          final String avatarId, final int size){
        Picasso.with(context).cancelRequest(imageView);
        if (avatarId == null || avatarId.isEmpty()){
            imageView.setImageResource(drawableDefault);
        }
        try {
            Picasso.with(context)
                    .load(Util.getPhotoUrlFromId(avatarId, size))
                    .noFade()
                    .placeholder(drawableDefault)
                    .error(drawableDefault)
                    .into(imageView);
        }
        catch (OutOfMemoryError e){
            Log.d("AAA", "UploadFileService:"+e.toString());
            //reload the avatar picture if needed
        }
    }

    public void loadImageURL(final ImageView imageView, final int drawableDefault,
                          final String avatarURL, final int width, final int height){
        Picasso.with(context).cancelRequest(imageView);
        if (avatarURL == null || avatarURL.isEmpty()){
            imageView.setImageResource(drawableDefault);
        }

        try {
            Picasso.with(context)
                    .load(avatarURL)
                    .resize(width, height)
                    .noFade()
                    .placeholder(drawableDefault)
                    .error(drawableDefault)
                    .into(imageView);
        }catch(OutOfMemoryError e){
            Log.d(App.APP_TAG, "loadImageURL:"+e.toString());
        }
    }

    public void loadAvatar(final ImageView imageView, final String avatarId,
                           TextView textView, String name){
        Picasso.with(context).cancelRequest(imageView);
        if (name != null && !name.isEmpty()){
            String[] nameParts = name.trim().split(" ");
            String shortName = "";
            if (nameParts.length > 0 && nameParts[0].length() > 0){
                shortName += nameParts[0].charAt(0);
                if (nameParts.length > 1 && nameParts[1].length() > 0){
                    shortName += nameParts[1].charAt(0);
                }
            }
            textView.setText(shortName.toUpperCase());
        }
        if (avatarId == null || avatarId.isEmpty() || "-1".equals(avatarId)){
            imageView.setImageResource(R.drawable.transparent1);
        } else {
            try {
                Picasso.with(context)
                        .load(Util.getPhotoUrlFromId(avatarId, 96))
                        .noFade()
                        .placeholder(R.drawable.transparent1)
                        .error(R.drawable.transparent1)
                        .into(imageView);
            }
            catch (OutOfMemoryError e){
                Log.d("AAA", "UploadFileService:"+e.toString());
                //reload the avatar picture if needed
            }
        }
    }

    public void loadAvatarURL(final ImageView imageView, final String avatarURL,
                           TextView textView, String name){
        if (name != null && !name.isEmpty()){
            String[] nameParts = name.trim().split(" ");
            String shortName = "";
            if (nameParts.length > 0 && nameParts[0].length() > 0){
                shortName += nameParts[0].charAt(0);
                if (nameParts.length > 1 && nameParts[1].length() > 0){
                    shortName += nameParts[1].charAt(0);
                }
            }
            textView.setText(shortName.toUpperCase());
        }
        Picasso.with(context).cancelRequest(imageView);
        if (avatarURL == null || avatarURL.isEmpty() || "-1".equals(avatarURL)){
            imageView.setImageResource(R.drawable.transparent1);
        } else {
            try {
                Picasso.with(context)
                        .load(avatarURL)
                        .resize(96, 96)
                        .noFade()
                        .placeholder(R.drawable.transparent1)
                        .error(R.drawable.transparent1)
                        .into(imageView);
            }
            catch (OutOfMemoryError e){
                Log.d("AAA", "UploadFileService:"+e.toString());
                //reload the avatar picture if needed
            }
        }
    }
}
