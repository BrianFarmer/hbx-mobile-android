package org.dchbx.coveragehq;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.dchbx.coveragehq.models.glossary.GlossaryTerm;

import java.util.ArrayList;

/**
 * Created by plast on 3/10/2017.
 */

public class GlossaryAdapter extends BaseAdapter implements Filterable{
    private Context context;
    private ArrayList<GlossaryTerm> allGlossaryTerms;
    private ArrayList<GlossaryTerm> glossaryTerms;
    private Filter filter = null;

    public GlossaryAdapter(Context contect, ArrayList<GlossaryTerm> glossaryTerms) {
        this.context = contect;
        this.glossaryTerms = glossaryTerms;
        this.allGlossaryTerms = glossaryTerms;
    }

    @Override
    public int getCount() {
        return glossaryTerms.size();
    }

    @Override
    public Object getItem(int i) {
        return glossaryTerms.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null){
            view = LayoutInflater.from(context).inflate(R.layout.glossary_item, parent, false);
        } else {
            view = convertView;
        }

        GlossaryTerm glossaryTerm = (GlossaryTerm) getItem(i);
        ((TextView)view.findViewById(R.id.textViewglossaryItemLabel)).setText(glossaryTerm.term);
        TextView textViewglossaryDefinitionLabel = (TextView)view.findViewById(R.id.textViewglossaryDefinitionLabel);
        textViewglossaryDefinitionLabel.setLinksClickable(true);
        String replacement = glossaryTerm.description.replace("/glossary#", "smallbiz://dchbx.org/glossary/");
        textViewglossaryDefinitionLabel.setText(Html.fromHtml(replacement));
        textViewglossaryDefinitionLabel.setMovementMethod(LinkMovementMethod.getInstance());
        return view;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class GlossaryFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<GlossaryTerm> filteredTerms = new ArrayList<>();
            for (GlossaryTerm glossaryTerm: allGlossaryTerms) {
                if (charSequence.length() == 1){
                    if (glossaryTerm.term.charAt(0) == charSequence.charAt(0)) {
                        filteredTerms.add(glossaryTerm);
                    }
                } else {
                    if (glossaryTerm.term.contains(charSequence)) {
                        filteredTerms.add(glossaryTerm);
                    }
                }
            }
            FilterResults filteredResults = new FilterResults();
            filteredResults.values = filteredTerms;
            filteredResults.count = filteredTerms.size();
            return filteredResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            glossaryTerms = (ArrayList<GlossaryTerm>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}


