package evgeniy.kurinnoy.calc;


import java.util.ArrayList;

public class Calc {
    private ArrayList<String> maintask;

    public Calc(){
        setMaintask(new ArrayList<String>());
    }

    public Calc(ArrayList<String> maintask){
        this.setMaintask(maintask);
    }

    public boolean addItem(String item){
        if (maintask.isEmpty() && (item.equals(".") || isOperation(item) || item.equals(")"))){
            if (!item.equals("-"))
                return false;
        }
        if (isOperation(getLastStr()) && isOperation(item)){
            return false;
        }
        if (getLastStr().equals("(") && isOperation(item)){
            if (!item.equals("-"))
                return false;
        }
        if (!isInteger(getLastStr()) && item.equals(".")){
            return false;
        }
        if ((isOperation(getLastStr()) || getLastStr().equals("(")) && item.equals(")")) {
            return false;
        }
        if ((isNumeric(getLastStr()) || getLastStr().equals(")")) && item.equals("(")){
            return false;
        }
        if (isOperation(getLastStr()) && item.equals(".")) {
            return false;
        }
        if (getLastStr().equals(")") && isNumeric(item)){
            return false;
        }
        ArrayList<String> temp = formatToPerform(item);
        if (isNumeric(getLastStr()) && isNumeric(temp.get(0))){
            maintask.set(maintask.size()-1, getLastStr() + temp.get(0));
            temp.remove(0);
        }
        maintask.addAll(temp);
        return true;
    }

    public static String getLastStr(ArrayList<String> in) {
        if (!in.isEmpty())
            return in.get(in.size() - 1);
        return "";
    }
    public  String getLastStr() {
        if (!maintask.isEmpty())
            return maintask.get(maintask.size() - 1);
        return "";
    }
    public void removeLastItem(){
        maintask.remove(maintask.size()-1);
    }
    public void removeLastChar(){
        String s1 = getLastStr();
        maintask.set(maintask.size()-1, s1.substring(0, s1.length()-1));
        if(maintask.get((maintask.size()-1)).isEmpty())
            maintask.remove(maintask.size()-1);
    }

    public static ArrayList<String> formatToPerform(String input){ //разделяет входную строку на строки типа: число, знак или строка
        ArrayList<String> output = new ArrayList<>();
        output.add(String.valueOf(input.charAt(0)));
        for (int i=1; i<input.length(); i++){
            if (!isNumeric(String.valueOf(input.charAt(i))) && !Character.isLetter(input.charAt(i))
                    || isNumeric(String.valueOf(input.charAt(i))) && !isNumeric(String.valueOf(input.charAt(i-1)))
                    || Character.isLetter(input.charAt(i)) && !Character.isLetter(input.charAt(i-1))){
                output.add(String.valueOf(input.charAt(i)));
            }
            else if (isNumeric(String.valueOf(input.charAt(i))) && isNumeric(String.valueOf(input.charAt(i-1)))
                    || Character.isLetter(input.charAt(i)) && Character.isLetter(input.charAt(i-1))){
                output.set(output.size()-1, output.get(output.size()-1) + input.charAt(i));
            }
        }
        return output;
    }

    public double perform(){
        return perform(maintask);
    }

    public static double perform(ArrayList<String> task1){
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

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch(NumberFormatException nfe) {
            if (str.equals("."))
                return true;
            return false;
        }
        return true;
    }
    public static boolean isInteger(String str){
        try {
            int d = Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public boolean isOperation(String input){
        return input.equals("+")
                || input.equals("-")
                || input.equals("*")
                || input.equals("/")
                || input.equals("log");
    }
    public void clear(){
        maintask.clear();
    }
    public ArrayList<String> getMaintask(){
        return maintask;
    }

    public void setMaintask(ArrayList<String> maintask) {
        this.maintask = maintask;
    }
}
