package evgeniy.kurinnoy.calc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{
    private TextView mainText, result;
    private ArrayList<String> mainlist;
    private TableLayout moreFunc;
    private int countBracket=0;
    private Vibrator vbr;
    private static final String TAG = "CALC";
    private boolean vibrate;
    private boolean toast;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // moreFunc = (TableLayout) findViewById(R.id.moreFunc);
        mainText = (TextView) findViewById(R.id.mainText);
        mainlist = new ArrayList<String>(10);
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
        outState.putStringArrayList("mainList", mainlist);
        outState.putString("mainText", mainText.getText().toString());
        outState.putString("result", result.getText().toString());
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        countBracket = savedInstanceState.getInt("countBracket");
        mainlist = savedInstanceState.getStringArrayList("mainList");
        mainText.setText(savedInstanceState.getString("mainText"));
        result.setText(savedInstanceState.getString("result"));
    }

    public void pointButtonListener(View v){
        if (mainlist.isEmpty())
            return;
        String s = mainlist.get(mainlist.size()-1);
        if (isNumeric(mainlist.get(mainlist.size()-1))
                && s.charAt(s.length()-1) != '.') {
            mainText.setText(mainText.getText() + ".");
            mainlist.set(mainlist.size() - 1, mainlist.get(mainlist.size() - 1) + ".");
        }
        vibration(vibrate);
    }

    public void numberButtonListener(View v){
        if (!mainlist.isEmpty() &&mainlist.get(mainlist.size()-1).equals(")"))
            return;
        if(v == null || !(v instanceof Button)) {
            return;
        }
        String text = ((Button)v).getText().toString();
        mainText.setText(mainText.getText() + text);
        if(mainlist.isEmpty()|| !isNumeric(mainlist.get(mainlist.size()-1)))
            mainlist.add(text);
        else
            mainlist.set(mainlist.size()-1, mainlist.get(mainlist.size()-1) + text);
        equalsButtonListener();
        vibration(vibrate);
    }

    public void operationButtonListener(View v) {

        if (mainlist.isEmpty()){
            if (v.getId() == R.id.log)
                showToast(getString(R.string.logFormat));
            return;
    }
        if(v == null || !(v instanceof Button)) {
            return;
        }
        String text = ((Button)v).getText().toString();

        if (isNumeric(mainlist.get(mainlist.size()-1)) || mainlist.get(mainlist.size()-1).equals(")")) {
            mainText.setText(mainText.getText() + text);
            mainlist.add(text);
        } else if (v.getId() == R.id. log)
            showToast(getString(R.string.logFormat));
        vibration(vibrate);
    }

    public void minusButtonListener(View v){
        if (mainlist.isEmpty()
                || isNumeric(mainlist.get(mainlist.size()-1))
                || mainlist.get(mainlist.size()-1).equals(")")
                || mainlist.get(mainlist.size()-1).equals("(")) {
            mainText.setText(mainText.getText() + "-");
            mainlist.add("-");
        }
        vibration(vibrate);
    }

    public void deleteButtonListener(View v){
        if(mainlist.isEmpty())
            return;
        if (mainlist.get(mainlist.size()-1).equals(")"))
            countBracket++;
        if (mainlist.get(mainlist.size()-1).equals("("))
            countBracket--;
        if (mainlist.get(mainlist.size()-1).equals("log"))
            mainText.setText(mainText.getText().toString().substring(0, mainText.getText().length()-3));
        else
            mainText.setText(mainText.getText().toString().substring(0, mainText.getText().length()-1));
        if(!isNumeric(mainlist.get(mainlist.size()-1)))
            mainlist.remove(mainlist.size() - 1);
        else{
            String s1 = mainlist.get(mainlist.size()-1);
            mainlist.set(mainlist.size()-1, s1.substring(0, s1.length()-1));
            if(mainlist.get((mainlist.size()-1)).isEmpty())
                mainlist.remove(mainlist.size()-1);
        }
        if(mainText.getText().toString().isEmpty())
            mainlist.clear();
        equalsButtonListener();
        vibration(vibrate);
    }

    public void deleteAllButtonListener(View v){
        mainText.setText("");
        mainlist.clear();
        countBracket = 0;
        result.setText("");
        vibration(vibrate);
    }

   public void openBracketListener(View v){
       if (!mainlist.isEmpty() && isNumeric(mainlist.get(mainlist.size()-1)))
            return;
       mainText.setText(mainText.getText() + "(");
       mainlist.add("(");
       countBracket++;
       vibration(vibrate);
   }

    public void closeBracketListener(View v){
        if (!mainlist.isEmpty() && (isNumeric(mainlist.get(mainlist.size()-1)) || mainlist.get(mainlist.size()-1).equals(")"))) {
            if (countBracket > 0) {
                mainText.setText(mainText.getText() + ")");
                mainlist.add(")");
                countBracket--;
                equalsButtonListener();
            }
        }
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
            result.setText(nf.format(perform(mainlist)).toString().replace(",", "."));
            Log.i(TAG, "Успешно");
        }
        catch (Exception e){
            result.setText(getString(R.string.error));
            Log.e(TAG, "Ошибка", e);
        }
    }

    public double perform(ArrayList<String> task1){
        ArrayList<String> task = new ArrayList<String>(task1);
        if (task.isEmpty())
            return 0;
        //если выражение заканчивается операцией - удаляем эту операцию
        if(!isNumeric(task.get(task.size()-1)) && !task.get(task.size()-1).equals(")"))
            task.remove(task.size()-1);
        if (task.isEmpty()) //после удаления операции выражение может оказаться пустым
            return 0;
        while (true) { //обработка выражений в скобках
            int start = -1;   // индекс начала выражения в скобках
            int end = -1;   // индекс конца выражения в скобках
            int count = 0;   //счетчик скобок, определяет закрытие скобки
            for(int i=0; i<task.size(); i++){  //поиск начала выражения
                if (task.get(i).equals("(")) {
                    start = i;
                    count++;
                    break;
                }
            }
            if (start != -1) {
                for (int i = start+1; i<task.size(); i++){   //поиск конца выражения
                    if (task.get(i).equals("(")) {
                        count++;
                        continue;
                    }
                    if (task.get(i).equals(")")){
                        count--;
                        if (count == 0){
                            end = i;
                            break;
                        }
                    }
                }
                if (end == -1)
                    end = task.size();
                ArrayList<String> s = new ArrayList<>(task.subList(start + 1, end));
                double d = perform(s);
                task.set(start, Double.toString(d));
                if (end != task.size())
                    end++;
                for (int i = start + 1; i < end; i++) {
                    task.remove(start + 1);
                }
            }
            else break;
        }
        for(int i=0; i < task.size(); i++){  //обработка логарифма
            if (task.get(i).equals("log")){
                double d = Math.log(Double.parseDouble(task.get(i+1))) / Math.log(Double.parseDouble(task.get(i-1)));
                task.set(i-1, Double.toString(d));
                task.remove(i);
                task.remove(i);
                i--;
                continue;
            }
        }
        for(int i=0; i < task.size(); i++){ // обработка умножения и деления
            if (task.get(i).equals("*")){
                double d;
                try {
                    d = Double.parseDouble(task.get(i-1)) * Double.parseDouble(task.get(i+1));
                }catch (IndexOutOfBoundsException e){
                    continue;
                }
                task.set(i-1, Double.toString(d));
                task.remove(i);
                task.remove(i);
                i--;
                continue;
            }
            if(task.get(i).equals("/")){
                double d;
                try {
                    d = Double.parseDouble(task.get(i - 1)) / Double.parseDouble(task.get(i + 1));
                }catch (IndexOutOfBoundsException e){
                    continue;
                }
                task.set(i-1, Double.toString(d));
                task.remove(i);
                task.remove(i);
                i--;
                continue;
            }
        }
        for(int i=0; i < task.size(); i++){ // обработка сложения и вычитания
            if (task.get(i).equals("+")){
                double d;
                try {
                    d = Double.parseDouble(task.get(i - 1)) + Double.parseDouble(task.get(i + 1));
                } catch (IndexOutOfBoundsException e){
                    continue;
                }
                task.set(i-1, Double.toString(d));
                task.remove(i);
                task.remove(i);
                i--;
                continue;
            }
            if(task.get(i).equals("-")){
                double d;
                try {
                    d = Double.parseDouble(task.get(i - 1)) - Double.parseDouble(task.get(i + 1));
                    task.set(i-1, Double.toString(d));
                    task.remove(i);
                    task.remove(i);
                    i--;
                }catch (IndexOutOfBoundsException e){
                    d = 0 - Double.parseDouble(task.get(i + 1));
                    task.set(i, Double.toString(d));
                    task.remove(i+1);
                }
                continue;
            }
        }
        return Double.parseDouble(task.get(0));
    }

    public static boolean isNumeric(String str)
    {
        try {
            double d = Double.parseDouble(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
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
