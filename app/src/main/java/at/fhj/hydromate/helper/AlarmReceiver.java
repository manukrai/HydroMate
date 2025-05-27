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

/**
 * {@code AlarmReceiver} ist ein {@link BroadcastReceiver}, der bei einem Alarm ausgelöst wird
 * und eine Benachrichtigung zur Erinnerung ans Trinken anzeigt.
 *
 * <p>Diese Klasse erstellt bei Bedarf einen {@link NotificationChannel} (ab Android O)
 * und zeigt dann eine Benachrichtigung mit hoher Priorität an.</p>
 *
 * <p>Auf neueren Android-Versionen (ab Android 13 / TIRAMISU) wird geprüft, ob die Berechtigung
 * {@code POST_NOTIFICATIONS} vorhanden ist, bevor die Benachrichtigung angezeigt wird.</p>
 *
 * <p>Wird z. B. in Kombination mit einem {@link android.app.AlarmManager} oder
 * {@link android.app.PendingIntent} verwendet, um regelmäßige Erinnerungen zu senden.</p>
 *
 * Beispieltext: „Zeit, etwas Wasser zu trinken!“
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * Wird aufgerufen, wenn der Alarm ausgelöst wird.
     *
     * @param context Der Kontext, in dem der Receiver läuft.
     * @param intent  Der empfangene Intent, der den Alarm repräsentiert.
     */
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
