package evgeniy.kurinnoy.calc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity{
    private TextView mainText, result;
    private int countBracket=0;
    private Vibrator vbr;
    private static final String TAG = "CALC";
    private boolean vibrate;
    private boolean toast;
    Calc calc;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = (TextView) findViewById(R.id.mainText);
        calc = new Calc();
        result = (TextView) findViewById(R.id.result);
        vbr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onStart(){
        super.onStart();
        setConfig();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt("countBracket", countBracket);
        outState.putStringArrayList("mainList", calc.getMaintask());
        outState.putString("mainText", mainText.getText().toString());
        outState.putString("result", result.getText().toString());
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        countBracket = savedInstanceState.getInt("countBracket");
        calc.setMaintask(savedInstanceState.getStringArrayList("mainList"));
        mainText.setText(savedInstanceState.getString("mainText"));
        result.setText(savedInstanceState.getString("result"));
    }
    public void buttonListener(View v) {
        String text = ((Button)v).getText().toString();
        if (v.getId() == R.id.cBracket && countBracket <=0)
            return;
        if (v.getId() == R.id.oBracket)
            countBracket++;
        else if (v.getId() == R.id.cBracket)
            countBracket--;
        if (calc.addItem(text)){
            mainText.setText(mainText.getText() + text);
            equalsButtonListener();
        }else if (v.getId() == R.id.log)
            showToast(getString(R.string.logFormat));

        vibration(vibrate);
    }

    public void deleteButtonListener(View v){
        if(mainText.getText().toString().isEmpty())
            return;
        if (calc.getLastStr().equals(")"))
            countBracket++;
        if (calc.getLastStr().equals("("))
            countBracket--;
        if (calc.getLastStr().equals("log"))
            mainText.setText(mainText.getText().toString().substring(0, mainText.getText().length()-3));
        else
            mainText.setText(mainText.getText().toString().substring(0, mainText.getText().length()-1));

       calc.removeLastChar();
        if(mainText.getText().toString().isEmpty())
            deleteAllButtonListener(v) ;
        equalsButtonListener();
        vibration(vibrate);
    }

    public void deleteAllButtonListener(View v){
        mainText.setText("");
        calc.clear();
        countBracket = 0;
        result.setText("");
        vibration(vibrate);
    }

    public void equalsButtonListener(){
        if (mainText.getText().toString().isEmpty()) {
            result.setText("");
            return;
        }
        try {
            NumberFormat nf = new DecimalFormat("#.########");
            nf.setRoundingMode(RoundingMode.HALF_UP);
            result.setText(nf.format(calc.perform()).toString().replace(",", "."));
            Log.i(TAG, "Успешно");
        }
        catch (Exception e){
            result.setText(getString(R.string.error));
            Log.e(TAG, "Ошибка", e);
        }
    }

    private void vibration (boolean b){
        if(b)
            vbr.vibrate(30);
    }

    private void setConfig() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        vibrate = prefs.getBoolean("vibration", true);
        toast = prefs.getBoolean("toast", true);
    }
    public void prefButtonAction(View v){
        Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
        startActivity(intent);
    }
    private void showToast(String message){
        if(!toast) return;
        Toast t = Toast.makeText(getApplicationContext(),
                message, Toast.LENGTH_SHORT);
        t.show();
    }


}
