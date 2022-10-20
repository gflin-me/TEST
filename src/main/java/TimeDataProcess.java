import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @className: TimeDataProcess
 * @description: 时间戳处理相关
 * @author: Guifeng lin
 * @date: 2022/07/09
 * @version: 1.0
 **/
public class TimeDataProcess {
    //时间戳格式
    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * long类型的时间戳转string格式（HH:mm:ss.SSS）
     * @param dataLong long格式的时间
     * @return String格式的时间【yyyy-MM-dd HH:mm:ss.SSS】
     */
    public static String getStrTime(long dataLong){
        Date curDate =  new Date(dataLong);
        return timeFormatter.format(curDate) ;
    }

    // date要转换的date类型的时间

    /**
     * data形式的时间戳转为long格式的时间戳
     * @param date Date格式的时间戳
     * @return long格式的时间戳
     */
    public static long dateTimeToLong(Date date) {
        return date.getTime();
    }

    /**
     * String格式的时间戳转long格式的时间戳
     * @param strTime String格式的时间戳
     * @return long格式的时间戳
     */
    public static long stringTimeToLong(String strTime){
        Date date = stringTimeToDate(strTime); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateTimeToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

    /**
     * String格式的时间戳转为Date格式的时间戳
     * @param strTime String格式的时间戳
     * @return  Date格式的时间戳
     */
    public static Date stringTimeToDate(String strTime ) {
        Date date = null;
        try {
            date = timeFormatter.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }



}
