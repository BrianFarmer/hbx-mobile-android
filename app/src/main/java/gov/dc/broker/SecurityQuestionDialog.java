package gov.dc.broker;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by plast on 9/23/2016.
 */

public class SecurityQuestionDialog extends AppCompatDialogFragment {
    View view;
    SecurityQuestionDialog dialog;
    private Context context;
    static private String SECURITY_QUESTION = "SecurityQuestion";

    public static SecurityQuestionDialog build(String question) {
        SecurityQuestionDialog dialog = new SecurityQuestionDialog();

        Bundle bundle = new Bundle();
        bundle.putCharSequence(SECURITY_QUESTION, question);
        dialog.setArguments(bundle);
        return dialog;
    }

    public SecurityQuestionDialog(){
        this.dialog = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.security_question_dialog,container, false);

        Bundle arguments = getArguments();
        String securityQuestion = arguments.getCharSequence(SECURITY_QUESTION).toString();
        TextView securityQuestionTextView = (TextView) view.findViewById(R.id.textViewSecurityQuestion);
        securityQuestionTextView.setText(securityQuestion);

        EditText securityAnswer = (EditText)view.findViewById(R.id.editTextSecurityAnswer);
        securityAnswer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ((LoginActivity) dialog.getActivity()).securityDialogOkClicked(((EditText) view.findViewById(R.id.editTextSecurityAnswer)).getText().toString());
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });

        Button cancelButton = (Button) view.findViewById(R.id.buttonCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Button okButton = (Button) view.findViewById(R.id.buttonOk);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LoginActivity)dialog.getActivity()).securityDialogOkClicked(((EditText)view.findViewById(R.id.editTextSecurityAnswer)).getText().toString());
                dialog.dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
