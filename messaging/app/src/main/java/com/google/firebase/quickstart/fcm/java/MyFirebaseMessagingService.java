/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.fcm.java;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.quickstart.fcm.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    private final OkHttpClient httpClient = new OkHttpClient();

    private final Gson gson = new Gson();

    private LiveData<List<WorkInfo>> savedWorkInfo;

    LiveData<List<WorkInfo>> getOutputWorkInfo() { return savedWorkInfo; }

    public MyFirebaseMessagingService() {
        savedWorkInfo = WorkManager.getInstance().getWorkInfosByTagLiveData(TAG);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            //sendNotification(remoteMessage.getNotification().getBody());
            sendMessageToObserver(remoteMessage);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {

        // [START dispatch_job]
        // build an immediate OneTimeWorkRequest to fetch events from remote
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .addTag(TAG)
                .build();

        WorkManager.getInstance()
                .beginWith(work)
                .enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        try {

            Map<String, String> param = new HashMap<>();
            param.put("apiKey", "AAAAOmEYEZg:APA91bGsm_KrO4UFTroKVrVgH6mlgVW5udkd4LD2mJxOZnd64l9gFlNmO07uV5xBZ0k08jOleATwPGRtXqmjzmj-lecLpn7n4vg5e_DR7ftgqc0arWGV9o0Qn7JkLLiP3l508pSGA8DN");
            param.put("token", token);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), gson.toJson(param));

            Request request = new Request.Builder()
                    //.url(String.format("http://192.168.0.188:8080/generate/enroll?token=%s", token))
                    .url(String.format("http://192.168.0.188:8080/fcm/enroll"))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    //System.out.println(response.body().string());
                    Log.d(TAG, String.format("%s >>> %s", request.url().toString(), response.body().string()));
                }
            });
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /**
     * An async work to send Message to MainActivity using WorkManager.
     */
    private void sendMessageToObserver(RemoteMessage remoteMessage) {
        Log.d(TAG, "sendMessageToObserver: " + remoteMessage.getData());

        Data myData = new Data.Builder()
                .putString("title", remoteMessage.getNotification().getTitle())
                .putString("body", remoteMessage.getNotification().getBody())
                .build();

        // [START dispatch_job]
        // build an immediate OneTimeWorkRequest to fetch events from remote

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .setInputData(myData)
                .addTag(TAG)
                .build();

        // Using ListenableFuture
        WorkManager.getInstance()
                .beginUniqueWork(TAG, ExistingWorkPolicy.KEEP, work)
                .enqueue();

        // [END dispatch_job]
    }

    /**
     * send acknowledgement to Server
     */
    public void sendAcknowledgementToServer(Data outputData) {
        Log.d(TAG, "sendAcknowledgementToServer: " + outputData.getString("title"));
        try {

            Map<String, String> param = new HashMap<>();
            param.put("title", outputData.getString("title"));
            param.put("body", outputData.getString("body"));

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), gson.toJson(param));

            Request request = new Request.Builder()
                    .url(String.format("http://192.168.0.188:8080/fcm/ack"))
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    //System.out.println(response.body().string());
                    Log.d(TAG, String.format("%s >>> %s", request.url().toString(), response.body().string()));
                }
            });
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}
