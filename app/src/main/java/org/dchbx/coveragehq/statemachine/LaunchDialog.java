package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

public class LaunchDialog implements StateMachineAction{
    @Override
    public void call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents e, StateManager.AppStates leavingState, StateManager.AppStates enterState) throws IOException, CoverageException {
        stateMachine.push(new ActivityInfo(enterState, null));
        stateManager.launchDialog(null);
    }
}
