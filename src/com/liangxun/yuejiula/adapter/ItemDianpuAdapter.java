package com.liangxun.yuejiula.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.liangxun.yuejiula.R;
import com.liangxun.yuejiula.entity.EmpDianpu;
import com.liangxun.yuejiula.entity.PaopaoGoods;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yixia.camera.demo.UniversityApplication;

import java.util.List;

/**
 * author: ${zhanghailong}
 * Date: 2014/8/6
 * Time: 8:47
 * 类的功能、说明写在此处.
 */
public class ItemDianpuAdapter extends BaseAdapter {
    private ViewHolder holder;
    private List<EmpDianpu> list;
    private Context context;
    Resources res;

    public ItemDianpuAdapter(List<EmpDianpu> list, Context context) {
        res = context.getResources();
        this.list = list;
        this.context = context;
    }

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    ImageLoader imageLoader = ImageLoader.getInstance();//图片加载类

    private OnClickContentItemListener onClickContentItemListener;

    public void setOnClickContentItemListener(OnClickContentItemListener onClickContentItemListener) {
        this.onClickContentItemListener = onClickContentItemListener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount()
    {
        // TODO Auto-generated method stub
        return 2;
    }

    @Override
    public int getItemViewType(int position)
    {
        // TODO Auto-generated method stub
        if (position == 0)
        {
            return 0;
        }
        return 1;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_dianpu, null);
            holder.item_cover = (ImageView) convertView.findViewById(R.id.item_cover);
            holder.item_title = (TextView) convertView.findViewById(R.id.item_title);
            holder.item_location = (ImageView) convertView.findViewById(R.id.item_location);
            holder.head = (ImageView) convertView.findViewById(R.id.head);
            holder.item_address = (TextView) convertView.findViewById(R.id.item_address);
            holder.item_nickname = (TextView) convertView.findViewById(R.id.item_nickname);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        EmpDianpu cell = list.get(position);
        holder.item_address.setText(cell.getCompany_address());
        holder.item_title.setText(cell.getCompany_name());
        holder.item_nickname.setText(cell.getCompany_person());
        //加载图片
        imageLoader.displayImage(cell.getCompany_pic(), holder.item_cover, UniversityApplication.options, animateFirstListener);
        imageLoader.displayImage(cell.getEmpCover(), holder.head, UniversityApplication.txOptions, animateFirstListener);
        return convertView;
    }

    class ViewHolder {
        ImageView item_cover;
        TextView item_title;
        ImageView item_location;
        ImageView head;
        TextView item_nickname;
        TextView item_address;
    }
}
