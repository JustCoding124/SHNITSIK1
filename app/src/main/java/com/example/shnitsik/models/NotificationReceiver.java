package com.example.shnitsik.models;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.example.shnitsik.R;

/**
 * NotificationReceiver is a {@link BroadcastReceiver} that listens for scheduled
 * broadcast intents related to order status and displays system notifications accordingly.
 * <p>
 * It supports two types of notifications:
 * <ul>
 *     <li><b>ideal</b>: Indicates that the order is ready for pickup (idealPrepTime)</li>
 *     <li><b>requested</b>: Indicates that the requested pickup time has arrived (requestedTime)</li>
 * </ul>
 *
 * The Intent used to trigger this receiver should include:
 * <ul>
 *     <li><code>orderId</code>: The unique order identifier</li>
 *     <li><code>type</code>: (Optional) Either "ideal" or "requested" (defaults to "ideal")</li>
 * </ul>
 *
 * @author Ariel Kanitork
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "order_channel";

    /**
     * Called when the receiver receives a broadcast.
     * Constructs and displays a notification based on the order ID and type.
     *
     * @param context The context in which the receiver is running.
     * @param intent  The broadcast intent containing order details.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String orderId = intent.getStringExtra("orderId");
        String type = intent.getStringExtra("type");

        if (orderId == null) orderId = "Unknown";
        if (type == null) type = "ideal";

        createNotificationChannel(context);

        String title, message;
        switch (type) {
            case "requested":
                title = "Pickup Time Arrived";
                message = intent.getStringExtra("message");
                if (message == null) message = "Your requested pickup time has arrived.";
                break;
            case "ideal":
                title = "Preparation Started";
                message = "The preparation of your order has started.";
                break;
            default:
                title = "Reminder";
                message = "Might wanna check your order status.";
                break;
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((orderId + "_" + type).hashCode(), builder.build());
    }

    /**
     * Creates the notification channel required for API level 26 and above.
     * Ensures that notifications can be displayed on modern Android versions.
     *
     * @param context The context used to access system services.
     */
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Order Notifications";
            String description = "Notifies when orders are ready or pickup time is reached";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
