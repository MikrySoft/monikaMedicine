package pl.mikrysoft.monika.medicine;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.android.gms.common.api.CommonStatusCodes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class WelcomeActivity extends AppCompatActivity {

    public static final int RC_OCR_CAPTURE = 9003;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext, btnSaveDaily, btnSaveAlarm, btnSaveLek, btngo;
    private PreferenceManager prefManager;
    TimePicker timePicker;
    CalendarView calendarView;
    TextView myDate;
    EditText pressure;
    EditText weight;

    EditText lek;
    EditText dawka;
    EditText rano;
    EditText poludnie;
    EditText wieczor;
    Button btnOCR;
    Button btnAlarmOff;

    TextToSpeech toSpeech;
    int result;
    EditText editText;
    String text;

    DatabaseHelper myDb;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking for first time launch - before calling setContentView()
        prefManager = new PreferenceManager(this);
//        if (!prefManager.isFirstTimeLaunch()) {
//            launchHomeScreen();
//            finish();
//        }

        myDb = new DatabaseHelper(this);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_welcome);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnNext = (Button) findViewById(R.id.btn_next);



//        editText= (EditText) findViewById(R.id.editText_lek);
//        toSpeech = new TextToSpeech(WelcomeActivity.this, new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int i) {
//                if(i==TextToSpeech.SUCCESS)
//                    result=toSpeech.setLanguage(Locale.UK);
//                else
//                    Toast.makeText(WelcomeActivity.this, "",Toast.LENGTH_LONG).show();
//            }
//        });



        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.slide_screen1,
                R.layout.slide_screen2a,
                R.layout.slide_screen3,
                R.layout.slide_screen4,
                R.layout.slide_screen5};

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });



        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
            }
        });



    }




    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void setAlarm(long time) {
        //getting the alarm manager
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //creating a new intent specifying the broadcast receiver
        Intent i = new Intent(this, MyAlarm.class);

        //creating a pending intent using the intent
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);

        //setting the repeating alarm that will be fired every day
        am.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pi);
        Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show();
    }

    private void cancelAlarm() {
        /* Request the AlarmManager object */
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, MyAlarm.class);

        /* Create the PendingIntent that would have launched the BroadcastReceiver */
        PendingIntent pending = PendingIntent.getBroadcast(this, 0, intent, 0);

        /* Cancel the alarm associated with that PendingIntent */
        manager.cancel(pending);
    }


    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(WelcomeActivity.this, HomeActivity.class));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            if (position < layouts.length) {
                addBottomDots(position);
            }
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public void showMessage(String title, String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }



        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);

            switch (position) {
                case 0:
                    btnSaveAlarm = (Button) view.findViewById(R.id.buttonAlarm);
                    timePicker = (TimePicker) view.findViewById(R.id.timePicker);
                    btnAlarmOff = (Button) view.findViewById(R.id.btnAlarmOff);
                    btnSaveAlarm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Calendar calendar = Calendar.getInstance();
                            if (android.os.Build.VERSION.SDK_INT >= 23) {
                                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                                        timePicker.getHour(), timePicker.getMinute(), 0);
                            } else {
                                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                                        timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
                            }

                            setAlarm(calendar.getTimeInMillis());
                        }
                    });

                    btnAlarmOff.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(WelcomeActivity.this, "Alarm wyłączony",Toast.LENGTH_LONG).show();
                            cancelAlarm();
                        }
                    });

                    break;
                case 1:
                    lek = (EditText) view.findViewById(R.id.editLek);
                    dawka = (EditText) view.findViewById(R.id.editDawka);
                    rano = (EditText) view.findViewById(R.id.editRano);
                    poludnie = (EditText) view.findViewById(R.id.editPoludnie);
                    wieczor = (EditText) view.findViewById(R.id.editWieczorem);
                    btnSaveLek = (Button) view.findViewById(R.id.btnSaveLek);
                    btnSaveLek.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean isInserted = myDb.insertDateLeki(lek.getText().toString(), dawka.getText().toString(), rano.getText().toString(),poludnie.getText().toString(),wieczor.getText().toString());
                            if(isInserted=true)
                                Toast.makeText(WelcomeActivity.this, "Zapisano lek",Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(WelcomeActivity.this, "Nie zapisano leku",Toast.LENGTH_LONG).show();

                        }
                    });

                    btnOCR = (Button) view.findViewById(R.id.btnOCR);
                    btnOCR.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(v.getContext(), OcrCaptureActivity.class);
                            intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                            intent.putExtra(OcrCaptureActivity.UseFlash, false);

                            startActivityForResult(intent, RC_OCR_CAPTURE);

                        }
                });

                    break;
                case 2:
                    pressure = (EditText) view.findViewById(R.id.textedit_pressure);
                    weight = (EditText) view.findViewById(R.id.textedit_weight);
                    btnSaveDaily = (Button) view.findViewById(R.id.btnSaveDaily);
                    btnSaveDaily.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Date date = new Date();
                            boolean isInserted = myDb.insertDatePomiary(date.toString(), weight.getText().toString(), pressure.getText().toString());
                            if(isInserted=true)
                                Toast.makeText(WelcomeActivity.this, "Zapisano pomiar",Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(WelcomeActivity.this, "Nie zapisano pomiaru",Toast.LENGTH_LONG).show();

                        }
                    });

                    break;
                case 3:
                    calendarView =(CalendarView) view.findViewById(R.id.calendarView);

                    calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                        @Override
                        public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            String selectedDate = sdf.format(new Date(calendarView.getDate()));

                            Cursor res = myDb.getAllPressureData();
                            if (res.getCount() ==0) {
                                Toast.makeText(WelcomeActivity.this, "Brak danych",Toast.LENGTH_LONG).show();
                            }

                            StringBuffer buffer = new StringBuffer();
                            while(res.moveToNext()){
                                buffer.append("Waga:" + res.getString(2)+"\n\n");
                                buffer.append("Ciśnienie:" + res.getString(3));
                            }
                        showMessage(selectedDate, buffer.toString());
                        }
                    });
                    break;
                case 4:
                    Cursor res = myDb.getAllMedicineData();
                    if (res.getCount() ==0) {
                        //show sth
                    }

                    StringBuffer buffer = new StringBuffer();
                    while(res.moveToNext()){
                        buffer.append("Id:" + res.getString(0)+"\n");
                        buffer.append("Lek:" + res.getString(1)+"\n");
                        buffer.append("Dawka:" + res.getString(2)+"\n");
                        buffer.append("Rano:" + res.getString(3)+"\n");
                        buffer.append("Poludnie:" + res.getString(4)+"\n");
                        buffer.append("Wieczór:" + res.getString(5)+"\n");
                    }

                    Toast.makeText(WelcomeActivity.this, "4",Toast.LENGTH_LONG).show();
                    break;
                case 5:
                    Toast.makeText(WelcomeActivity.this, "5",Toast.LENGTH_LONG).show();






                    break;

            }
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                   // statusMessage.setText(R.string.ocr_success);
                    lek.setText(text);
                    Log.d("OCR", "Text read: " + text);
                } else {
                    //statusMessage.setText(R.string.ocr_failure);
                    Log.d("OCR", "No Text captured, intent data is null");
                }
            } else {
                //statusMessage.setText(String.format(getString(R.string.ocr_error),
                        //CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
