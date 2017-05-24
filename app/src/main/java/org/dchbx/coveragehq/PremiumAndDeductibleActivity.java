package org.dchbx.coveragehq;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.dchbx.coveragehq.models.planshopping.Plan;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by plast on 5/11/2017.
 */

public class PremiumAndDeductibleActivity extends BaseActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.premium_and_deductible);
        getMessages().getPlans();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetPlansResult getPlansResult) throws Exception {
        planList = getPlansResult.getPlanList();
        currentPremium = getPlansResult.getPremiumFilter();
        currentDeductible = getPlansResult.getDeductibleFilter();
        populate();
    }

    private void populate() {
        maxPremium = PlanUtilities.roundToHundress(PlanUtilities.getMaxPremium(planList));
        maxDeductible = PlanUtilities.roundToHundress(PlanUtilities.getMaxDeductible(planList));
        deductibleHint = (TextView)findViewById(R.id.deductibleHint);
        premiumHint = (TextView)findViewById(R.id.premiumHint);

        premium = (SeekBar) findViewById(R.id.premium);
        premium.setMax((int)maxPremium/100);
        if (currentPremium > -1) {
            premium.setProgress((int)(currentPremium/100));
            setHint(currentPremium, premium, premiumHint);
        } else {
            premium.setProgress((int)(maxPremium/100));
            setHint(maxPremium, premium, premiumHint);
        }

        deductible = (SeekBar) findViewById(R.id.deductible);
        deductible.setMax((int)maxDeductible/100);
        if (currentDeductible > -1){
            deductible.setProgress((int)(currentDeductible/100));
            setHint(currentDeductible, deductible, deductibleHint);
        } else {
            deductible.setProgress((int)(maxDeductible/100));
            setHint(maxDeductible, deductible, deductibleHint);
        }

        premium.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                setPlansAvailableText(value*100, deductible.getProgress()*100);
                NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();
                String premiumString = currencyInstance.format(value * 100);
                setHint(value * 100, premium, premiumHint);
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
                setPlansAvailableText(premium.getProgress()*100, value*100);
                updateFilters();
                setHint(value * 100, deductible, deductibleHint);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        plansAvailable = (Button)findViewById(R.id.plansAvailable);
        setPlansAvailableText(maxPremium, maxDeductible);
        plansAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intents.launchPlanSelector(PremiumAndDeductibleActivity.this);
            }
        });
    }

    private void updateFilters() {
        if (premium == null
            || deductible == null){
            return;
        }
        getMessages().updateFilters(premium.getProgress() * 100, deductible.getProgress() * 100);
    }

    private void setHint(double value, SeekBar seekBar, TextView hint) {
        NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();
        String valueString = currencyInstance.format(value);
        valueString = valueString.substring(0, valueString.length() - 3);
        hint.setText(valueString);
        int hintWidth = deductibleHint.getWidth();
        Rect bounds = seekBar.getThumb().getBounds();
        int x = (int) (bounds.centerX() - hintWidth/2 + (int)seekBar.getX());// + getResources().getDimension(R.dimen.ivl_seekbar_margin));
        hint.setX(x);
    }

    private void setPlansAvailableText(double maxPremium, double maxDeductible) {
        List<Plan> plansInRange = PlanUtilities.getPlansInRange(planList, maxPremium, maxDeductible);
        plansAvailable.setText(String.format(getString(R.string.see_plans_available), plansInRange.size()));
    }
}
