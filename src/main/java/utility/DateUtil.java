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

        String result = null;

        if (string.contains("Jan")) {

            result = previousResult + "01-";

        } else if (string.contains("Feb")) {

            result = previousResult + "02-";

        } else if (string.contains("Mar")) {

            result = previousResult + "03-";

        } else if (string.contains("Apr")) {

            result = previousResult + "04-";

        } else if (string.contains("May")) {

            result = previousResult + "05-";

        } else if (string.contains("Jun")) {

            result = previousResult + "06-";

        } else if (string.contains("Jul")) {

            result = previousResult + "07-";

        } else if (string.contains("Aug")) {

            result = previousResult + "08-";

        } else if (string.contains("Sep")) {

            result = previousResult + "09-";

        } else if (string.contains("Oct")) {

            result = previousResult + "10-";

        } else if (string.contains("Nov")) {

            result = previousResult + "11-";

        } else if (string.contains("Dec")) {

            result = previousResult + "12-";

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
