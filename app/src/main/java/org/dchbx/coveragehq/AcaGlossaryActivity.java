package org.dchbx.coveragehq;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;

import org.dchbx.coveragehq.models.glossary.GlossaryTerm;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by plast on 3/1/2017.
 */

public class AcaGlossaryActivity extends AppCompatActivity {
    private String TAG = AcaGlossaryActivity.class.getName();

    private EventBus eventBus;
    private String currentFilter;
    private String currentSearchTerm;
    private HashMap<String, TextView> filterTextViews = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "In AcaGlossaryActivity.onCreate");

        eventBus = EventBus.getDefault();
        eventBus.register(this);
        eventBus.post(new Events.GetGlossary());

        Intent intent = getIntent();
        if (intent != null){
            Uri data = intent.getData();
            if (data != null) {
                String lastPathSegment = data.getLastPathSegment();
                if (lastPathSegment != null){
                    currentSearchTerm = lastPathSegment;
                }
            }
        }

        setContentView(R.layout.aca_glossary);
        FlexboxLayout flexboxLayoutLetters = (FlexboxLayout) findViewById(R.id.flexboxLayoutLetters);
        int childCount = flexboxLayoutLetters.getChildCount();
        filterTextViews = new HashMap<>();
        for (int i = 0; i < childCount; i ++){
            View child = flexboxLayoutLetters.getChildAt(i);
            String tag = (String) child.getTag();
            if (tag != null
                && tag.compareTo(getString(R.string.glossary_filter_tag)) == 0){
                final TextView filter = (TextView) child;
                filter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence letter = ((TextView) view).getText();
                        setFilter(letter.subSequence(0, 1).toString(), null);
                    }
                });
                filterTextViews.put(filter.getText().toString(), filter);
            }
        }
        Button buttonClearFilter = (Button) findViewById(R.id.buttonClearFilter);
        buttonClearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFilter(null, null);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Glossary glossaryEvent) {
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

        ListView listViewGlossary = (ListView) findViewById(R.id.listViewGlossary);
        ArrayList<GlossaryTerm> glossaryTerms = new ArrayList<>(glossaryEvent.getGlossary());
        listViewGlossary.setAdapter(new GlossaryAdapter(this, glossaryTerms));
    }

    private void filter(View view) {
    }

    public void setFilter(String filter, String search) {

        // Check to see if there is a current filter and clear the underline.
        if (this.currentFilter != null) {
            TextView textView = filterTextViews.get(this.currentFilter);
            textView.setPaintFlags(textView.getPaintFlags() ^ Paint.UNDERLINE_TEXT_FLAG);
        }

        // If we are filtering, underline it.
        if (filter != null) {
            TextView textView = filterTextViews.get(filter);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        this.currentFilter = filter;
        this.currentSearchTerm = search;

        if (filter == null){
        } else {
            Toast toast = Toast.makeText(this, "filtering to " + filter, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
