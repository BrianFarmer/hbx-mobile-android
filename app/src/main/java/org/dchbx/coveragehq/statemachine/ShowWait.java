package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

public class ShowWait implements StateMachineAction {
    private final StateManager.UiActivity uiActivity;

    public ShowWait(StateManager.UiActivity uiActivity){
        this.uiActivity = uiActivity;
    }

    @Override
    public void call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event,
                     StateManager.AppStates leavingState, StateManager.AppStates enterState,
                     EventParameters eventParameters) throws IOException, CoverageException {
        stateMachine.push(new ActivityInfo((StateManager.AppStates) enterState, event, uiActivity));
        stateManager.showWait();
    }
}
