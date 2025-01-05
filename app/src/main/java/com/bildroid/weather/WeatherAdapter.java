package com.bildroid.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    Context context;
    ArrayList<WeatherModel> weatherModels;

    public WeatherAdapter(Context context, ArrayList<WeatherModel> weatherModels) {
        this.context = context;
        this.weatherModels = weatherModels;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {

        WeatherModel model= weatherModels.get(position);
        holder.idTemperature.setText(model.getTemperature() + "℃");
        Picasso.get().load("http:".concat(model.getIcon())).into(holder.idCondition);
        holder.idWindSpeed.setText(model.getWindSpeed() + "Km/h");
        SimpleDateFormat input = new SimpleDateFormat("yyyy-mm-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try {
            Date date = input.parse(model.getTime());
            holder.idTime.setText(output.format(date));
        }catch (ParseException e){
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return weatherModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView idTime, idWindSpeed, idTemperature;
        ImageView idCondition;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            idTime=itemView.findViewById(R.id.idTime);
            idWindSpeed=itemView.findViewById(R.id.idWindSoeed);
            idTemperature=itemView.findViewById(R.id.idTemperature);
            idCondition=itemView.findViewById(R.id.idCondition);
        }
    }
}
