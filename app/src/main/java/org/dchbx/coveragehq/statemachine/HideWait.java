package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

public class HideWait extends StateMachineAction {

    public HideWait(){
    }


    @Override
    public boolean call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event, StateManager.AppStates leavingState, StateManager.AppStates enterState, EventParameters intentParameters) throws IOException, CoverageException {
        stateManager.hideWait();
        return false;
    }
}
