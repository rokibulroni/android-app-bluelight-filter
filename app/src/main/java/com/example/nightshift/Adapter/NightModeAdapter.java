package com.example.nightshift.Adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nightshift.Listener.IOnClickRecylerView;
import com.example.nightshift.Model.NightMode;
import com.example.nightshift.R;

import java.util.List;

/**
 * Created by SonPham on 18/07/2019
 * Company : Gpaddy
 */

public class NightModeAdapter extends RecyclerView.Adapter<NightModeAdapter.ViewHolder> {
    private Context context;
    private List<NightMode> arrNightModes;
    private IOnClickRecylerView onClickRecylerView;

    public NightModeAdapter(Context context, List<NightMode> arrNightModes, IOnClickRecylerView onClickRecylerView) {
        this.context = context;
        this.arrNightModes = arrNightModes;
        this.onClickRecylerView = onClickRecylerView;
    }

    @NonNull
    @Override
    public NightModeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_night_mode, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final NightModeAdapter.ViewHolder viewHolder, final int position) {
        bind(viewHolder, position);
    }

    @Override
    public void onBindViewHolder(@NonNull final NightModeAdapter.ViewHolder viewHolder, int position, List<Object> payload) {
        bind(viewHolder, position);
    }

    private void bind(ViewHolder viewHolder, final int position) {
        final NightMode nightMode = arrNightModes.get(position);

        if (nightMode.isChoose()) {
            viewHolder.imgAvatar.setImageResource(nightMode.getAvatarClick());
        } else {
            viewHolder.imgAvatar.setImageResource(nightMode.getAvatar());
        }

        viewHolder.imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRecylerView.onClick(position);
                updateItem(position);
            }
        });
    }

    private void updateItem(int position) {
        for (int i = 0; i < arrNightModes.size(); i++) {
            if (arrNightModes.get(i).isChoose()) {
                arrNightModes.get(i).setChoose(false);
                notifyItemChanged(i, "payload " + i);
            }
        }

        arrNightModes.get(position).setChoose(true);
        notifyItemChanged(position, "payload " + position);
    }

    @Override
    public int getItemCount() {
        return arrNightModes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
