package hp.test.mytv.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.List;

import hp.test.mytv.R;
import hp.test.mytv.activity.MovieDetail;
import hp.test.mytv.model.on_air.OnAirItem;
import hp.test.mytv.model.sql_lite.OnAir;
import hp.test.mytv.utils.DatabaseHelper;


public class OnAirAdapterSQL extends RecyclerView.Adapter<OnAirAdapterSQL.OnAirViewHolder> {

    private List<OnAir> data;
    private Context mContext;
    public OnAirAdapterSQL(List<OnAir> inputData,Context context) {
        data = inputData;
        mContext=context;
        DatabaseHelper databaseHelper = new DatabaseHelper(mContext);
    }

    class OnAirViewHolder extends RecyclerView.ViewHolder {
        TextView tvMainTitle;
        TextView tvTitle;
        TextView tvDesc;
        ImageView ivPoster;
        ToggleButton btnFavorite;

        OnAirViewHolder(View view) {
            super(view);
            tvMainTitle = view.findViewById(R.id.tv_mainTitle);
            tvTitle = view.findViewById(R.id.tv_title);
            btnFavorite = view.findViewById(R.id.btn_favorite);
            ivPoster = view.findViewById(R.id.iv_poster);
            tvDesc = view.findViewById(R.id.tv_description);
        }


    }


    @NonNull
    @Override
    public OnAirViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_main, parent, false);
        return new OnAirViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull final OnAirViewHolder holder, @SuppressLint("RecyclerView") final int position) {


        final DatabaseHelper databaseHelper = new DatabaseHelper(holder.tvDesc.getContext());

        holder.tvMainTitle.setText(data.get(position).getOriginalName());
        holder.tvTitle.setText(data.get(position).getName());
        holder.tvDesc.setText(data.get(position).getOverview());
        String imgUrl = "http://image.tmdb.org/t/p/w300/" + data.get(position).getPosterPath();

        Picasso.get().load(imgUrl).into(holder.ivPoster);

        holder.btnFavorite.setOnCheckedChangeListener(null);
        holder.btnFavorite.setChecked(data.get(position).getFavorite());
        holder.btnFavorite.setBackgroundDrawable(ContextCompat.getDrawable(mContext, data.get(position).getFavorite()?R.drawable.ic_favorite_24dp:R.drawable.ic_favorite_black_24dp));
        holder.btnFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                data.get(position).setFavorite(isChecked);
                if (isChecked) {
                    holder.btnFavorite.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_black_24dp));
                    databaseHelper.addFavorite(data.get(position));
                } else {
                    holder.btnFavorite.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_24dp));
                    databaseHelper.removeFavorite(data.get(position).getId());
                }
                notifyDataSetChanged();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MovieDetail.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("SHOW_ID",data.get(position).getId());
                intent.putExtra("SHOW_NAME",data.get(position).getOriginalName());
                intent.putExtra("SHOW_SUBNAME",data.get(position).getName());
                intent.putExtra("FAVORITE",data.get(position).getFavorite());
                intent.putExtra("OVERVIEW",data.get(position).getOverview());
                mContext.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {

        return data.size();
    }

}
