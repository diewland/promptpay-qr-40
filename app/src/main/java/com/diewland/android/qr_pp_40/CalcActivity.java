package com.diewland.android.qr_pp_40;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class CalcActivity extends AppCompatActivity {

    String TAG = "DIEWLAND";

    EditText screen;
    TextView mod;
    Button pad_1;
    Button pad_2;
    Button pad_3;
    Button pad_4;
    Button pad_5;
    Button pad_6;
    Button pad_7;
    Button pad_8;
    Button pad_9;
    Button pad_0;
    Button pad_dot;
    Button pad_equal;
    Button pad_del;
    Button pad_divide;
    Button pad_multiply;
    Button pad_minus;
    Button pad_add;

    List<String> math_actions = Arrays.asList(
         "+",
        "-",
        "x",
        "÷"
    );

    // calc
    Double calc_a   = null;
    Double calc_b   = null;
    String calc_mod = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        screen = (EditText)findViewById(R.id.screen);
        mod = (TextView) findViewById(R.id.mod);
        pad_1 = (Button)findViewById(R.id.pad_1);
        pad_2 = (Button)findViewById(R.id.pad_2);
        pad_3 = (Button)findViewById(R.id.pad_3);
        pad_4 = (Button)findViewById(R.id.pad_4);
        pad_5 = (Button)findViewById(R.id.pad_5);
        pad_6 = (Button)findViewById(R.id.pad_6);
        pad_7 = (Button)findViewById(R.id.pad_7);
        pad_8 = (Button)findViewById(R.id.pad_8);
        pad_9 = (Button)findViewById(R.id.pad_9);
        pad_0 = (Button)findViewById(R.id.pad_0);
        pad_dot = (Button)findViewById(R.id.pad_dot);
        pad_equal = (Button)findViewById(R.id.pad_equal);
        pad_del = (Button)findViewById(R.id.pad_del);
        pad_divide = (Button)findViewById(R.id.pad_divide);
        pad_multiply = (Button)findViewById(R.id.pad_multiply);
        pad_minus = (Button)findViewById(R.id.pad_minus);
        pad_add = (Button)findViewById(R.id.pad_add);

        List<Button> pads = Arrays.asList(
            pad_1,
            pad_2,
            pad_3,
            pad_4,
            pad_5,
            pad_6,
            pad_7,
            pad_8,
            pad_9,
            pad_0,
            pad_dot,
            pad_equal,
            pad_del,
            pad_divide,
            pad_multiply,
            pad_minus,
            pad_add
        );
        for(Button pad : pads){
            pad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String v = ((Button)view).getText().toString();
                    handle_screen(v);
                }
            });
        }
    }

    private void handle_screen(String v){
        Log.d(TAG, "Input --> " + v);
        if(v.equals("DEL")){ // delete
            String scr = screen.getText().toString();
            if(scr.length() > 0){
                scr = scr.substring(0, scr.length()-1);
                screen.setText(scr);
            }
            mod.setText("");
            calc_mod = null;
        }
        else if(math_actions.contains(v)){ // +-x/
            // TODO equal case
            if(screen.getText().toString().length() > 0){
                mod.setText(v);
                calc_mod = v.equals("÷") ? "/" : v;
            }
        }
        else if(v.equals("=")){ // equal

        }
        else {
            if(calc_mod == null){ // value A
                screen.append(v);
            }
            else { // value B

            }
        }
    }
}
