package org.desperu.go4lunch.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Go4LunchPrefsTest {


    @Mock Context mockContext;
    @Mock SharedPreferences mockPrefs;

    private String key = "test";
    private int intTest = 1;
    private String stringTest = "A string !";
    private Long longTest = System.currentTimeMillis();
    private boolean booleanTest = true;

    @Before
    public void before() {
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);

        when(mockPrefs.getString(key, null)).thenReturn(stringTest);
        when(mockPrefs.getInt(key, 0)).thenReturn(intTest);
        when(mockPrefs.getLong(key, 0)).thenReturn(longTest);
        when(mockPrefs.getBoolean(key, false)).thenReturn(booleanTest);
    }

    @Test
    public void Given_String_When_getString_Then_checkValue() {
        String output = Go4LunchPrefs.getString(mockContext, key, null);

        assertEquals(stringTest, output);
    }

    @Test
    public void Given_integer_When_getInt_Then_checkValue() {
        int output = Go4LunchPrefs.getInt(mockContext, key, 0);

        assertEquals(intTest, output);
    }

    @Test
    public void Given_Long_When_getLong_Then_checkValue() {
        Long output = Go4LunchPrefs.getLong(mockContext, key, 0);

        assertEquals(longTest, output);
    }

    @Test
    public void Given_boolean_When_getBoolean_Then_checkValue() {
        boolean output = Go4LunchPrefs.getBoolean(mockContext, key, false);

        assertEquals(booleanTest, output);
    }
}
