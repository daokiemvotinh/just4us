package com.dkvt.justforus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dkvt.justforus.R;
import com.dkvt.justforus.externalclass.ImageLoader;
import com.dkvt.justforus.externalclass.ResizableImageView;

import java.util.ArrayList;
import java.util.HashMap;

public class WallAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<HashMap<String, String>> data;
    public ImageLoader imageLoader;

    public WallAdapter(Context mContext,
                       ArrayList<HashMap<String, String>> paramArrayList) {
        this.mContext = mContext;
        this.data = paramArrayList;
        this.imageLoader = new ImageLoader(mContext);
    }

    public int getCount() {
        return this.data.size();
    }

    public Object getItem(int paramInt) {
        return this.data.get(paramInt);
    }

    public long getItemId(int paramInt) {
        return paramInt;
    }

    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
        ViewHolder holder;
        if (paramView == null) {
            LayoutInflater mInflater = LayoutInflater.from(mContext);
            paramView = mInflater.inflate(R.layout.wall_item, paramViewGroup,false);
            holder = new ViewHolder();
            paramView.setTag(holder);
        } else {
            holder = (ViewHolder) paramView.getTag();
        }

        holder.imgMain=(ResizableImageView) paramView.findViewById(R.id.list_image);
        holder.btnEdit=(ImageButton) paramView.findViewById(R.id.btn_edit);
        holder.btnDelete= (ImageButton) paramView.findViewById(R.id.btn_delete);
        holder.status = (TextView) paramView.findViewById(R.id.name);
        holder.time = (TextView) paramView.findViewById(R.id.time);


        HashMap localHashMap = (HashMap) this.data.get(paramInt);
        imageLoader.DisplayImage((String) localHashMap.get("pathImgStory"), holder.imgMain, null);
        holder.status.setText((CharSequence) localHashMap.get("comment"));
        holder.time.setText((CharSequence) localHashMap.get("timePost"));
        return paramView;
    }
    public class ViewHolder {
        ResizableImageView imgMain;
        ImageButton btnEdit;
        ImageButton btnDelete;
        TextView status;
        TextView time;
    }
}
