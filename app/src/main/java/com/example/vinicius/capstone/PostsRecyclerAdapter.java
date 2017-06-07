package com.example.vinicius.capstone;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.vinicius.capstone.data.SubredditContract;
import com.example.vinicius.capstone.utils.CursorRecyclerAdapter;

/**
 * Created by vinicius on 29/05/17.
 */

public class PostsRecyclerAdapter extends CursorRecyclerAdapter<PostsRecyclerAdapter.CustomViewHolder>
{
	private Context context;
	private PostListItemClickListener mOnClickListener;

	public PostsRecyclerAdapter(Context context, Cursor c, int flags, PostListItemClickListener mOnClickListener)
	{
		super(context, c, flags);

		this.context = context;
		this.mOnClickListener = mOnClickListener;
	}

	@Override
	public void bindViewHolder(PostsRecyclerAdapter.CustomViewHolder holder, Context context, Cursor cursor)
	{
		Glide.with(context).load(cursor.getString(cursor.getColumnIndex(SubredditContract.
				  SubredditsPostsEntry.COLUMN_THUMBNAIL))).into(holder.postThumbnail);
		holder.postTitle.setText(cursor.getString(cursor.getColumnIndex(SubredditContract.SubredditsPostsEntry.COLUMN_TITLE)));
		holder.postAuthor.setText(cursor.getString(cursor.getColumnIndex(SubredditContract.SubredditsPostsEntry.COLUMN_AUTHOR)));
	}

	@Override
	public PostsRecyclerAdapter.CustomViewHolder createViewHolder(Context context, ViewGroup parent, int viewType)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);

		CustomViewHolder viewHolder = new CustomViewHolder(view);

		return viewHolder;
	}

	public interface PostListItemClickListener
	{
		void onListItemClick(int postId);
	}

	class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		protected ImageView postThumbnail;
		protected TextView postTitle;
		protected TextView postAuthor;

		public CustomViewHolder(View itemView)
		{
			super(itemView);

			itemView.setOnClickListener(this);

			this.postThumbnail = (ImageView) itemView.findViewById(R.id.postThumbnail);
			this.postTitle = (TextView) itemView.findViewById(R.id.postTitle);
			this.postAuthor = (TextView) itemView.findViewById(R.id.postAuthor);
		}

		@Override
		public void onClick(View v)
		{
			Cursor cursor = getCursor();

			int adapterPosition = getAdapterPosition();
			cursor.moveToPosition(adapterPosition);

			int idColumnIndex = cursor.getColumnIndex(SubredditContract.SubredditsPostsEntry._ID);

			mOnClickListener.onListItemClick(cursor.getInt(idColumnIndex));
		}
	}
}
