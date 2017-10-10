package org.dchbx.coveragehq.planshopping;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;

import java.text.NumberFormat;
import java.util.List;

import static org.dchbx.coveragehq.planshopping.PlanShoppingService.DeductibleFilter;
import static org.dchbx.coveragehq.planshopping.PlanShoppingService.GetPlansResult;
import static org.dchbx.coveragehq.planshopping.PlanShoppingService.PremiumFilter;

/**
 * Created by plast on 5/11/2017.
 */

public class PremiumAndDeductibleActivity extends BaseActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(PremiumAndDeductibleActivity.class);
    static private String TAG = "PremiumDeductibleActvty";

    private List<Plan> planList;
    private Button plansAvailable;
    private double maxPremium;
    private int maxDeductible;
    private TextView deductibleHint;
    private TextView premiumHint;
    private SeekBar deductible;
    private SeekBar premium;
    private double currentPremium;
    private double currentDeductible;
    private Events.GetPlansResult planResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        planResults = PlanShoppingService.getPlanResults(getIntent());
        planList = planResults.getPlanList();
        currentDeductible = planResults.getDeductibleFilter();;
        currentPremium = planResults.getPremiumFilter();

        setContentView(R.layout.premium_and_deductible);
        configToolbar();

        premium = (SeekBar) findViewById(R.id.premium);
        deductible = (SeekBar) findViewById(R.id.deductible);
        premium.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                premium.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                populate();
            }
        });
    }

    private void populate() {
        maxPremium = PlanUtilities.roundToHundress(PlanUtilities.getMaxPremium(planList));
        maxDeductible = PlanUtilities.roundToHundress(PlanUtilities.getMaxDeductible(planList));
        deductibleHint = (TextView)findViewById(R.id.deductibleHint);
        premiumHint = (TextView)findViewById(R.id.premiumHint);

        premium.setMax((int)maxPremium/100);
        if (currentPremium > -1) {
            premium.setProgress((int)(currentPremium/100));
            setHint(currentPremium, premium, premiumHint);
        } else {
            premium.setProgress((int)(maxPremium/100));
            currentPremium = maxPremium;
            setHint(maxPremium, premium, premiumHint);
        }

        deductible.setMax((int)maxDeductible/100);
        if (currentDeductible > -1){
            deductible.setProgress((int)(currentDeductible/100));
            setHint(currentDeductible, deductible, deductibleHint);
        } else {
            deductible.setProgress((int)(maxDeductible/100));
            currentDeductible = maxDeductible;
            setHint(maxDeductible, deductible, deductibleHint);
        }

        premium.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                updateFilters();
                setHint(value * 100, premium, premiumHint);
                setPlansAvailableText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        deductible.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                updateFilters();
                setHint(value * 100, deductible, deductibleHint);
                setPlansAvailableText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        plansAvailable = (Button)findViewById(R.id.plansAvailable);
        plansAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMessages().appEvent(StateManager.AppEvents.SeePlans, EventParameters.build()
                        .add(GetPlansResult, planResults).add(DeductibleFilter, currentDeductible).add(PremiumFilter, currentPremium));
            }
        });
        setPlansAvailableText();
        updateFilters();
    }

    private void updateFilters() {
        if (premium == null
            || deductible == null){
            return;
        }
        currentPremium = premium.getProgress() * 100;
        currentDeductible = deductible.getProgress() * 100;
    }

    private void setHint(double value, SeekBar seekBar, TextView hint) {
        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();
        String valueString = currencyInstance.format(value);
        valueString = valueString.substring(0, valueString.length() - 3);
        hint.setText(valueString);
        int hintWidth = hint.getWidth();
        Drawable thumb = seekBar.getThumb();
        Rect bounds = thumb.getBounds();

        Rect textBounds = new Rect();
        Paint textPaint = hint.getPaint();
        textPaint.getTextBounds(valueString,0,valueString.length(),textBounds);
        int width = textBounds.width();
        int x = (int) (bounds.centerX() - width/2 + (int)seekBar.getX());// + getResources().getDimension(R.dimen.ivl_seekbar_margin));
        Log.d(TAG, String.format("%s l: %d center: %d r: %d width/2: %d seekbar: %d", valueString, bounds.left, bounds.centerX(), bounds.right, hintWidth/2, (int)seekBar.getX()));// + getResources().getDimension(R.dimen.ivl_seekbar_margin));
        hint.setX(x);
    }

    private void setPlansAvailableText() {
        List<Plan> plansInRange = PlanUtilities.getPlansInRange(planList, currentPremium, currentDeductible);
        plansAvailable.setText(String.format(getString(R.string.see_plans_available), plansInRange.size()));
    }
}
