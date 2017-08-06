package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

class Back implements StateMachineAction {

    @Override
    public void call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event, StateManager.AppStates leavingState, StateManager.AppStates enterState) throws IOException, CoverageException {
        stateMachine.back();
    }
}
