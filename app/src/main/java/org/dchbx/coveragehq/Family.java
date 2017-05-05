package org.dchbx.coveragehq;

import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by plast on 5/4/2017.
 */

public class Family extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.family);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.Employee  employeeEvent) throws Exception {
    }
}
