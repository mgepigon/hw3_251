package edu.ucsb.ece150.gauchopay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.braintreepayments.cardform.view.CardForm;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.SharedPreferences;

import android.widget.Toast;

public class AddCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add Card");
        setSupportActionBar(toolbar);

        // Note that the requirements here are just for creating the fields on the form. For
        // example, if the cvvRequired setting was set to "false", the form would not contain
        // a field for CVV. ("Requirement" DOES NOT mean "Valid".)
        final CardForm cardForm = findViewById(R.id.card_form);
        cardForm.cardRequired(true)
                //.expirationRequired(true)
                //.cvvRequired(true)
                //.postalCodeRequired(true)
                .actionLabel("Add Card")
                .setup(this);

        // [TODO] Implement a method of getting card information and sending it to the main activity.
        FloatingActionButton accept = findViewById(R.id.accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences card = getSharedPreferences("Cards", Context.MODE_PRIVATE);
                //Store card number (key is its own unique number)
                card.edit().putString(cardForm.getCardNumber(), cardForm.getCardNumber()).apply();
                Toast.makeText(AddCardActivity.this, "New Card Added", Toast.LENGTH_SHORT).show();

                //Go back to home screen
                Intent toCardList = new Intent(AddCardActivity.this, CardListActivity.class);
                startActivity(toCardList);
            }
        });

    }
}
