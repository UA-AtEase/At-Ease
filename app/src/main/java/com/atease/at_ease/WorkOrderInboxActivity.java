package com.atease.at_ease;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.software.shell.fab.ActionButton;

import java.util.ArrayList;
import java.util.List;

import co.dift.ui.SwipeToAction;

/**
 * Created by Mark on 10/3/2015.
 */
public class WorkOrderInboxActivity extends AppCompatActivity {
    private Toolbar toolbar;
    RecyclerView recyclerView;
    WorkOrderInboxRecyclerViewAdapter adapter;
    SwipeToAction swipeToAction;

    List<WorkOrder> workOrderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_order_inbox);

        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call

        AtEaseApplication application = (AtEaseApplication) getApplicationContext();
        application.getNewDrawerBuilder().withActivity(this).build();

        ActionButton actionButton = (ActionButton) findViewById(R.id.action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkOrderInboxActivity.this, NewWorkOrderExpandableActivity.class);
                startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new WorkOrderInboxRecyclerViewAdapter(this.workOrderList);
        recyclerView.setAdapter(adapter);

        swipeToAction = new SwipeToAction(recyclerView, new SwipeToAction.SwipeListener<WorkOrder>() {
            @Override
            public boolean swipeLeft(final WorkOrder inWorkOrder) {
                final int pos = removeWorkOrder(inWorkOrder);
                new MaterialDialog.Builder(WorkOrderInboxActivity.this)
                        .title("Confirm Deletion")
                        .content("Are you sure you want to cancel this Work Order? This action can not be undone!")
                        .positiveText("Delete")
                        .negativeText("Cancel")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                return;
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ParseUser currentUser = ParseUser.getCurrentUser();
                                if(inWorkOrder.getTenant() == currentUser) {
                                    inWorkOrder.deleteInBackground(new DeleteCallback() {
                                        public void done(ParseException e) {
                                            if (e != null) {
                                                // There was some error.
                                                Log.d("At-Ease", "Work order deletion not successful: " + e.getMessage());
                                                displaySnackbar(inWorkOrder.getSubject() + " deletion not successful", null, null);
                                                return;
                                            } else {
                                                Log.i("At-Ease", "Work order deleted");
                                                displaySnackbar(inWorkOrder.getSubject() + " deleted", null, null);
                                            }
                                        }
                                    });
                                } else {
                                    Log.i("At-Ease", "Managers are not allowed to cancel a work order");
                                    displaySnackbar("Managers are not allowed to cancel a work order", null, null);
                                }

                            }
                        })
                        .show();
//                displaySnackbar(inWorkOrder.getSubject() + " removed", "Undo", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        addWorkOrder(pos, inWorkOrder);
//                    }
//                });
                return true;
            }

            @Override
            public boolean swipeRight(WorkOrder inWorkOrder) {
                displaySnackbar(inWorkOrder.getSubject() + " loved", null, null);
                return true;
            }

            @Override
            public void onClick(WorkOrder inWorkOrder) {
                displaySnackbar(inWorkOrder.getSubject() + " clicked", null, null);
                Intent intent = new Intent(WorkOrderInboxActivity.this, ViewWorkOrderActivity.class);
                Log.d("At-Ease", ":" + inWorkOrder.getObjectId());
                intent.putExtra("workOrder", inWorkOrder.getObjectId());
                startActivity(intent);
            }

            @Override
            public void onLongClick(WorkOrder inWorkOrder) {
                displaySnackbar(inWorkOrder.getSubject() + " long-clicked", null, null);
            }
        });

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            Log.i("At-Ease", "Populating inbox for current user: "
                    + currentUser.getUsername());
            populateFromParse(currentUser);
        } else {
            // show the signup or login screen
            Log.d("MYAPPTAG", "No Current User, populating with default");
            populate();
        }

    }

    private void populate() {
    }

    private void populateFromParse(final ParseUser user) {
        ParseQuery<WorkOrder> tenantQuery = ParseQuery.getQuery("WorkOrder");
        ParseQuery<WorkOrder> managerQuery = ParseQuery.getQuery("WorkOrder");
        ParseQuery<WorkOrder> query = ParseQuery.getQuery("WorkOrder");
        if(user.getBoolean("isTenant") && user.getBoolean("isManager")) {
            tenantQuery.whereEqualTo("tenant", user);
            managerQuery.whereEqualTo("manager", user);
            List<ParseQuery<WorkOrder>> queries = new ArrayList<>();
            queries.add(tenantQuery);
            queries.add(managerQuery);
            query = ParseQuery.or(queries);
        }
        else if(user.getBoolean("isTenant")) {
            query.whereEqualTo("tenant", user);
        }
        else if(user.getBoolean("isManager")) {
            query.whereEqualTo("manager", user);
        }

        query.findInBackground(new FindCallback<WorkOrder>() {
            @Override
            public void done(List<WorkOrder> results, ParseException e) {
                if (e == null) {
                    Log.d("At-Ease", "Retrieved " + results.size() + " work orders");
                    int i = 0;
                    for (WorkOrder workOrder : results) {
                        WorkOrderInboxActivity.this.workOrderList.add(workOrder);
                        Log.d("At-Ease", "Created work order " + Integer.toString(i));
                        i++;
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d("At-Ease", "Error: " + e.getMessage());
                }
            }
        });
        Log.i("At-Ease", "Populated " + workOrderList.size() + " items from Parse");
        adapter.notifyDataSetChanged();
    }

    private void displaySnackbar(String text, String actionName, View.OnClickListener action) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction(actionName, action);

        View v = snackbar.getView();
        v.setBackgroundColor(getResources().getColor(R.color.secondary));
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(Color.BLACK);

        snackbar.show();
    }

    private int removeWorkOrder(WorkOrder workOrder) {
        int pos = workOrderList.indexOf(workOrder);
        workOrderList.remove(workOrder);
        adapter.notifyItemRemoved(pos);
        return pos;
    }

    private void addWorkOrder(int pos, WorkOrder workOrder) {
        workOrderList.add(pos, workOrder);
        adapter.notifyItemInserted(pos);
    }
}


