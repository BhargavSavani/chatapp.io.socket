package com.example.chat_socket.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static String formatTimeToWhatsAppStyle(Date date) {
        if (date == null) return "";

        // Set the time zone to IST
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");

        // Get current time in IST
        Calendar now = Calendar.getInstance(istTimeZone);

        // Get the time of the message in IST
        Calendar msgTime = Calendar.getInstance();
        msgTime.setTime(date);
        msgTime.setTimeZone(istTimeZone);

        // Calculate the difference in days between now and the message time
        long daysDiff = TimeUnit.MILLISECONDS.toDays(now.getTimeInMillis() - msgTime.getTimeInMillis());

        if (daysDiff == 0) {
            // Check if it's today
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            timeFormat.setTimeZone(istTimeZone);
            return "Today, " + timeFormat.format(date);
        } else if (daysDiff == 1) {
            // Check if it's yesterday
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            timeFormat.setTimeZone(istTimeZone);
            return "Yesterday, " + timeFormat.format(date);
        } else {
            // Else, show the date and time
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            dateTimeFormat.setTimeZone(istTimeZone);
            return dateTimeFormat.format(date);
        }
    }
}
