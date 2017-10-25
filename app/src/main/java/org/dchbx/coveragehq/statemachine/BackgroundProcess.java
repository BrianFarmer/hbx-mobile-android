package org.dchbx.coveragehq.statemachine;

import org.dchbx.coveragehq.CoverageException;
import org.dchbx.coveragehq.Events;

import java.io.IOException;

public class BackgroundProcess extends StateMachineAction {
    private final Class c;

    public BackgroundProcess(Class c){
        this.c = c;
    }

    @Override
    public boolean call(StateMachine stateMachine, StateManager stateManager, StateManager.AppEvents event,
                        StateManager.AppStates leavingState, StateManager.AppStates enterState,
                        EventParameters intentParameters) throws IOException, CoverageException {
        stateMachine.push(new WaitActivityInfo<StateManager.AppEvents, StateManager.AppStates>(enterState, event, null, this));
        stateManager.showWait();
        try {
            Object o = c.newInstance();
            Events.BackgroundProcess eBP = (Events.BackgroundProcess) o;
            eBP.setEventParameters(intentParameters);
            stateManager.getMessages().sendBackgroundProcess(eBP);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean autoPopOnTransitionAway(){
        return true;
    }
}
