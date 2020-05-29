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

        if (string.contains("2011")) {

            result = "2011-";

        } else if (string.contains("2012")) {

            result = "2012-";

        } else if (string.contains("2013")) {

            result = "2013-";

        } else if (string.contains("2014")) {

            result = "2014-";

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
            case "May":
                result = previousResult + "05-";
                break;
            case "Jun":
                result = previousResult + "06-";
                break;
            case "Jul":
                result = previousResult + "07-";
                break;
            case "Aug":
                result = previousResult + "08-";
                break;
            case "Sep":
                result = previousResult + "09-";
                break;
            case "Oct":
                result = previousResult + "10-";
                break;
            case "Nov":
                result = previousResult + "11-";
                break;
            case "Dec":
                result = previousResult + "12-";
                break;

            default:

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
