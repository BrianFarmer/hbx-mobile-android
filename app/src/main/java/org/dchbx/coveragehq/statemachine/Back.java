package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

class Back implements StateMachineAction {

    @Override
    public void call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event,
                     StateManager.AppStates leavingState, StateManager.AppStates enterState,
                     EventParameters eventParameters) throws IOException, CoverageException {
        StateInfoBase pop = stateMachine.getStatesStack().pop();
        pop.onPop(stateMachine, stateManager);
    }
}
