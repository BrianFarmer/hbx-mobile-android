package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

public class LaunchActivity extends StateMachineAction {
    private StateManager.UiActivity uiActivity;

    public LaunchActivity(StateManager.UiActivity uiActivity){
        this.uiActivity = uiActivity;
    }

    @Override
    public boolean call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event,
                        StateManager.AppStates leavingState, StateManager.AppStates enterState,
                        EventParameters eventParameters) throws IOException, CoverageException {
        stateMachine.push(new ActivityInfo(enterState, event, uiActivity));
        stateManager.launchActivity(uiActivity, eventParameters, this);
        return false;
    }
}
