package org.dchbx.coveragehq.statemachine;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class EventParameters {
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

    public EventParameters add(String name, StateManager.AppStates value){
        eventParameters.add(new EventStateParameter(name, value));
        return this;
    }

    public EventParameters add(String name, int value){
        eventParameters.add(new IntegerParameter(name, value));
        return this;
    }

    public EventParameters add(String name, String value){
        eventParameters.add(new StringParameter(name, value));
        return this;
    }

    public void initBundle(Bundle bundle) {
        for (EventParameter eventParameter : eventParameters) {
            eventParameter.initBundle(bundle);
        }

    }

    public static abstract class EventParameter {
        public abstract void initIntent(Intent intent);
        public abstract void initBundle(Bundle bundle);
    }

    public static class EventStateParameter extends EventParameter {

        private String name;
        private StateManager.AppStates value;

        public EventStateParameter(String name, StateManager.AppStates value){
            this.name = name;
            this.value = value;
        }

        @Override
        public void initIntent(Intent intent) {
            intent.putExtra(name, value.ordinal());
        }

        @Override
        public void initBundle(Bundle bundle) {
            bundle.putInt(name, value.ordinal());
        }
    }

    public static class StringParameter extends EventParameter {

        private String name;
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

        private String name;
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
}
