/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.liangxun.yuejiula.huanxin.chat.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.TextView.BufferType;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.easemob.EMCallBack;
import com.easemob.chat.*;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Type;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.*;
import com.liangxun.yuejiula.R;
import com.liangxun.yuejiula.adapter.OnClickContentItemListener;
import com.liangxun.yuejiula.base.InternetURL;
import com.liangxun.yuejiula.data.EmpsDATA;
import com.liangxun.yuejiula.entity.Emp;
import com.liangxun.yuejiula.face.FaceConversionUtil;
import com.liangxun.yuejiula.huanxin.chat.HxConstant;
import com.liangxun.yuejiula.huanxin.chat.db.HxUserDao;
import com.liangxun.yuejiula.huanxin.chat.domain.HxUser;
import com.liangxun.yuejiula.huanxin.chat.task.LoadImageTask;
import com.liangxun.yuejiula.huanxin.chat.task.LoadVideoImageTask;
import com.liangxun.yuejiula.huanxin.chat.utils.ImageCache;
import com.liangxun.yuejiula.util.StringUtil;
import com.liangxun.yuejiula.huanxin.chat.activity.*;
import com.yixia.camera.demo.UniversityApplication;

import java.io.File;
import java.util.*;

//import com.umeng.analytics.MobclickAgent;
//imageLoader.displayImage(userDao.getCoverByName(username), holder.avatar , UniversityApplication.txOptions, animateFirstListener);
public class MessageAdapter extends BaseAdapter {

    private final static String TAG = "msg";

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_SENT_FILE = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;
    private static final int MESSAGE_TYPE_SENT_VOICE_CALL = 12;
    private static final int MESSAGE_TYPE_RECV_VOICE_CALL = 13;
    private static final int MESSAGE_TYPE_SENT_VIDEO_CALL = 14;
    private static final int MESSAGE_TYPE_RECV_VIDEO_CALL = 15;

    public static final String IMAGE_DIR = "chat/image/";
    public static final String VOICE_DIR = "chat/audio/";
    public static final String VIDEO_DIR = "chat/video";

    private String username;
    private LayoutInflater inflater;
    private Activity activity;

    // reference to conversation object in chatsdk
    private EMConversation conversation;

    private ChatActivity context;

    private Map<String, Timer> timers = new Hashtable<String, Timer>();

    HxUserDao userDao;
    private Emp toChatUser;

    private Emp emp;
    private ImageLoader imageLoader;//图片加载类

    private OnClickContentItemListener onClickContentItemListener;

    public void setOnClickContentItemListener(OnClickContentItemListener onClickContentItemListener) {
        this.onClickContentItemListener = onClickContentItemListener;
    }

    //原来的构造方法
    public MessageAdapter(Context context, String username, int chatType) {
        this.username = username;
        this.context = (ChatActivity) context;
        inflater = LayoutInflater.from(context);
        activity = (Activity) context;
        emp = ((UniversityApplication) activity.getApplication()).getCurrentEmp();
        this.conversation = EMChatManager.getInstance().getConversation(username);
    }

    //扩展的构造方法
    public MessageAdapter(Context context, String username, int chatType, Emp toChatUser, ImageLoader imageLoader) {
        this.username = username;
        this.context = (ChatActivity) context;
        this.toChatUser = toChatUser;
        inflater = LayoutInflater.from(context);
        activity = (Activity) context;
        this.conversation = EMChatManager.getInstance().getConversation(username);
        userDao = new HxUserDao(context);
        emp = ((UniversityApplication) activity.getApplication()).getCurrentEmp();
        this.imageLoader = imageLoader;
    }


    /**
     * 获取item数
     */
    public int getCount() {
        return conversation.getMsgCount();
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        notifyDataSetChanged();
    }

    public EMMessage getItem(int position) {
        return conversation.getMessage(position);
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取item类型
     */
    public int getItemViewType(int position) {
        EMMessage message = conversation.getMessage(position);
        if (message.getType() == Type.TXT) {
            if (message.getBooleanAttribute(HxConstant.MESSAGE_ATTR_IS_VOICE_CALL, false))
                return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE_CALL : MESSAGE_TYPE_SENT_VOICE_CALL;
            else if (message.getBooleanAttribute(HxConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false))
                return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO_CALL : MESSAGE_TYPE_SENT_VIDEO_CALL;
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_TXT : MESSAGE_TYPE_SENT_TXT;
        }
        if (message.getType() == Type.IMAGE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_IMAGE : MESSAGE_TYPE_SENT_IMAGE;

        }
        if (message.getType() == Type.LOCATION) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_LOCATION : MESSAGE_TYPE_SENT_LOCATION;
        }
        if (message.getType() == Type.VOICE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VOICE : MESSAGE_TYPE_SENT_VOICE;
        }
        if (message.getType() == Type.VIDEO) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_VIDEO : MESSAGE_TYPE_SENT_VIDEO;
        }
        if (message.getType() == Type.FILE) {
            return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_FILE : MESSAGE_TYPE_SENT_FILE;
        }

