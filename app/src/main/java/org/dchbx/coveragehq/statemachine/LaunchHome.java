package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

public class LaunchHome extends StateMachineAction {

    public LaunchHome(){
    }

    @Override
    public boolean call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event,
                        StateManager.AppStates leavingState, StateManager.AppStates enterState,
                        EventParameters eventParameters) throws IOException, CoverageException {
        stateManager.launchHome(eventParameters, this);
        return false;
    }
}
