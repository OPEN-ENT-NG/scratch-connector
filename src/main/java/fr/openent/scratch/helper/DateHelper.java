package fr.openent.scratch.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {
    private static final SimpleDateFormat workspaceFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss.SSS");
    private static final SimpleDateFormat repriseFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat defaultFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static String tryFormat (String dateToFormat) {
        // Delete microseconds if exists
        if (dateToFormat.length() - dateToFormat.lastIndexOf('.') > 4) {
            dateToFormat = dateToFormat.substring(0, dateToFormat.lastIndexOf('.') + 4);
        }

        try {
            return workspaceFormatter.parse(dateToFormat).toInstant().toString();
        } catch (ParseException e1) {
            try {
                return repriseFormatter.parse(dateToFormat).toInstant().toString();
            } catch (ParseException e2) {
                try {
                    return defaultFormatter.parse(dateToFormat).toInstant().toString();
                } catch (ParseException e3) {
                    return new Date().toString();
                }
            }
        }
    }
}