        return -1;// invalid
    }

    public int getViewTypeCount() {
        return 16;
    }

    private View createViewByMessage(EMMessage message, int position) {
        switch (message.getType()) {
            case LOCATION:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_location, null) : inflater.inflate(
                        R.layout.row_sent_location, null);
            case IMAGE:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_picture, null) : inflater.inflate(
                        R.layout.row_sent_picture, null);

            case VOICE:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice, null) : inflater.inflate(
                        R.layout.row_sent_voice, null);
            case VIDEO:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_video, null) : inflater.inflate(
                        R.layout.row_sent_video, null);
            case FILE:
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_file, null) : inflater.inflate(
                        R.layout.row_sent_file, null);
            default:
                // 语音通话
                if (message.getBooleanAttribute(HxConstant.MESSAGE_ATTR_IS_VOICE_CALL, false))
                    return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_voice_call, null) : inflater
                            .inflate(R.layout.row_sent_voice_call, null);
                    // 视频通话
                else if (message.getBooleanAttribute(HxConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false))
                    return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_video_call, null) : inflater
                            .inflate(R.layout.row_sent_video_call, null);
                return message.direct == EMMessage.Direct.RECEIVE ? inflater.inflate(R.layout.row_received_message, null) : inflater.inflate(
                        R.layout.row_sent_message, null);
        }
    }

    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final EMMessage message = getItem(position);
        ChatType chatType = message.getChatType();
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByMessage(message, position);
            if (message.getType() == Type.IMAGE) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.iv_sendPicture));
                    holder.head_iv = (NetworkImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            } else if (message.getType() == Type.TXT) {

                try {
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.head_iv = (NetworkImageView) convertView.findViewById(R.id.iv_userhead);
                    // 这里是文字内容
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                    holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

                // 语音通话及视频通话
                if (message.getBooleanAttribute(HxConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)
                        || message.getBooleanAttribute(HxConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
                    holder.iv = (ImageView) convertView.findViewById(R.id.iv_call_icon);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                }

            } else if (message.getType() == Type.VOICE) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.iv_voice));
                    holder.head_iv = (NetworkImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                    holder.iv_read_status = (ImageView) convertView.findViewById(R.id.iv_unread_voice);
                } catch (Exception e) {
                }
            } else if (message.getType() == Type.LOCATION) {
                try {
                    holder.head_iv = (NetworkImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.tv_location);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }
            } else if (message.getType() == Type.VIDEO) {
                try {
                    holder.iv = ((ImageView) convertView.findViewById(R.id.chatting_content_iv));
                    holder.head_iv = (NetworkImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.size = (TextView) convertView.findViewById(R.id.chatting_size_iv);
                    holder.timeLength = (TextView) convertView.findViewById(R.id.chatting_length_iv);
                    holder.playBtn = (ImageView) convertView.findViewById(R.id.chatting_status_btn);
                    holder.container_status_btn = (LinearLayout) convertView.findViewById(R.id.container_status_btn);
                    holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);

                } catch (Exception e) {
                }
            } else if (message.getType() == Type.FILE) {
                try {
                    holder.head_iv = (NetworkImageView) convertView.findViewById(R.id.iv_userhead);
                    holder.tv_file_name = (TextView) convertView.findViewById(R.id.tv_file_name);
                    holder.tv_file_size = (TextView) convertView.findViewById(R.id.tv_file_size);
                    holder.pb = (ProgressBar) convertView.findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView.findViewById(R.id.msg_status);
                    holder.tv_file_download_state = (TextView) convertView.findViewById(R.id.tv_file_state);
                    holder.ll_container = (LinearLayout) convertView.findViewById(R.id.ll_file_container);
                    // 这里是进度值
                    holder.tv = (TextView) convertView.findViewById(R.id.percentage);
                } catch (Exception e) {
                }
                try {
                    holder.tv_userId = (TextView) convertView.findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 群聊时，显示接收的消息的发送人的名称
        if (chatType == ChatType.GroupChat && message.direct == EMMessage.Direct.RECEIVE) {
            // demo用username代替nick
            String nick = userDao.getNick(message.getFrom());
            if (nick == null || "".equals(nick)) {
                getEmp(message.getFrom());
            }
            holder.tv_userId.setText(message.getFrom().equals("12345678910") ? "管理员" : userDao.getNick(message.getFrom()));
            loadImage(message, holder.head_iv);//加载头像

        }
        // 如果是发送的消息并且不是群聊消息，显示已读textview
        if (message.direct == EMMessage.Direct.SEND && chatType != ChatType.GroupChat) {
            holder.tv_ack = (TextView) convertView.findViewById(R.id.tv_ack);
            holder.tv_delivered = (TextView) convertView.findViewById(R.id.tv_delivered);
            if (holder.tv_ack != null) {
                if (message.isAcked) {
                    if (holder.tv_delivered != null) {
                        holder.tv_delivered.setVisibility(View.INVISIBLE);
                    }
                    holder.tv_ack.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_ack.setVisibility(View.INVISIBLE);

                    // check and display msg delivered ack status
                    if (holder.tv_delivered != null) {
                        if (message.isDelivered) {
                            holder.tv_delivered.setVisibility(View.VISIBLE);
                        } else {
                            holder.tv_delivered.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        } else {
            // 如果是文本或者地图消息并且不是group messgae，显示的时候给对方发送已读回执
            if ((message.getType() == Type.TXT || message.getType() == Type.LOCATION) && !message.isAcked && chatType != ChatType.GroupChat) {
                // 不是语音通话记录
                if (!message.getBooleanAttribute(HxConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    try {
                        EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                        // 发送已读回执
                        message.isAcked = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        switch (message.getType()) {
            // 根据消息type显示item
            case IMAGE: // 图片
                handleImageMessage(message, holder, position, convertView);
                break;
            case TXT: // 文本
                if (message.getBooleanAttribute(HxConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)
                        || message.getBooleanAttribute(HxConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false))
                    // 音视频通话
                    handleCallMessage(message, holder, position);
                else
                    handleTextMessage(message, holder, position);
                break;
            case LOCATION: // 位置
                handleLocationMessage(message, holder, position, convertView);
                break;
            case VOICE: // 语音
                handleVoiceMessage(message, holder, position, convertView);
                break;
            case VIDEO: // 视频
                handleVideoMessage(message, holder, position, convertView);
                break;
            case FILE: // 一般文件
                handleFileMessage(message, holder, position, convertView);
                break;
            default:
                // not supported
        }

        if (message.direct == EMMessage.Direct.SEND) {
            View statusView = convertView.findViewById(R.id.msg_status);
            // 重发按钮点击事件
            statusView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 显示重发消息的自定义alertdialog
                    Intent intent = new Intent(activity, HxAlertDialog.class);
                    intent.putExtra("msg", activity.getString(R.string.confirm_resend));
                    intent.putExtra("title", activity.getString(R.string.resend));
                    intent.putExtra("cancel", true);
                    intent.putExtra("position", position);
                    if (message.getType() == Type.TXT)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_TEXT);
                    else if (message.getType() == Type.VOICE)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VOICE);
                    else if (message.getType() == Type.IMAGE)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_PICTURE);
                    else if (message.getType() == Type.LOCATION)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_LOCATION);
                    else if (message.getType() == Type.FILE)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_FILE);
                    else if (message.getType() == Type.VIDEO)
                        activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_VIDEO);

                }
            });

        } else {
            final String st = context.getResources().getString(R.string.Into_the_blacklist);
            // 长按头像，移入黑名单
            holder.head_iv.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent(activity, HxAlertDialog.class);
                    intent.putExtra("msg", st);
                    intent.putExtra("cancel", true);
                    intent.putExtra("position", position);
                    activity.startActivityForResult(intent, ChatActivity.REQUEST_CODE_ADD_TO_BLACKLIST);
                    return true;
                }
            });
            holder.head_iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickContentItemListener.onClickContentItem(position, 1, null);
                }

            });
        }

        TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);

        if (position == 0) {
            timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
            timestamp.setVisibility(View.VISIBLE);
        } else {
            // 两条消息时间离得如果稍长，显示时间
            if (DateUtils.isCloseEnough(message.getMsgTime(), conversation.getMessage(position - 1).getMsgTime())) {
                timestamp.setVisibility(View.GONE);
            } else {
                timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

    /**
     * 文本消息
     *
     * @param message
     * @param holder
     * @param position
     */
    private void handleTextMessage(EMMessage message, ViewHolder holder, final int position) {
        loadImage(message, holder.head_iv);//加载头像
        TextMessageBody txtBody = (TextMessageBody) message.getBody();
        String texstr=txtBody.getMessage().equals("12345678910邀请你加入了群聊") ? "管理员邀请你加入了群聊" : txtBody.getMessage();
        int textsize = ( int) holder.tv.getTextSize();
        textsize = StringUtil.dp2px(context, textsize + 25);
        Spannable span = FaceConversionUtil.getInstace().getExpressionString(context, texstr,textsize);
        // 设置内容
        holder.tv.setText(span, BufferType.SPANNABLE);
        // 设置长按事件监听
        holder.tv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(
                        (new Intent(activity, HxContextMenu.class)).putExtra("position", position).putExtra("type",
                                Type.TXT.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });

        if (message.direct == EMMessage.Direct.SEND) {
            switch (message.status) {
                case SUCCESS: // 发送成功
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                case FAIL: // 发送失败
                    holder.pb.setVisibility(View.GONE);
                    holder.staus_iv.setVisibility(View.VISIBLE);
                    break;
                case INPROGRESS: // 发送中
                    holder.pb.setVisibility(View.VISIBLE);
                    holder.staus_iv.setVisibility(View.GONE);
                    break;
                default:
                    // 发送消息
                    sendMsgInBackground(message, holder);
            }
        }
    }

    /**
     * 音视频通话记录
     *
     * @param message
     * @param holder
     * @param position
     */
    private void handleCallMessage(EMMessage message, ViewHolder holder, final int position) {
        TextMessageBody txtBody = (TextMessageBody) message.getBody();
        holder.tv.setText(txtBody.getMessage());
        loadImage(message, holder.head_iv);//加载头像

    }

    /**
     * 图片消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleImageMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
        holder.pb.setTag(position);
        holder.iv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(
                        (new Intent(activity, HxContextMenu.class)).putExtra("position", position).putExtra("type",
                                Type.IMAGE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });

        loadImage(message, holder.head_iv);//加载头像
        // 接收方向的消息
        if (message.direct == EMMessage.Direct.RECEIVE) {
            // "it is receive msg";
            if (message.status == EMMessage.Status.INPROGRESS) {
                // "!!!! back receive";
                holder.iv.setImageResource(R.drawable.default_image);
                showDownloadImageProgress(message, holder);
                // downloadImage(message, holder);
            } else {
                // "!!!! not back receive, show image directly");
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.iv.setImageResource(R.drawable.default_image);
                ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
                if (imgBody.getLocalUrl() != null) {
                    // String filePath = imgBody.getLocalUrl();
                    String remotePath = imgBody.getRemoteUrl();
                    String filePath = com.liangxun.yuejiula.huanxin.chat.utils.ImageUtils.getImagePath(remotePath);
                    String thumbRemoteUrl = imgBody.getThumbnailUrl();
                    String thumbnailPath = com.liangxun.yuejiula.huanxin.chat.utils.ImageUtils.getThumbnailImagePath(thumbRemoteUrl);
                    showImageView(thumbnailPath, holder.iv, filePath, imgBody.getRemoteUrl(), message);
                }
            }
            return;
        }

        // 发送的消息
        // process send message
        // send pic, show the pic directly
        ImageMessageBody imgBody = (ImageMessageBody) message.getBody();
        String filePath = imgBody.getLocalUrl();
        if (filePath != null && new File(filePath).exists()) {
            showImageView(com.liangxun.yuejiula.huanxin.chat.utils.ImageUtils.getThumbnailImagePath(filePath), holder.iv, filePath, null, message);
        } else {
            showImageView(com.liangxun.yuejiula.huanxin.chat.utils.ImageUtils.getThumbnailImagePath(filePath), holder.iv, filePath, IMAGE_DIR, message);
        }

        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.staus_iv.setVisibility(View.GONE);
                holder.pb.setVisibility(View.VISIBLE);
                holder.tv.setVisibility(View.VISIBLE);
                if (timers.containsKey(message.getMsgId()))
                    return;
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);
                                holder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    // message.setSendingStatus(Message.SENDING_STATUS_SUCCESS);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    // message.setSendingStatus(Message.SENDING_STATUS_FAIL);
                                    // message.setProgress(0);
                                    holder.staus_iv.setVisibility(View.VISIBLE);
                                    Toast.makeText(activity,
                                            activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), Toast.LENGTH_SHORT)
                                            .show();
                                    timer.cancel();
                                }

                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                sendPictureMessage(message, holder);
        }
    }

    /**
     * 视频消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleVideoMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {

        loadImage(message, holder.head_iv);//加载头像
        VideoMessageBody videoBody = (VideoMessageBody) message.getBody();
        // final File image=new File(PathUtil.getInstance().getVideoPath(),
        // videoBody.getFileName());
        String localThumb = videoBody.getLocalThumb();

        holder.iv.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(
                        new Intent(activity, HxContextMenu.class).putExtra("position", position).putExtra("type",
                                Type.VIDEO.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });

        if (localThumb != null) {

            showVideoThumbView(localThumb, holder.iv, videoBody.getThumbnailUrl(), message);
        }
        if (videoBody.getLength() > 0) {
            String time = DateUtils.toTimeBySecond(videoBody.getLength());
            holder.timeLength.setText(time);
        }
        holder.playBtn.setImageResource(R.drawable.video_download_btn_nor);

        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (videoBody.getVideoFileLength() > 0) {
                String size = TextFormater.getDataSize(videoBody.getVideoFileLength());
                holder.size.setText(size);
            }
        } else {
            if (videoBody.getLocalUrl() != null && new File(videoBody.getLocalUrl()).exists()) {
                String size = TextFormater.getDataSize(new File(videoBody.getLocalUrl()).length());
                holder.size.setText(size);
            }
        }

        if (message.direct == EMMessage.Direct.RECEIVE) {

            // System.err.println("it is receive msg");
            if (message.status == EMMessage.Status.INPROGRESS) {
                // System.err.println("!!!! back receive");
                holder.iv.setImageResource(R.drawable.default_image);
                showDownloadImageProgress(message, holder);

            } else {
                // System.err.println("!!!! not back receive, show image directly");
                holder.iv.setImageResource(R.drawable.default_image);
                if (localThumb != null) {
                    showVideoThumbView(localThumb, holder.iv, videoBody.getThumbnailUrl(), message);
                }

            }

            return;
        }
        holder.pb.setTag(position);

        // until here ,deal with send video msg
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                if (timers.containsKey(message.getMsgId()))
                    return;
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);
                                holder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    // message.setSendingStatus(Message.SENDING_STATUS_SUCCESS);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    // message.setSendingStatus(Message.SENDING_STATUS_FAIL);
                                    // message.setProgress(0);
                                    holder.staus_iv.setVisibility(View.VISIBLE);
                                    Toast.makeText(activity,
                                            activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), Toast.LENGTH_SHORT)
                                            .show();
                                    timer.cancel();
                                }

                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                // sendMsgInBackground(message, holder);
                sendPictureMessage(message, holder);

        }

    }

    /**
     * 语音消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleVoiceMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
        loadImage(message, holder.head_iv);//加载头像
        VoiceMessageBody voiceBody = (VoiceMessageBody) message.getBody();
        holder.tv.setText(voiceBody.getLength() + "\"");
        holder.iv.setOnClickListener(new VoicePlayClickListener(message, holder.iv, holder.iv_read_status, this, activity, username));
        holder.iv.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(
                        (new Intent(activity, HxContextMenu.class)).putExtra("position", position).putExtra("type",
                                Type.VOICE.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return true;
            }
        });
        if (((ChatActivity) activity).playMsgId != null
                && ((ChatActivity) activity).playMsgId.equals(message
                .getMsgId()) && VoicePlayClickListener.isPlaying) {
            AnimationDrawable voiceAnimation;
            if (message.direct == EMMessage.Direct.RECEIVE) {
                holder.iv.setImageResource(R.drawable.voice_from_icon);
            } else {
                holder.iv.setImageResource(R.drawable.voice_to_icon);
            }
            voiceAnimation = (AnimationDrawable) holder.iv.getDrawable();
            voiceAnimation.start();
        } else {
            if (message.direct == EMMessage.Direct.RECEIVE) {
                holder.iv.setImageResource(R.drawable.chatfrom_voice_playing);
            } else {
                holder.iv.setImageResource(R.drawable.chatto_voice_playing);
            }
        }


        if (message.direct == EMMessage.Direct.RECEIVE) {
            if (message.isListened()) {
                // 隐藏语音未听标志
                holder.iv_read_status.setVisibility(View.INVISIBLE);
            } else {
                holder.iv_read_status.setVisibility(View.VISIBLE);
            }
            System.err.println("it is receive msg");
            if (message.status == EMMessage.Status.INPROGRESS) {
                holder.pb.setVisibility(View.VISIBLE);
                System.err.println("!!!! back receive");
                ((FileMessageBody) message.getBody()).setDownloadCallback(new EMCallBack() {

                    @Override
                    public void onSuccess() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.INVISIBLE);
                                notifyDataSetChanged();
                            }
                        });

                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(int code, String message) {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                });

            } else {
                holder.pb.setVisibility(View.INVISIBLE);

            }
            return;
        }

        // until here, deal with send voice msg
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.pb.setVisibility(View.VISIBLE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            default:
                sendMsgInBackground(message, holder);
        }
    }

    /**
     * 文件消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleFileMessage(final EMMessage message, final ViewHolder holder, int position, View convertView) {
        loadImage(message, holder.head_iv);//加载头像
        final NormalFileMessageBody fileMessageBody = (NormalFileMessageBody) message.getBody();
        final String filePath = fileMessageBody.getLocalUrl();
        holder.tv_file_name.setText(fileMessageBody.getFileName());
        holder.tv_file_size.setText(TextFormater.getDataSize(fileMessageBody.getFileSize()));
        holder.ll_container.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                File file = new File(filePath);
                if (file != null && file.exists()) {
                    // 文件存在，直接打开
                    FileUtils.openFile(file, (Activity) context);
                } else {
                    // 下载
                    context.startActivity(new Intent(context, ShowNormalFileActivity.class).putExtra("msgbody", fileMessageBody));
                }
                if (message.direct == EMMessage.Direct.RECEIVE && !message.isAcked) {
                    try {
                        EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                        message.isAcked = true;
                    } catch (EaseMobException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
        String st1 = context.getResources().getString(R.string.Have_downloaded);
        String st2 = context.getResources().getString(R.string.Did_not_download);
        if (message.direct == EMMessage.Direct.RECEIVE) { // 接收的消息
            System.err.println("it is receive msg");
            File file = new File(filePath);
            if (file != null && file.exists()) {
                holder.tv_file_download_state.setText(st1);
            } else {
                holder.tv_file_download_state.setText(st2);
            }
            return;
        }

        // until here, deal with send voice msg
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.INVISIBLE);
                holder.tv.setVisibility(View.INVISIBLE);
                holder.staus_iv.setVisibility(View.INVISIBLE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.INVISIBLE);
                holder.tv.setVisibility(View.INVISIBLE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                if (timers.containsKey(message.getMsgId()))
                    return;
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMsgId(), timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);
                                holder.tv.setText(message.progress + "%");
                                if (message.status == EMMessage.Status.SUCCESS) {
                                    holder.pb.setVisibility(View.INVISIBLE);
                                    holder.tv.setVisibility(View.INVISIBLE);
                                    timer.cancel();
                                } else if (message.status == EMMessage.Status.FAIL) {
                                    holder.pb.setVisibility(View.INVISIBLE);
                                    holder.tv.setVisibility(View.INVISIBLE);
                                    holder.staus_iv.setVisibility(View.VISIBLE);
                                    Toast.makeText(activity,
                                            activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), Toast.LENGTH_SHORT)
                                            .show();
                                    timer.cancel();
                                }

                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                // 发送消息
                sendMsgInBackground(message, holder);
        }

    }

    /**
     * 处理位置消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleLocationMessage(final EMMessage message, final ViewHolder holder, final int position, View convertView) {
        loadImage(message, holder.head_iv);//加载头像
        TextView locationView = ((TextView) convertView.findViewById(R.id.tv_location));
        LocationMessageBody locBody = (LocationMessageBody) message.getBody();
        locationView.setText(locBody.getAddress());
        LatLng loc = new LatLng(locBody.getLatitude(), locBody.getLongitude());
        locationView.setOnClickListener(new MapClickListener(loc, locBody.getAddress()));
        locationView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.startActivityForResult(
                        (new Intent(activity, HxContextMenu.class)).putExtra("position", position).putExtra("type",
                                Type.LOCATION.ordinal()), ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                return false;
            }
        });

        if (message.direct == EMMessage.Direct.RECEIVE) {
            return;
        }
        // deal with send message
        switch (message.status) {
            case SUCCESS:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.GONE);
                break;
            case FAIL:
                holder.pb.setVisibility(View.GONE);
                holder.staus_iv.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                holder.pb.setVisibility(View.VISIBLE);
                break;
            default:
                sendMsgInBackground(message, holder);
        }
    }

    /**
     * 发送消息
     *
     * @param message
     * @param holder
     * @param
     */
    public void sendMsgInBackground(final EMMessage message, final ViewHolder holder) {
        holder.staus_iv.setVisibility(View.GONE);
        holder.pb.setVisibility(View.VISIBLE);

        final long start = System.currentTimeMillis();
        // 调用sdk发送异步发送方法
        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

            @Override
            public void onSuccess() {
                //umeng自定义事件，
                sendEvent2Umeng(message, start);

                updateSendedView(message, holder);
            }

            @Override
            public void onError(int code, String error) {
                sendEvent2Umeng(message, start);

                updateSendedView(message, holder);
            }

            @Override
            public void onProgress(int progress, String status) {
            }

        });

    }

    /*
     * chat sdk will automatic download thumbnail image for the image message we
     * need to register callback show the download progress
     */
    private void showDownloadImageProgress(final EMMessage message, final ViewHolder holder) {
        System.err.println("!!! show download image progress");
        // final ImageMessageBody msgbody = (ImageMessageBody)
        // message.getBody();
        final FileMessageBody msgbody = (FileMessageBody) message.getBody();
        if (holder.pb != null)
            holder.pb.setVisibility(View.VISIBLE);
        if (holder.tv != null)
            holder.tv.setVisibility(View.VISIBLE);

        msgbody.setDownloadCallback(new EMCallBack() {

            @Override
            public void onSuccess() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // message.setBackReceive(false);
                        if (message.getType() == Type.IMAGE) {
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                        }
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onProgress(final int progress, String status) {
                if (message.getType() == Type.IMAGE) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.tv.setText(progress + "%");

                        }
                    });
                }

            }

        });
    }

    /*
     * send message with new sdk
     */
    private void sendPictureMessage(final EMMessage message, final ViewHolder holder) {
        try {
            String to = message.getTo();

            // before send, update ui
            holder.staus_iv.setVisibility(View.GONE);
            holder.pb.setVisibility(View.VISIBLE);
            holder.tv.setVisibility(View.VISIBLE);
            holder.tv.setText("0%");

            final long start = System.currentTimeMillis();
            // if (chatType == ChatActivity.CHATTYPE_SINGLE) {
            EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

                @Override
                public void onSuccess() {
                    Log.d(TAG, "send image message successfully");
                    sendEvent2Umeng(message, start);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            // send success
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onError(int code, String error) {
                    sendEvent2Umeng(message, start);

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            holder.pb.setVisibility(View.GONE);
                            holder.tv.setVisibility(View.GONE);
                            // message.setSendingStatus(Message.SENDING_STATUS_FAIL);
                            holder.staus_iv.setVisibility(View.VISIBLE);
                            Toast.makeText(activity,
                                    activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onProgress(final int progress, String status) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            holder.tv.setText(progress + "%");
                        }
                    });
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新ui上消息发送状态
     *
     * @param message
     * @param holder
     */
    private void updateSendedView(final EMMessage message, final ViewHolder holder) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // send success
                if (message.getType() == Type.VIDEO) {
                    holder.tv.setVisibility(View.GONE);
                }
                if (message.status == EMMessage.Status.SUCCESS) {
                    // if (message.getType() == EMMessage.Type.FILE) {
                    // holder.pb.setVisibility(View.INVISIBLE);
                    // holder.staus_iv.setVisibility(View.INVISIBLE);
                    // } else {
                    // holder.pb.setVisibility(View.GONE);
                    // holder.staus_iv.setVisibility(View.GONE);
                    // }

                } else if (message.status == EMMessage.Status.FAIL) {
                    // if (message.getType() == EMMessage.Type.FILE) {
                    // holder.pb.setVisibility(View.INVISIBLE);
                    // } else {
                    // holder.pb.setVisibility(View.GONE);
                    // }
                    // holder.staus_iv.setVisibility(View.VISIBLE);
                    Toast.makeText(activity, activity.getString(R.string.send_fail) + activity.getString(R.string.connect_failuer_toast), Toast.LENGTH_SHORT)
                            .show();
                }

                notifyDataSetChanged();
            }
        });
    }

    /**
     * load image into image view
     *
     * @param thumbernailPath
     * @param iv
     * @param
     * @return the image exists or not
     */
    private boolean showImageView(final String thumbernailPath, final ImageView iv, final String localFullSizePath, String remoteDir,
                                  final EMMessage message) {
        // String imagename =
        // localFullSizePath.substring(localFullSizePath.lastIndexOf("/") + 1,
        // localFullSizePath.length());
        // final String remote = remoteDir != null ? remoteDir+imagename :
        // imagename;
        final String remote = remoteDir;
        EMLog.d("###", "local = " + localFullSizePath + " remote: " + remote);
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = ImageCache.getInstance().get(thumbernailPath);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);
            iv.setClickable(true);
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.err.println("image view on click");
                    Intent intent = new Intent(activity, ShowBigImage.class);
                    File file = new File(localFullSizePath);
                    if (file.exists()) {
                        Uri uri = Uri.fromFile(file);
                        intent.putExtra("uri", uri);
                        System.err.println("here need to check why download everytime");
                    } else {
                        // The local full size pic does not exist yet.
                        // ShowBigImage needs to download it from the server
                        // first
                        // intent.putExtra("", message.get);
                        ImageMessageBody body = (ImageMessageBody) message.getBody();
                        intent.putExtra("secret", body.getSecret());
                        intent.putExtra("remotepath", remote);
                    }
                    if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked
                            && message.getChatType() != ChatType.GroupChat) {
                        try {
                            EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                            message.isAcked = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    activity.startActivity(intent);
                }
            });
            return true;
        } else {

            new LoadImageTask().execute(thumbernailPath, localFullSizePath, remote, message.getChatType(), iv, activity, message);
            return true;
        }

    }

    /**
     * 展示视频缩略图
     *
     * @param localThumb
     *            本地缩略图路径
     * @param iv
     * @param thumbnailUrl
     *            远程缩略图路径
     * @param message
     */
    private void showVideoThumbView(String localThumb, ImageView iv, String thumbnailUrl, final EMMessage message) {
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = ImageCache.getInstance().get(localThumb);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);
            iv.setClickable(true);
            iv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    VideoMessageBody videoBody = (VideoMessageBody) message.getBody();
                    System.err.println("video view is on click");
                    Intent intent = new Intent(activity, ShowVideoActivity.class);
                    intent.putExtra("localpath", videoBody.getLocalUrl());
                    intent.putExtra("secret", videoBody.getSecret());
                    intent.putExtra("remotepath", videoBody.getRemoteUrl());
                    if (message != null && message.direct == EMMessage.Direct.RECEIVE && !message.isAcked
                            && message.getChatType() != ChatType.GroupChat) {
                        message.isAcked = true;
                        try {
                            EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    activity.startActivity(intent);

                }
            });

        } else {
            new LoadVideoImageTask().execute(localThumb, thumbnailUrl, iv, activity, message, this);
        }

    }

    public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView staus_iv;
        NetworkImageView head_iv;
        TextView tv_userId;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;

        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;
        public TextView textView;
    }

    /*
     * 点击地图消息listener
     */
    class MapClickListener implements OnClickListener {

        LatLng location;
        String address;

        public MapClickListener(LatLng loc, String address) {
            location = loc;
            this.address = address;

        }

        @Override
        public void onClick(View v) {
            Intent intent;
            intent = new Intent(context, BaiduMapActivity.class);
            intent.putExtra("latitude", location.latitude);
            intent.putExtra("longitude", location.longitude);
            intent.putExtra("address", address);
            activity.startActivity(intent);
        }

    }

    /**
     * umeng自定义事件统计
     * @param message
     */
    private void sendEvent2Umeng(final EMMessage message, final long start) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                long costTime = System.currentTimeMillis() - start;
                Map<String, String> params = new HashMap<String, String>();
                if (message.status == EMMessage.Status.SUCCESS)
                    params.put("status", "success");
                else
                    params.put("status", "failure");

                switch (message.getType()) {
                    case TXT:
                    case LOCATION:
//					MobclickAgent.onEventValue(activity, "text_msg", params, (int) costTime);
//					MobclickAgent.onEventDuration(activity, "text_msg", (int) costTime);
                        break;
                    case IMAGE:
//					MobclickAgent.onEventValue(activity, "img_msg", params, (int) costTime);
//					MobclickAgent.onEventDuration(activity, "img_msg", (int) costTime);
                        break;
                    case VOICE:
//					MobclickAgent.onEventValue(activity, "voice_msg", params, (int) costTime);
//					MobclickAgent.onEventDuration(activity, "voice_msg", (int) costTime);
                        break;
                    case VIDEO:
//					MobclickAgent.onEventValue(activity, "video_msg", params, (int) costTime);
//					MobclickAgent.onEventDuration(activity, "video_msg", (int) costTime);
                        break;
                    default:
                        break;
                }

            }
        });
    }

    private void loadImage(EMMessage message, NetworkImageView view) {
        if (message.direct == EMMessage.Direct.SEND) {

//            imageLoader.displayImage(emp.getEmpCover(), view , UniversityApplication.txOptions, animateFirstListener);
            view.setDefaultImageResId(R.drawable.default_image);
            view.setErrorImageResId(R.drawable.default_image);
            view.setImageUrl(emp == null ? "" : emp.getEmpCover(), imageLoader);
        } else {
            view.setDefaultImageResId(R.drawable.default_image);
            view.setErrorImageResId(R.drawable.default_image);
            if (userDao.getCoverByName(message.getFrom()) == null || "".equals(userDao.getCoverByName(message.getFrom()))) {
                getEmp(message.getFrom());
            }
            view.setImageUrl(userDao.getCoverByName(message.getFrom()), imageLoader);
//            imageLoader.displayImage(url, view , UniversityApplication.txOptions, animateFirstListener);
        }
    }

    private void getEmp(final String username) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
//                InternetURL.GET_FRIENDS_URL,
                InternetURL.GET_INVITE_CONTACT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (StringUtil.isJson(s)) {
                            EmpsDATA data = context.getGson().fromJson(s, EmpsDATA.class);
                            if (data.getCode() == 200) {
                                if (data.getData().size() > 0) {
                                    Emp empInfo = data.getData().get(0);
                                    HxUser hxUser = new HxUser();
                                    hxUser.setUsername(empInfo.getHxUsername());
                                    hxUser.setNick(empInfo.getEmpName());
                                    hxUser.setCover(empInfo.getEmpCover());
                                    userDao.saveContact(hxUser);
                                    refresh();
                                }

                            } else {

                            }
                        } else {

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("hxUserNames", username);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        context.getRequestQueue().add(request);
    }
}