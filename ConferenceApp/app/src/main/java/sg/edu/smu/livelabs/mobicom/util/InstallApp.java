package sg.edu.smu.livelabs.mobicom.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class InstallApp extends AsyncTask<Void, Void, Void> {
	private Context context;
	private String appUrl;
	
	public InstallApp(Context context, String appUrl) {
		this.context = context;
		this.appUrl = appUrl;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			URL url = new URL(appUrl);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.connect();
			
			
			String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/LiveStore";
			File file = new File(PATH);
			file.mkdirs();
			File outputFile = new File(file, "smuddy.apk");
			if (outputFile.exists()) {
				outputFile.delete();
			}
			
			FileOutputStream fos = new FileOutputStream(outputFile);

			InputStream is = c.getInputStream();

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len1);
			}
			fos.close();
			is.close();

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this
															// flag android
															// returned a
															// intent error!
			context.startActivity(intent);
		} catch (Exception e) {
			Log.e("", "Download error! " + e);
		}
		return null;
	}
}
