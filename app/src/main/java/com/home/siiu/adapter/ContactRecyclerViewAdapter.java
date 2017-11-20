package com.home.siiu.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.home.siiu.R;
import com.home.siiu.activity.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Tim Kern on 11/19/2017.
 */

public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.MyViewHolder> {

    Context context;
    public List<String> imgName;
    public List<String> userNames;
    public List<String> userIds;
    public Boolean isSearchOrContact;

    public ContactRecyclerViewAdapter(Context context, List<String> img, List<String> username, List<String> userid, Boolean searchorcontact) {

        this.context = context;
        this.imgName = img;
        this.userNames = username;
        this.userIds = userid;
        isSearchOrContact = searchorcontact;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Picasso.with(holder.avatarImg.getContext()).load(imgName.get(position)).into(holder.avatarImg);
        holder.userName.setText(userNames.get(position));
        if (isSearchOrContact) {
            holder.actionImg.setImageResource(R.drawable.add);
        } else {
            holder.actionImg.setImageResource(R.drawable.nextscreen);
        }
    }

    @Override
    public int getItemCount() {

        return imgName.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView avatarImg;
        TextView userName;
        ImageView actionImg;

        public MyViewHolder(View itemView) {
            super(itemView);
            avatarImg = (CircleImageView)itemView.findViewById(R.id.Avatar);
            userName = itemView.findViewById(R.id.UserName);
            actionImg = itemView.findViewById(R.id.ContactAction);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int pos = getAdapterPosition();
            if (isSearchOrContact) {

                ((MainActivity)context).addContact(imgName.get(pos), userNames.get(pos), userIds.get(pos), pos);
            } else {

                ((MainActivity)context).gotoFragment(1);
            }
        }
    }
}
