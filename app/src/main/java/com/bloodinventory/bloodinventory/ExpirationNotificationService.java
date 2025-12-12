package com.bloodinventory.bloodinventory;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ExpirationNotificationService extends Worker {

    private static final String CHANNEL_ID = "expiration_channel";
    private static final int NOTIFICATION_ID = 1;

    public ExpirationNotificationService(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            checkExpiringItems();
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    public static void scheduleNotificationCheck(Context context) {
        // Schedule periodic daily check (minimum interval is 15 minutes, so we use that)
        PeriodicWorkRequest periodicWork = new PeriodicWorkRequest.Builder(
                ExpirationNotificationService.class,
                1, TimeUnit.DAYS)
                .build();

        WorkManager.getInstance(context).enqueue(periodicWork);
    }

    private void checkExpiringItems() throws ExecutionException, InterruptedException {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        List<QueryDocumentSnapshot> documents = Tasks.await(
                db.collection("stock")
                        .whereEqualTo("userId", user.getUid())
                        .whereEqualTo("type", "add")
                        .get()
        ).getDocuments();

        int expiringCount = 0;
        StringBuilder message = new StringBuilder();

        for (QueryDocumentSnapshot document : documents) {
            String expireDateStr = document.getString("expireDate");
            if (expireDateStr != null) {
                try {
                    Date expireDate = dateFormat.parse(expireDateStr);
                    if (expireDate != null) {
                        Calendar now = Calendar.getInstance();
                        long diffInMillis = expireDate.getTime() - now.getTimeInMillis();
                        long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

                        if (diffInDays <= 30 && diffInDays >= 0) {
                            expiringCount++;
                            String itemName = document.getString("itemName");
                            if (itemName != null) {
                                if (message.length() > 0) {
                                    message.append("\n");
                                }
                                message.append(itemName).append(" - ").append(expireDateStr);
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (expiringCount > 0) {
            showNotification(expiringCount, message.toString());
        }
    }

    private void showNotification(int count, String details) {
        Context context = getApplicationContext();
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("تنبيه انتهاء الصلاحية")
                .setContentText(count + " صنف يقترب من انتهاء الصلاحية خلال شهر")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(details))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Expiration Alerts";
            String description = "Notifications for items expiring soon";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

