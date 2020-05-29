package utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DateUtil{

    public Date stringToDate(String s) {

        Date result = new Date();
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            result = dateFormat.parse(s);

        } catch (ParseException e) {

            e.printStackTrace();

        }

        return result;

    }

    public String convertYear(String string){

        String result = null;

        if(string.contains("2008")){

            result = "2008-";

        }else if(string.contains("2009")){

            result = "2009-";

        }else {

            for (int i = 10; i < 21; i++) {

                if(string.contains("20" + i)) {

                    result = "20" + i + "-";

                }

            }

        }

        return result;

    }

    public String convertMonth(String string, String previousResult){

        String result;

        switch (string.substring(4, 7)){

            case "Jan":
                result = previousResult + "01-";
                break;
            case "Feb":
                result = previousResult + "02-";
                break;
            case "Mar":
                result = previousResult + "03-";
                break;
            case "Apr":
                result = previousResult + "04-";
                break;

            default:

                result = secondConvert(string, previousResult);

        }

        return result;
    }

    public String secondConvert(String string, String previousResult){

        String out;

        String string1 = string.substring(4, 7);

        switch(string1){

            case "May":
                out = previousResult + "05-";
                break;
            case "Jun":
                out = previousResult + "06-";
                break;
            case "Jul":
                out = previousResult + "07-";
                break;
            case "Aug":
                out = previousResult + "08-";
                break;

            default:

                out = thirdConvert(string, previousResult);

        }

        return out;

    }

    public String thirdConvert(String string, String previousResult){

        String result;
        String string1 = string.substring(4, 7);

        if(string1.equals("Sep")){

            result = previousResult + "09-";

        }else if(string1.equals("Oct")){

            result = previousResult + "10-";


        }else if(string1.equals("Nov")){

            result = previousResult + "11-";


        }else if(string1.equals("Dec")){

            result = previousResult + "12-";


        }else{

            result = null;

        }

        return result;

    }

    public String convertDate(String string){

        String result;
        String finalResult;

        if(string != null){

            result = convertMonth(string, convertYear(string));

            finalResult = result + string.substring(8, 10);

        }else{

            finalResult = "none";

        }

        return finalResult;

    }

    public String betweenInterval(List<String[]> interval, Date date3){

        Date date1;
        Date date2;
        String result = null;

        for(int i = 0; i < interval.size() - 1; i++){

            date1 = stringToDate(interval.get(i)[0]);

            date2 = stringToDate(interval.get(i+1)[0]);

            if(date3.after(date1) && date3.before(date2)){

                result = date1.toString();

            }

        }

        return result;

    }

}
