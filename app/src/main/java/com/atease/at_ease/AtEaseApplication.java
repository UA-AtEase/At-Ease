package com.atease.at_ease;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.parse.Parse;

/**
 * Created by Mark on 10/8/2015.
 */
public class AtEaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "RWYMOqEP4OgQ4oZIqFjyqHGxG7uYzbaPDWuzvZPq", "WJgweZXzSzoCgsdwgv5h5VzHwryAiAV1FvTFrZyF");
    }

    public DrawerBuilder getNewDrawerBuilder() {
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("home");
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withName("settings");

        return new DrawerBuilder()
                .withTranslucentNavigationBar(false)
                .withActionBarDrawerToggle(false)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new SecondaryDrawerItem().withName("secondary2")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        Log.d("At-Ease", "Pos: " + position);
                        return true;
                    }
                });
    }
}