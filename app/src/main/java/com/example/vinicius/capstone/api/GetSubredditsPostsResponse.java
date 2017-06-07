package com.example.vinicius.capstone.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vinicius on 28/05/17.
 */

public class GetSubredditsPostsResponse
{
	@SerializedName("data")
	@Expose
	private GetSubredditsPostsResponse.Data data;

	public GetSubredditsPostsResponse.Data getData()
	{
		return data;
	}

	public class Data
	{
		@SerializedName("children")
		@Expose
		private List<GetSubredditsPostsResponse.Children> children;

		public List<GetSubredditsPostsResponse.Children> getChildren()
		{
			return children;
		}
	}

	public class Children
	{
		@SerializedName("data")
		@Expose
		private GetSubredditsPostsResponse.Data_ data;

		public GetSubredditsPostsResponse.Data_ getData()
		{
			return data;
		}
	}

	public class Data_
	{
		@SerializedName("over_18")
		@Expose
		private boolean isOver18;

		@SerializedName("thumbnail")
		@Expose
		private String thumbnail;

		@SerializedName("title")
		@Expose
		private String title;

		@SerializedName("num_comments")
		@Expose
		private int numberOfComments;

		@SerializedName("author")
		@Expose
		private String author;

		@SerializedName("permalink")
		@Expose
		private String permalink;

		@SerializedName("name")
		@Expose
		private String name;

		@SerializedName("created_utc")
		@Expose
		private long createdUtc;


		public boolean isOver18()
		{
			return isOver18;
		}

		public String getThumbnail()
		{
			return thumbnail;
		}

		public String getTitle()
		{
			return title;
		}

		public int getNumberOfComments()
		{
			return numberOfComments;
		}

		public String getAuthor()
		{
			return author;
		}

		public String getPermalink()
		{
			return permalink;
		}

		public String getName()
		{
			return name;
		}

		public long getCreatedUtc()
		{
			return createdUtc;
		}
	}
}
