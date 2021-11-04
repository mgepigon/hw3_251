package edu.ucsb.ece150.gauchopay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CardListActivity extends AppCompatActivity {

    private static final int RC_HANDLE_INTERNET_PERMISSION = 2;

    private ArrayList<String> cardArray;
    private ArrayAdapter adapter;

    private int chosenCard;

    private ListView cardList;
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Launch the asynchronous process to grab the web API
                    new ReadWebServer(getApplicationContext()).execute("");
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        //Create notification channel
        initChannels(getApplicationContext());

        // Ensure that we have Internet permissions
        int internetPermissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if(internetPermissionGranted != PackageManager.PERMISSION_GRANTED) {
            final String[] permission = new String[] {Manifest.permission.INTERNET};
            ActivityCompat.requestPermissions(this, permission, RC_HANDLE_INTERNET_PERMISSION);
        }

        //Creation of activity views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.exclamation)
                .setContentTitle("WebRequest")
                .setContentText("Testing")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //Card storage
        cardArray = new ArrayList<String>();
        cardList = findViewById(R.id.cardList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cardArray);
        cardList.setAdapter(adapter);

        //What happens when floating button is clicked
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAddCardActivity = new Intent(getApplicationContext(), AddCardActivity.class);
                startActivity(toAddCardActivity);
            }
        });

        //If a card is chosen
        cardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int posID = (int) id;
                // If "lastAmount > 0" the last API call is a valid request (that the user must
                // respond to.
                if (ReadWebServer.getLastAmount() != 0) {
                    // [TODO] Send the card information back to the web API. Reference the
                    // WriteWebServer constructor to know what information must be passed.

                    chosenCard = position;
                    // Get the card number from the cardArray based on the position in the array.
                    WriteWebServer web_write = new WriteWebServer(getApplicationContext(), cardArray.get(chosenCard));
                    web_write.execute("");
                    Toast.makeText(CardListActivity.this, "Sent Card Information", Toast.LENGTH_SHORT).show();

                    // Reset the stored information from the last API call
                    ReadWebServer.resetLastAmount();
                }
            }
        });

        // Start the timer to poll the webserver every 5000 ms
        timer.schedule(task, 0, 5000);
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // [TODO] Modify the card information in the cardArray ArrayList
        // accordingly.
        SharedPreferences card = getSharedPreferences("Cards", Context.MODE_PRIVATE);

        //If the card holder is not empty
        if (!card.getAll().isEmpty()){
            Map<String, ?> allCards = card.getAll();
            for (Map.Entry<String, ?> entry : allCards.entrySet()) {
                Log.d("Cards", "Key: " + entry.getKey() + " Value: " + entry.getValue().toString());
                cardArray.add(entry.getValue().toString());
            }
        }

        // This is how you tell the adapter to update the ListView to reflect the current state
        // of your ArrayList (which holds all of the card information).
        adapter.notifyDataSetChanged();
    }
}
