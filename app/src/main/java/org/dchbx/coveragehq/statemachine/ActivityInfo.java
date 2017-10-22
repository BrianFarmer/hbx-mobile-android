package org.dchbx.coveragehq.statemachine;

public class ActivityInfo extends StateInfoBase {
    private int uiActivityId;
    private transient StateManager.UiActivity uiActivity;

    public ActivityInfo(StateManager.AppStates state, StateManager.AppEvents event,
                        StateManager.UiActivity uiActivity) {
        super(state,event);
        this.uiActivity = uiActivity;
        if (uiActivity != null) {
            uiActivityId = uiActivity.getId();
        } else {
            uiActivityId = 0;
        }
    }

    public StateManager.UiActivity getUiActivity(){
        return uiActivity;
    }

    @Override
    public void reconstitute(){
        uiActivity = StateManager.UiActivity.getUiActivityType(uiActivityId).uiActivity;
    }

    @Override
    public void onPop(StateMachine stateMachine, StateManager stateManager, EventParameters eventParameters){
        stateManager.back(eventParameters);
    }
}
