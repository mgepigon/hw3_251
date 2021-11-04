package edu.ucsb.ece150.gauchopay;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReadWebServer extends AsyncTask<String, String, String> {

    private static final String myUserID = "5332408"; // [TODO] Fill in your ID. Your PERM number is ideal since it is a unique code that only you have access to.
    private static final String requestURL = "http://android.bryanparmenter.com/payment_listen.php?id=" + myUserID;

    private URL urlObject;
    private static int lastAmount;

    private static boolean notified;

    private WeakReference<Context> callingContext;

    private NotificationCompat.Builder builder;

    ReadWebServer(Context context) {
        this.callingContext = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(String... uri) {
        String responseString = null;

        //Create notification channel
        initChannels(callingContext.get());

        try {
            urlObject = new URL(requestURL);
        }
        catch(Exception e) {
            e.printStackTrace();
            responseString = "FAILED";
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) urlObject.openConnection();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // If this log is printed, then something went wrong with your call
                Log.d("Response from Send", "FAILED");
            } else {
                // Parse the input into a string and then read it
                return readFullyAsString(connection.getInputStream());
            }
        } catch(Exception e) {
            e.printStackTrace();
            responseString = "FAILED";
        } finally {
            connection.disconnect();
        }

        return responseString;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // The server responds with an integer that is encoded as a string
        //Log.d("RWS", result);
        int resultInt = Integer.parseInt(result);
        Context context = callingContext.get();

        //Valid Request
        if(resultInt != 0) {
            lastAmount = resultInt; // Do not modify this!

            //If there is a notification don't send it again.
            if (notified){
                //do nothing
                return;
            }
            // [TODO] A response was received from the server. Notify the user to select a card
            notifyUser();
        }
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }

        //Create notification manager
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Create notification channel
        NotificationChannel channel = new NotificationChannel("default",
                "Payment Request",
                NotificationManager.IMPORTANCE_DEFAULT);

        channel.setDescription("Payment Notification");
        notificationManager.createNotificationChannel(channel);

        //Build notification
        builder = new NotificationCompat.Builder(callingContext.get(), "default")
                .setSmallIcon(R.drawable.exclamation)
                .setContentTitle("Payment Requested")
                .setContentText("Select a card to complete payment.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);

    }

    public void notifyUser(){
        //Show Notification
        notified = true;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(callingContext.get());
        notificationManager.notify(0, builder.build());
    }

    private String readFullyAsString(InputStream inputStream) throws IOException {
        return readFully(inputStream).toString("UTF-8");
    }

    private ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while((length = inputStream.read(buffer)) != -1) {
            stream.write(buffer, 0, length);
        }

        return stream;
    }

    // Do not modify this! This helps keep track of the current API call. Since only one request
    // is made at a time, this value will stay the same until the pending transaction is cleared.
    static int getLastAmount() {
        return lastAmount;
    }

    static void resetLastAmount() {
        notified = false; lastAmount = 0; // Do not modify this!
    }

}
