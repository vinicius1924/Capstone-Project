package com.example.vinicius.capstone;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.vinicius.capstone.data.SubredditContract;
import com.example.vinicius.capstone.utils.CursorRecyclerAdapter;

/**
 * Created by vinicius on 27/05/17.
 */

public class SubredditsRecyclerAdapter extends CursorRecyclerAdapter<SubredditsRecyclerAdapter.CustomViewHolder>
{
	private Context context;
	private SubredditListItemClickListener mOnClickListener;
	private SubscribeUnsubscribeSubredditListener mSubscribeUnsubscribeListener;

	public SubredditsRecyclerAdapter(Context context, Cursor c, int flags, SubredditListItemClickListener mOnClickListener,
												SubscribeUnsubscribeSubredditListener mSubscribeUnsubscribeListener)
	{
		super(context, c, flags);

		this.context = context;
		this.mOnClickListener = mOnClickListener;
		this.mSubscribeUnsubscribeListener = mSubscribeUnsubscribeListener;
	}


	@Override
	public void bindViewHolder(CustomViewHolder holder, Context context, Cursor cursor)
	{
		holder.subredditName.setText(cursor.getString(cursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_NAME)));

		if(cursor.getInt(cursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED)) == 0)
		{
			TextViewCompat.setTextAppearance(holder.subscribeUnsubscribeButton, R.style.ButtonUnsubscribedTextStyle);
			holder.subscribeUnsubscribeButton.setText(R.string.unsubscribed);
		}
		else
		{
			TextViewCompat.setTextAppearance(holder.subscribeUnsubscribeButton, R.style.ButtonSubscribedTextStyle);
			holder.subscribeUnsubscribeButton.setText(R.string.subscribed);
		}
	}

	@Override
	public CustomViewHolder createViewHolder(Context context, ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subreddits_list_item, parent, false);

		CustomViewHolder viewHolder = new CustomViewHolder(view);

		return viewHolder;
	}

	public interface SubredditListItemClickListener
	{
		void onListItemClick(int subredditId);
	}

	public interface SubscribeUnsubscribeSubredditListener
	{
		void onSubscribeReddit(int subredditId, String subredditUrl);
		void onUnSubscribeReddit(int subredditId, String subredditUrl);
	}

	public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		protected TextView subredditName;
		protected Button subscribeUnsubscribeButton;

		public CustomViewHolder(View itemView)
		{
			super(itemView);

			itemView.setOnClickListener(this);

			this.subredditName = (TextView) itemView.findViewById(R.id.subredditName);
			this.subscribeUnsubscribeButton = (Button) itemView.findViewById(R.id.subscribeUnsubscribeButton);

			this.subscribeUnsubscribeButton.setOnClickListener(this);
		}

		@Override
		public void onClick(View v)
		{
			Cursor cursor = getCursor();

			int adapterPosition = getAdapterPosition();
			cursor.moveToPosition(adapterPosition);

			int idColumnIndex = cursor.getColumnIndex(SubredditContract.SubredditsEntry._ID);
			int subscribedColumnIndex = cursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_SUBSCRIBED);
			int subredditUrlColumnIndex = cursor.getColumnIndex(SubredditContract.SubredditsEntry.COLUMN_URL);

			if(v.getId() == R.id.subscribeUnsubscribeButton)
			{
				if(cursor.getInt(subscribedColumnIndex) == 0)
				{
					mSubscribeUnsubscribeListener.onSubscribeReddit(cursor.getInt(idColumnIndex),
							  cursor.getString(subredditUrlColumnIndex));
				}
				else
				{
					mSubscribeUnsubscribeListener.onUnSubscribeReddit(cursor.getInt(idColumnIndex),
							  cursor.getString(subredditUrlColumnIndex));
				}
			}
			else
			{
				mOnClickListener.onListItemClick(cursor.getInt(idColumnIndex));
			}
		}

		public TextView getSubredditName()
		{
			return subredditName;
		}

		public Button getSubscribeUnsubscribeButton()
		{
			return subscribeUnsubscribeButton;
		}
	}
}
