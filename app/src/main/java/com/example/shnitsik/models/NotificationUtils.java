package com.example.shnitsik.models;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.shnitsik.R;

/**
 * Utility class {@code NotificationUtils} is designed to manage and display notifications within the application,
 * specifically tailored for order status updates. It handles the creation of notification channels for
 * Android Oreo (API level 26) and above, and constructs and dispatches notifications to the user.
 * This class encapsulates the logic for sending notifications, making it easy to alert users
 * when their order is ready or has other status changes.
 *
 * The class uses a predefined notification channel ID {@code "order_ready_channel"} to categorize
 * its notifications. This allows users to manage notification settings for order updates specifically
 * through the system settings if they are on Android Oreo or newer.
 *
 * @author Ariel Kanitork
 */
public class NotificationUtils {

    private static final String CHANNEL_ID = "order_ready_channel";

    /**
     * Send notification.
     *
     * @param context the context
     * @param orderId the order id
     * @param title   the title
     * @param message the message
     */
    public static void sendNotification(Context context, String orderId, String title, String message) {
        // יצירת ערוץ ההתראות (אם צריך)
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // תחליף באייקון אמיתי שלך
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(orderId.hashCode(), builder.build());
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Order Notifications";
            String description = "Notifies when an order is ready";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
