package gov.dc.broker;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by plast on 9/23/2016.
 */

public class EmployeeFilterDialog extends AppCompatDialogFragment {
    View view;
    EmployeeFilterDialog dialog;
    private Context context;
    static private String SECURITY_QUESTION = "SecurityQuestion";

    private OnDialogFinishedListener onDialogFinishedListener = null;

    public void setOnDialogFinishedListener(RosterFragment onDialogFinishedListener) {
        this.onDialogFinishedListener = onDialogFinishedListener;
    }

    public interface OnDialogFinishedListener {
        void canceled();
        void filter(String filterName);
    }

    public static EmployeeFilterDialog build(RosterFragment rosterFragment) {
        EmployeeFilterDialog dialog = new EmployeeFilterDialog();
        dialog.setOnDialogFinishedListener(rosterFragment);
        return dialog;
    }

    public EmployeeFilterDialog(){
        this.dialog = this;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.employee_filter_dialog,container, false);

        TextView textViewEnrolledFilter = (TextView) view.findViewById(R.id.textViewEnrolledFilter);
        textViewEnrolledFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDialogFinishedListener != null){
                    onDialogFinishedListener.filter("enrolled");
                }
                dialog.dismiss();
            }
        });
        TextView textViewWaivedFilter = (TextView) view.findViewById(R.id.textViewWaivedFilter);
        textViewWaivedFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDialogFinishedListener != null){
                    onDialogFinishedListener.filter("waived");
                }
                dialog.dismiss();            }
        });
        TextView textViewNotEnrolledFilter = (TextView) view.findViewById(R.id.textViewNotEnrolledFilter);
        textViewNotEnrolledFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDialogFinishedListener != null){
                    onDialogFinishedListener.filter("not_enrolled");
                }
                dialog.dismiss();
            }
        });
        TextView textViewTerminatedFilter = (TextView) view.findViewById(R.id.textViewTerminatedFilter);
        textViewTerminatedFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onDialogFinishedListener != null){
                    onDialogFinishedListener.filter("terminated");
                }
                dialog.dismiss();
            }
        });

        Button cancelButton = (Button) view.findViewById(R.id.buttonCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDialogFinishedListener != null){
                    onDialogFinishedListener.canceled();
                }
                dialog.dismiss();
            }
        });
        return view;
    }
}
