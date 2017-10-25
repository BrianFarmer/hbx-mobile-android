package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;
import org.dchbx.coveragehq.Events;

import java.io.IOException;

public class PopAndErrorMessage extends StateMachineAction{

    @Override
    public boolean call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event,
                        StateManager.AppStates leavingState, StateManager.AppStates enterState,
                        EventParameters intentParameters) throws IOException, CoverageException {
        stateMachine.getStatesStack().pop();
        stateManager.hideWait();
        stateManager.getMessages().stateAction(Events.StateAction.Action.PopAndServerErrorMessage, intentParameters);
        return true;
    }

    public boolean autoPopOnTransitionAway(){
        return true;
    }
}
