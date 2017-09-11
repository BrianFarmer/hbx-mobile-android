package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

public class PopActivity implements StateMachineAction {
    private StateManager.UiActivity uiActivity;

    public PopActivity(){
    }

    @Override
    public void call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event,
                     StateManager.AppStates leavingState, StateManager.AppStates enterState,
                     EventParameters eventParameters) throws IOException, CoverageException {
        StateInfoBase pop = stateMachine.getStatesStack().pop();
        stateManager.popActivity(uiActivity, eventParameters);
    }
}
