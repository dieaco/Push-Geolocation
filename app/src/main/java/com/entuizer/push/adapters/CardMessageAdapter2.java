package com.entuizer.push.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entuizer.push.R;
import com.entuizer.push.interfaces.OnItemClickListener;
import com.entuizer.push.models.Message;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diego Acosta on 25/04/2016.
 */
public class CardMessageAdapter2 extends  RecyclerView.Adapter<CardMessageAdapter2.ViewHolder>{

    ArrayList<Message> items;
    Context context;
    OnItemClickListener onItemClickListener;

    public CardMessageAdapter2(Context context, ArrayList<Message> items){
        super();
        this.items = items;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_card_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message list =  items.get(position);

        //Is Picture null?
        Log.i("PICTURE", "MESSAGE_ID: " + list.getId() + " - " + "VALUE: " + list.getPicture());

        if(list.getPicture() == null){
            holder.ivPicture.setVisibility(View.GONE);
            holder.layoutParams.weight = 3.0f;
        }else{
            holder.ivPicture.setVisibility(View.VISIBLE);
            holder.layoutParams.weight = 2.0f;
        }

        holder.ivPicture.setImageBitmap(list.getPicture());
        holder.txtMensaje.setText(list.getMensaje());
        holder.txtTimestamp.setText(list.getTimestamp());
        holder.txtId.setText(list.getId());

        int isReadFromServer = list.getIsRead();

        //Asigna status correspondiente para cada notificación dependiendo del usuario del dispositivo
        if(isReadFromServer == 1){
            holder.ivIsRead.setImageResource(R.drawable.bullet);
        }else{
            holder.ivIsRead.setImageResource(R.drawable.bullet_gray);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView ivPicture;
        public TextView txtMensaje;
        public TextView txtTimestamp;
        public TextView txtId;
        public ImageView ivIsRead;
        public LinearLayout llData;

        public LinearLayout.LayoutParams layoutParams;

        public ViewHolder(View itemView) {
            super(itemView);

            ivPicture = (ImageView) itemView.findViewById(R.id.ivPicture);
            txtMensaje = (TextView) itemView.findViewById(R.id.txtMessage);
            txtTimestamp = (TextView) itemView.findViewById(R.id.txtTimestamp);
            txtId = (TextView) itemView.findViewById(R.id.txtMessageId);
            ivIsRead = (ImageView) itemView.findViewById(R.id.ivIsRead);
            llData = (LinearLayout)itemView.findViewById(R.id.llData);

            layoutParams = (LinearLayout.LayoutParams)llData.getLayoutParams();

        }

        @Override
        public void onClick(View v) {

        }
    }

}
