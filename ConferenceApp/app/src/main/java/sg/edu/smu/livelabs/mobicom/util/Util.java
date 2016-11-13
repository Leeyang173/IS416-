package sg.edu.smu.livelabs.mobicom.util;

import android.content.Context;

import java.security.MessageDigest;
import java.util.Locale;

import sg.edu.smu.livelabs.mobicom.net.RestClient;

/**
 * Created by Aftershock PC on 13/7/2015.
 */
public class Util {
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
                        : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString().toLowerCase(Locale.ENGLISH);
    }

    public static String SHA1(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        } catch (Exception e) {

        }
        return "";
    }

    public static String getPhotoUrlFromId(String imageId, int width) {
//        return RestClient.PHOTO_BASE_URL + "photo/" + imageId + "?width=" + width;
        return RestClient.PHOTO_BASE_URL +"uploaded_images/" + width + "/" + imageId +".jpeg";
    }

    public static String getPhotoUrlFromId(String imageId) {
        return RestClient.PHOTO_BASE_URL + "uploaded_images/" + 512 + "/" + imageId +".jpeg";
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
