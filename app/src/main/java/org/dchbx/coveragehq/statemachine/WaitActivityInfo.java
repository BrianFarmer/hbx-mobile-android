package org.dchbx.coveragehq.statemachine;

public class WaitActivityInfo<E,S> extends ActivityInfo {

    private StateManager.UiActivity uiActivity;

    public WaitActivityInfo(StateManager.AppStates state, StateManager.AppEvents event, StateManager.UiActivity uiActivity) {
        super(state, event, uiActivity);
        this.uiActivity = uiActivity;
    }

    public StateManager.UiActivity getUiActivity(){
        return uiActivity;
    }

    @Override
    public void onPop(StateMachine stateMachine, StateManager stateManager, EventParameters eventParameters){
        stateManager.hideWait();
    }

    @Override

    public StateManager.ResumeActions onResume(){
        return StateManager.ResumeActions.Pop;
    }

    public void onError(StateMachine stateMachine, StateManager stateManager){
        stateManager.hideWait();
    }
}
