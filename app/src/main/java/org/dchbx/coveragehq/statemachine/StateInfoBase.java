package org.dchbx.coveragehq.statemachine;

import java.io.Serializable;

import static org.dchbx.coveragehq.statemachine.StateManager.ResumeActions.Continue;

public abstract class StateInfoBase  implements Serializable{

    private final StateManager.AppStates state;
    private StateManager.AppEvents event;

    public StateInfoBase(StateManager.AppStates state, StateManager.AppEvents event){
        this.state = state;
        this.event = event;

    }

    public void onPop(StateMachine stateMachine, StateManager stateManager){
        stateManager.back();
    }

    public void onError(StateMachine stateMachine, StateManager stateManager){

    }

    public void reconstitute(){

    }

    public StateManager.AppStates getState() {
        return state;
    }

    public StateManager.ResumeActions onResume(){
        return Continue;
    }

    public StateManager.AppEvents getEvent() {
        return event;
    }
}
