package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

public class LaunchDialog implements StateMachineAction{
    private final StateManager.UiDialog uiDialog;

    public LaunchDialog(StateManager.UiDialog uiDialog) {

        this.uiDialog = uiDialog;
    }

    @Override
    public void call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents e,
                     StateManager.AppStates leavingState, StateManager.AppStates enterState,
                     EventParameters eventParameters) throws IOException, CoverageException {
        stateMachine.push(new DialogInfo(enterState, e, null));
        stateManager.launchDialog(uiDialog, eventParameters);
    }
}
