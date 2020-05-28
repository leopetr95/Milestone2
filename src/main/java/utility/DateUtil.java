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

    public String convertDate(String stringa){

        String result = null;

        if(stringa != null){

            if (stringa.contains("2011")) {

                result = "2011-";

            } else if (stringa.contains("2012")) {

                result = "2012-";

            } else if (stringa.contains("2013")) {

                result = "2013-";

            } else if (stringa.contains("2014")) {

                result = "2014-";

            }

            if (stringa.contains("Jan")) {

                result = result + "01-";

            } else if (stringa.contains("Feb")) {

                result = result + "02-";

            } else if (stringa.contains("Mar")) {

                result = result + "03-";

            } else if (stringa.contains("Apr")) {

                result = result + "04-";

            } else if (stringa.contains("May")) {

                result = result + "05-";

            } else if (stringa.contains("Jun")) {

                result = result + "06-";

            } else if (stringa.contains("Jul")) {

                result = result + "07-";

            } else if (stringa.contains("Aug")) {

                result = result + "08-";

            } else if (stringa.contains("Sep")) {

                result = result + "09-";

            } else if (stringa.contains("Oct")) {

                result = result + "10-";

            } else if (stringa.contains("Nov")) {

                result = result + "11-";

            } else if (stringa.contains("Dec")) {

                result = result + "12-";

            }

            result = result + stringa.substring(8, 10);

        }else{

            result = "none";

        }

        return result;

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
