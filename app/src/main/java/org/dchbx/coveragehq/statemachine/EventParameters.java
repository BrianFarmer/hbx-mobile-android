package org.dchbx.coveragehq.statemachine;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.dchbx.coveragehq.models.fe.Field;
import org.dchbx.coveragehq.models.startup.ResumeParameters;

import java.util.ArrayList;
import java.util.List;

public class EventParameters {
    private static final String TAG = "EventParameters";
    private ArrayList<EventParameter> eventParameters;

    public EventParameters(){
        eventParameters = new ArrayList<EventParameter>();
    }

    public void initIntent(Intent intent){
        for (EventParameter eventParameter : eventParameters) {
            eventParameter.initIntent(intent);
        }
    }

    static public EventParameters build(){
        return new EventParameters();
    }

    public EventParameters add(String name, Field field){
        eventParameters.add(new FieldParameter(name, field));
        return this;
    }

    public EventParameters add(String name, StateManager.ActivityResultCodes value){
        eventParameters.add(new ActivityResultParameter(name, value));
        return this;
    }

    public EventParameters add(String name, StateManager.AppStates value){
        eventParameters.add(new EventStateParameter(name, value));
        return this;
    }

    public EventParameters add(String name, int value){
        eventParameters.add(new IntegerParameter(name, value));
        return this;
    }

    public EventParameters add(String name, JsonObject value){
        eventParameters.add(new JsonObjectParameter(name, value));
        return this;
    }

    public EventParameters add(String name, List<Field> value){
        eventParameters.add(new FieldListParameter(name, value));
        return this;
    }

    public EventParameters add(String name, ArrayList<Integer> value){
        eventParameters.add(new IntegerListParameter(name, value));
        return this;
    }

    public EventParameters add(String name, String value){
        eventParameters.add(new StringParameter(name, value));
        return this;
    }

    public EventParameters add(String name, Object obj){
        eventParameters.add(new StringParameter(name, new Gson().toJson(obj)));
        return this;
    }

    public void initBundle(Bundle bundle) {
        for (EventParameter eventParameter : eventParameters) {
            eventParameter.initBundle(bundle);
        }

    }

    public int getInteger(String name) throws Exception {
        for (EventParameter eventParameter : eventParameters) {
            if (eventParameter.getName().equals(name)){
                return ((IntegerParameter)eventParameter).value;
            }
        }

        throw new Exception("Can find ResultCode event parameter");
    }

    public StateManager.ActivityResultCodes getActivityResultCode(String name) throws Exception {
        for (EventParameter eventParameter : eventParameters) {
            if (eventParameter.getName().equals(name)){
                return ((ActivityResultParameter)eventParameter).value;
            }
        }

        throw new Exception("Can find ResultCode event parameter");
    }

    public Object getObject(String loginParameters, Class<ResumeParameters> resumeParametersClass) {
        int i = 0;
        for (EventParameter eventParameter : eventParameters) {
            if (eventParameter.getName().equals(loginParameters)){
                break;
            }
            i ++;
        }

        EventParameter eventParameter = eventParameters.get(i);
        StringParameter stringParameter = (StringParameter) eventParameter;
        return new Gson().fromJson(stringParameter.value, resumeParametersClass);
    }

    public static abstract class EventParameter {
        protected String name;

        public String getName(){
            return name;
        }

        public abstract void initIntent(Intent intent);
        public abstract void initBundle(Bundle bundle);
    }


    public static class ActivityResultParameter extends EventParameter {

        private StateManager.ActivityResultCodes value;

        public ActivityResultParameter(String name, StateManager.ActivityResultCodes value){
            this.name = name;
            this.value = value;
        }

        @Override
        public void initIntent(Intent intent) {
            if (value != null) {
                intent.putExtra(name, value.ordinal());
            }
        }

        @Override
        public void initBundle(Bundle bundle) {
            if (value != null){
                bundle.putInt(name, value.ordinal());
            }
        }
    }

    public static class EventStateParameter extends EventParameter {

        private StateManager.AppStates value;

        public EventStateParameter(String name, StateManager.AppStates value){
            this.name = name;
            this.value = value;
        }

        @Override
        public void initIntent(Intent intent) {
            if (value != null) {
                intent.putExtra(name, value.ordinal());
            }
        }

        @Override
        public void initBundle(Bundle bundle) {
            if (value != null){
                bundle.putInt(name, value.ordinal());
            }
        }
    }

    public static class FieldParameter extends EventParameter {

        private Field value;

        public FieldParameter(String name, Field value){
            this.name = name;
            this.value = value;
        }

        @Override
        public void initIntent(Intent intent) {
            intent.putExtra(name, new Gson().toJson(value));
        }

        @Override
        public void initBundle(Bundle bundle) {
            bundle.putString(name, new Gson().toJson(value));
        }
    }

    public static class StringParameter extends EventParameter {

        private String value;

        public StringParameter(String name, String value){
            this.name = name;
            this.value = value;
        }

        @Override
        public void initIntent(Intent intent) {
            intent.putExtra(name, value);
        }

        @Override
        public void initBundle(Bundle bundle) {
            bundle.putString(name, value);
        }
    }

    public static class IntegerParameter extends EventParameter {

        private int value;

        public IntegerParameter(String name, int value){
            this.name = name;
            this.value = value;
        }

        @Override
        public void initIntent(Intent intent) {
            intent.putExtra(name, value);
        }

        @Override
        public void initBundle(Bundle bundle) {
            bundle.putInt(name, value);
        }
    }

    public static class FieldListParameter extends EventParameter {

        private List<Field> value;

        public FieldListParameter(String name, List<Field> value){
            this.name = name;
            this.value = value;
        }

        @Override
        public void initIntent(Intent intent) {
            Gson gson = new Gson();
            try {
                String jsonString = gson.toJson(value);
                intent.putExtra(name, jsonString);
            } catch (Exception e){
                Log.e(TAG, "exception build json: " + e);
            }
        }

        @Override
        public void initBundle(Bundle bundle) {
            Gson gson = new Gson();
            String jsonString = gson.toJson(value);
            bundle.putString(name, jsonString);
        }
    }

    public static class JsonObjectParameter extends EventParameter {

        private JsonObject value;

        public JsonObjectParameter(String name, JsonObject value){
            this.name = name;
            this.value = value;
        }

        @Override
        public void initIntent(Intent intent) {
            Gson gson = new Gson();
            try {
                String jsonString = gson.toJson(value);
                intent.putExtra(name, jsonString);
            } catch (Exception e){
                Log.e(TAG, "exception build json: " + e);
            }
        }

        @Override
        public void initBundle(Bundle bundle) {
            Gson gson = new Gson();
            String jsonString = gson.toJson(value);
            bundle.putString(name, jsonString);
        }
    }

    public static class IntegerListParameter extends EventParameter {

        private ArrayList<Integer> value;

        public IntegerListParameter(String name, ArrayList<Integer> value){
            this.name = name;
            this.value = value;
        }

        @Override
        public void initIntent(Intent intent) {
            intent.putIntegerArrayListExtra(name, value);
        }

        @Override
        public void initBundle(Bundle bundle) {
            bundle.putIntegerArrayList(name, value);
        }
    }
}
