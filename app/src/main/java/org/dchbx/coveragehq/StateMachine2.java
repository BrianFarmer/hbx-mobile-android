package org.dchbx.coveragehq;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
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

    private final StateManager stateManager;
    private HashMap<S, HashMap<E, Transition<E,S>>> statesMap;
    private HashMap<S, HashMap<E, EventProcessor<E>>> statesToEventProcessors;
    private Deque<StateInfoBase<E,S>> statesStack;

    public StateMachine2(StateManager stateManager){
        this.stateManager = stateManager;
        statesMap = new HashMap<>();
        statesStack = new ArrayDeque<StateInfoBase<E,S>>();
        statesToEventProcessors = new HashMap<>();
    }

    public void start(StateInfoBase<E,S> stateInfoBase){
        statesStack.push(stateInfoBase);
    }

    public Deque<StateInfoBase<E,S>> getStatesStack() {
        return statesStack;
    }

    public void process(E appEvent) throws IOException, CoverageException {
        StateInfoBase<E,S> curState = statesStack.peek();

        // This checks and processes events that have to modified by data in the system.
        HashMap<E, EventProcessor<E>> eventProcessorHashMap = statesToEventProcessors.get(curState.state);
        if (eventProcessorHashMap != null) {
            EventProcessor<E> eventProcessor = eventProcessorHashMap.get(appEvent);
            if (eventProcessor != null) {
                try {
                    E processedEvent = eventProcessor.process(appEvent);
                    HashMap<E, Transition<E, S>> transitionHashMap = statesMap.get(curState.state);
                    if (transitionHashMap.containsKey(processedEvent)) {
                        Transition<E, S> transition = transitionHashMap.get(processedEvent);
                        if (transition.exitAction != null) {
                            transition.exitAction.call(this, stateManager, appEvent, curState.state, transition.toState);
                        }
                        if (transition.enterAction != null) {
                            transition.enterAction.call(this, stateManager, appEvent, curState.state, transition.toState);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        // This checks and processes events that aren't modified.

        HashMap<E, Transition<E, S>> transitionHashMap = statesMap.get(curState.state);
        Transition<E, S> transition = transitionHashMap.get(appEvent);
        if (transition != null){
            if (transition.exitAction != null){
                transition.exitAction.call(this, stateManager, appEvent, curState.state, transition.toState);
            }
            if (transition.enterAction != null){
                transition.enterAction.call(this, stateManager, appEvent, curState.state, transition.toState);
            }
            return;
        }

        // Now check if there are global event handlers.
        transitionHashMap = statesMap.get(StateManager.AppStates.Any);
        if (transitionHashMap != null){
            transition = transitionHashMap.get(appEvent);
            if (transition != null){
                if (transition.exitAction != null){
                    transition.exitAction.call(this, stateManager, appEvent, curState.state, transition.toState);
                }
                if (transition.enterAction != null){
                    transition.enterAction.call(this, stateManager, appEvent, curState.state, transition.toState);
                }
            }
        }
    }

    public void back() {
        StateInfoBase<E, S> pop = statesStack.pop();
        pop.pop(this);
    }

    public void showWait(ActivityInfo<E,S> activityInfo) {
        statesStack.push(activityInfo);
        stateManager.showWait();
    }

    public void hideWait() {
        stateManager.hideWait();
    }

    public void launchActivity(ActivityInfo<E,S> activityInfo) {
        statesStack.push(activityInfo);
        stateManager.launchActivity(activityInfo.uiActivity);
    }

    private void launchDialog(DialogInfo<E,S> dialogInfo) {
        statesStack.push(dialogInfo);
        stateManager.launchDialog(dialogInfo.uiDialog);
    }

    public class Transition<E, S> {
        private final E event;
        private final S toState;
        private final StateMachineAction<E, S> exitAction;
        private final StateMachineAction<E, S> enterAction;

        public Transition(E event, S toState, StateMachineAction<E,S> exitAction, StateMachineAction<E,S> enterAction){
            this.event = event;
            this.toState = toState;
            this.exitAction = exitAction;
            this.enterAction = enterAction;
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

        public StateMachineAction<E, S> getEnterAction() {
            return enterAction;
        }
    }

    public FromState<E,S> from(S state){
        HashMap<E, Transition<E, S>> transitionsMap;
        HashMap<E, EventProcessor<E>> processorsMap;

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

    public class FromState<E, S>{
        private final S state;
        private final StateMachineAction<E,S> action;
        private final StateMachine2<E, S> stateMachine;
        private final HashMap<E, Transition<E, S>> transitionsMap;
        private final HashMap<E, EventProcessor<E>> processorsMap;

        public FromState(S state, StateMachineAction<E, S> action, StateMachine2<E, S> stateMachine, HashMap<E, Transition<E, S>> transitionsMap){
            this.state = state;
            this.action = action;
            this.stateMachine = stateMachine;
            this.transitionsMap = transitionsMap;
            processorsMap = null;
        }

        public FromState(S state, StateMachineAction<E, S> action, StateMachine2<E, S> stateMachine,
                         HashMap<E, Transition<E, S>> transitionsMap, HashMap<E, EventProcessor<E>> processorsMap){
            this.state = state;
            this.action = action;
            this.stateMachine = stateMachine;
            this.transitionsMap = transitionsMap;
            this.processorsMap = processorsMap;
        }

        public OnEvent on(E e){
            return new OnEvent(e, this, transitionsMap);
        }

        public OnUnprocessedEvent processEvent(E e, EventProcessor<E> eventProcessor){
            return new OnUnprocessedEvent(e, eventProcessor, this, transitionsMap, processorsMap);
        }

        public S getState() {
            return state;
        }

        public StateMachineAction<E, S> getAction() {
            return action;
        }

        public void restartApp() {
            stateManager.restartApp();
        }
    }

    public class OnUnprocessedEvent<E, S> {
        private final Object e;
        private final EventProcessor<E> eventProcessor;
        private final FromState<E,S> esFromState;
        private final HashMap<E, Transition<E, S>> transitionsMap;
        private final HashMap<E, EventProcessor<E>> processorsMap;

        public OnUnprocessedEvent(E e, EventProcessor<E> eventProcessor, FromState<E, S> esFromState,
                                  HashMap<E, Transition<E, S>> transitionsMap,
                                  HashMap<E, EventProcessor<E>> processorsMap) {

            this.e = e;
            this.eventProcessor = eventProcessor;
            this.esFromState = esFromState;
            this.transitionsMap = transitionsMap;
            this.processorsMap = processorsMap;

            this.processorsMap.put(e, eventProcessor);
        }

        public OnEvent<E,S> on(E e) {
            return new OnEvent<>(e, esFromState, transitionsMap);
        }
    }

    public class OnEvent<E, S>{

        private final E event;
        private final FromState<E, S> fromState;
        private final HashMap<E, Transition<E, S>> transitionsMap;

        public OnEvent(E event, FromState<E, S> fromState, HashMap<E, Transition<E, S>> transitionsMap) {
            this.event = event;
            this.fromState = fromState;
            this.transitionsMap = transitionsMap;
        }

        public FromState<E,S> to(S state, StateMachineAction<E,S> exitAction){
            transitionsMap.put(event, new Transition<E, S>(event, state, fromState.getAction(), exitAction));
            return fromState;
        }

        public FromState<E,S> doThis(StateMachineAction<E,S> exitAction){
            transitionsMap.put(event, new Transition<E, S>(event, null, fromState.getAction(), exitAction));
            return fromState;
        }

        public E getEvent() {
            return event;
        }

        public FromState<E, S> getFromState() {
            return fromState;
        }

        public OnEvent on(E e) {
            return null;
        }
    }

    private HashMap<S, HashMap<E, Transition<E,S>>> getStatesMap() {
        return statesMap;
    }


    public interface StateMachineAction<E,S>{
        void call(StateMachine2<E,S> stateMachine, StateManager stateManager, E e, S leavingState, S enterState) throws IOException, CoverageException;
    }

    public interface EventProcessor<E>{
        E process(E e) throws Exception;
    }

    public static class Back<E,S> implements StateMachineAction<E,S> {
        @Override
        public void call(StateMachine2<E, S> stateMachine, StateManager stateManager, E e, S leavingState, S enterState) throws IOException, CoverageException {
            stateMachine.back();
        }
    }

    public static class ShowWait<E,S> implements StateMachineAction<E,S> {
        private final StateManager.UiActivity uiActivity;

        public ShowWait(org.dchbx.coveragehq.StateManager.UiActivity uiActivity){
            this.uiActivity = uiActivity;
        }
        @Override
        public void call(StateMachine2<E, S> stateMachine, StateManager stateManager, E e, S leavingState, S enterState) throws IOException, CoverageException {
            stateMachine.showWait(new ActivityInfo<E,S>(enterState, uiActivity));
        }
    }

    public static class HideWait<E,S> implements StateMachineAction<E,S> {

        public HideWait(){
        }

        @Override
        public void call(StateMachine2<E, S> stateMachine, StateManager stateManager, E e, S leavingState, S enterState) throws IOException, CoverageException {
            stateMachine.hideWait();
        }
    }

    public static class LaunchDialog<E,S> implements StateMachineAction<E,S>{
        @Override
        public void call(StateMachine2<E, S> stateMachine, StateManager stateManager, E e, S leavingState, S enterState) throws IOException, CoverageException {
            stateMachine.launchActivity(new ActivityInfo<E,S>(enterState, null));
        }
    }

    public static class LaunchActivity<E,S> implements StateMachineAction<E,S>{
        private StateManager.UiActivity uiActivity;

        public LaunchActivity(org.dchbx.coveragehq.StateManager.UiActivity uiActivity){
            this.uiActivity = uiActivity;
        }

        @Override
        public void call(StateMachine2<E,S> stateMachine, StateManager stateManager, E e, S leavingState, S enterState) throws IOException, CoverageException {
            stateMachine.launchActivity(new ActivityInfo<E,S>(enterState, uiActivity));
        }
    }

    public static class PopAndLaunchActivity<E,S> implements StateMachineAction<E,S>{
        private StateManager.UiActivity uiActivity;

        public PopAndLaunchActivity(org.dchbx.coveragehq.StateManager.UiActivity uiActivity){
            this.uiActivity = uiActivity;
        }

        @Override
        public void call(StateMachine2<E,S> stateMachine, StateManager stateManager, E e, S leavingState, S enterState) throws IOException, CoverageException {
            stateMachine.back();
            stateMachine.launchActivity(new ActivityInfo<E,S>(enterState, uiActivity));
        }
    }


    public static abstract class StateInfoBase<E,S>{
        private final S state;

        public StateInfoBase(S state){
            this.state = state;
        }

        public void pop(StateMachine2<E,S> stateMachine){
            stateMachine.stateManager.back();
        }
    }

    public static class ActivityInfo<E, S> extends StateMachine2.StateInfoBase<E,S> {

        private final StateManager.UiActivity uiActivity;

        public ActivityInfo(S state, StateManager.UiActivity uiActivity) {
            super(state);
            this.uiActivity = uiActivity;
        }

        public StateManager.UiActivity getUiActivity(){
            return uiActivity;
        }
    }

    public static class WaitActivityInfo<E,S> extends StateMachine2.ActivityInfo<E,S> {

        private final StateManager.UiActivity uiActivity;

        public WaitActivityInfo(S state, StateManager.UiActivity uiActivity) {
            super(state, uiActivity);
            this.uiActivity = uiActivity;
        }

        public StateManager.UiActivity getUiActivity(){
            return uiActivity;
        }

        @Override
        public void pop(StateMachine2<E,S> stateMachine){
            stateMachine.hideWait();
        }
    }

    public class DialogInfo<E,S> extends StateMachine2.StateInfoBase<E,S> {

        private final StateManager.UiDialog uiDialog;
        private final ActivityInfo activityInfo;

        public DialogInfo(S state, StateManager.UiDialog uiDialog, ActivityInfo activityInfo) {
            super(state);
            this.uiDialog = uiDialog;
            this.activityInfo = activityInfo;
        }
    }
}




