package org.dchbx.coveragehq.financialeligibility;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.BrokerActivity;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.ServiceManager;
import org.dchbx.coveragehq.Utilities;
import org.dchbx.coveragehq.models.fe.Field;
import org.dchbx.coveragehq.models.fe.Option;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.OnActivityResultListener;
import org.dchbx.coveragehq.statemachine.StateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.text.InputType.TYPE_CLASS_NUMBER;

/*
    This file is part of DC.

    DC Health Link SmallBiz is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.  If not, see <http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file, close to the copyright notices. When using the Lesser GPL, insert the word “Lesser” before “General” in all three places. When using the GNU AGPL, insert the word “Affero” before “General” in all three places.
*/
public class ApplicationQuestionsActivity extends BrokerActivity {
    private static String TAG = "ApplicationQuestions";

    protected ArrayList<Field> schemaFields;
    protected JsonObject jsonObject;
    protected LinearLayout linearLayout;
    private ArrayList<FieldType> fields;
    protected static HashMap<String, String> replacements;

    public ApplicationQuestionsActivity(){
        schemaFields = null;
        jsonObject = null;
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG, "resuming");
    }

    protected void populate() throws Exception {
        if (schemaFields == null
            || jsonObject == null){
            return;
        }
        hideProgress();

        LayoutInflater inflater = LayoutInflater.from(this);
        fields = populateSchemaFields(schemaFields, jsonObject, inflater, true);

    }

    protected ArrayList<FieldType> populateSchemaFields(ArrayList<Field> schemaFields, JsonObject values, LayoutInflater inflater, boolean first) throws Exception {

        ArrayList<FieldType> fields = new ArrayList<>();
        for (Field field : schemaFields) {
            Log.d(TAG, "field name: " + field.field + " type: " + field.type);
            FieldType newField;
            TextField textField;
            switch (Schema.fieldTypes.get(field.type.toLowerCase())){
            case text:
                Log.d(TAG, "found text field");
                textField = new TextField(field, values, false, linearLayout);
                newField = textField;
                fields.add(textField);
                textField.fillField(inflater, first);
                break;
            case numeric:
                Log.d(TAG, "found numeric field");
                textField = new TextField(field, values, true, linearLayout);
                newField = textField;
                fields.add(textField);
                textField.fillField(inflater, first);
                break;
            case date:
                Log.d(TAG, "found date field");
                DateField dateField = new DateField(field, values, linearLayout);
                newField = dateField;
                fields.add(dateField);
                dateField.fillField(inflater, first);
                break;
            case dropdown:
                Log.d(TAG, "found dropdown field");
                DropDownField dropDownField = new DropDownField(this, field, values, linearLayout);
                newField = dropDownField;
                fields.add(dropDownField);
                dropDownField.fillField(inflater, first);
                break;
            case id:
                Log.d(TAG, "found id field");
                Idfield idfield = new Idfield(field, values, linearLayout);
                newField = idfield;
                fields.add(idfield);
                idfield.fillField(inflater, first);
                break;
            case multidropdown:
                Log.d(TAG, "found multidropdown field");
                MultiDropDownField multiDropDownField = new MultiDropDownField(field, values, linearLayout);
                newField = multiDropDownField;
                fields.add(multiDropDownField);
                multiDropDownField.fillField(inflater, first);
                break;
            case section:
                Log.d(TAG, "found section field");
                SectionField sectionField = new SectionField(field, values, linearLayout);
                newField = sectionField;
                fields.add(sectionField);
                sectionField.fillField(inflater, first);
                break;
            case ssn:
                Log.d(TAG, "found ssn field");
                SsnField ssnField = new SsnField(field, values, linearLayout);
                newField = ssnField;
                fields.add(ssnField);
                ssnField.fillField(inflater, first);
                break;
            case yesnoradio:
                Log.d(TAG, "found yesnoradio field");
                YesNoRadioField yesNoRadioField = new YesNoRadioField(field, values, linearLayout);
                newField = yesNoRadioField;
                fields.add(yesNoRadioField);
                yesNoRadioField.fillField(inflater, first);
                break;
            case zip:
                Log.d(TAG, "found zip field");
                ZipField zipField = new ZipField(field, values, linearLayout);
                newField = zipField;
                fields.add(zipField);
                zipField.fillField(inflater, first);
                break;
            case hardwired:
                Log.d(TAG, "found hardwired field");
                HardwiredField hardwiredField = new HardwiredField(field, values);
                newField = hardwiredField;
                fields.add(hardwiredField);
                hardwiredField.fillField(inflater, first);
                break;
            case idgen:
                Log.d(TAG, "found eapersonid field");
                EaPersonIdField eaPersonIdField = new EaPersonIdField(field, values);
                newField = eaPersonIdField;
                fields.add(newField);
                eaPersonIdField.fillField(inflater, first);
                break;
            default:
                throw new Exception("Unknown field type: " + field.type);
            }
            first = false;
            if (field.dependentFields != null
                && field.dependentFields.size() > 0){
                try {
                    newField.dependentFields = populateSchemaFields(field.dependentFields, values, inflater, false);
                } catch (Throwable t){
                    Log.e(TAG, "field name: " + field.field);
                }
                    try {
                    newField.configureDependentFieldsVisibility(newField.dependentFields);
                } catch (Throwable t){
                    Log.e(TAG, "field name: " + field.field);
                }
            }
        }
        return fields;
    }

    public static String replace(String string){
        if (replacements != null){
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                string = string.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return string;
    }

    protected JsonObject getValues() {
        JsonObject values = new JsonObject();
        getValues(values, fields);
        return values;
    }
    protected void getValues(JsonObject values, ArrayList<FieldType> fields){
        String mostRecentField = "";
        FieldType mostRecentFieldType = null;
        try {
            for (FieldType field : fields) {
                mostRecentField = field.field.field;
                mostRecentFieldType = field;
                if (field.field.field.equals("mpostalcode")) {
                    Log.d(TAG, "found it");
                }
                if (field.field.field.equals("isseparatemailaddress")) {
                    Log.d(TAG, "found it");
                }
                if (field.field.prereqValues == null
                        || field.field.prereqValues.size() == 0
                        || (values.has(field.field.prereqField)
                            && field.field.prereqValues.contains(values.get(field.field.prereqField).getAsString()))) {
                    JsonElement value = field.getValue();
                    if (value != null) {
                        values.add(field.field.field, value);
                    }
                    if (field.dependentFields != null) {
                        try {
                            getValues(values, field.dependentFields);
                        } catch (Throwable t) {
                            Log.e(TAG, "exception: " + t);
                            Log.e(TAG, "field name: " + field.field.field);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "exception: " + t);
            Log.e(TAG, "field name: " + mostRecentField);
            if (mostRecentFieldType != null) {
                Log.e(TAG, mostRecentFieldType.field.field);
            }
        }
    }

    public static abstract class FieldType{
        protected Field field;
        protected final LinearLayout linearLayout;
        protected JsonObject values;
        protected final ApplicationQuestionsActivity activity;
        protected ArrayList<FieldType> dependentFields;
        protected View view;

        public FieldType(Field field, LinearLayout linearLayout, JsonObject values,
                         ApplicationQuestionsActivity activity){
            this.field = field;
            this.linearLayout = linearLayout;
            this.values = values;
            this.activity = activity;
        }

        public FieldType(Field field, JsonObject values) {
            this.field = field;
            this.values = values;
            linearLayout = null;
            activity = null;
        }

        public abstract JsonElement getValue();
        abstract public void fillField(LayoutInflater inflater, boolean first);
        public abstract void configureDependentFieldsVisibility(ArrayList<FieldType> fields);

        public void showView(int visibility){
            if (view != null){
                view.setVisibility(visibility);
            }
        }

        public void setVisibility(int visible){
            if (view != null){
                view.setVisibility(visible);
            }
        }
    }

    public static class TextField extends FieldType {
        private final boolean numericOnly;
        private EditText value;
        protected View indicator;

        public TextField(Field field, JsonObject values, boolean numericOnly, LinearLayout linearLayout) {
            super(field, linearLayout, values, null);
            this.numericOnly = numericOnly;
        }

        @Override
        public JsonElement getValue() {
            return new JsonPrimitive(value.getText().toString());
        }

        public void fillField(LayoutInflater inflater, boolean first) {
            view = inflater.inflate(R.layout.app_text_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            value = (EditText) view.findViewById(R.id.value);
            if (numericOnly){
                value.setInputType(TYPE_CLASS_NUMBER);
            }
            if (values.has(field.field)){
                String logstr = values.get(field.field).getAsString();
                Log.d(TAG, logstr);
                value.setText(values.get(field.field).getAsString());
            } else {
                if (field.defaultValue != null){
                    value.setText(field.defaultValue);
                }
            }
            if (first){
                value.requestFocus();
            }
            indicator = view.findViewById(R.id.indicator);
            if (field.optional.equals("N")){
                value.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        checkIndicator();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                checkIndicator();
            } else {
                indicator.setVisibility(View.GONE);
            }
        }

        private void checkIndicator() {
            indicator.setVisibility(value.getText().length() > 0?View.GONE:View.VISIBLE);
        }

        public void testDependentFields(String value){
            for (FieldType fieldType : dependentFields) {
                boolean assigned = false;
                for (Object prereqValue : fieldType.field.prereqValues) {
                    if (prereqValue.toString().equals(value)){
                        fieldType.showView(View.VISIBLE);
                        assigned = true;
                        if (fieldType.dependentFields != null) {
                            fieldType.configureDependentFieldsVisibility(fieldType.dependentFields);
                        }
                    }
                }
                if (!assigned){
                    fieldType.showView(View.GONE);
                }
            }
        }

        @Override
        public void configureDependentFieldsVisibility(final ArrayList<FieldType> fields) {

            this.dependentFields = fields;
            value.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    testDependentFields(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            testDependentFields(value.getText().toString());
        }
    }

    public static class DropDownField extends FieldType {
        private TextView value;
        private String savedValue;

        public DropDownField(ApplicationQuestionsActivity activity, Field field, JsonObject values,
                             LinearLayout linearLayout) {
            super(field, linearLayout, values, activity);
            if (values.has(field.field)) {
                savedValue = values.get(field.field).getAsString();
            }
        }

        @Override
        public JsonElement getValue() {
            return new JsonPrimitive(savedValue);
        }

        @Override
        public void fillField(LayoutInflater inflater, boolean first) {
            view = inflater.inflate(R.layout.app_dropdown_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(ApplicationQuestionsActivity.replace(field.label));
            value = (TextView) view.findViewById(R.id.value);

            if (values.has(field.field)){
                value.setText(getStringForDropDownValue(field, values));
            } else {
                if (field.defaultValue != null) {
                    value.setText(field.defaultValue);
                }
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ApplicationQuestionsActivity.setOnActivityResultListener(new OnActivityResultListener() {
                        @Override
                        public void onActivityResult(Intent intent) {
                            if (intent != null) {
                                int result = intent.getIntExtra("Result", -1);
                                Option option = field.options.get(result);
                                savedValue = option.value;
                                values.addProperty(field.field, option.value);
                                value.setText(getStringForDropDownValue(field, values));
                                if (dependentFields != null) {
                                    configureDependentFieldsVisibility(dependentFields);
                                }
                                checkIndicator();
                            }
                        }
                    });
                    activity.getMessages().appEvent(StateManager.AppEvents.ShowDropDown,
                            EventParameters.build().add("Field", field)
                                    .add("Value", getDropDownValue(field, values)));
                }
            });
            checkIndicator();
        }

        private void checkIndicator() {
            if (values.has(field.field)){
                String valueAsString = values.get(field.field).getAsString();
                if (valueAsString != null
                    && valueAsString.length() > 0){
                    view.findViewById(R.id.indicator).setVisibility(View.GONE);
                    return;
                }
            }
            view.findViewById(R.id.indicator).setVisibility(View.VISIBLE);
        }

        private String getStringForDropDownValue(Field field, JsonObject values) {
            if (values.has(field.field)){
                String value = values.get(field.field).getAsString();
                for (Option option : field.options) {
                    if (option.value.equals(value)){
                        return option.name;
                    }
                }
            }
            return field.defaultValue;
        }

        private String getDropDownValue(Field field, JsonObject values) {
            if (values.has(field.field)){
                return values.get(field.field).getAsString();
            }
            return field.defaultValue;
        }

        @Override
        public void configureDependentFieldsVisibility(final ArrayList<FieldType> fields) {
            testDependentValue(fields);
        }

        private void testDependentValue(ArrayList<FieldType> fields) {
            if (!values.has(field.field)){
                for (FieldType fieldType : fields) {
                    if (fieldType.view != null) {
                        fieldType.view.setVisibility(View.GONE);
                    }
                }
            }
            String curValue;
            if (values.has(field.field)) {
                curValue = values.get(field.field).getAsString();
            } else {
                curValue = field.defaultValue;
                if (curValue == null){
                    for (FieldType fieldType : fields) {
                        fieldType.showView(View.GONE);
                    }
                }
            }
            for (FieldType fieldType : fields) {
                boolean assigned = false;
                if (fieldType.field.prereqValues != null
                    && fieldType.field.prereqValues.size() > 0){
                    for (Object prereqValue : fieldType.field.prereqValues) {
                        if (curValue != null
                            && curValue.equals(prereqValue.toString())){
                            fieldType.showView(View.VISIBLE);
                            assigned = true;
                            if (fieldType.dependentFields != null){
                                fieldType.configureDependentFieldsVisibility(fieldType.dependentFields);
                            }
                        }
                    }
                }
                if (!assigned){
                    fieldType.showView(View.GONE);
                }
            }
        }
    }

    public static class Idfield extends FieldType {
        private EditText value;

        public Idfield (Field field, JsonObject values, LinearLayout linearLayout) {
            super(field, linearLayout, values, null);
            this.field = field;
            this.values = values;
        }

        @Override
        public JsonElement getValue() {
            return new JsonPrimitive(((TextView)value).getText().toString());
        }

        @Override
        public void fillField(LayoutInflater inflater, boolean first) {
            view = inflater.inflate(R.layout.app_id_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            value = (EditText) view.findViewById(R.id.value);
            if (values.has(field.field)){
                String str = (String) values.get(field.field).getAsString();
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }
            if (first){
                value.requestFocus();
            }
        }

        @Override
        public void configureDependentFieldsVisibility(ArrayList<FieldType> fields) {

        }
    }

    public static class DateField extends FieldType {

        private EditText value;
        protected View indicator;

        public DateField(Field field, JsonObject values, LinearLayout linearLayout) {
            super(field, linearLayout, values, null);
            this.field = field;
            this.values = values;
        }

        @Override
        public JsonElement getValue() {
            CharSequence text = value.getText();
            if (text == null){
                return null;
            }
            String s = text.toString();
            try {
                return new JsonPrimitive(Utilities.DateAsString(Utilities.parseDate(s.toString())));
            } catch (Exception e){
                return null;
            }
        }

        @Override
        public void fillField(LayoutInflater inflater, boolean first) {
            view = inflater.inflate(R.layout.app_date_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            value = (EditText) view.findViewById(R.id.value);
            if (values.has(field.field)){
                String str = (String) values.get(field.field).getAsString();
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }
            if (first){
                value.requestFocus();
            }
            indicator = view.findViewById(R.id.indicator);
            if (field.optional.equals("N")){
                value.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        checkIndicator();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                checkIndicator();
            } else {
                indicator.setVisibility(View.GONE);
            }
        }

        private void checkIndicator() {
            indicator.setVisibility(value.getText().length() < 10?View.VISIBLE:View.GONE);
        }

        public void testDependentFields(String value){
            for (FieldType fieldType : dependentFields) {
                boolean assigned = false;
                for (Object prereqValue : fieldType.field.prereqValues) {
                    if (prereqValue.toString().equals(value)){
                        fieldType.showView(View.VISIBLE);
                        assigned = true;
                        if (fieldType.dependentFields != null){
                            fieldType.configureDependentFieldsVisibility(fieldType.dependentFields);
                        }
                    }
                }
                if (!assigned){
                    fieldType.showView(View.GONE);
                }
            }
        }

        @Override
        public void configureDependentFieldsVisibility(final ArrayList<FieldType> fields) {

            this.dependentFields = fields;
            value.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    testDependentFields(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            testDependentFields(value.getText().toString());
        }
    }

    class MultiDropDownField extends FieldType {

        public MultiDropDownField(Field field, JsonObject values, LinearLayout linearLayout) {
            super(field, linearLayout, values, null);
        }

        @Override
        public JsonElement getValue() {
            return null;
        }

        @Override
        public void fillField(LayoutInflater inflater, boolean first) {
            /*
            View view = inflater.inflate(R.layout.app_text_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            EditText value = (EditText) view.findViewById(value);
            if (jsonObject.containsKey(field.field)){
                String str = (String)jsonObject.get(field.field);
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }*/
        }

        @Override
        public void configureDependentFieldsVisibility(ArrayList<FieldType> fields) {

        }
    }

    class SectionField extends FieldType {

        private JsonArray savedValues;
        private View indicator;

        public SectionField(Field field, JsonObject values, LinearLayout linearLayout) {
            super(field, linearLayout, values, null);
            if (values.has(field.field)){
                try {

                    Object object = values.get(field.field);
                    Log.d(TAG, "object: " + object);
                    savedValues = values.get(field.field).getAsJsonArray();
                } catch (Throwable t){
                    Log.e(TAG, "Exception: " + t);
                }
            } else {
                savedValues = null;
            }
        }

        @Override
        public JsonElement getValue() {
            return savedValues;
        }

        @Override
        public void fillField(LayoutInflater inflater, boolean first) {
            view = inflater.inflate(R.layout.app_section_field, linearLayout, false);
            indicator = view.findViewById(R.id.indicator);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseActivity.setOnActivityResultListener(new OnActivityResultListener() {
                        @Override
                        public void onActivityResult(Intent intent) {
                            try{
                                if (intent != null) {
                                    String jsonString = intent.getStringExtra("Result");
                                    Gson gson = new Gson();
                                    JsonObject sectionValues = gson.fromJson(jsonString, JsonObject.class);
                                    JsonArray jsonArray = new JsonArray();
                                    jsonArray.add(sectionValues);
                                    savedValues = jsonArray;
                                    configureDependentFieldsVisibility(dependentFields);
                                    checkIndicator();
                                }
                            } catch (Throwable t){
                                Log.e(TAG, "exception: " + t);
                            }
                        }
                    });

                    String json;
                    if (savedValues == null){
                        JsonArray jsonArray = new JsonArray();
                        jsonArray.add(ServiceManager.getServiceManager().getFinancialEligibilityService().build(field.dependentFields));
                        savedValues = jsonArray;
                    }
                    getMessages().appEvent(StateManager.AppEvents.OpenSection, EventParameters.build()
                            .add("Section", new Gson().toJson(savedValues.get(0)))
                            .add("Schema", field.subFields));
                }
            });
            checkIndicator();
        }

        private void checkIndicator() {
            if (savedValues == null){
                indicator.setVisibility(View.VISIBLE);
                return;
            }
            JsonElement jsonElement = savedValues.get(0);
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            indicator.setVisibility(FinancialEligibilityService.checkObject(asJsonObject, field.dependentFields)?View.GONE:View.VISIBLE);
        }

        @Override
        public void configureDependentFieldsVisibility(ArrayList<FieldType> fields) {
            Log.d(TAG, "section dependent fields!");
        }
    }

    public static class SsnField extends FieldType {

        private EditText value;
        protected View indicator;

        public SsnField(Field field, JsonObject values, LinearLayout linearLayout) {
            super(field, linearLayout, values,  null);
        }

        @Override
        public JsonElement getValue() {
            return new JsonPrimitive(value.getText().toString());
        }

        @Override
        public void fillField(LayoutInflater inflater, boolean first) {
            view = inflater.inflate(R.layout.app_ssn_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            value = (EditText) view.findViewById(R.id.value);
            if (values.has(field.field)){
                String str = values.get(field.field).getAsString();
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }
            if (first){
                value.requestFocus();
            }
            indicator = view.findViewById(R.id.indicator);
            if (field.optional.equals("N")){
                value.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        checkIndicator();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                checkIndicator();
            } else {
                indicator.setVisibility(View.GONE);
            }
        }

        private void checkIndicator() {
            indicator.setVisibility(value.getText().length() == 11?View.GONE:View.VISIBLE);
        }

        public void testDependentFields(String value){
            for (FieldType fieldType : dependentFields) {
                boolean assigned = false;
                for (Object prereqValue : fieldType.field.prereqValues) {
                    if (prereqValue.toString().equals(value)){
                        fieldType.showView(View.VISIBLE);
                        assigned = true;
                        if (fieldType.dependentFields != null){
                            fieldType.configureDependentFieldsVisibility(fieldType.dependentFields);
                        }
                    }
                }
                if (!assigned){
                    fieldType.showView(View.GONE);
                }
            }
        }

        @Override
        public void configureDependentFieldsVisibility(final ArrayList<FieldType> fields) {

            this.dependentFields = fields;
            value.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    testDependentFields(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            testDependentFields(value.getText().toString());
        }
    }

    class YesNoRadioField extends FieldType {

        private RadioGroup radioGroup;
        private String savedValue;
        private View indicator;

        public YesNoRadioField(Field field, JsonObject values, LinearLayout linearLayout) {
            super(field, linearLayout, values, null);
        }

        @Override
        public JsonElement getValue() {
            if (savedValue == null){
                return null;
            }
            return new JsonPrimitive(savedValue);
        }

        @Override
        public void fillField(LayoutInflater inflater, boolean first) {
            view = inflater.inflate(R.layout.app_yesnoradio_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup);
            RadioButton yesButton = (RadioButton) view.findViewById(R.id.yesButton);
            RadioButton noButton = (RadioButton) view.findViewById(R.id.noButton);
            if (values.has(field.field)){
                savedValue = values.get(field.field).getAsString();
                if (savedValue.equals("Y")){
                    yesButton.setChecked(true);
                    if (first) {
                        yesButton.requestFocus();
                    }
                } else {
                    if (savedValue.equals("N")){
                        noButton.setChecked(true);
                        if (first) {
                            noButton.requestFocus();
                        }
                    }
                }
            } else {
                if (field.defaultValue != null
                        && field.defaultValue.toLowerCase().compareTo("Y") == 0) {
                    yesButton.setChecked(true);
                    if (first) {
                        yesButton.requestFocus();
                    }
                } else if (field.defaultValue != null
                        && field.defaultValue.toLowerCase().compareTo("N") == 0) {
                    noButton.setChecked(true);
                    if (first) {
                        noButton.requestFocus();
                    }
                }
            }
            indicator = view.findViewById(R.id.indicator);
            checkIndicator();
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    if (checkedId == R.id.yesButton){
                        savedValue = "Y";
                    } else if (checkedId == R.id.noButton){
                        savedValue = "N";
                    }
                    testDependentFields(checkedId);
                    checkIndicator();
                }
            });
        }

        private void checkIndicator() {
            indicator.setVisibility(radioGroup.getCheckedRadioButtonId() == -1?View.VISIBLE:View.GONE);
        }

        @Override
        public void configureDependentFieldsVisibility(ArrayList<FieldType> fields) {
            this.dependentFields = fields;
            testDependentFields(radioGroup.getCheckedRadioButtonId());
        }

        private void testDependentFields(int checkedId) {
            if (dependentFields == null){
                return;
            }
            if (checkedId == R.id.yesButton){
                for (FieldType dependentFieldType : dependentFields) {
                    boolean foundPrereqValue = false;
                    for (Object prereqValue : dependentFieldType.field.prereqValues) {
                        if (prereqValue.equals("Y")) {
                            dependentFieldType.setVisibility(View.VISIBLE);
                            foundPrereqValue = true;
                            if (dependentFieldType.dependentFields != null){
                                dependentFieldType.configureDependentFieldsVisibility(dependentFieldType.dependentFields);
                            }
                            break;
                        }
                    }
                    if (!foundPrereqValue
                        && dependentFieldType.view != null){
                        dependentFieldType.view.setVisibility(View.GONE);
                    }
                }

            } else if (checkedId == R.id.noButton){
                for (FieldType dependentFieldType : dependentFields) {
                    boolean foundPrereqValue = false;
                    for (Object prereqValue : dependentFieldType.field.prereqValues) {
                        if (prereqValue.equals("N")) {
                            if (dependentFieldType.view != null){
                                dependentFieldType.setVisibility(View.VISIBLE);
                            }
                            foundPrereqValue = true;
                        }
                    }
                    if (!foundPrereqValue
                        && dependentFieldType.view != null) {
                        dependentFieldType.setVisibility(View.GONE);
                    }
                }
            } else {
                for (FieldType dependentFieldType : dependentFields) {
                    if (dependentFieldType.view != null) {
                        dependentFieldType.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    class ZipField extends FieldType {
        private EditText value;
        protected View indicator;

        public ZipField(Field field, JsonObject values, LinearLayout linearLayout) {
            super(field, linearLayout, values, null);
        }

        @Override
        public JsonElement getValue() {
            return new JsonPrimitive(value.getText().toString());
        }

        @Override
        public void fillField(LayoutInflater inflater, boolean first) {
            view = inflater.inflate(R.layout.app_zip_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            value = (EditText) view.findViewById(R.id.value);
            if (jsonObject.has(field.field)){
                String str = jsonObject.get(field.field).getAsString();
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }
            if (first){
                value.requestFocus();
            }
            indicator = view.findViewById(R.id.indicator);
            if (field.optional.equals("N")){
                value.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        checkIndicator();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                checkIndicator();
            } else {
                indicator.setVisibility(View.GONE);
            }
        }

        private void checkIndicator() {
            indicator.setVisibility(value.getText().length() < 5?View.VISIBLE:View.GONE);
        }

        @Override
        public void configureDependentFieldsVisibility(ArrayList<FieldType> fields) {

        }
    }

    class HardwiredField extends FieldType {

        public HardwiredField(Field field, JsonObject values) {
            super(field, values);
        }

        @Override
        public JsonElement getValue() {
            try {
                return new JsonPrimitive(field.options.get(0).value);
            } catch (Throwable t){
                Log.e(TAG, "exception getting hardwired field value: " + field.field);
            }
            return null;
        }

        @Override
        public void fillField(LayoutInflater inflater, boolean first) {
        }

        @Override
        public void configureDependentFieldsVisibility(ArrayList<FieldType> fields) {

        }
    }

    class EaPersonIdField extends FieldType {

        public EaPersonIdField(Field field, JsonObject values) {
            super(field, values);
        }

        @Override
        public JsonElement getValue() {
            //if (values.has(field.field)){
            //    return values.get(field.field);
            //}
            return new JsonPrimitive(UUID.randomUUID().toString());
        }

        @Override
        public void fillField(LayoutInflater inflater, boolean first) {
        }

        @Override
        public void configureDependentFieldsVisibility(ArrayList<FieldType> fields) {

        }
    }
}
