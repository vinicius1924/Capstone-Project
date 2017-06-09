package com.example.vinicius.capstone.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by vinicius on 25/05/17.
 */

public class SubredditContract
{
	/*
	 * O "Content authority" é um nome para o content provider, similar como a relação entre um dominio
	 * e seu website. Uma string conveniente para usar no content authority é o package name do app, que
	 * é garantido que será único
	 */
	public static final String CONTENT_AUTHORITY = "com.example.vinicius.capstone";
	/*
	 * Usa-se o CONTENT_AUTHORITY para criar a base de todas as URI's que serão usadas para chamar
	 * o content provider
	 */
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	/*
	 * Possiveis caminhos (adicionados a BASE_CONTENT_URI para possiveis URI's).
	 * Por exemplo, content://com.example.vinicius.capstone/subreddits/ é um caminho válido para
	 * procurar por dados de um subreddit. content://com.example.vinicius.capstone/givemeroot/
	 * irá falhar, porque o content provider não tem nenhuma informação sobre o que fazer com "givemeroot"
	 */
	public static final String PATH_SUBREDDITS = "subreddits";
	public static final String PATH_SUBREDDITS_POSTS = "subreddits_posts";

	/* Classe que define o conteúdo da tabela Subreddits */
	public static final class SubredditsEntry implements BaseColumns
	{
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBREDDITS).build();

		/* O content type de um diretório de items */
		public static final String CONTENT_TYPE =
				  ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDITS;
		/* O content type de um único item */
		public static final String CONTENT_ITEM_TYPE =
				  ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDITS;

		public static final String TABLE_NAME = "subreddits";

		public static final String COLUMN_NAME = "display_name";
		public static final String COLUMN_URL = "url";
		public static final String COLUMN_SUBSCRIBED = "subscribed";
		public static final String COLUMN_LAST_DOWNLOADED = "last_downloaded";

		/* Função para construir uma URI para encontrar um subreddit específico por seu id */
		public static Uri buildSubredditsUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}
	}

	public static final class SubredditsPostsEntry implements BaseColumns
	{
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBREDDITS_POSTS).build();

		/* O content type de um diretório de items */
		public static final String CONTENT_TYPE =
				  ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDITS_POSTS;
		/* O content type de um único item */
		public static final String CONTENT_ITEM_TYPE =
				  ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDITS_POSTS;

		public static final String TABLE_NAME = "subreddits_posts";

		public static final String COLUMN_THUMBNAIL = "thumbnail";
		public static final String COLUMN_TITLE = "title";
		public static final String COLUMN_NUM_COMMENTS = "num_comments";
		public static final String COLUMN_AUTHOR = "author";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_PERMALINK = "permalink";
		public static final String COLUMN_CREATED_UTC = "created_utc";
		public static final String COLUMN_SUBREDDITS_ID = "subreddits_id";

		public static Uri buildSubredditsPostsUri(long subredditId) {
			return ContentUris.withAppendedId(CONTENT_URI, subredditId);
		}

		public static Uri buildPostUri(long postId)
		{
			return CONTENT_URI.buildUpon().appendPath("postId").appendPath(String.valueOf(postId)).build();
		}
	}

}
