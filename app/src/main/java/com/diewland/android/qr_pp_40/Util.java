package com.diewland.android.qr_pp_40;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Util {

    // https://www.blognone.com/node/95133
    public static String gen_qr_text(String acc_id, String amount){
        String pp_acc_id = "";
        String pp_amount = "";
        String pp_chksum = "";

         // process acc_id
        if(acc_id.length() == 15){ // truemoney e-wallet
            pp_acc_id = "0315" + acc_id;
        }
        else if(acc_id.length() == 13){ // card-id
            pp_acc_id = "0213" + acc_id;
        }
        else if(acc_id.length() == 10){ // tel-no
            pp_acc_id = "01130066" + acc_id.substring(1);
        }
        else { // invalid acc_id
            return null;
        }

        // process amount
        if(!amount.isEmpty()){
            pp_amount = String.format("54%02d%s", amount.length(), amount);
        }

        // build pp string
        String field_29 = "0016A000000677010111" + pp_acc_id;
        String pp_str = "000201010211"
                      + "29" + field_29.length() + field_29
                      + "5303764"
                      + pp_amount
                      + "5802TH"
                      + "6304";

        // process checksum
        pp_chksum = CRC16.checksum(pp_str);
        pp_str += pp_chksum;
        return pp_str;
    }

    // draw text on bitmap
    // https://www.skoumal.net/en/android-how-draw-text-bitmap/
    public static Bitmap drawTextToBitmap(Context gContext, Bitmap bitmap, String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (14 * scale));
        // text shadow
        // paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = 0; //(bitmap.getWidth() - bounds.width())/2;
        int y = 45; //(bitmap.getHeight() + bounds.height())/2;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }

    // resize bitmap function
    // https://stackoverflow.com/a/40996725/466693
    public static Bitmap resize_bitmap(Bitmap temp, int size) {
        if (size > 0) {
            int width = temp.getWidth();
            int height = temp.getHeight();
            float ratioBitmap = (float) width / (float) height;
            int finalWidth = size;
            int finalHeight = size;
            if (ratioBitmap < 1) {
                finalWidth = (int) ((float) size * ratioBitmap);
            } else {
                finalHeight = (int) ((float) size / ratioBitmap);
            }
            return Bitmap.createScaledBitmap(temp, finalWidth, finalHeight, true);
        } else {
            return temp;
        }
    }

    // Bitmap <=> base64 String
    // https://stackoverflow.com/a/8586244/466693
    public static Bitmap b64tobitmap(String b64){
        byte[] b = Base64.decode(b64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }
    public static String bitmap2b64(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    // request permissions dialog
    // https://stackoverflow.com/a/37184243/466693
    public static boolean requestPermissions(Activity that, int request_code){
        int contact = ContextCompat.checkSelfPermission(that, Manifest.permission.READ_CONTACTS);
        int storage = ContextCompat.checkSelfPermission(that, Manifest.permission.READ_EXTERNAL_STORAGE);
        int camera = ContextCompat.checkSelfPermission(that, Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (contact != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(that,listPermissionsNeeded.toArray
                (new String[listPermissionsNeeded.size()]), request_code);
            return false;
        }
        return true;
    }

    // get telephone number from Contact List ( in ActivityResult )
    // https://stackoverflow.com/a/37614997/466693
    public static void getTelNoFromContacts(Context ctx, ContentResolver cr, Intent data, final TextView tv) {
        Cursor cursor = null;
        String phoneNumber = "";
        List<String> allNumbers = new ArrayList<String>();
        int phoneIdx = 0;
        try {
            Uri result = data.getData();
            String id = result.getLastPathSegment();
            cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[] { id }, null);
            phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
            if (cursor.moveToFirst()) {
                while (cursor.isAfterLast() == false) {
                    phoneNumber = cursor.getString(phoneIdx);
                    allNumbers.add(phoneNumber);
                    cursor.moveToNext();
                }
            } else {
                //no results actions
            }
        } catch (Exception e) {
            //error actions
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("เลือกเบอร์โทร");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    String selectedNumber = items[item].toString();
                    selectedNumber = selectedNumber.replace("-", "");
                    tv.setText(selectedNumber);
                }
            });
            AlertDialog alert = builder.create();
            if(allNumbers.size() > 1) {
                alert.show();
            } else {
                String selectedNumber = phoneNumber.toString();
                selectedNumber = selectedNumber.replace("-", "");
                tv.setText(selectedNumber);
            }
            if (phoneNumber.length() == 0) {
                //no numbers found actions
            }
        }
    }
}
