package org.dchbx.coveragehq;

import java.util.ArrayList;
import java.util.HashMap;

/*
    This file is part of DC.

    DC Health Link SmallBiz is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.  If not, see <http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file, close to the copyright notices. When using the Lesser GPL, insert the word “Lesser” before “General” in all three places. When using the GNU AGPL, insert the word “Affero” before “General” in all three places.
*/
public class StateMachine2<E,S> {

    private HashMap<S, HashMap<E, Transition<E,S>>> statesMap;
    private ArrayList<S> statesStack;

    public StateMachine2(){
        statesMap = new HashMap<>();
    }

    public void start(S s){
        statesStack.add(s);
    }

    public class Transition<E, S> {
        private final E event;
        private final S toState;
        private final StateMachineAction<E, S> exitAction;
        private final StateMachineAction<E, S> enterAtion;

        public Transition(E event, S toState, StateMachineAction<E,S> exitAction, StateMachineAction<E,S> enterAtion){
            this.event = event;
            this.toState = toState;
            this.exitAction = exitAction;
            this.enterAtion = enterAtion;
        }

        public E getEvent() {
            return event;
        }

        public S getToState() {
            return toState;
        }

        public StateMachineAction<E, S> getExitAction() {
            return exitAction;
        }

        public StateMachineAction<E, S> getEnterAtion() {
            return enterAtion;
        }
    }

    public FromState<E,S> from(S state, StateMachineAction<E,S> exitAction){
        return new FromState(state, exitAction, this);
    }

    public class FromState<E, S>{
        private final S state;
        private final StateMachineAction<E,S> action;
        private final StateMachine2<E, S> stateMachine;

        public FromState(S state, StateMachineAction<E, S> action, StateMachine2<E,S> stateMachine){
            this.state = state;
            this.action = action;
            this.stateMachine = stateMachine;
        }

        public OnEvent onEvent(E e){
            return new OnEvent(e, this);
        }

        public S getState() {
            return state;
        }

        public StateMachineAction<E, S> getAction() {
            return action;
        }

        public StateMachine2<E, S> getStateMachine() {
            return stateMachine;
        }
    }

    public class OnEvent<E, S>{

        private final E event;
        private final FromState<E, S> fromState;

        public OnEvent(E event, FromState<E, S> fromState){
            this.event = event;
            this.fromState = fromState;
        }

        public ToState<E, S> to(S state, StateMachineAction<E,S> exitAction){
            return new ToState(this, state, exitAction);
        }

        public E getEvent() {
            return event;
        }

        public FromState<E, S> getFromState() {
            return fromState;
        }
    }

    public class ToState<E, S>{

        private final OnEvent<E,S> onEvent;
        private final S state;
        private final StateMachineAction<E, S> enterAction;

        public ToState(OnEvent<E,S> onEvent, S state, StateMachineAction<E,S> enterAction) {

            this.onEvent = onEvent;
            this.state = state;
            this.enterAction = enterAction;

            FromState<E, S> fromState = onEvent.getFromState();
            HashMap<S, HashMap<E, StateMachine2<E,S>.Transition<E, S>>> statesMap = fromState.getStateMachine().getStatesMap();

            if (!statesMap.containsKey(onEvent.fromState.getState())){
                statesMap.put(onEvent.fromState.getState(), new HashMap<E, StateMachine2<E,S>.Transition<E,S>>());
            }
            HashMap<E, StateMachine2<E,S>.Transition<E,S>> transitionsMap = statesMap.get(onEvent.fromState.getState());
            StateMachine2<E,S>.Transition<E, S> transition = new StateMachine2<E,S>.Transition<E,S>(onEvent.getEvent(), state, onEvent.getFromState().getAction(), enterAction);
            transitionsMap.put(onEvent.getEvent(), transition);
        }
    }

    private HashMap<S, HashMap<E, Transition<E,S>>> getStatesMap() {
        return statesMap;
    }


    public interface StateMachineAction<E,S>{
        void call(E e, S leavingState, S enterState);
    }
}




