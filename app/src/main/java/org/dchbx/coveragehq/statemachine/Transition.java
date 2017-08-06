package org.dchbx.coveragehq.statemachine;

public class Transition {
    private final StateManager.AppEvents event;
    private final StateManager.AppStates toState;
    private final StateMachineAction exitAction;
    private final StateMachineAction enterAction;

    public Transition(StateManager.AppEvents event, StateManager.AppStates toState, StateMachineAction exitAction, StateMachineAction enterAction){
        this.event = event;
        this.toState = toState;
        this.exitAction = exitAction;
        this.enterAction = enterAction;
    }

    public StateManager.AppEvents getEvent() {
        return event;
    }

    public StateManager.AppStates getToState() {
        return toState;
    }

    public StateMachineAction getExitAction() {
        return exitAction;
    }

    public StateMachineAction getEnterAction() {
        return enterAction;
    }
}
