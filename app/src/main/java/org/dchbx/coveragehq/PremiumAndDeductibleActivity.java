package org.dchbx.coveragehq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;

import org.dchbx.coveragehq.models.planshopping.Plan;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by plast on 5/11/2017.
 */

public class PremiumAndDeductibleActivity extends BaseActivity {

    private List<Plan> planList;
    private Button plansAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.premium_and_deductible);
        getMessages().getPlans();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetPlansResult getPlansResult) throws Exception {
        planList = getPlansResult.getPlanList();
        populate();
    }

    private void populate() {
        double maxPremium = PlanUtilities.getMaxPremium(planList);
        double maxDeductible = PlanUtilities.getMaxDeductible(planList);

        final SeekBar premium = (SeekBar) findViewById(R.id.premium);
        final SeekBar deductible = (SeekBar) findViewById(R.id.deductible);

        premium.setMax((int)maxPremium);
        premium.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setPlansAvailableText(i, deductible.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        deductible.setMax((int)maxDeductible);
        deductible.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setPlansAvailableText(premium.getProgress(), i);
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
    }

    private void setPlansAvailableText(double maxPremium, double maxDeductible) {
        List<Plan> plansInRange = PlanUtilities.getPlansInRange(planList, maxPremium, maxDeductible);
        plansAvailable.setText(String.format(getString(R.string.see_plans_available), plansInRange.size()));
    }
}
