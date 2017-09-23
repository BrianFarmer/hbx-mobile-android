package org.dchbx.coveragehq.statemachine;

import android.util.Log;

import org.dchbx.coveragehq.CoverageException;

import java.io.IOException;
import java.util.ArrayDeque;
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

public class StateMachine {

    private static final String TAG = "StateMachine";

    private final StateManager stateManager;
    private HashMap<StateManager.AppStates, HashMap<StateManager.AppEvents, Transition>> statesMap;
    private HashMap<StateManager.AppStates, HashMap<StateManager.AppEvents, EventProcessor>> statesToEventProcessors;
    private ArrayDeque<StateInfoBase> statesStack;

    public StateMachine(StateManager stateManager){
        this.stateManager = stateManager;
        statesMap = new HashMap<>();
        statesStack = new ArrayDeque<StateInfoBase>();
        statesToEventProcessors = new HashMap<>();
    }

    public void start(ArrayDeque<StateInfoBase> stack){
        Log.d(TAG, "states: " + stack.size());
        statesStack = stack;
    }

    public void start(StateInfoBase stateInfoBase){
        statesStack.push(stateInfoBase);
    }

    public ArrayDeque<StateInfoBase> getStatesStack() {
        Log.d(TAG, "states: " + statesStack.size());
        return statesStack;
    }

