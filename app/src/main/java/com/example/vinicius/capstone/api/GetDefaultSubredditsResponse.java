package com.example.vinicius.capstone.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by vinicius on 25/05/17.
 */

public class GetDefaultSubredditsResponse
{
	@SerializedName("data")
	@Expose
	private Data data;

	public Data getData()
	{
		return data;
	}

	public class Data
	{
		@SerializedName("children")
		@Expose
		private List<Children> children;

		public List<Children> getChildren()
		{
			return children;
		}
	}

	public class Children
	{
		@SerializedName("data")
		@Expose
		private Data_ data;

		public Data_ getData()
		{
			return data;
		}
	}

	public class Data_
	{
		@SerializedName("display_name")
		@Expose
		private String displayName;

		@SerializedName("url")
		@Expose
		private String url;

		@SerializedName("subscribed")
		@Expose
		private int subscribed;

		public String getDisplayName()
		{
			return displayName;
		}

		public String getUrl()
		{
			return url;
		}

		public int getSubscribed()
		{
			return subscribed;
		}
	}
}
