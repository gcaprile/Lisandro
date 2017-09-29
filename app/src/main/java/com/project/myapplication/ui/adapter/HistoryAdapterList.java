package com.project.myapplication.ui.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.myapplication.R;
import com.project.myapplication.model.UserLocation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryAdapterList extends RecyclerView.Adapter<HistoryAdapterList.HistoryViewHolder>{

    private List<UserLocation> mLocationsList;

    public HistoryAdapterList(List<UserLocation> locationList){
        this.mLocationsList = locationList;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);

        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        holder.tvDate.setText(getDateFromMilis(mLocationsList.get(position).getId()));

        String location = String.valueOf(mLocationsList.get(position).getLatitude()) + ", " + String.valueOf(mLocationsList.get(position).getLongitude());

        holder.tvCoordinates.setText(location);
    }

    @Override
    public int getItemCount() {
        return mLocationsList.size();
    }

    private  String getDateFromMilis(long milis){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milis);

        return formatter.format(calendar.getTime());
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.textView3)
        TextView tvDate;

        @BindView(R.id.textView4)
        TextView tvCoordinates;

        HistoryViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
