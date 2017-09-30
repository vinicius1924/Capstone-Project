package com.example.vinicius.capstone.view;

import android.support.compat.BuildConfig;
import android.support.v4.app.ActivityCompat;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vinicius on 19/09/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTest
{
	private MainActivity activity;

	@Before
	public void setUp() throws Exception
	{
		/* Cria uma nova instância de MainActivity e chama o método onCreate() */
		activity = Robolectric.buildActivity(MainActivity.class).create().get();
	}

	@Test
	public void addition_isCorrect() throws Exception
	{
		assertEquals(4, 2 + 2);
	}

}
