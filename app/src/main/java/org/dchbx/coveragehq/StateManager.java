package org.dchbx.coveragehq;

import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.models.ridp.VerifiyIdentityResponse;
import org.dchbx.coveragehq.ridp.AcctAddress;
import org.dchbx.coveragehq.ridp.AcctAuthConsent;
import org.dchbx.coveragehq.ridp.AcctCreate;
import org.dchbx.coveragehq.ridp.AcctDateOfBirth;
import org.dchbx.coveragehq.ridp.AcctPreAuthActivity;
import org.dchbx.coveragehq.ridp.AcctSsn;
import org.dchbx.coveragehq.ridp.AcctSsnWithEmployer;
import org.dchbx.coveragehq.ridp.AcctSystemFoundYou;
import org.dchbx.coveragehq.ridp.AcctSystemFoundYouAceds;
import org.dchbx.coveragehq.ridp.RidpQuestionsActivity;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static org.dchbx.coveragehq.StateManager.ProcessResult.Action.None;
import static org.dchbx.coveragehq.StateManager.ProcessResult.Action.Pop;
import static org.dchbx.coveragehq.StateManager.ProcessResult.Action.Push;

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
        public enum Action {
            None,
            Pop,
            Push
        }

        public ProcessResult(Object postObject, Task task, Action action){
            this.postObject = postObject;
            this.task = task;
            this.action = action;
        }

        public Object postObject = null;
        public Task task = null;
        public Action action = None;
    }

    public StateManager(){
        stateList = new Stack<>();
        stateList.push(SignUpTask.build(new UIBuilder()));
        //stateList.push(RidpTask.build(new UIBuilder()));
    }

    public void process(EventBus eventBus, int screenId, int buttonId, Object o) {
        switch (screenId){
            case R.layout.acct_create:
            case R.layout.acct_address:
            case R.layout.acct_ssn:
            case R.layout.acct_date_of_birth:
            case R.layout.acct_auth_consent:
                ConfigurationStorageHandler storageHandler = ServiceManager.getServiceManager().getConfigurationStorageHandler();
                storageHandler.store((Account)o);
                break;

        }
        Task task = stateList.peek();
        task.doWorkStart(o, this, buttonId);
        ProcessResult result = task.process(this, task.getService(), buttonId);
        if (result != null){
            switch (result.action){
                case Pop:
                    stateList.pop();
                    if (result.task != null){
                        stateList.push(result.task);
                    }
                    break;
                case Push:
                    stateList.push(result.task);
                    break;
                case None:
                    break;
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

class StateProcessor{
}


abstract class Task<T extends StateProcessor, T2> {
    private final String name;
    protected T service;

    public Task(String name, T service){
        this.name = name;
        this.service = service;
    }

    public abstract StateManager.ProcessResult process(StateManager stateManager, T service , int buttonId);
    public abstract int getUiActivityId();
    public T getService(){
        return service;
    }

    public void doWorkStart(Object o, StateManager stateManager, int buttonId){
        doWork((T2)o, stateManager, buttonId);
    }

    public abstract void doWork(T2 t2, StateManager stateManager, int buttonId);
}

abstract class ActivityLauncher<T extends StateProcessor, T2> extends Task<T, T2> {

    private final StateManager.UiActivity uiActivity;

    public ActivityLauncher(String name, StateManager.UiActivity uiActivity, T t) {
        super(name, t);
        this.uiActivity = uiActivity;
    }

    abstract public int getUiActivityId();
}

class SimpleResponse<T extends StateProcessor, T2> extends ActivityLauncher<T, T2>{
    private final int id;
    private final ActionProcessor<T> serviceProcess;

    public SimpleResponse(String name, int id, StateManager.UiActivity uiActivity, ActionProcessor<T> serviceProcess, T service) {
        super(name, uiActivity, service);
        this.id = id;
        this.serviceProcess = serviceProcess;
    }

    public int getId() {
        return id;
    }

    @Override
    public int getUiActivityId() {
        return id;
    }

    public StateManager.ProcessResult process(StateManager stateManager, T service , int buttonId) {
        return null;
    }

    public void doWork(T2 t2, StateManager stateManager, int buttonId){
        if (serviceProcess != null) {
            serviceProcess.process(stateManager, service, buttonId);
        }
    }
}


class StartPage<T extends StateProcessor, T2> extends ActivityLauncher<T, T2> {
    private final Wizard wizard;
    private final int activityId;

    public StartPage(String name, Wizard wizard, StateManager.UiActivity uiActivity, T service){
        super(name, uiActivity, service);
        this.wizard = wizard;
        this.activityId = uiActivity.getId();
    }

    @Override
    public StateManager.ProcessResult process(StateManager stateManager, StateProcessor service, int buttonId) {
        return new StateManager.ProcessResult(new Events.StateAction(activityId),
                wizard, Push);
    }

    @Override
    public int getUiActivityId() {
        return activityId;
    }

    @Override
    public void doWork(T2 t2, StateManager stateManager, int buttonId) {

    }
}

class Done<T extends StateProcessor, T2> extends Task<T, T2> {
    public Done(String name, T service){
        super(name, service);
    }

    @Override
    public StateManager.ProcessResult process(StateManager stateManager, T service, int buttonId) {
        return new StateManager.ProcessResult(null, null, Pop);
    }

    @Override
    public int getUiActivityId() {
        return -1;
    }

    @Override
    public void doWork(T2 t2, StateManager stateManager, int buttonId) {

    }
}

class Wizard<T extends StateProcessor, T2> extends Task<T, T2> {
    public ArrayList<Task> tasks;
    public int currentPosition;

    public Wizard(String name, T service){
        super(name, service);

        currentPosition = -1;
    }

    @Override
    public StateManager.ProcessResult process(StateManager stateManager, T service, int buttonId) {
        if (currentPosition == -1){
            currentPosition = 0;
            return tasks.get(0).process(stateManager, service, buttonId);
        } else {
            return tasks.get(currentPosition + 1).process(stateManager, service, buttonId);
        }
    }

    @Override
    public int getUiActivityId() {
        return tasks.get(currentPosition).getUiActivityId();
    }

    @Override
    public void doWork(T2 t2, StateManager stateManager,int buttonId) {
        if (currentPosition == -1) {
            return;
        }
        tasks.get(currentPosition).doWork(t2, stateManager, buttonId);
    }
}

interface ActionProcessor<T extends StateProcessor> {
    void process(StateManager stateManager, T service, int buttonId);
}

class WizardPage<T extends StateProcessor, T2> extends SimpleResponse<T, T2>{
    private T service;
    private final Wizard wizard;
    private final int buttonId;
    private final int activityId;
    private final List<SimpleResponse> otherButtons;

    public WizardPage(String name, Wizard wizard, int buttonId, StateManager.UiActivity uiActivity, List<SimpleResponse> otherButtons, ActionProcessor<T> serviceProcess, T service) {
        super(name, buttonId, uiActivity, serviceProcess, service);
        this.wizard = wizard;
        this.buttonId = buttonId;
        this.activityId = uiActivity.getId();
        this.otherButtons = otherButtons;
    }

    @Override
    public StateManager.ProcessResult process(StateManager stateManager, T service , int buttonId) {
        if (buttonId == this.buttonId){
            wizard.currentPosition ++;
            StateManager.ProcessResult processResult = new StateManager.ProcessResult(new Events.StateAction(activityId),
                                                                                        null, None);
            return processResult;
        }

        if (otherButtons != null){
            for (SimpleResponse otherButton : otherButtons) {
                StateManager.ProcessResult processResult = otherButton.process(stateManager, service, buttonId);
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

    public Wizard getWizard() {
        return wizard;
    }

    public int getActivityId(){
        return activityId;
    }
}

class UIBuilder {
    public <T extends StateProcessor, T2> StartPage startPoint(String name, Wizard wizard, StateManager.UiActivity uiActivity, T service) {
        return new StartPage(name, wizard, uiActivity, service);
    }

    public <T extends StateProcessor, T2> SimpleResponse simpleResponse(String name, int id, Class<?> cls, T service) {
        return new SimpleResponse(name, id, new StateManager.UiActivity(cls), null, service);
    }

    //    public WizardPage(Wizard wizard, int buttonId, int activityId, List<SimpleResponse> otherButtons, ActionProcessor<T> serviceProcess, T service) {

    public <T extends StateProcessor, T2> WizardPage wizardPage(String name, Wizard wizard, int buttonId, StateManager.UiActivity uiActivity, List<SimpleResponse>  otherTasks, ActionProcessor<T> actionProcessor, T service) {
        return new WizardPage(name, wizard, buttonId, uiActivity, otherTasks, actionProcessor, service);
    }

    public <T extends StateProcessor, T2> SimpleResponse simpleResponse(String name, int id, StateManager.UiActivity uiActivity, ActionProcessor<T> serviceProcess, T service) {
        return new SimpleResponse(name, id, uiActivity, serviceProcess, service);
    }

    public <T extends StateProcessor, T2> Done done(String name, T service) {
        return new Done(name, service);
    }
}

class RidpTask extends Wizard<RidpService, Account> {
    private RidpTask(){
        super("ridp wizard", RidpService.getService());
    }

    public static RidpTask build(UIBuilder uiBuilder) {
        RidpTask ridpTask = new RidpTask();
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(uiBuilder.startPoint("create account", ridpTask, AcctCreate.uiActivity, ServiceManager.getServiceManager().getRidpService()));
        tasks.add(uiBuilder.wizardPage("pre auth", ridpTask, R.id.continueButton, AcctPreAuthActivity.uiActivity, null, null, ServiceManager.getServiceManager().getRidpService()));
        tasks.add(uiBuilder.wizardPage("address", ridpTask, R.id.continueButton, AcctAddress.uiActivity, null, null, ServiceManager.getServiceManager().getRidpService()));
        tasks.add(uiBuilder.wizardPage("date of birth", ridpTask, R.id.continueButton, AcctDateOfBirth.uiActivity, null, null, ServiceManager.getServiceManager().getRidpService()));
        tasks.add(uiBuilder.wizardPage("ssn", ridpTask, R.id.continueButton, AcctSsn.uiActivity, null, null, ServiceManager.getServiceManager().getRidpService()));
        tasks.add(uiBuilder.wizardPage("auth consent", ridpTask, R.id.continueButton, AcctAuthConsent.uiActivity, null, new ActionProcessor<RidpService>() {
            @Override
            public void process(StateManager stateManager, RidpService service, int buttonId) {
                service.verificationRequest(stateManager, service, buttonId);
            }
        }, ServiceManager.getServiceManager().getRidpService()));
        tasks.add(uiBuilder.wizardPage("questions", ridpTask, R.id.continueButton, RidpQuestionsActivity.uiActivity, null, new ActionProcessor<RidpService>() {
            @Override
            public void process(StateManager stateManager, RidpService service, int buttonId) {
                service.sendAnswers(stateManager, service, buttonId);
            }
        }, ServiceManager.getServiceManager().getRidpService()));


        ArrayList<SwitchPath<RidpService, Account, WizardPage<RidpService, Account>, SubmitAnswers.PossibleResults>> switchPaths = new ArrayList<>();
        switchPaths.add(new SwitchPath<RidpService, Account, WizardPage<RidpService, Account>, SubmitAnswers.PossibleResults>(SubmitAnswers.PossibleResults.InEnroll,
                uiBuilder.wizardPage("in enroll", ridpTask, R.id.continueButton, AcctSystemFoundYou.uiActivity, null, null, ServiceManager.getServiceManager().getRidpService())));
        switchPaths.add(new SwitchPath<RidpService, Account, WizardPage<RidpService, Account>, SubmitAnswers.PossibleResults>(SubmitAnswers.PossibleResults.InRoster,
                uiBuilder.wizardPage("in enroll", ridpTask, R.id.continueButton, AcctSsnWithEmployer.uiActivity, null, null, ServiceManager.getServiceManager().getRidpService())));
        switchPaths.add(new SwitchPath<RidpService, Account, WizardPage<RidpService, Account>, SubmitAnswers.PossibleResults>(SubmitAnswers.PossibleResults.InACEDS,
                uiBuilder.wizardPage("in enroll", ridpTask, R.id.continueButton, AcctSystemFoundYouAceds.uiActivity, null, null, ServiceManager.getServiceManager().getRidpService())));
        switchPaths.add(new SwitchPath<RidpService, Account, WizardPage<RidpService, Account>, SubmitAnswers.PossibleResults>(SubmitAnswers.PossibleResults.Ok,
                uiBuilder.wizardPage("in enroll", ridpTask, R.id.continueButton, AcctSystemFoundYou.uiActivity, null, null, ServiceManager.getServiceManager().getRidpService())));
        switchPaths.add(new SwitchPath<RidpService, Account, WizardPage<RidpService, Account>, SubmitAnswers.PossibleResults>(SubmitAnswers.PossibleResults.VerificationFailed,
                uiBuilder.wizardPage("in enroll", ridpTask, R.id.continueButton, AcctSystemFoundYou.uiActivity, null, null, ServiceManager.getServiceManager().getRidpService())));

        tasks.add(new SubmitAnswers("results switch", ridpTask, R.id.continueButton, AcctSystemFoundYou.uiActivity, null, null, ServiceManager.getServiceManager().getRidpService(), switchPaths));

        tasks.add(uiBuilder.done("done", ServiceManager.getServiceManager().getRidpService()));
        ridpTask.tasks = tasks;
        return ridpTask;
    }
}

class SwitcherTask<T extends StateProcessor, T2> extends Task<T, T2> {
    public HashMap<Integer, Task> tasks;

    public SwitcherTask(String name, T service){
        super(name, service);
    }

    @Override
    public StateManager.ProcessResult process(StateManager stateManager, T service, int buttonId) {
        if (tasks.containsKey(buttonId)){
            Task task = tasks.get(buttonId);
            return task.process(stateManager, service, buttonId);
        }
        return null;
    }

    @Override
    public int getUiActivityId() {
        return PlanShoppingChoicesActivity.uiActivity.getId();
    }

    @Override
    public void doWork(T2 t2, StateManager stateManager, int buttonId) {

    }
}

class SignUpTask extends SwitcherTask<SignUpService, Account> {
    private SignUpTask(SignUpService service) {
        super("SignUpTask", service);
    }

    public static SwitcherTask build(UIBuilder uiBuilder){
        SignUpTask signUpTask = new SignUpTask(ServiceManager.getServiceManager().getSignUpService());
        HashMap<Integer, Task> tasks = new HashMap<>();
        tasks.put(R.id.startShopping, RidpTask.build(uiBuilder));
        tasks.put(R.id.checkFinancialAssistance, RidpTask.build(uiBuilder));
        signUpTask.tasks = tasks;
        return signUpTask;
    }
}


class SwitchPath <T extends StateProcessor, D, TT extends Task<T, D>, E>  {
    private final E e;
    private final TT task;

    public SwitchPath(E e, TT task){
        this.e=e;
        this.task=task;
    }

    public TT getTask(){
        return task;
    }

    public E getState(){
        return e;
    }
}

interface GetSwitchState<E> {
    public E getState();
}

class SwitchedPaths<T extends StateProcessor, D, TT extends Task<T,D>, E> {

    private final HashMap<E, SwitchPath<T, D, TT, E>> pathsMap;
    private final GetSwitchState<E> getSwitchState;

    public SwitchedPaths(ArrayList<SwitchPath<T, D, TT, E>> paths, GetSwitchState<E> getSwitchState){

        pathsMap = new HashMap<>();
        for (SwitchPath<T, D, TT, E> path : paths) {
            pathsMap.put(path.getState(), path);
        }

        this.getSwitchState = getSwitchState;
    }

    public TT getTask(){
        return pathsMap.get(getSwitchState.getState()).getTask();
    }

}

abstract class SwitchedWizardPage<T extends StateProcessor, D, E> extends WizardPage<T, D>{

    private SwitchedPaths<T, D, WizardPage<T, D>, E> switchedPaths;

    public SwitchedWizardPage(String name, Wizard wizard, int buttonId, StateManager.UiActivity uiActivity,
                              List<SimpleResponse> otherButtons, ActionProcessor<T> serviceProcess, T service,
                              ArrayList<SwitchPath<T, D, WizardPage<T, D>, E>> switchPaths ) {
        super(name, wizard, buttonId, uiActivity, otherButtons, serviceProcess, service);
        final T finalService = service;

        switchedPaths = new SwitchedPaths<>(switchPaths, new GetSwitchState<E>() {
            @Override
            public E getState() {
                return SwitchedWizardPage.this.getState();
            }
        });
    }


    @Override
    public StateManager.ProcessResult process(StateManager stateManager, T service, int buttonId) {
        WizardPage<T, D> task = switchedPaths.getTask();
        return task.process(stateManager, service, buttonId);
    }


    @Override
    public int getUiActivityId() {
        WizardPage<T, D> task = switchedPaths.getTask();
        return task.getUiActivityId();
    }

    protected abstract E getState();
}

class SubmitAnswers extends SwitchedWizardPage<RidpService, Account, SubmitAnswers.PossibleResults> {

    public SubmitAnswers(String name, Wizard wizard, int buttonId, StateManager.UiActivity uiActivity,
                         List otherButtons, ActionProcessor serviceProcess, RidpService service,
                         ArrayList<SwitchPath<RidpService, Account, WizardPage<RidpService, Account>, PossibleResults>> switchPaths) {
        super(name, wizard, buttonId, uiActivity, otherButtons, serviceProcess, service, switchPaths);
    }

    @Override
    protected PossibleResults getState() {
        ServiceManager serviceManager = ServiceManager.getServiceManager();
        ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
        VerifiyIdentityResponse verifiyIdentityResponse = configurationStorageHandler.readVerifiyIdentityResponse();

        if (verifiyIdentityResponse.ridpVerified == false){
            return PossibleResults.VerificationFailed;
        }

        if (verifiyIdentityResponse.userFoundInEnroll == true){
            return PossibleResults.InEnroll;
        }

        if (verifiyIdentityResponse.employers !=  null){
            return PossibleResults.InRoster;
        }

        return PossibleResults.Ok;
    }

    public enum PossibleResults {
        Ok,
        InACEDS,
        InEnroll,
        InRoster,
        VerificationFailed
    }


}

