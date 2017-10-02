package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;

public class InitAndLaunchActivity implements StateMachineAction {
    private StateManager.UiActivity uiActivity;
    private final InitEventParameter init;

    public interface InitEventParameter {
        void init(EventParameters eventParameters);
    }

    public InitAndLaunchActivity(StateManager.UiActivity uiActivity, InitEventParameter init){
        this.uiActivity = uiActivity;
        this.init = init;
    }

    @Override
    public void call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event,
                     StateManager.AppStates leavingState, StateManager.AppStates enterState,
                     EventParameters eventParameters) throws IOException, CoverageException {
        init.init(eventParameters);
        stateMachine.push(new ActivityInfo(enterState, event, uiActivity));
        stateManager.launchActivity(uiActivity, eventParameters);
    }
}
