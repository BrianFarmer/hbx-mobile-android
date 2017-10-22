package org.dchbx.coveragehq.ridp;

import android.widget.CheckBox;
import android.widget.TextView;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.Utilities;
import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ValidatedActivityBase extends BaseActivity {

    private String getTextFromField(int fieldId){
        TextView field = (TextView)findViewById(fieldId);
        return field.getText().toString();
    }

    public static class Patterns {
        //of the various email regexs online, this is an intentionally simple one, to avoid false negatives
        public final static String EMAIL = "^.+@.+\\..+$";

        public final static String STATE = "^[A-Z]{2}$";
        public final static String ZIPCODE = "^[0-9]{5}$";
        public final static String SSN = "^\\d{3}-\\d{2}-\\d{4}$";
        public final static String PHONE = "^\\d{3}-\\d{3}-\\d{4}$";

        public final static String DATE_FORMAT = "MM/dd/yyyy";
    }


    private boolean applyValidation(boolean valid, String errorMessage, List<String> issues) {
        if (!valid) {
            issues.add(errorMessage);
        }
        return valid;
    }


    private boolean applyDateValidation(boolean valid, int errorMessageFormat, int fieldName,
                                        Date date, List<String> issues) {
        String message = String.format(getString(errorMessageFormat), getText(fieldName), date, date, date);
        return applyValidation(valid, message, issues);
    }

    protected boolean validateDateInRange(int fieldId, int fieldName, Date min, Date max,
                                          List<String> issues){
        try {
            DateFormat format = new SimpleDateFormat(Patterns.DATE_FORMAT);
            format.setLenient(false);
            Date parsed = format.parse(getTextFromField(fieldId));
            Calendar calendar = Calendar.getInstance();
            calendar.setLenient(false);
            calendar.setTime(parsed);
            Date legit = calendar.getTime();
            return applyDateValidation(!min.after(legit), R.string.minDateValidationError, fieldName, min, issues)
                    & applyDateValidation(!max.before(legit), R.string.maxDateValidationError, fieldName, max, issues);
        } catch (Exception e) {
            issues.add(String.format(getString(R.string.invalidDateValidationError), getText(fieldName)));
            return false;
        }
    }

    protected boolean validateTextFieldByRegex(int fieldId, String pattern, int errorMessage,
                                               List<String> issues) {
        Matcher matcher = Pattern.compile(pattern).matcher(getTextFromField(fieldId));
        return applyValidation(matcher.matches(), getString(errorMessage), issues);
    }

    protected boolean validateRequiredTextField(int fieldId, int fieldName, List<String> issues) {
        int length = getTextFromField(fieldId).length();
        String format = getString(R.string.requiredFieldValidationFormat);
        return applyValidation (length > 0, String.format(format, getString(fieldName)), issues);
    }

    protected boolean validateRequiredCheckBox(int fieldId, int fieldName, List<String> issues) {
        boolean checked = ((CheckBox) findViewById(fieldId)).isChecked();
        String format = getString(R.string.requiredFieldValidationFormat);
        return applyValidation (checked, String.format(format, getString(fieldName)), issues);
    }

    protected boolean validateTextFieldsMatch(int fieldId, int fieldName, int fieldId2,
                                              int fieldName2, List<String> issues) {
        String field1 = getTextFromField(fieldId);
        String field2 = getTextFromField(fieldId2);
        String errorMessage = String.format(getString(R.string.matchingValidationError),
                getString(fieldName), getString(fieldName2));
        return applyValidation(field1.equals(field2), errorMessage, issues);
    }

    protected boolean validate(List<String> issues) {
        return false;
    }

    public void onClick(Account account){
        List<String> issues = new ArrayList<String>();
        if (!validate(issues)) {
            String issueList = String.format("<ul><li>%s</li>", Utilities.join(issues, "<li>"));
            simpleAlert(getString(R.string.validationDialogTitle), issueList);
        } else {
            getMessages().appEvent(StateManager.AppEvents.Continue, EventParameters.build().add("Account", account));
        }
    }
}
