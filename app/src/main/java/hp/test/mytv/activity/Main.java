package hp.test.mytv.activity;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hp.test.mytv.R;
import hp.test.mytv.adapter.OnAirAdapterSQL;
import hp.test.mytv.model.on_air.OnAirResult;
import hp.test.mytv.model.sql_lite.OnAir;
import hp.test.mytv.services.FetchJobService;
import hp.test.mytv.utils.APIClient;
import hp.test.mytv.utils.DatabaseHelper;
import hp.test.mytv.utils.TMDBInterface;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MainActivity";

    Toolbar tb;

    OnAirResult onAirResult;
    List<OnAir>  onAirItems = new ArrayList<OnAir>();

    RecyclerView recyclerView;
    OnAirAdapterSQL mAdapter;
    ProgressBar loadingLayer;

    TMDBInterface tmdbInterface;


    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration config = getBaseContext().getResources().getConfiguration();

        String lang = settings.getString("LANG", "");
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final NavigationView navView = findViewById(R.id.nav_view);

        //Initialize Toolbar

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize Recycle view

        loadingLayer = findViewById(R.id.progressPanel);

        recyclerView = findViewById(R.id.rv);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new OnAirAdapterSQL(onAirItems,getApplicationContext());

        recyclerView.setAdapter(mAdapter);
        //Initialize drawer

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Api Client

        tmdbInterface = APIClient.getClient().create(TMDBInterface.class);

        //Initialize Data

        refreshRv2();

        RunJobScheduler();
    }

    private void RunJobScheduler(){
        ComponentName componentName = new ComponentName(this, FetchJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(5 * 60 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshRv2();
    }

    private void refreshRv2(){
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        onAirItems.clear();
        onAirItems.addAll(databaseHelper.getOnAirs());
        loadingLayer.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();
    }

//    private void refreshRv(){
//        int nextPage = 0;
//        if (this.onAirResult!=null){
//            if (this.onAirResult.getTotalPages()>nextPage) {
//                nextPage = this.onAirResult.getPage() + 1;
//            }
//
//        }else{
//            nextPage=1;
//        }
//
//        final Call<OnAirResult> onAirResultCall = tmdbInterface.getOnAir(nextPage);
//
//        onAirResultCall.enqueue(new Callback<OnAirResult>() {
//            @Override
//            public void onResponse(Call<OnAirResult> call, Response<OnAirResult> response) {
//                assert response.body() != null;
//                setData(response.body());
//                loadingLayer.setVisibility(View.GONE);
//                recyclerView.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onFailure(Call<OnAirResult> call, Throwable t) {
//
//            }
//        });
//
//
//    }

//    private void setData(@NonNull OnAirResult onAirResult){
//        this.onAirResult = onAirResult;
//        this.onAirItems.addAll(onAirResult.getResults());
//        mAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //@Override
   // public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
   //     getMenuInflater().inflate(R.menu.main, menu);
   //     return true;
   // }

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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){

            case R.id.nav_setting:
                Intent setting = new Intent(Main.this,Setting.class);
                startActivity(setting);
                break;
            case R.id.nav_favorite:
                Intent favorite = new Intent(Main.this,Favorite.class);
                startActivity(favorite);
                break;
            case R.id.nav_info:
                Intent info = new Intent(Main.this,Info.class);
                startActivity(info);
                break;

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
