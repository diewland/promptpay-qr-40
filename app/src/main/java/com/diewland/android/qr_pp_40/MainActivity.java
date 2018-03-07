package com.diewland.android.qr_pp_40;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;

    private static final String TAG = "DIEWLAND";
    private static final String STATE_ACC_ID = "STATE_ACC_ID";
    private static final String STATE_AMOUNT = "STATE_AMOUNT";
    private static final String STATE_REMARK = "STATE_REMARK";
    private static final String STATE_LOGO   = "STATE_LOGO";
    private static final int PICK_CONTACT    = 100;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_AMOUNT = 2;
    private static final int QR_SIZE = 512;
    private static final int LOGO_SIZE = 90;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;

    private TextView tv_acc_id;
    private TextView tv_amount;
    private TextView tv_remark;
    private LinearLayout ll_action;
    private Button btn_logo;
    private Button btn_share;
    private ImageButton btn_tel;
    private ImageButton btn_calc;
    private ImageView img_qr;

    private Bitmap bitmap_qr;
    private Bitmap bitmap_logo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request permissions first
        Util.requestPermissions(this, REQUEST_ID_MULTIPLE_PERMISSIONS);

        // get app objects
        tv_acc_id = (TextView)findViewById(R.id.account_id);
        tv_amount = (TextView)findViewById(R.id.amount);
        tv_remark = (TextView)findViewById(R.id.remark);
        ll_action = (LinearLayout)findViewById(R.id.action);
        btn_logo  = (Button)findViewById(R.id.logo);
        btn_share = (Button)findViewById(R.id.share);
        btn_tel   = (ImageButton)findViewById(R.id.tel_no);
        btn_calc  = (ImageButton)findViewById(R.id.calc);
        img_qr    = (ImageView) findViewById(R.id.qr);

        // render qr-code on text-changed
        tv_acc_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                renderQR();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        tv_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                renderQR();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        tv_remark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                renderQR();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // bind tel-no button
        btn_tel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });

        // bind calc
        btn_calc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalcActivity.class);
                intent.putExtra("AMOUNT", tv_amount.getText().toString());
                startActivityForResult(intent, PICK_AMOUNT);
            }
        });

        // bind add logo button
        btn_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bitmap_logo == null){
                    // https://stackoverflow.com/a/5309217/466693
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Logo"), PICK_IMAGE);
                }
                else { // remove logo
                    bitmap_logo = null;
                    renderQR();
                    btn_logo.setText("ใส่โลโก้");
                }
            }
        });

        // bind share button
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // use file provider for share bitmap W/O additional permissions
            // https://stackoverflow.com/a/30172792/466693
            File cachePath = new File(getCacheDir(), "images");
            String random_name = "pp-" + UUID.randomUUID() + ".png";

            // save bitmap to cache directory
            try {
                cachePath.mkdirs(); // don't forget to make the directory
                FileOutputStream stream = new FileOutputStream(cachePath + "/" + random_name); // overwrites this image every time
                bitmap_qr.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // share intent
            File newFile = new File(cachePath, random_name);
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.diewland.android.qr_pp_40.fileprovider", newFile);
            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Choose an app"));
            }
            }
        });

        // restore input states
        sharedPref = getSharedPreferences("SAVE_STATE", MODE_PRIVATE);
        String prev_acc_id = sharedPref.getString(STATE_ACC_ID, null);
        String prev_amount = sharedPref.getString(STATE_AMOUNT, null);
        String prev_remark = sharedPref.getString(STATE_REMARK, null);
        String prev_logo   = sharedPref.getString(STATE_LOGO, null);
        // set logo first..
        if(prev_logo != null){
            bitmap_logo = Util.b64tobitmap(prev_logo);
        }
        // when set text, renderQR will automatic execute
        if(prev_acc_id != null) tv_acc_id.setText(prev_acc_id);
        if(prev_amount != null) tv_amount.setText(prev_amount);
        if(prev_remark != null) tv_remark.setText(prev_remark);
    }

    // render QR image
    private void renderQR(){
        String acc_id = tv_acc_id.getText().toString();
        String amount = tv_amount.getText().toString();
        String remark = tv_remark.getText().toString();

        // get promptpay text
        String pp_str = Util.gen_qr_text(acc_id, amount);

        // reset image if invalid format
        if(pp_str == null){
            img_qr.setImageDrawable(null);
            ll_action.setVisibility(View.INVISIBLE);
            return;
        }

        // render qr bitmap
        bitmap_qr = QRCode.from(pp_str)
                .to(ImageType.PNG)
                .withSize(QR_SIZE, QR_SIZE)
                .bitmap();

        // if remark, paint into bitmap
        if(!remark.isEmpty()){
            bitmap_qr = Util.drawTextToBitmap(this, bitmap_qr, remark);
        }

        // render logo
        if(bitmap_logo != null){
            Canvas canvas_qr = new Canvas(bitmap_qr);
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
            int logo_x = (QR_SIZE-bitmap_logo.getWidth())/2;
            int logo_y = (QR_SIZE-bitmap_logo.getHeight())/2;
            canvas_qr.drawBitmap(bitmap_logo, logo_x, logo_y, paint);
            btn_logo.setText("ลบโลโก้");
        }

        // render bitmap
        img_qr.setImageBitmap(bitmap_qr);
        ll_action.setVisibility(View.VISIBLE);
    }

    // save input state when pause
    @Override
    protected void onPause() {
        super.onPause();

        // save input states
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(STATE_ACC_ID, tv_acc_id.getText().toString());
        editor.putString(STATE_AMOUNT, tv_amount.getText().toString());
        editor.putString(STATE_REMARK, tv_remark.getText().toString());
        if(bitmap_logo != null){
            String encodedImage = Util.bitmap2b64(bitmap_logo);
            editor.putString(STATE_LOGO, encodedImage);
        }
        else {
            editor.remove(STATE_LOGO);
        }
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // add logo on qr-code
        // https://stackoverflow.com/a/37614997/466693
        if((resultCode == RESULT_OK) && (requestCode == PICK_IMAGE)){
            if((data != null)&&(data.getData() != null)){
                try {
                    InputStream imageStream = getContentResolver().openInputStream(data.getData());
                    bitmap_logo = BitmapFactory.decodeStream(imageStream);
                    bitmap_logo = Util.resize_bitmap(bitmap_logo, LOGO_SIZE);
                    renderQR();
                }
                catch(Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, e.getStackTrace().toString());
                }
            }
            else {
                Toast.makeText(this, "ไม่พบโลโก้ของคุณ กรุณาลองใหม่อีกครั้ง", Toast.LENGTH_LONG).show();
            }
        }
        // set telephone number from contact list
        else if((resultCode == RESULT_OK) && (requestCode == PICK_CONTACT)){
            Util.getTelNoFromContacts(MainActivity.this, getContentResolver(), data, tv_acc_id);
        }
        // get calculated amount from calculator
        else if((resultCode == RESULT_OK) && (requestCode == PICK_AMOUNT)){
            if(data != null){
                String amt = data.getStringExtra("AMOUNT");
                tv_amount.setText(amt);
            }
        }
    }

}
