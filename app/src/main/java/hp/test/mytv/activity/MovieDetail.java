package hp.test.mytv.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Objects;

import hp.test.mytv.R;
import hp.test.mytv.adapter.DetailGenreAdapter;
import hp.test.mytv.adapter.DetailNetworkAdapter;
import hp.test.mytv.model.show_detail.ShowDetailResult;
import hp.test.mytv.model.sql_lite.OnAir;
import hp.test.mytv.utils.APIClient;
import hp.test.mytv.utils.DatabaseHelper;
import hp.test.mytv.utils.ExpandableHeightGridView;
import hp.test.mytv.utils.TMDBInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetail extends AppCompatActivity {

    private ImageView ivBackdropPath;
    private ProgressBar pgLoadingPoster;
    private TMDBInterface tmdbInterface;
    private TextView tvOverview;
    private TextView tvRating;
    private TextView tvRatingCount;
    private TextView tvPopularity;
    private TextView tvStatus;
    private TextView tvSubname;
    private TextView tvLastAirDate;
    private RecyclerView rvNetwork;
    private Target mTarget;
    private FloatingActionButton fab;
    private ExpandableHeightGridView gvGenres;


    private DatabaseHelper databaseHelper;
    private boolean favorite;

    @SuppressLint({"RestrictedApi", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ivBackdropPath = findViewById(R.id.iv_collapsing_toolbar);
        pgLoadingPoster = findViewById(R.id.pg_loading_poster);
        tvOverview = findViewById(R.id.tv_overview);
        tvPopularity = findViewById(R.id.tv_popularity);
        tvRating = findViewById(R.id.tv_rating);
        tvRatingCount = findViewById(R.id.tv_rating_count);
        tvStatus = findViewById(R.id.tv_status);
        gvGenres = findViewById(R.id.gv_genres);
        tvLastAirDate = findViewById(R.id.tv_last_air_date);
        tvSubname = findViewById(R.id.tv_subname);

        rvNetwork = findViewById(R.id.rv_network);
        rvNetwork.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        gvGenres.setNumColumns(2);
        gvGenres.setExpanded(true);

        databaseHelper = new DatabaseHelper(getApplicationContext());

        final int showID = (int) getIntent().getIntExtra("SHOW_ID",0);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getIntent().getStringExtra("SHOW_NAME"));
        tvSubname.setText(getIntent().getStringExtra("SHOW_SUBNAME"));
        favorite = getIntent().getBooleanExtra("FAVORITE",false);
        tvOverview.setText(getIntent().getStringExtra("OVERVIEW"));

        tmdbInterface = APIClient.getClient().create(TMDBInterface.class);


        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        setData(showID);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageDrawable(getDrawable(favorite?R.drawable.ic_favorite_black_24dp:R.drawable.ic_unfavorite_24dp));



    }

    public boolean updateFavorite(){
        favorite = ! favorite;
        return favorite;
    }

    private void setData(int id){

        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int width = displayMetrics.widthPixels;
                ivBackdropPath.getLayoutParams().height = (int) ((double)bitmap.getHeight()/bitmap.getWidth()*width);
                ivBackdropPath.requestLayout();
                ivBackdropPath.setImageBitmap(bitmap);

                ivBackdropPath.setVisibility(View.VISIBLE);
                pgLoadingPoster.setVisibility(View.GONE);

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };


        final Call<ShowDetailResult> movieDetail = tmdbInterface.getMovieDetail(id);
        movieDetail.enqueue(new Callback<ShowDetailResult>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<ShowDetailResult> call, @NonNull final Response<ShowDetailResult> response) {
                assert response.body() != null;
                ShowDetailResult movieDetailResult = response.body();
                String imgUrl = "http://image.tmdb.org/t/p/w780" + movieDetailResult.getPosterPath();
                Log.d("imgURL", String.valueOf(movieDetailResult.getNetworks()==null));

                tvPopularity.setText(Double.toString(movieDetailResult.getPopularity()));
                tvRating.setText(Double.toString(movieDetailResult.getVoteAverage()));
                tvRatingCount.setText(Integer.toString(movieDetailResult.getVoteCount()));
                tvStatus.setText(
                        movieDetailResult.getInProduction() ? "On Air":"Finish Airing"
                );

                rvNetwork.setAdapter(new DetailNetworkAdapter(movieDetailResult.getNetworks()));

                tvLastAirDate.setText(movieDetailResult.getLastAirDate());

                gvGenres.setAdapter(new DetailGenreAdapter(movieDetailResult.getGenres(),tvOverview.getContext()));

                Picasso.get().load(imgUrl).into(mTarget);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (updateFavorite()){
                            fab.setImageDrawable(getDrawable(R.drawable.ic_favorite_black_24dp));
                            databaseHelper.addFavorite(
                                    new OnAir(
                                            response.body().getId(),
                                            response.body().getOriginalName(),
                                            response.body().getName(),
                                            response.body().getOverview(),
                                            response.body().getPosterPath(),true
                                    )
                            );
                        }else{
                            fab.setImageDrawable(getDrawable(R.drawable.ic_unfavorite_24dp));
                            databaseHelper.removeFavorite(response.body().getId());
                        };

                    }
                });


            }

            @Override
            public void onFailure(Call<ShowDetailResult> call, Throwable t) {

            }
        });

    }


}

