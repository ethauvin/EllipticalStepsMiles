/*
 * MainActivity.java
 *
 * Copyright (c) 2015 Erik C. Thauvin (http://erik.thauvin.net/)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the authors nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.thauvin.erik.android.ellipticalstepsmiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity
{

    private final String defaultValue = "0";
    private final int defaultStrideValue = 20;

    private SharedPreferences prefs;

    /**
     * Validates a string.
     *
     * @param s The string to validate.
     * @return returns <code>true</code> if the string is not empty or null, <code>false</code> otherwise.
     */
    public static boolean isValid(String s)
    {
        return (s != null) && (!s.trim().isEmpty());
    }

    /**
     * Builds OnTouchListener for clear buttons.
     *
     * @param field The EditText field.
     * @return A new OnTouchListener.
     */
    private View.OnTouchListener buildOnTouchListener(final EditText field, final Drawable img)
    {
        return new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                // Is there an X showing?
                if (field.getCompoundDrawables()[2] == null)
                {
                    return false;
                }

                // Only do this for up touches
                if (event.getAction() != MotionEvent.ACTION_UP)
                {
                    return false;
                }

                // Is touch one of our clear buttons?
                if (event.getX() > field.getWidth() - field.getPaddingRight() - img.getIntrinsicWidth())
                {
                    field.requestFocusFromTouch();
                    field.setText("");
                }

                return false;
            }
        };
    }

    /**
     * Copy value to clipboard, if valid.
     *
     * @param label The label string.
     * @param value The value string.
     */
    private void copyToClipboard(CharSequence label, String value)
    {
        if (isValid(value) && !defaultValue.equals(value))
        {
            final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            final ClipData clip = ClipData.newPlainText(label, value);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getApplicationContext(), getString(R.string.toast_copied, label), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Returns the value of the specified shared reference based on the specified string id.
     *
     * @param id           The string id.
     * @param defaultValue The default value, used if the preference is empty.
     * @return The preference value.
     */
    private String getPref(int id, String defaultValue)
    {
        return prefs.getString(getString(id), defaultValue);
    }

    /**
     * Returns the current version number.
     *
     * @return The current version number or empty.
     */
    private String getVersionNumber()
    {
        try
        {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (final PackageManager.NameNotFoundException e)
        {
            return "";
        }
    }

    /**
     * Initializes the various variables.
     */
    private void init()
    {
        final EditText revolutions = (EditText) findViewById(R.id.revolutions);
        revolutions.setImeOptions(EditorInfo.IME_ACTION_DONE);

        final TextView steps = (TextView) findViewById(R.id.steps);
        final TextView miles = (TextView) findViewById(R.id.miles);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        revolutions.setText(getPref(R.string.prefs_revolutions, ""));
        steps.setText(getPref(R.string.prefs_steps, defaultValue));
        miles.setText(getPref(R.string.prefs_miles, defaultValue));

        final Drawable imgX = getResources().getDrawable(R.drawable.x, getTheme());

        revolutions.setOnEditorActionListener(
                new TextView.OnEditorActionListener()
                {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                    {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        {
                            if (event == null || !event.isShiftPressed())
                            {
                                final String totalRevs = revolutions.getText().toString();
                                if (isValid(totalRevs))
                                {
                                    final int revs = Integer.parseInt(totalRevs);
                                    if (revs >= 0)
                                    {
                                        final int strideLength = prefs.getInt(getString(R.string.prefs_stride_length), defaultStrideValue);
                                        final String totalSteps = String.valueOf(revs * 2);
                                        final String totalMiles = String.format("%.2f", (revs * 2) / (63360.0 / strideLength));

                                        steps.setText(totalSteps);
                                        miles.setText(totalMiles);

                                        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(revolutions.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                                        savePrefs(strideLength, totalRevs, totalSteps, totalMiles);
                                    }
                                }

                                return true;
                            }
                        }
                        return false;
                    }
                }
        );

        final TextWatcher tw = new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                manageClearButton(revolutions, imgX);

                if ("".equals(revolutions.getText().toString()))
                {
                    steps.setText(defaultValue);
                    miles.setText(defaultValue);
                }
            }
        };

        revolutions.addTextChangedListener(tw);
        revolutions.setOnTouchListener(buildOnTouchListener(revolutions, imgX));


        steps.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                copyToClipboard(getString(R.string.steps_label), steps.getText().toString());
            }
        });

        miles.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                copyToClipboard(getString(R.string.miles_label), miles.getText().toString());
            }
        });

        manageClearButton(revolutions, imgX);
    }

    /**
     * Manages the clear button.
     *
     * @param view The text view.
     * @param img  The image.
     */
    private void manageClearButton(TextView view, Drawable img)
    {
        if (view.getText().toString().equals(""))
        {
            view.setCompoundDrawablesWithIntrinsicBounds(view.getCompoundDrawables()[0], view.getCompoundDrawables()[1], null,
                    view.getCompoundDrawables()[3]);
        }
        else
        {
            view.setCompoundDrawablesWithIntrinsicBounds(view.getCompoundDrawables()[0], view.getCompoundDrawables()[1], img,
                    view.getCompoundDrawables()[3]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_stride)
        {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.action_stride);

            final int stride = prefs.getInt(getString(R.string.prefs_stride_length), defaultStrideValue);

            final String unit = getString(R.string.stride_unit);
            final int[] strideLength = getResources().getIntArray(R.array.stride_lengths);
            final String[] items = new String[strideLength.length];

            int strideIndex = 0;
            for (int i = 0; i < items.length; i++)
            {
                if (strideLength[i] == stride)
                {
                    strideIndex = i;
                }
                items[i] = String.valueOf(strideLength[i]) + unit;
            }

            builder.setSingleChoiceItems(items, strideIndex, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    savePrefs(strideLength[which], "", defaultValue, defaultValue);
                    final EditText revolutions = (EditText) findViewById(R.id.revolutions);
                    revolutions.setText("");

                    dialog.dismiss();
                }
            });


            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });

            builder.show();

            return true;
        }
        else if (id == R.id.action_about)
        {
            final AlertDialog builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(Html.fromHtml(String.format(getString(R.string.about_message), getVersionNumber())))
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    dialog.dismiss();
                                }
                            })
                    .show();
            ((TextView) builder.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Saves the preferences.
     *
     * @param strideLength The stride length.
     * @param revolutions  The revolutions count.
     * @param steps        The steps count.
     * @param miles        The miles count.
     */
    private void savePrefs(int strideLength, String revolutions, String steps, String miles)
    {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(getString(R.string.prefs_stride_length), strideLength);
        editor.putString(getString(R.string.prefs_revolutions), revolutions);
        editor.putString(getString(R.string.prefs_steps), steps);
        editor.putString(getString(R.string.prefs_miles), miles);
        editor.apply();
    }
}
