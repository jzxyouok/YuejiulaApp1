package com.liangxun.yuejiula.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.liangxun.yuejiula.R;
import com.liangxun.yuejiula.entity.Relate;
import com.liangxun.yuejiula.util.StringUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yixia.camera.demo.UniversityApplication;

import java.util.List;

/**
 * author: ${zhanghailong}
 * Date: 2015/2/6
 * Time: 14:06
 * 类的功能、说明写在此处.
 */
public class AndMeAdapter extends BaseAdapter {
    private ViewHolder holder;
    private List<Relate> findEmps;
    private Context mContext;

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    ImageLoader imageLoader = ImageLoader.getInstance();//图片加载类

    public AndMeAdapter(List<Relate> findEmps, Context mContext) {
        this.findEmps = findEmps;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return findEmps.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.andme_item_xml, null);
            holder.andme_item_cover = (ImageView) convertView.findViewById(R.id.andme_item_cover);
            holder.andme_item_nickname = (TextView) convertView.findViewById(R.id.andme_item_nickname);
            holder.andme_item_cont = (TextView) convertView.findViewById(R.id.andme_item_cont);
            holder.andme_item_dateline = (TextView) convertView.findViewById(R.id.andme_item_dateline);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Relate favour = findEmps.get(position);
        if (findEmps != null) {
            imageLoader.displayImage(favour.getEmpCover(), holder.andme_item_cover, UniversityApplication.txOptions, animateFirstListener);
            holder.andme_item_nickname.setText(StringUtil.replaceBlank(favour.getEmpName()));
            holder.andme_item_cont.setText(favour.getCont());
            holder.andme_item_dateline.setText(favour.getDateline());
        }
        return convertView;
    }

    class ViewHolder {
        ImageView andme_item_cover;
        TextView andme_item_nickname;
        TextView andme_item_cont;
        TextView andme_item_dateline;

    }

}