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
 * The type Notification receiver.
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "order_channel";

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
