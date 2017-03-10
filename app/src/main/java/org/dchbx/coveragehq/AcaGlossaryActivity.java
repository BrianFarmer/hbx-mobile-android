package org.dchbx.coveragehq;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 3/1/2017.
 */

public class AcaGlossaryActivity extends AppCompatActivity {
    private String TAG = AcaGlossaryActivity.class.getName();

    private EventBus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "In AcaGlossaryActivity.onCreate");

        eventBus = EventBus.getDefault();
        eventBus.register(this);
        eventBus.post(new Events.GetGlossary());

        setContentView(R.layout.aca_glossary);
        FlexboxLayout flexboxLayoutLetters = (FlexboxLayout) findViewById(R.id.flexboxLayoutLetters);
        int childCount = flexboxLayoutLetters.getChildCount();
        for (int i = 0; i < childCount; i ++){
            View child = flexboxLayoutLetters.getChildAt(i);
            String tag = (String) child.getTag();
            if (tag != null
                && tag.compareTo(getString(R.string.glossary_filter_tag)) == 0){
                final TextView filter = (TextView) child;
                filter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        filter(view);
                    }
                });
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Carriers carriersEvent) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setLogo(R.drawable.app_header);
        toolbar.setTitle("");
    }

    private void filter(View view) {
        CharSequence letter = ((TextView) view).getText();
        setFilter(letter.charAt(0));
    }

    public void setFilter(char filter) {
        Toast toast = Toast.makeText(this, "filtering to " + filter, Toast.LENGTH_LONG);
        toast.show();
    }
}