    public void process(StateManager.AppEvents appEvent, EventParameters eventParameters) throws IOException, CoverageException {
        StateInfoBase curState = statesStack.peek();
        if (eventParameters == null){
            eventParameters = new EventParameters();
        }
        eventParameters.add("OldState", curState.getState());

        if (curState.getState() == StateManager.AppStates.AcctAuthConsentFe
            && appEvent == StateManager.AppEvents.ConsentGiven){
            Log.d(TAG, "the button you asked for has been clicked.");
        }

        // This checks and processes events that have to modified by data in the system.
        HashMap<StateManager.AppEvents, EventProcessor> eventProcessorHashMap = statesToEventProcessors.get(curState.getState());
        if (eventProcessorHashMap != null) {
            EventProcessor eventProcessor = eventProcessorHashMap.get(appEvent);
            if (eventProcessor != null) {
                try {
                    StateManager.AppEvents processedEvent = eventProcessor.process(appEvent);
                    HashMap<StateManager.AppEvents, Transition> transitionHashMap = statesMap.get(curState.getState());
                    if (transitionHashMap.containsKey(processedEvent)) {
                        Transition transition = transitionHashMap.get(processedEvent);
                        eventParameters.add("NewState", transition.getToState());
                        StateMachineAction exitAction = transition.getExitAction();
                        if (exitAction != null) {
                            exitAction.call(this, stateManager, appEvent, curState.getState(), transition.getToState(), eventParameters);
                        }
                        StateMachineAction enterAction = transition.getEnterAction();
                        if (enterAction != null) {
                            enterAction.call(this, stateManager, appEvent, curState.getState(), transition.getToState(), eventParameters);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        // This checks and processes events that aren't modified.

        HashMap<StateManager.AppEvents, Transition> transitionHashMap = statesMap.get(curState.getState());
        if (transitionHashMap != null) {
            Transition transition = transitionHashMap.get(appEvent);
            if (transition != null) {
                eventParameters.add("NewState", transition.getToState());
                StateMachineAction exitAction = transition.getExitAction();
                if (exitAction != null) {
                    exitAction.call(this, stateManager, appEvent, curState.getState(), transition.getToState(), eventParameters);
                }
                StateMachineAction enterAction = transition.getEnterAction();
                if (enterAction != null) {
                    enterAction.call(this, stateManager, appEvent, curState.getState(), transition.getToState(), eventParameters);
                }
                return;
            }
        }

        // Now check if there are global event handlers.
        transitionHashMap = statesMap.get(StateManager.AppStates.Any);
        if (transitionHashMap != null){
            Transition transition = transitionHashMap.get(appEvent);
            if (eventParameters == null || transition == null){
                Log.d(TAG, "eek!");
            }
            eventParameters.add("NewState", transition.getToState());
            if (transition != null){
                StateMachineAction exitAction = transition.getExitAction();
                if (exitAction != null){
                    exitAction.call(this, stateManager, appEvent, curState.getState(), transition.getToState(), eventParameters);
                }
                StateMachineAction enterAction = transition.getEnterAction();
                if (enterAction != null){
                    enterAction.call(this, stateManager, appEvent, curState.getState(), transition.getToState(), eventParameters);
                }
            }
        }
    }

    //private void launchDialog(DialogInfo<E,S> dialogInfo) {
    //    statesStack.push(dialogInfo);
    //    stateManager.launchDialog(dialogInfo.uiDialog);
    //}

    public StateManager getStateManager() {
        return stateManager;
    }

    public FromState from(StateManager.AppStates state){
        HashMap<StateManager.AppEvents, Transition> transitionsMap;
        HashMap<StateManager.AppEvents, EventProcessor> processorsMap;

        if (statesMap.containsKey(state)){
            transitionsMap = statesMap.get(state);
        } else {
            transitionsMap = new HashMap<>();
            statesMap.put(state, transitionsMap);
        }
        if (statesToEventProcessors.containsKey(state)){
            processorsMap = statesToEventProcessors.get(state);
        } else {
            processorsMap = new HashMap<>();
            statesToEventProcessors.put(state, processorsMap);
        }

        return new FromState(state, null, this, transitionsMap, processorsMap);
    }

    public class FromState{
        private final StateManager.AppStates state;
        private final StateMachineAction action;
        private final StateMachine stateMachine;
        private final HashMap<StateManager.AppEvents, Transition> transitionsMap;
        private final HashMap<StateManager.AppEvents, EventProcessor> processorsMap;

        public FromState(StateManager.AppStates state, StateMachineAction action, StateMachine stateMachine, HashMap<StateManager.AppEvents, Transition> transitionsMap){
            this.state = state;
            this.action = action;
            this.stateMachine = stateMachine;
            this.transitionsMap = transitionsMap;
            processorsMap = null;
        }

        public FromState(StateManager.AppStates state, StateMachineAction action, StateMachine stateMachine,
                         HashMap<StateManager.AppEvents, Transition> transitionsMap, HashMap<StateManager.AppEvents, EventProcessor> processorsMap){
            this.state = state;
            this.action = action;
            this.stateMachine = stateMachine;
            this.transitionsMap = transitionsMap;
            this.processorsMap = processorsMap;
        }

        public OnEvent on(StateManager.AppEvents e){
            return new OnEvent(e, this, transitionsMap);
        }

        public OnUnprocessedEvent processEvent(StateManager.AppEvents e, EventProcessor eventProcessor){
            return new OnUnprocessedEvent(e, eventProcessor, this, transitionsMap, processorsMap);
        }

        public StateManager.AppStates getState() {
            return state;
        }

        public StateMachineAction getAction() {
            return action;
        }

        public void restartApp() {
            stateManager.restartApp();
        }
    }

    public class OnUnprocessedEvent {
        private final Object e;
        private final EventProcessor eventProcessor;
        private final FromState esFromState;
        private final HashMap<StateManager.AppEvents, Transition> transitionsMap;
        private final HashMap<StateManager.AppEvents, EventProcessor> processorsMap;

        public OnUnprocessedEvent(StateManager.AppEvents e, EventProcessor eventProcessor, FromState esFromState,
                                  HashMap<StateManager.AppEvents, Transition> transitionsMap,
                                  HashMap<StateManager.AppEvents, EventProcessor> processorsMap) {

            this.e = e;
            this.eventProcessor = eventProcessor;
            this.esFromState = esFromState;
            this.transitionsMap = transitionsMap;
            this.processorsMap = processorsMap;

            this.processorsMap.put(e, eventProcessor);
        }

        public OnEvent on(StateManager.AppEvents e) {
            return new OnEvent(e, esFromState, transitionsMap);
        }
    }

    public class OnEvent {

        private final StateManager.AppEvents event;
        private final FromState fromState;
        private final HashMap<StateManager.AppEvents, Transition> transitionsMap;

        public OnEvent(StateManager.AppEvents event, FromState fromState, HashMap<StateManager.AppEvents, Transition> transitionsMap) {
            this.event = event;
            this.fromState = fromState;
            this.transitionsMap = transitionsMap;
        }

        public FromState to(StateManager.AppStates state, StateMachineAction exitAction){
            transitionsMap.put(event, new Transition(event, state, fromState.getAction(), exitAction));
            return fromState;
        }

        public FromState doThis(StateMachineAction exitAction){
            transitionsMap.put(event, new Transition(event, null, fromState.getAction(), exitAction));
            return fromState;
        }

        public StateManager.AppEvents getEvent() {
            return event;
        }

        public FromState getFromState() {
            return fromState;
        }

        public OnEvent on(StateManager.AppEvents e) {
            return null;
        }
    }

    private HashMap<StateManager.AppStates, HashMap<StateManager.AppEvents, Transition>> getStatesMap() {
        return statesMap;
    }

    public void push(StateInfoBase info) {
        statesStack.push(info);
    }
}
