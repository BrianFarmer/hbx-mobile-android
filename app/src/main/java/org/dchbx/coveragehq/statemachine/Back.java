package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

class Back extends StateMachineAction {

    @Override
    public boolean call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event,
                     StateManager.AppStates leavingState, StateManager.AppStates enterState,
                     EventParameters eventParameters) throws IOException, CoverageException {
        StateInfoBase pop = stateMachine.getStatesStack().pop();
        pop.onPop(stateMachine, stateManager, eventParameters);
        return false;
    }
}
