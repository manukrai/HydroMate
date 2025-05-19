package at.fhj.hydromate.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import at.fhj.hydromate.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("WASSER_REMINDER", "Wasser Erinnerung", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Erinnert dich ans Trinken");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "WASSER_REMINDER")
                .setSmallIcon(R.drawable.water_bottle)
                .setContentTitle("Trink-Erinnerung")
                .setContentText("Zeit, etwas Wasser zu trinken!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(1001, builder.build());
            } else {
                Log.w("Notification", "POST_NOTIFICATIONS permission not granted");
            }
        } else {
            notificationManager.notify(1001, builder.build());
        }
    }

}
