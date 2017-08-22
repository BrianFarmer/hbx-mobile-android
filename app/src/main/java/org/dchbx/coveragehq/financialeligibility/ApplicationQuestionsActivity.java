package org.dchbx.coveragehq.financialeligibility;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.dchbx.coveragehq.BrokerActivity;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.models.fe.Field;
import org.dchbx.coveragehq.models.fe.Option;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.statemachine.StateManager;

import java.util.ArrayList;
import java.util.HashMap;

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

    protected Schema schema;
    protected HashMap<String, Object> person;
    protected LinearLayout linearLayout;

    public ApplicationQuestionsActivity(){
        schema = null;
        person = null;
    }

    protected void populate() throws Exception {
        if (schema == null
            || person == null){
            return;
        }
        hideProgress();

        LayoutInflater inflater = LayoutInflater.from(this);
        ArrayList<FieldType> fields = new ArrayList<>();
        HashMap<String, Object> values = new HashMap<>();
        populateSchemeFields(schema.Person, inflater);

    }

    protected ArrayList<FieldType> populateSchemeFields(ArrayList<Field> schemaFields, LayoutInflater inflater) throws Exception {

        ArrayList<FieldType> fields = new ArrayList<>();
        HashMap<String, Object> values = new HashMap<>();
        for (Field field : schemaFields) {
            Log.d(TAG, "field name: " + field.field + " type: " + field.type);
            FieldType newField;
            if (field.type.toLowerCase().compareTo("text") == 0){
                Log.d(TAG, "found text field");
                TextField textField = new TextField(field, values);
                newField = textField;
                fields.add(textField);
                textField.fillField(inflater);
            } else if (field.type.toLowerCase().compareTo("date") == 0){
                Log.d(TAG, "found date field");
                DateField dateField = new DateField(field, values);
                newField = dateField;
                fields.add(dateField);
                dateField.fillField(inflater);
            } else if (field.type.toLowerCase().compareTo("dropdown") == 0){
                Log.d(TAG, "found dropdown field");
                DropDownField dropDownField = new DropDownField(this, field, values);
                newField = dropDownField;
                fields.add(dropDownField);
                dropDownField.fillField(inflater);
            } else if (field.type.toLowerCase().compareTo("id") == 0){
                Log.d(TAG, "found id field");
                Idfield idfield = new Idfield(field, values);
                newField = idfield;
                fields.add(idfield);
                idfield.fillField(inflater);
            } else if (field.type.toLowerCase().compareTo("multidropdown") == 0){
                Log.d(TAG, "found multidropdown field");
                MultiDropDownField multiDropDownField = new MultiDropDownField(field, values);
                newField = multiDropDownField;
                fields.add(multiDropDownField);
                multiDropDownField.fillField(inflater);
            } else if (field.type.toLowerCase().compareTo("section") == 0){
                Log.d(TAG, "found section field");
                SectionField sectionField = new SectionField(field, values);
                newField = sectionField;
                fields.add(sectionField);
                sectionField.fillField(inflater);
            } else if (field.type.toLowerCase().compareTo("ssn") == 0){
                Log.d(TAG, "found ssn field");
                SsnField ssnField = new SsnField(field, values);
                newField = ssnField;
                fields.add(ssnField);
                ssnField.fillField(inflater);
            } else if (field.type.toLowerCase().compareTo("yesnoradio") == 0){
                Log.d(TAG, "found yesnoradio field");
                YesNoRadioField yesNoRadioField = new YesNoRadioField(field, values);
                newField = yesNoRadioField;
                fields.add(yesNoRadioField);
                yesNoRadioField.fillField(inflater);
            } else if (field.type.toLowerCase().compareTo("zip") == 0){
                Log.d(TAG, "found zip field");
                ZipField zipField = new ZipField(field, values);
                newField = zipField;
                fields.add(zipField);
                zipField.fillField(inflater);
            } else if (field.type.toLowerCase().compareTo("hardwired") == 0){
                Log.d(TAG, "found hardwired field");
                HardwiredField hardwiredField = new HardwiredField(field, values);
                newField = hardwiredField;
                fields.add(hardwiredField);
                hardwiredField.fillField(inflater);
            } else if (field.type.toLowerCase().compareTo("idgen") == 0){
                Log.d(TAG, "found eapersonid field");
                HardwiredField hardwiredField = new HardwiredField(field, values);
                newField = hardwiredField;
                fields.add(hardwiredField);
                hardwiredField.fillField(inflater);
            } else {
                throw new Exception("Unknown field type");
            }
            if (field.dependentFields != null){
                ArrayList<FieldType> fieldTypes = populateSchemeFields(field.dependentFields, inflater);
                newField.setDependentFields(fieldTypes);
            }
        }
        return fields;
    }


    private void fillTextField(LayoutInflater inflater, Field field) {
    }


    abstract class FieldType{
        protected Field field;
        protected HashMap<String, Object> values;
        protected ArrayList<FieldType> fields;
        protected View view;

        abstract public void save();
        abstract public void fillField(LayoutInflater inflater);
        public abstract void setDependentFields(ArrayList<FieldType> fields);

        public void showView(int visibility){
            if (view != null){
                view.setVisibility(visibility);
            }
        }
    }

    class TextField extends FieldType {
        private EditText value;

        public TextField(Field field, HashMap<String, Object> values) {

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        public void fillField(LayoutInflater inflater) {
            view = inflater.inflate(R.layout.app_text_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            value = (EditText) view.findViewById(R.id.value);
            if (person.containsKey(field.field)){
                String str = (String)person.get(field.field);
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }
        }

        public void testDependentFields(String value){
            for (FieldType fieldType : fields) {
                boolean assigned = false;
                for (Object prereqValue : fieldType.field.prereqValues) {
                    if (prereqValue.toString().equals(value)){
                        fieldType.showView(View.VISIBLE);
                        assigned = true;
                    }
                }
                if (!assigned){
                    fieldType.showView(View.GONE);
                }
            }
        }

        @Override
        public void setDependentFields(final ArrayList<FieldType> fields) {

            this.fields = fields;
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

    class DropDownField extends FieldType {
        private final ApplicationQuestionsActivity activity;
        private Spinner value;

        public DropDownField(ApplicationQuestionsActivity activity, Field field, HashMap<String, Object> values) {
            this.activity = activity;

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        @Override
        public void fillField(LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.app_dropdown_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);

            ArrayList<String> values = new ArrayList<>();
            for (Option option : field.options) {
                values.add(option.name);
            }

            value = (Spinner) view.findViewById(R.id.value);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, values);
            value.setAdapter(spinnerAdapter);

        }

        @Override
        public void setDependentFields(final ArrayList<FieldType> fields) {
            value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    testDependentValue(position, fields);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            testDependentValue(value.getSelectedItemPosition(), fields);
        }

        private void testDependentValue(int position, ArrayList<FieldType> fields) {
            String optionValue = field.options.get(position).value;
            for (FieldType fieldType : fields) {
                boolean assigned = false;
                for (Object prereqValue : fieldType.field.prereqValues) {
                    if (optionValue.compareTo(prereqValue.toString()) == 0){
                        fieldType.showView(View.VISIBLE);
                        assigned = true;
                    }
                }
                if (!assigned){
                    fieldType.showView(View.GONE);
                }
            }
        }
    }

    class Idfield extends FieldType {

        public Idfield (Field field, HashMap<String, Object> values) {

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        @Override
        public void fillField(LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.app_id_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            EditText value = (EditText) view.findViewById(R.id.value);
            if (person.containsKey(field.field)){
                String str = (String)person.get(field.field);
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }

        }

        @Override
        public void setDependentFields(ArrayList<FieldType> fields) {

        }
    }

    class DateField extends FieldType {

        private EditText value;

        public DateField(Field field, HashMap<String, Object> values) {

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        @Override
        public void fillField(LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.app_date_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            value = (EditText) view.findViewById(R.id.value);
            if (person.containsKey(field.field)){
                String str = (String)person.get(field.field);
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }
        }

        public void testDependentFields(String value){
            for (FieldType fieldType : fields) {
                boolean assigned = false;
                for (Object prereqValue : fieldType.field.prereqValues) {
                    if (prereqValue.toString().equals(value)){
                        fieldType.showView(View.VISIBLE);
                        assigned = true;
                    }
                }
                if (!assigned){
                    fieldType.showView(View.GONE);
                }
            }
        }

        @Override
        public void setDependentFields(final ArrayList<FieldType> fields) {

            this.fields = fields;
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

        public MultiDropDownField(Field field, HashMap<String, Object> values) {

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        @Override
        public void fillField(LayoutInflater inflater) {
            /*
            View view = inflater.inflate(R.layout.app_text_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            EditText value = (EditText) view.findViewById(value);
            if (person.containsKey(field.field)){
                String str = (String)person.get(field.field);
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }*/
        }

        @Override
        public void setDependentFields(ArrayList<FieldType> fields) {

        }
    }

    class SectionField extends FieldType {

        public SectionField(Field field, HashMap<String, Object> values) {

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        @Override
        public void fillField(LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.app_section_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            ImageButton openSectionButton = (ImageButton)findViewById(R.id.sectionButton);
            getMessages().appEvent(StateManager.AppEvents.OpenSection);
        }

        @Override
        public void setDependentFields(ArrayList<FieldType> fields) {

        }
    }

    class SsnField extends FieldType {

        private EditText value;

        public SsnField(Field field, HashMap<String, Object> values) {

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        @Override
        public void fillField(LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.app_ssn_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            value = (EditText) view.findViewById(R.id.value);
            if (person.containsKey(field.field)){
                String str = (String)person.get(field.field);
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }
        }

        public void testDependentFields(String value){
            for (FieldType fieldType : fields) {
                boolean assigned = false;
                for (Object prereqValue : fieldType.field.prereqValues) {
                    if (prereqValue.toString().equals(value)){
                        fieldType.showView(View.VISIBLE);
                        assigned = true;
                    }
                }
                if (!assigned){
                    fieldType.showView(View.GONE);
                }
            }
        }

        @Override
        public void setDependentFields(final ArrayList<FieldType> fields) {

            this.fields = fields;
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

        public YesNoRadioField(Field field, HashMap<String, Object> values) {

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        @Override
        public void fillField(LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.app_yesnoradio_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup);
            if (field.defaultValue != null
                && field.defaultValue.toLowerCase().compareTo("Y") == 0){
                RadioButton yesButton = (RadioButton) findViewById(R.id.yesButton);
                yesButton.setChecked(true);
            } else if (field.defaultValue != null
                       && field.defaultValue.toLowerCase().compareTo("N") == 0){
                RadioButton noButton = (RadioButton) findViewById(R.id.noButton);
                noButton.setChecked(true);
            }
        }

        @Override
        public void setDependentFields(ArrayList<FieldType> fields) {
            this.fields = fields;
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                    testDependentFields(checkedId);
                }
            });
            testDependentFields(radioGroup.getCheckedRadioButtonId());
        }

        private void testDependentFields(int checkedId) {
            if (checkedId == R.id.yesButton){
                for (FieldType fieldType : fields) {
                    if (field.prereqValues.get(0).equals("Y")){
                        fieldType.view.setVisibility(View.VISIBLE);
                    } else {
                        fieldType.view.setVisibility(View.GONE);
                    }
                }

            } else if (checkedId == R.id.noButton){
                for (FieldType fieldType : fields) {
                    if (field.prereqValues.get(0).equals("N")){
                        fieldType.view.setVisibility(View.VISIBLE);
                    } else {
                        fieldType.view.setVisibility(View.GONE);
                    }
                }
            } else {
                for (FieldType fieldType : fields) {
                    fieldType.view.setVisibility(View.GONE);
                }
            }
        }
    }

    class ZipField extends FieldType {

        public ZipField(Field field, HashMap<String, Object> values) {

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        @Override
        public void fillField(LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.app_zip_field, linearLayout, false);
            linearLayout.addView(view);
            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(field.label);
            EditText value = (EditText) view.findViewById(R.id.value);
            if (person.containsKey(field.field)){
                String str = (String)person.get(field.field);
                value.setText(str);
            } else {
                if (field.defaultValue != null){
                    value.setText((String)field.defaultValue);
                }
            }
        }

        @Override
        public void setDependentFields(ArrayList<FieldType> fields) {

        }
    }

    class HardwiredField extends FieldType {

        public HardwiredField(Field field, HashMap<String, Object> values) {

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        @Override
        public void fillField(LayoutInflater inflater) {
        }

        @Override
        public void setDependentFields(ArrayList<FieldType> fields) {

        }
    }

    class EaPersonIdField extends FieldType {

        public EaPersonIdField(Field field, HashMap<String, Object> values) {

            this.field = field;
            this.values = values;
        }

        @Override
        public void save() {

        }

        @Override
        public void fillField(LayoutInflater inflater) {
        }

        @Override
        public void setDependentFields(ArrayList<FieldType> fields) {

        }
    }
}
