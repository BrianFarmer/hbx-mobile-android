package org.dchbx.coveragehq;

import org.dchbx.coveragehq.ridp.AcctCreate;
import org.dchbx.coveragehq.ridp.AcctDateOfBirth;
import org.dchbx.coveragehq.ridp.AcctSsn;
import org.dchbx.coveragehq.ridp.RidpQuestionsActivity;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

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
public class StateManager {
    Stack<Task> stateList;


    public static class ProcessResult {
        public Object postObject = null;
        public Task newTask = null;
        public boolean pop = false;
    }

    public StateManager(){
        stateList = new Stack<>();
        stateList.push(RidpTask.build(new UIBuilder()));
    }

    public void process(EventBus eventBus, int buttonId) {
        ProcessResult result = stateList.peek().process(this, buttonId);
        if (result != null){
            if (result.pop){
                stateList.pop();
            }
            if (result.newTask != null){
                stateList.push(result.newTask);
            }

            if (result.postObject != null){
                eventBus.post(result.postObject);
            }
        }
    }

    public void push(Task task){
        stateList.push(task);
    }

    public int getActivityId() {
        Task topTask = stateList.peek();
        return topTask.getUiActivityId();
    }

    abstract static class Process {
        private static int nextId = 1;
        private final int id;

        protected static int getNextId(){
            return nextId ++;
        }

        public Process(){
            id = getNextId();
        }

        abstract void doThis(BrokerActivity brokerActivity);

        public int getId() {
            return id;
        }
    }

    public static class UiActivity extends  Process {
        public static class Info {
            Class<?> cls;
            UiActivity uiActivity;
        }
        private static HashMap<Integer, Info> uiActivityMap = new HashMap<>();

        public static Info getUiActivityType(int id){
            return uiActivityMap.get(id);
        }

        public UiActivity(Class<?> cls) {
            Info info = new Info();
            info.cls = cls;
            info.uiActivity = this;
            uiActivityMap.put(getId(), info);
        }

        @Override
        void doThis(BrokerActivity brokerActivity) {
            Info info = uiActivityMap.get(getId());
            Intents.launchActivity(info.cls, brokerActivity);
        }
    }

    public class UiDialog extends  Process {
        @Override
        void doThis(BrokerActivity brokerActivity) {

        }
    }


}


abstract class Task {
    public abstract StateManager.ProcessResult process(StateManager stateManager, int buttonId);
    public abstract int getUiActivityId();
}

abstract class ActivityLauncher extends Task {
    abstract public int getUiActivityId();
}

class SimpleResponse extends ActivityLauncher{
    private final int id;
    private final StateManager.Process process;

    public SimpleResponse(int id, StateManager.Process process) {

        this.id = id;
        this.process = process;
    }

    public int getId() {
        return id;
    }

    @Override
    public int getUiActivityId() {
        return id;
    }

    @Override
    public StateManager.ProcessResult process(StateManager stateManager, int buttonId) {
        return null;
    }
}

class ParallelResponse extends Task{
    private final int id;
    private final List<Process> processes;

    public ParallelResponse(int id, List<Process> processes) {
        this.id = id;
        this.processes = processes;
    }

    @Override
    public StateManager.ProcessResult process(StateManager stateManager, int buttonId) {
        return null;
    }

    @Override
    public int getUiActivityId() {
        return 0;
    }
}

class StartPage extends ActivityLauncher {
    private final StateManager.Process process;

    public StartPage(StateManager.Process process){
        this.process = process;
    }

    @Override
    public int getUiActivityId() {
        return process.getId();
    }

    @Override
    public StateManager.ProcessResult process(StateManager stateManager, int buttonId) {
        return null;
    }
}

class Done extends Task {
    @Override
    public StateManager.ProcessResult process(StateManager stateManager, int buttonId) {
        return null;
    }

    @Override
    public int getUiActivityId() {
        return -1;
    }
}

class Wizard extends Task {
    public ArrayList<Task> tasks;
    public int currentPosition;

    @Override
    public StateManager.ProcessResult process(StateManager stateManager, int buttonId) {
        StateManager.ProcessResult processResult = tasks.get(currentPosition + 1).process(stateManager, buttonId);
        return processResult;
    }

    @Override
    public int getUiActivityId() {
        return tasks.get(currentPosition).getUiActivityId();
    }
}

class WizardPage extends SimpleResponse{
    private final Wizard wizard;
    private final int buttonId;
    private final int activityId;
    private final List<SimpleResponse> otherButtons;

    public WizardPage(Wizard wizard, int buttonId, int activityId, List<SimpleResponse> otherButtons) {
        super(activityId, StateManager.UiActivity.getUiActivityType(activityId).uiActivity);
        this.wizard = wizard;
        this.buttonId = buttonId;
        this.activityId = activityId;
        this.otherButtons = otherButtons;
    }

    @Override
    public StateManager.ProcessResult process(StateManager stateManager, int buttonId) {
        if (buttonId == this.buttonId){
            wizard.currentPosition ++;
            StateManager.ProcessResult processResult = new StateManager.ProcessResult();
            processResult.postObject = new Events.StateAction(activityId);
            return processResult;
        }

        if (otherButtons != null){
            for (SimpleResponse otherButton : otherButtons) {
                StateManager.ProcessResult processResult = otherButton.process(stateManager, buttonId);
                if (processResult != null){
                    return processResult;
                }
            }
        }
        return null;
    }

    public int getButtonId() {
        return buttonId;
    }
}

class UIBuilder {
    public StartPage startPoint(StateManager.Process process) {
        return new StartPage(process);
    }

    public SimpleResponse simpleResponse(int id, Class<?> cls) {
        return new SimpleResponse(id, new StateManager.UiActivity(cls));
    }

    public WizardPage wizardPage(Wizard wizard, int buttonId, StateManager.UiActivity uiActivity, List<SimpleResponse>  otherTasks) {
        return new WizardPage(wizard, buttonId, uiActivity.getId(), otherTasks);
    }

    public SimpleResponse simpleResponse(int id, StateManager.UiActivity uiActivity) {
        return new SimpleResponse(id, uiActivity);
    }

    public Done done() {
        return new Done();
    }
}

class RidpTask extends Wizard {
    private RidpTask(){
    }

    public static RidpTask build(UIBuilder uiBuilder) {
        RidpTask ridpTask = new RidpTask();
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(uiBuilder.startPoint(AcctCreate.uiActivity));
        tasks.add(uiBuilder.wizardPage(ridpTask, R.id.continueButton, AcctDateOfBirth.uiActivity, null));
        tasks.add(uiBuilder.wizardPage(ridpTask, R.id.continueButton, AcctSsn.uiActivity, null));
        tasks.add(uiBuilder.wizardPage(ridpTask, R.id.continueButton, RidpQuestionsActivity.uiActivity, null));
        tasks.add(uiBuilder.done());
        ridpTask.tasks = tasks;
        return ridpTask;
    }
}
