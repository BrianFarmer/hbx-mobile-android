package org.dchbx.coveragehq;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.dchbx.coveragehq.models.roster.Health;
import org.dchbx.coveragehq.models.services.Service;

/**
 * Created by plast on 4/6/2017.
 */

public class PlanResourcesAdapter extends BaseAdapter {



    public PlanResourcesAdapter(Health health){

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}


abstract class PlanResourceItemWrapperBase {
}

class SummarySectionHeaderItem extends PlanResourceItemWrapperBase {
}

class SummaryColumnsItem extends PlanResourceItemWrapperBase {

}

class SummaryItem extends PlanResourceItemWrapperBase {
    private PlanResourcesAdapter planResourcesAdapter;

    public SummaryItem(PlanResourcesAdapter planResourcesAdapter, Service service){
        this.planResourcesAdapter = planResourcesAdapter;
    }
}

class ResourcesHeaderItem extends PlanResourceItemWrapperBase {

}

class ResourcesItem extends PlanResourceItemWrapperBase {

}


