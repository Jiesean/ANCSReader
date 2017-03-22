package com.jiesean.readancs;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiesean.readancs.dataprocess.Notification;

import java.util.ArrayList;

/**
 * Created by Jiesean on 16-8-16.
 */
public class LocalAdapter extends RecyclerView.Adapter<LocalAdapter.ViewHolder> implements View.OnClickListener {
    private final String TAG = "LocalAdapter";
    private ArrayList<Notification> mList;
    int clickPosition;

    /**
     * Provide a suitable constructor (depends on the kind of dataset)
     *
     * @param mList 存储数据的集合
     */
    public LocalAdapter(ArrayList<Notification> mList) {
        this.mList = mList;
    }

    /**
     * define interface
     */
    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position, Notification notification);
    }


    //define a interface variable
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        public ImageView mCategoryIcon;
        public TextView mTitleTV;
        public TextView mCategoryTV;
        public TextView mMessageTV;
        public Button mPositiveBtn;
        public Button mNegativeBtn;
        public Button mBackBtn;

        public ViewHolder(View v) {
            super(v);
            mCategoryIcon = (ImageView) v.findViewById(R.id.category_icon_iv);
            mTitleTV = (TextView) v.findViewById(R.id.notification_title_tv);
            mCategoryTV = (TextView) v.findViewById(R.id.category_tv);
            mMessageTV = (TextView) v.findViewById(R.id.content_tv);
            mPositiveBtn = (Button) v.findViewById(R.id.positive_btn);
            mNegativeBtn = (Button) v.findViewById(R.id.negative_btn);
            mBackBtn = (Button) v.findViewById(R.id.back_btn);
        }

    }

    /**
     * Create new views (invoked by the layout manager)
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public LocalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {

        // create a new view
//        View v = View.inflate(parent.getContext(),R.layout.card_view, null);
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);

        v.findViewById(R.id.more_info_btn).setOnClickListener(this);
        v.findViewById(R.id.positive_btn).setOnClickListener(this);
        v.findViewById(R.id.negative_btn).setOnClickListener(this);
        v.findViewById(R.id.back_btn).setOnClickListener(this);

        return vh;
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTitleTV.setText(mList.get(position).getTitle());
        holder.mCategoryTV.setText(mList.get(position).getCategory());
        holder.mMessageTV.setText(mList.get(position).getMessage());

        holder.mCategoryIcon.setImageResource(Constants.category_icons[mList.get(position).getCategoryId()]);

        //不存在动作
        if (mList.get(position).getAction() < 1) {
            holder.mPositiveBtn.setVisibility(View.INVISIBLE);
            holder.mNegativeBtn.setVisibility(View.INVISIBLE);
        }
        //存在positive动作
        else if (mList.get(position).getAction() < 2) {
            holder.mNegativeBtn.setVisibility(View.INVISIBLE);
        }
        //存在positive
        else if (mList.get(position).getAction() < 3) {
            holder.mPositiveBtn.setVisibility(View.INVISIBLE);
        }

        clickPosition = position;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * 点击事件执行的函数
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, clickPosition, mList.get(clickPosition));
        }
    }

    /**
     * @param listener
     */
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
