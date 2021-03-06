package com.liangxun.yuejiula.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.liangxun.yuejiula.R;
import com.liangxun.yuejiula.entity.Record;
import com.liangxun.yuejiula.face.FaceConversionUtil;
import com.liangxun.yuejiula.ui.GalleryUrlActivity;
import com.liangxun.yuejiula.util.Constants;
import com.liangxun.yuejiula.util.StringUtil;
import com.liangxun.yuejiula.widget.PictureGridview;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yixia.camera.demo.UniversityApplication;

import java.util.List;

/**
 * 动态
 */
public class MineRecordAdapter extends BaseAdapter {
    private ViewHolder holder;
    private List<Record> records;
    private Context mContext;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    ImageLoader imageLoader = ImageLoader.getInstance();//图片加载类

    private OnClickContentItemListener onClickContentItemListener;

    public void setOnClickContentItemListener(OnClickContentItemListener onClickContentItemListener) {
        this.onClickContentItemListener = onClickContentItemListener;
    }

    public MineRecordAdapter(List<Record> records, Context mContext) {
        this.records = records;
        this.mContext = (Activity) mContext;

    }

    @Override
    public int getCount() {
        return records.size();
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.home_item, null);
            holder.home_photo_item_comment_count = (TextView) convertView.findViewById(R.id.home_photo_item_comment_count);
            holder.home_viewed_item_cover = (ImageView) convertView.findViewById(R.id.home_viewed_item_cover);
            holder.home_viewed_item_name = (TextView) convertView.findViewById(R.id.home_viewed_item_name);
            holder.home_viewed_item_time = (TextView) convertView.findViewById(R.id.home_viewed_item_time);
            holder.home_viewed_item_cont = (TextView) convertView.findViewById(R.id.home_viewed_item_cont);
            holder.home_photo_item_photo = (ImageView) convertView.findViewById(R.id.home_photo_item_photo);
            holder.gridview_detail_picture = (PictureGridview) convertView.findViewById(R.id.gridview_detail_picture);
            holder.home_photo_item_photo_video = (ImageView) convertView.findViewById(R.id.home_photo_item_photo_video);
            holder.mine_home_item_delete = (TextView) convertView.findViewById(R.id.home_photo_item_delete);
            holder.home_photo_item_level = (TextView) convertView.findViewById(R.id.home_photo_item_level);
            holder.home_photo_item_like_count = (TextView) convertView.findViewById(R.id.home_photo_item_like_count);
            holder.home_item_school = (TextView) convertView.findViewById(R.id.home_item_school);
            holder.home_player_icon_video = (ImageView) convertView.findViewById(R.id.home_player_icon_video);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.home_photo_item_photo.setVisibility(View.GONE);
        holder.home_photo_item_level.setVisibility(View.GONE);
        holder.gridview_detail_picture.setVisibility(View.GONE);//隐藏九宫格
        holder.gridview_detail_picture.setSelector(new ColorDrawable(Color.TRANSPARENT));
        holder.mine_home_item_delete.setVisibility(View.VISIBLE);
        holder.home_photo_item_photo_video.setVisibility(View.GONE);
        holder.home_player_icon_video.setVisibility(View.GONE);
        holder.home_viewed_item_cont.setVisibility(View.GONE);
        final Record cell = records.get(position);//获得元素
        if (cell != null) {
            imageLoader.displayImage(cell.getEmpCover(), holder.home_viewed_item_cover, UniversityApplication.txOptions, animateFirstListener);
            holder.home_photo_item_comment_count.setText(cell.getPlNum());
            holder.home_viewed_item_name.setText(cell.getEmpName());
            holder.home_viewed_item_time.setText(cell.getDateLine());
            holder.home_item_school.setText(cell.getSchoolName());

            String urlStr = "  >>网页链接";
            if (!StringUtil.isNullOrEmpty(cell.getRecordCont())) {
                holder.home_viewed_item_cont.setVisibility(View.VISIBLE);
                String strcont = cell.getRecordCont();//内容
                int textsize = (int) holder.home_viewed_item_cont.getTextSize();
                textsize = StringUtil.dp2px(mContext, textsize+25);
                if (strcont.contains("http")) {
                    //如果包含http
                    String strhttp = strcont.substring(strcont.indexOf("http"), strcont.length());
                    strcont = strcont.replaceAll(strhttp, "");
                    holder.home_viewed_item_cont.setText(FaceConversionUtil.getInstace().getExpressionString(mContext, strcont + urlStr,textsize));
                }else {
                    holder.home_viewed_item_cont.setText(FaceConversionUtil.getInstace().getExpressionString(mContext,strcont,textsize));
                }

            }

            if (cell.getRecordType().equals(Constants.MOOD_TYPE)) {
                //说说
                if (!StringUtil.isNullOrEmpty(cell.getRecordPicUrl())) {
                    final String[] picUrls = cell.getRecordPicUrl().split(",");//图片链接切割
                    if (picUrls.length > 0) {
                        //有多张图
                        holder.gridview_detail_picture.setVisibility(View.VISIBLE);
                        holder.gridview_detail_picture.setAdapter(new ImageGridViewAdapter(picUrls, mContext));
                        if(picUrls.length ==1){
                            //如果只有1张图片
                            holder.gridview_detail_picture.setClickable(false);
                            holder.gridview_detail_picture.setPressed(false);
                            holder.gridview_detail_picture.setEnabled(false);
                        }else {
                            holder.gridview_detail_picture.setClickable(true);
                            holder.gridview_detail_picture.setPressed(true);
                            holder.gridview_detail_picture.setEnabled(true);
                        }
                        holder.gridview_detail_picture.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(mContext, GalleryUrlActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                intent.putExtra(Constants.IMAGE_URLS, picUrls);
                                intent.putExtra(Constants.IMAGE_POSITION, position);
                                mContext.startActivity(intent);
                            }
                        });

                    }
                }

            }
            if (cell.getRecordType().equals(Constants.VIDEO_TYPE)){
                //视频
                holder.home_photo_item_photo_video.setVisibility(View.VISIBLE);
                holder.home_player_icon_video.setVisibility(View.VISIBLE);
                imageLoader.displayImage(cell.getRecordPicUrl(), holder.home_photo_item_photo_video, UniversityApplication.videofailed, animateFirstListener);
                //视频播放器
                holder.home_player_icon_video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickContentItemListener.onClickContentItem(position, 5, null);
                    }
                });
            }

            holder.home_photo_item_like_count.setText(cell.getZanNum());
        }
        //评论
        holder.home_photo_item_comment_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickContentItemListener.onClickContentItem(position, 1, null);
            }
        });
        //点赞
        holder.home_photo_item_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickContentItemListener.onClickContentItem(position, 2, null);
            }
        });

        //删除
        holder.mine_home_item_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickContentItemListener.onClickContentItem(position, 3, null);
            }
        });
        //点击头像
        holder.home_viewed_item_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickContentItemListener.onClickContentItem(position, 4, null);
            }
        });
        holder.home_viewed_item_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickContentItemListener.onClickContentItem(position, 4, null);
            }
        });
        //推广
        holder.home_photo_item_photo_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickContentItemListener.onClickContentItem(position, 7, null);
            }
        });
//        holder.home_viewed_item_cont.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                onClickContentItemListener.onClickContentItem(position, 8, null);
//            }
//        });

        return convertView;
    }

    class ViewHolder {
        ImageView home_viewed_item_cover;//头像
        TextView home_viewed_item_name;//昵称
        TextView home_viewed_item_time;//日期
        TextView home_viewed_item_cont;//内容
        ImageView home_photo_item_photo;//图片
        ImageView home_photo_item_photo_video;//图片--视频
        TextView mine_home_item_delete;
        TextView home_photo_item_comment_count;//评论数量
        TextView home_photo_item_level;
        TextView home_photo_item_like_count;//赞的数量
        ImageView home_player_icon_video;//视频播放
        TextView home_item_school;//所属学校

        PictureGridview gridview_detail_picture;//九宫格--图片
    }

}
