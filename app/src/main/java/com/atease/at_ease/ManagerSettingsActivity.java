package com.atease.at_ease;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
//import android.widget.CheckBox;

import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.iconics.view.IconicsTextView;
import com.mikepenz.materialdrawer.Drawer;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;


import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.rey.material.widget.EditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ManagerSettingsActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView tvTitle;
    Button btnStripeConnect;
    Switch swUseStripe;
    CheckBox cbOccupied;
    MaterialCalendarView calendar;
    MaterialEditText etMonthlyRentDue;
    MaterialEditText etProrateDays;
    Button btnSave;
    TextView tvPropId;
    String propId;


    Date rentDueDate;
    Boolean occupied;
    Boolean stripePay;
    Boolean stripeAuthorized;
    String rentdue;
    int prorateDays;

    ParseObject stripeAuth;
    ParseObject mgrSettings;
    ParseObject property;
    ParseUser currentUser;
    /*
    private RecyclerView recyclerView;
    private TenantRecyclerViewAdapter adapter;
    private TenantRecyclerViewAdapter.TenantViewHolder viewHolder;
    private List<ParseUser> tenantList = new ArrayList<ParseUser>();
    */



    final static String TAG = "ManagerSettings";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_settings);

        currentUser = ParseUser.getCurrentUser();
        AtEaseApplication application = (AtEaseApplication) getApplicationContext();
        final Drawer myDrawer = application.getNewDrawerBuilder(currentUser.getBoolean("isManager"),this).withActivity(this).build();

        //Toolbar stuff
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        TextView title = (TextView) toolbar.findViewById(R.id.title);
        title.setText("Property Settings");
        IconicsTextView rightToggle = (IconicsTextView) toolbar.findViewById(R.id.rightToggle);
        rightToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myDrawer.isDrawerOpen()) {
                    myDrawer.closeDrawer();
                } else {
                    myDrawer.openDrawer();
                }
            }
        });

        setSupportActionBar(toolbar);// Setting toolbar as the ActionBar with setSupportActionBar() call
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);




        tvTitle = (TextView) findViewById(R.id.tvTitle);
        btnStripeConnect = (Button) findViewById(R.id.btnStripeConnect);
        swUseStripe = (Switch) findViewById(R.id.swUseStripe);
        cbOccupied = (CheckBox) findViewById(R.id.cbOccupied);
        tvPropId = (TextView) findViewById(R.id.tvPropId);
        calendar = (MaterialCalendarView) findViewById(R.id.calendarView);
        etMonthlyRentDue = (MaterialEditText) findViewById(R.id.etMonthlyRent);
        //etProrateDays = (MaterialEditText) findViewById(R.id.etProrateDays);
        btnSave = (Button) findViewById(R.id.btnSave);

        propId = getIntent().getStringExtra("propId");
        try{
            currentUser.fetchIfNeeded();
        }catch(ParseException ex){
            Log.d(TAG,ex.getMessage());
        }
    /*
        ParseQuery<ParseObject> stripeAuthQuery = ParseQuery.getQuery("StripeAuth");
        stripeAuthQuery.whereEqualTo("manager", currentUser);
        stripeAuthQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject stripeA, ParseException e) {
                if (e == null) {
                    stripeAuth = stripeA;
                    if(stripeAuth != null){
                    }
                } else {
                    Log.d(TAG, "couldn't get stripeAuth from Parse");
                }
            }
        });
*/  /*
        recyclerView = (RecyclerView) findViewById(R.id.recycler_tenant);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new TenantRecyclerViewAdapter(ManagerSettingsActivity.this, this.tenantList);
        recyclerView.setAdapter(adapter);
*/
        ParseQuery<ParseObject> mgrSettingsQuery = ParseQuery.getQuery("ManagerSettings");
        mgrSettingsQuery.whereEqualTo("manager", currentUser);
        mgrSettingsQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject mgr, ParseException e) {
                if (e == null) {
                    mgrSettings = mgr;
                    stripeAuthorized = mgrSettings.getBoolean("authorizedStripe");
                    if (stripeAuthorized) {
                        btnStripeConnect.setVisibility(View.GONE);
                        tvTitle.setText("Here you can choose if you want to turn Stripe Payments on or off");
                        stripePay = mgrSettings.getBoolean("useStripePayments");
                        swUseStripe.setChecked(stripePay);
                    } else {
                        swUseStripe.setVisibility(View.GONE);
                        stripePay = false;
                    }


                } else {
                    Log.d(TAG, "couldn't get ManagerSettings from Parse");
                }
            }
        });
        btnStripeConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerSettingsActivity.this, StripeConnectActivity.class);
                startActivity(intent);
            }
        });

        ParseQuery<ParseObject> propertyQuery = ParseQuery.getQuery("Property");
        propertyQuery.whereEqualTo("objectId", propId);
        propertyQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject prop, ParseException e) {
                if (e == null) {
                    property = prop;
                    rentDueDate = property.getDate("nextRentDue");
                    //DateTime dt = new DateTime(date);
                    if (rentDueDate != null) {
                        calendar.setDateSelected(rentDueDate, true);
                        calendar.setCurrentDate(rentDueDate);
                    }
                    occupied = property.getBoolean("occupied");
                    if (occupied != null) {
                        cbOccupied.setCheckedImmediately(occupied);
                    }
                    // assume 0 if doesn't exist?
                    rentdue = Integer.toString(property.getInt("monthlyRentDue"));

                    //String rentdue = Integer.toString(rentdue);
                    tvPropId.setText("Property ID: " + property.getObjectId());
                    if(rentdue.length() > 2) {
                        etMonthlyRentDue.setText(rentdue.substring(0, rentdue.length() - 2) + "." + rentdue.substring(rentdue.length() - 2, rentdue.length()));
                    }
                    else{
                        etMonthlyRentDue.setText("0");
                    }
                    //prorateDays = property.getInt("prorateDays");
                    //etProrateDays.setText(Integer.toString(prorateDays));

                } else {
                    Log.d(TAG, "couldn't get Property from Parse");
                }
            }
        });

      /*  try {
            populateTenants();
        } catch (ParseException e) {
            e.printStackTrace();
        }  */

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(ManagerSettingsActivity.this)
                        .title("Confirm Changes")
                        .content(combineChecks())
                        .positiveText("Confirm")
                        .negativeText("Cancel")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                dialog.dismiss();
                            }
                        }).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {


                        //save mgrSettings
                        mgrSettings.put("useStripePayments", swUseStripe.isChecked());

                        // save property settings
                        String amount = fixAmountForStore(etMonthlyRentDue.getText().toString());
                        property.put("monthlyRentDue", Integer.parseInt(amount));
                        int newRentAmount =  property.getInt("rentAmount") - Integer.parseInt(rentdue) + Integer.parseInt(amount);

                        property.put("rentAmount", newRentAmount);

                        property.put("occupied", cbOccupied.isChecked());
                        // property.put("prorateDays", Integer.parseInt(etProrateDays.getText().toString()));
                        property.put("nextRentDue", calendar.getSelectedDate().getDate());

                        //Save both mgrSettings and property in back ground
                        mgrSettings.saveInBackground();
                        property.saveInBackground();

                        dialog.dismiss(); //done, so dismiss the dialog
                    }
                })
                        .show();
            }
        });

    }



    private String combineChecks(){
        String ans = checkStripePay() + checkOccupied() + checkRent() + checkDate();
        return ans;
    }


    private String checkStripePay(){
        String ans = "";
        if(swUseStripe.isChecked() != stripePay){
            String onOff = (swUseStripe.isChecked() ? "On" : "Off");
            ans = "Stripe Payments : " + onOff + "\n";
        }
        return ans;
    }
    private String checkOccupied(){
        String ans = "";
        if(cbOccupied.isChecked() != occupied){
            String yesNo = (cbOccupied.isChecked() ? "Occupied" : "Not Occupied");
            ans = "Property : " + yesNo + "\n";
        }
        return ans;
    }
    private String checkRent(){
        String ans = "";
        String newRent = etMonthlyRentDue.getText().toString();

        if(!rentdue.equals(fixAmountForStore(newRent))){
            ans = "Rent : $" + fixAmountForDisplay(newRent) + "\n";
        }
        return ans;
    }
    private String checkProrate(){
        String ans = "";
        String newProrate = etProrateDays.getText().toString();
        if(Integer.parseInt(newProrate) != prorateDays){
            ans = "Prorate : " + newProrate + " Days\n";
        }
        return ans;
    }
    private String checkDate(){
        String ans = "";
        DateTime newDt,oldDt;
        oldDt = new DateTime(rentDueDate);
        newDt = new DateTime(calendar.getSelectedDate().getDate());
        if(oldDt.getMonthOfYear() != newDt.getMonthOfYear() ||
                oldDt.getYear() != newDt.getYear() ||
                oldDt.getDayOfMonth() != newDt.getDayOfMonth()){ //if the day isn't the same
            Log.d(TAG,Integer.toString(oldDt.getMonthOfYear()) + " " + Integer.toString(oldDt.getDayOfMonth())+" " + Integer.toString(oldDt.getYear()));
            Log.d(TAG,Integer.toString(newDt.getMonthOfYear()) + " " + Integer.toString(newDt.getDayOfMonth()) + " " + Integer.toString(newDt.getYear()));

            ans = "Rent Due : " + newDt.toString("MMM d, yyyy");
        }
        return ans;
    }

    public boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private String fixAmountForDisplay(String amount){
        String ans;
        if(amount.contains(".")){
            //Log.d(TAG,amount);
            String[] ansSplit = amount.split("[.]");
            //Log.d(TAG,ansSplit[0]);
            //Log.d(TAG,ansSplit[1]);
            if(ansSplit[1].length() > 2){
                ansSplit[1] = ansSplit[1].substring(0,2);
                ans = ansSplit[0] + "." + ansSplit[1];
            }
            else if(ansSplit[1].length() == 2){
                ans = ansSplit[0] + "." + ansSplit[1];
            }
            else if(ansSplit[1].length() == 1){
                ans = ansSplit[0] + "." + ansSplit[1] + "0";
            }
            else{
                ans = ansSplit[0] + "." +"00";
            }
        }
        else{
            ans = amount + "." + "00";
        }
        return ans;
    }
    private String fixAmountForStore(String amount){
        String ans;

        if(amount.contains(".")){
            Log.d(TAG,amount);
            String[] ansSplit = amount.split("[.]");
            Log.d(TAG,ansSplit[0]);
            Log.d(TAG,ansSplit[1]);
            if(ansSplit[1].length() > 2){
                ansSplit[1] = ansSplit[1].substring(0,2);
                ans = ansSplit[0] + ansSplit[1];
            }
            else if(ansSplit[1].length() == 2){
                ans = ansSplit[0] + ansSplit[1];
            }
            else if(ansSplit[1].length() == 1){
                ans = ansSplit[0] + ansSplit[1] + "0";
            }
            else{
                ans = ansSplit[0] + "00";
            }
        }
        else{
            ans = amount += "00";
        }
        return ans;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
