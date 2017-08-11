package org.dchbx.coveragehq.statemachine;

import android.util.Log;

import org.dchbx.coveragehq.AppStatusService;
import org.dchbx.coveragehq.BaseActivity;
import org.dchbx.coveragehq.BrokerActivity;
import org.dchbx.coveragehq.BrokerAppCompatDialogFragment;
import org.dchbx.coveragehq.BrokerApplication;
import org.dchbx.coveragehq.ChooseFinancialAssistanceActivity;
import org.dchbx.coveragehq.ConfigurationStorageHandler;
import org.dchbx.coveragehq.CoverageException;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.FamilyActivity;
import org.dchbx.coveragehq.GlossaryDialog;
import org.dchbx.coveragehq.HelloActivity;
import org.dchbx.coveragehq.Intents;
import org.dchbx.coveragehq.LoginActivity;
import org.dchbx.coveragehq.Messages;
import org.dchbx.coveragehq.PlanDetailsActivity;
import org.dchbx.coveragehq.PlanSelector;
import org.dchbx.coveragehq.PlanShoppingChoicesActivity;
import org.dchbx.coveragehq.PremiumAndDeductibleActivity;
import org.dchbx.coveragehq.ServerConfiguration;
import org.dchbx.coveragehq.ServiceManager;
import org.dchbx.coveragehq.StateProcessor;
import org.dchbx.coveragehq.WelcomeBackActivity;
import org.dchbx.coveragehq.YourMobilePasswordActivity;
import org.dchbx.coveragehq.ridp.AcctAddress;
import org.dchbx.coveragehq.ridp.AcctAuthConsent;
import org.dchbx.coveragehq.ridp.AcctCreate;
import org.dchbx.coveragehq.ridp.AcctDateOfBirth;
import org.dchbx.coveragehq.ridp.AcctGenderActivity;
import org.dchbx.coveragehq.ridp.AcctPreAuthActivity;
import org.dchbx.coveragehq.ridp.AcctSsn;
import org.dchbx.coveragehq.ridp.AcctSsnWithEmployer;
import org.dchbx.coveragehq.ridp.AcctSystemFoundYou;
import org.dchbx.coveragehq.ridp.AcctSystemFoundYouAceds;
import org.dchbx.coveragehq.ridp.RidpQuestionsActivity;
import org.dchbx.coveragehq.uqhp.FamilyRelationshipsActivity;
import org.dchbx.coveragehq.uqhp.UqhpConfirm;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.SubscriberExceptionEvent;
import org.greenrobot.eventbus.ThreadMode;

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
public class StateManager extends StateProcessor {

    private static final String TAG = "StateManager";
    private final ServiceManager serviceManager;

    private StateMachine stateMachine;
    private Messages messages;

    public StateManager(ServiceManager serviceManager){
        this.serviceManager = serviceManager;

    }

    public ArrayDeque<StateInfoBase> deserialize(String serializedString){
        SerializationHelper<ArrayDeque<StateInfoBase>> helper = new SerializationHelper<>();
        return helper.stringToObject(serializedString);
    }

    public void init(){
        messages = BrokerApplication.getBrokerApplication().getMessages(this);
        configStates();
        ConfigurationStorageHandler storageHandler = serviceManager.getConfigurationStorageHandler();
        String stateString = storageHandler.readStateString();

        if (stateString != null) {
            try {
                Log.d(TAG, "Restoring states: " + stateString);

                ArrayDeque<StateInfoBase> stack = deserialize(stateString);
                for (StateInfoBase appEventsAppStatesStateInfoBase : stack) {
                    appEventsAppStatesStateInfoBase.reconstitute();
                }
                checkStackOnResume(stack);

                Log.d(TAG, "states deque size: " + stack.size());
                stateMachine.start(stack);
            } catch (Exception e){
                Log.e(TAG, "Unable to restore state resetting to scratch.");
                stateMachine.start(new ActivityInfo(AppStates.Login, AppEvents.Init, LoginActivity.uiActivity));
            }
            //storeState();
        } else {
            Log.e(TAG, "Setting stack to empty.");
            stateMachine.start(new ActivityInfo(AppStates.Hello, AppEvents.Init, HelloActivity.uiActivity));
        }
        //launchActivity(LoginActivity.uiActivity);
    }

    private void checkStackOnResume(ArrayDeque<StateInfoBase> stack) {
        StateInfoBase top = stack.peek();
        ResumeActions resumeAction = top.onResume();
        switch (resumeAction){
            case Continue:
                break;
            case Pop:
                stack.pop();
                if (stack.size() > 0){
                    checkStackOnResume(stack);
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(SubscriberExceptionEvent exceptionEvent){
        try {
            process(AppEvents.Error, null);
        } catch (Exception e){
            Log.e(TAG, "exception handling exception from another service, NOT GOOD!!!");
            Log.e(TAG, e.getMessage());
            Log.e(TAG, e.getStackTrace().toString());
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.AppEvent appEvent){
        try {
            process(appEvent.getEvent(), appEvent.getIntentParameters());
        } catch (Exception e){

        }
    }

    // This is for processing an external event.
    public void process(AppEvents appEvent, EventParameters intentParameters) throws IOException, CoverageException {
        stateMachine.process(appEvent, intentParameters);
    }

    private void storeState() {
        // ArrayDeque<StateInfoBase> statesStack = stateMachine.getStatesStack();
        // SerializationHelper<ArrayDeque<StateInfoBase>> helper = new SerializationHelper<>();
        // String string = helper.objectToString(statesStack);
        // ConfigurationStorageHandler storageHandler = serviceManager.getConfigurationStorageHandler();
        // storageHandler.storeStateString(string);
        // Log.d(TAG, "**************************************");
    }


    public void launchActivity(StateManager.UiActivity uiActivity, EventParameters eventParameters) {
        messages.stateAction(Events.StateAction.Action.LaunchActivity, uiActivity.getId(), eventParameters);
    }

    public void launchDialog(StateManager.UiDialog uiDialog, EventParameters eventParameters) {
        messages.stateAction(Events.StateAction.Action.LaunchDialog, uiDialog.getId(), eventParameters);
    }

    public void restartApp() {

    }

    public void back(){
        messages.stateAction(Events.StateAction.Action.Finish);
    }

    public void showWait() {
        messages.stateAction(Events.StateAction.Action.ShowWait);
    }

    public void hideWait() {
        messages.stateAction(Events.StateAction.Action.HideWait);
    }

    public void showError(String message){
        messages.error(null, message);
    }

    public StateManager.UiActivity getCurrentActivity() {
        StateInfoBase peek = stateMachine.getStatesStack().peek();
        return ((ActivityInfo)peek).getUiActivity();
    }

    public void dismissDialog() {
        messages.stateAction(Events.StateAction.Action.Dismiss);
    }


    public enum ResumeActions {
        Continue,
        Pop
    }

    // You are discouraged from removing or reordring this enum. It is used in serialized objects.

    public enum AppStates {
        Any,
        AcctAddressFe,
        AcctAuthConsentFe,
        AcctCreateFe,
        AcctGenderFe,
        AcctDateOfBirthFe,
        AcctPreAuthFe,
        AcctSsnFe,
        AcctAddress,
        AcctAuthConsent,
        AcctCreate,
        AcctGender,
        AcctDateOfBirth,
        AcctPreAuth,
        AcctSsn,
        AcctSsnWithEmployer,
        AcctSystemFoundYou,
        AcctSystemFoundYouInCuramAceds,
        FoundEmployers,
        RidpQuestions,
        PlanShoppingChoices,
        Login,
        Initialized,
        PlanShoppingFamilyMembers,
        PlanShoppingPremiumAndDeductible,
        PlanSelector,
        PlanShoppingDetails,
        Broker,
        GetQuestions,
        VerifyingUser,
        CreatingAccount,
        UqhpConfirm,
        WalletLogin,
        Hello,
        YourMobilePassword,
        WelcomeBack,
        ChooseFinancialAssistance,
        FamilyRelationships,
        GlossaryDialog,
        CreatingAccountFe, VerifyingUserFe, RidpQuestionsFe, GetQuestionsFe, AcctSystemFoundYouInCuramAcedsFe, AcctSsnWithEmployerFe, AcctSystemFoundYouFe, Wallet
    }

    // You are discouraged from removing or reordring this enum. It is used in serialized objects.

    public enum AppEvents {
        Error,
        Startup,
        Login,
        LoginWait,
        Continue,
        Skip,
        Back,
        Close,
        Cancel,
        Ok,
        StartShopping,
        ConsentGiven,
        ConsentDenied,
        CheckFinancialAssitance,
        BrokerLoggedIn,
        ShowLogin,
        IndividualLoggedIn,
        SignUpIndividual,
        SeePlans,
        ShowPlanDetails,
        BuyPlan,
        ReturningSignUpIndividual,
        OperationComplete,
        ServiceErrorHappened,
        GetQuestionsOperationComplete,
        UserVerifiedFoundYou,
        UserVerifiedSsnWithEmployer,
        UserVerifiedOkToCreate,
        SignUpSuccessful,
        SignUpUserInAceds,
        ViewMyAccount, StartApplication, ResumeApplication, Yes, No, ShowGlossaryItem, ShowStateInfo, Init, ErrorHappened
    }

    public void configStates() {
        final StateManager stateManager = this;
        stateMachine = new StateMachine(this);


        // default actions that could happen at any time. If a state doesn't want one of
        // these to happen in needs to handle the event.

        stateMachine.from(AppStates.Any).on(AppEvents.Back).doThis(new Back());
        stateMachine.from(AppStates.Any).on(AppEvents.ShowGlossaryItem).to(AppStates.GlossaryDialog, new LaunchDialog(GlossaryDialog.uiDialog));


        // Initial states not associated with any major section of the app.


        stateMachine.from(AppStates.GlossaryDialog).on(AppEvents.Back).doThis(new Back());
        stateMachine.from(AppStates.GlossaryDialog).on(AppEvents.Close).doThis(new Back());
        stateMachine.from(AppStates.Hello).on(AppEvents.ViewMyAccount).to(AppStates.Login, new LaunchActivity(LoginActivity.uiActivity));
        stateMachine.from(AppStates.Hello).on(AppEvents.StartApplication).to(AppStates.YourMobilePassword, new LaunchActivity(YourMobilePasswordActivity.uiActivity));
        stateMachine.from(AppStates.Hello).on(AppEvents.ResumeApplication).to(AppStates.WelcomeBack, new LaunchActivity(WelcomeBackActivity.uiActivity));

        stateMachine.from(AppStates.YourMobilePassword).on(AppEvents.Ok).to(AppStates.ChooseFinancialAssistance, new LaunchActivity(ChooseFinancialAssistanceActivity.uiActivity));
        stateMachine.from(AppStates.YourMobilePassword).on(AppEvents.Cancel).to(AppStates.ChooseFinancialAssistance, new LaunchActivity(ChooseFinancialAssistanceActivity.uiActivity));

        stateMachine.from(AppStates.ChooseFinancialAssistance).on(AppEvents.Yes).to(AppStates.AcctCreateFe, new LaunchActivity(AcctCreate.uiActivity));
        stateMachine.from(AppStates.ChooseFinancialAssistance).on(AppEvents.No).to(AppStates.AcctCreate, new LaunchActivity(AcctCreate.uiActivity));

        stateMachine.from(AppStates.Login)
            .processEvent(AppEvents.Login, new UserStatusProcessor())
                .on(AppEvents.BrokerLoggedIn).to(AppStates.Broker, null)
                .on(AppEvents.IndividualLoggedIn).to(AppStates.Broker, null)
                .on(AppEvents.ReturningSignUpIndividual).to(AppStates.PlanShoppingChoices, new StateMachineAction() {
            @Override
            public void call(StateMachine stateMachine, StateManager stateManager, AppEvents appEvents, AppStates leavingState, AppStates enterState, EventParameters intentParameters) throws IOException, CoverageException {
                serviceManager.getAppStatusService().setUserStatus(ServerConfiguration.UserType.SignUpIndividual);
                serviceManager.getCoverageConnection().getEndPoints();
                stateMachine.push(new ActivityInfo(enterState, appEvents, PlanShoppingChoicesActivity.uiActivity));
            }
        });

        stateMachine.from(AppStates.Login).on(AppEvents.SignUpIndividual).to(AppStates.PlanShoppingChoices, new LaunchActivity(PlanShoppingChoicesActivity.uiActivity));

        stateMachine.from(AppStates.PlanShoppingChoices)
                .on(AppEvents.StartShopping)
                    .to(AppStates.AcctCreate, new LaunchActivity(AcctCreate.uiActivity));


        initRidpFeStates(stateMachine);
        initRidpStates(stateMachine);
        initUQHPStates(stateMachine);
        initBasicPlanShoppingStates(stateMachine);
        initWalletStates(stateMachine);
    }

    private void initUQHPStates(StateMachine stateMachine) {
        stateMachine.from(AppStates.PlanShoppingFamilyMembers).on(AppEvents.Continue).to(AppStates.FamilyRelationships, new LaunchActivity(FamilyRelationshipsActivity.uiActivity));
        stateMachine.from(AppStates.FamilyRelationships).on(AppEvents.Continue).to(AppStates.UqhpConfirm, new LaunchActivity(UqhpConfirm.uiActivity));
        stateMachine.from(AppStates.UqhpConfirm).on(AppEvents.Continue).to(AppStates.UqhpConfirm, new LaunchActivity(LoginActivity.uiActivity));
    }

    private void initWalletStates(StateMachine stateMachine) {

    }


    private void initBasicPlanShoppingStates(StateMachine stateMachine){
        stateMachine.from(AppStates.PlanShoppingFamilyMembers).on(AppEvents.Continue).to(AppStates.PlanShoppingPremiumAndDeductible, new LaunchActivity(PremiumAndDeductibleActivity.uiActivity));
        stateMachine.from(AppStates.PlanShoppingPremiumAndDeductible).on(AppEvents.SeePlans).to(AppStates.PlanSelector, new LaunchActivity(PlanSelector.uiActivity));
        stateMachine.from(AppStates.PlanSelector).on(AppEvents.ShowPlanDetails).to(AppStates.PlanShoppingDetails, new LaunchActivity(PlanDetailsActivity.uiActivity));
        stateMachine.from(AppStates.PlanSelector).on(AppEvents.BuyPlan).to(AppStates.PlanShoppingPremiumAndDeductible, new LaunchActivity(PremiumAndDeductibleActivity.uiActivity));
    }

    private void initRidpStates(StateMachine stateMachine) {

        stateMachine.from(AppStates.AcctCreate).on(AppEvents.Continue).to(AppStates.AcctPreAuth, new LaunchActivity(AcctPreAuthActivity.uiActivity));
        stateMachine.from(AppStates.AcctCreate).on(AppEvents.Skip).to(AppStates.PlanShoppingFamilyMembers, new LaunchActivity(FamilyActivity.uiActivity));
        stateMachine.from(AppStates.AcctPreAuth).on(AppEvents.Continue).to(AppStates.AcctAddress, new LaunchActivity(AcctAddress.uiActivity));
        stateMachine.from(AppStates.AcctAddress).on(AppEvents.Continue).to(AppStates.AcctGender, new LaunchActivity(AcctGenderActivity.uiActivity));
        stateMachine.from(AppStates.AcctGender).on(AppEvents.Continue).to(AppStates.AcctDateOfBirth, new LaunchActivity(AcctDateOfBirth.uiActivity));
        stateMachine.from(AppStates.AcctDateOfBirth).on(AppEvents.Continue).to(AppStates.AcctSsn, new LaunchActivity(AcctSsn.uiActivity));
        stateMachine.from(AppStates.AcctSsn).on(AppEvents.Continue).to(AppStates.AcctAuthConsent, new LaunchActivity(AcctAuthConsent.uiActivity));
        stateMachine.from(AppStates.AcctAuthConsent).on(AppEvents.ConsentGiven).to(AppStates.GetQuestions, new StateManager.GetQuestions());
        stateMachine.from(AppStates.GetQuestions).on(AppEvents.GetQuestionsOperationComplete).to(AppStates.RidpQuestions, new PopAndLaunchActivity(RidpQuestionsActivity.uiActivity));
        stateMachine.from(AppStates.AcctAuthConsent).on(AppEvents.ConsentDenied).to(AppStates.Login, new LaunchActivity(AcctSystemFoundYouAceds.uiActivity));
        stateMachine.from(AppStates.RidpQuestions).on(AppEvents.Continue).to(AppStates.VerifyingUser, new VerifyUser());
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.UserVerifiedFoundYou).to(AppStates.AcctSystemFoundYou, new LaunchActivity(AcctSystemFoundYou.uiActivity));
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.UserVerifiedSsnWithEmployer).to(AppStates.AcctSsnWithEmployer, new LaunchActivity(AcctSsnWithEmployer.uiActivity));
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.UserVerifiedOkToCreate).to(AppStates.CreatingAccount, new CreateAccount());
        stateMachine.from(AppStates.CreatingAccount).on(AppEvents.SignUpUserInAceds).to(AppStates.AcctSystemFoundYouInCuramAceds, new LaunchActivity(AcctSystemFoundYouAceds.uiActivity));
        stateMachine.from(AppStates.CreatingAccount).on(AppEvents.SignUpSuccessful).to(AppStates.Login, new PopAndLaunchActivity(LoginActivity.uiActivity));
    }

    private void initRidpFeStates(StateMachine stateMachine) {

        stateMachine.from(AppStates.AcctCreateFe).on(AppEvents.Continue).to(AppStates.AcctPreAuthFe, new LaunchActivity(AcctPreAuthActivity.uiActivity));
        stateMachine.from(AppStates.AcctPreAuthFe).on(AppEvents.Continue).to(AppStates.AcctAddressFe, new LaunchActivity(AcctAddress.uiActivity));
        stateMachine.from(AppStates.AcctAddressFe).on(AppEvents.Continue).to(AppStates.AcctGenderFe, new LaunchActivity(AcctGenderActivity.uiActivity));
        stateMachine.from(AppStates.AcctGenderFe).on(AppEvents.Continue).to(AppStates.AcctDateOfBirthFe, new LaunchActivity(AcctDateOfBirth.uiActivity));
        stateMachine.from(AppStates.AcctDateOfBirthFe).on(AppEvents.Continue).to(AppStates.AcctSsnFe, new LaunchActivity(AcctSsn.uiActivity));
        stateMachine.from(AppStates.AcctSsnFe).on(AppEvents.Continue).to(AppStates.AcctAuthConsentFe, new LaunchActivity(AcctAuthConsent.uiActivity));
        stateMachine.from(AppStates.AcctAuthConsentFe).on(AppEvents.ConsentGiven).to(AppStates.GetQuestionsFe, new StateManager.GetQuestions());
        stateMachine.from(AppStates.GetQuestionsFe).on(AppEvents.GetQuestionsOperationComplete).to(AppStates.RidpQuestionsFe, new PopAndLaunchActivity(RidpQuestionsActivity.uiActivity));
        stateMachine.from(AppStates.AcctAuthConsentFe).on(AppEvents.ConsentDenied).to(AppStates.Login, new LaunchActivity(AcctSystemFoundYouAceds.uiActivity));
        stateMachine.from(AppStates.RidpQuestionsFe).on(AppEvents.Continue).to(AppStates.VerifyingUserFe, new VerifyUser());
        stateMachine.from(AppStates.VerifyingUserFe).on(AppEvents.UserVerifiedFoundYou).to(AppStates.AcctSystemFoundYouFe, new LaunchActivity(AcctSystemFoundYou.uiActivity));
        stateMachine.from(AppStates.VerifyingUserFe).on(AppEvents.UserVerifiedSsnWithEmployer).to(AppStates.AcctSsnWithEmployerFe, new LaunchActivity(AcctSsnWithEmployer.uiActivity));
        stateMachine.from(AppStates.VerifyingUserFe).on(AppEvents.UserVerifiedOkToCreate).to(AppStates.CreatingAccountFe, new CreateAccount());
        stateMachine.from(AppStates.CreatingAccountFe).on(AppEvents.SignUpUserInAceds).to(AppStates.AcctSystemFoundYouInCuramAcedsFe, new LaunchActivity(AcctSystemFoundYouAceds.uiActivity));
        stateMachine.from(AppStates.CreatingAccountFe).on(AppEvents.SignUpSuccessful).to(AppStates.Login, new PopAndLaunchActivity(LoginActivity.uiActivity));
    }


    class UserStatusProcessor implements EventProcessor {

        public StateManager.AppEvents process(StateManager.AppEvents appEvents) throws Exception {
            AppStatusService appStatusService = ServiceManager.getServiceManager().getAppStatusService();
            switch (appStatusService.getUserStatus()){
                case Unknown:
                    return StateManager.AppEvents.ErrorHappened;
                case NotLoggedIn:
                    return StateManager.AppEvents.ShowLogin;
                case Broker:
                    return StateManager.AppEvents.BrokerLoggedIn;
                case Employer:
                    return StateManager.AppEvents.BrokerLoggedIn;
                case Employee:
                    return StateManager.AppEvents.BrokerLoggedIn;
                case SignUpIndividual:
                    return StateManager.AppEvents.SignUpIndividual;
                case Individual:
                    return StateManager.AppEvents.IndividualLoggedIn;
            }
            throw new Exception("Unknown user status.");
        }
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
    public static class UiActivity extends Process {
        public static class Info {
            public Class<?> cls;
            UiActivity uiActivity;
        }
        private static HashMap<Integer, Info> uiActivityMap = new HashMap<>();

        public static Info getUiActivityType(int id){
            return uiActivityMap.get(id);
        }

        public static Info getUiActivityType(UiActivity uiActivity){
            return uiActivityMap.get(uiActivity.getId());
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

    public static class UiDialog extends Process {
        @Override
        void doThis(BrokerActivity brokerActivity) {

        }

        public static class Info {
            public Class<?> cls;
            public UiDialog uiDialog;
            public DialogBuilder dialogBuilder;
        }

        private static HashMap<Integer, UiDialog.Info> uiDialogMap = new HashMap<>();

        public static UiDialog.Info getUiDialogType(int id){
            return uiDialogMap.get(id);
        }

        public static UiDialog.Info getUiDialogType(UiDialog uiDialog){
            return uiDialogMap.get(uiDialog.getId());
        }


        public UiDialog(Class<?> cls, DialogBuilder dialogBuilder){
            Info info = new Info();
            info.cls = cls;
            info.dialogBuilder = dialogBuilder;
            info.uiDialog = this;
            uiDialogMap.put(getId(), info);
        }


        public BrokerAppCompatDialogFragment build(EventParameters eventParameters, BaseActivity activity) {
            return getUiDialogType(getId()).dialogBuilder.build(eventParameters, activity);
        }
    }

    // Get the Experian questions.

    public static class GetQuestions implements StateMachineAction {
        private StateManager.UiActivity uiActivity;

        @Override
        public void call(StateMachine stateMachine, StateManager stateManager, AppEvents event,
                         AppStates leavingState, AppStates enterState,
                         EventParameters intentParameters) throws IOException, CoverageException {
            stateMachine.push(new WaitActivityInfo<AppEvents, AppStates>(AppStates.GetQuestions, event, null));
            stateManager.showWait();
            stateManager.messages.getVerificationResponse();
        }
    }

    // Verifiy user / send answers to server.

    public static class VerifyUser implements StateMachineAction {
        @Override
        public void call(StateMachine stateMachine, StateManager stateManager, AppEvents event,
                         AppStates leavingState, AppStates enterState,
                         EventParameters intentParameters) throws IOException, CoverageException {
            stateMachine.push(new WaitActivityInfo(enterState, event, null));
            stateManager.showWait();
            stateManager.messages.verifyUser();
        }
    }

        // Do the actual account creation.

    public static class CreateAccount implements StateMachineAction {
        private StateManager.UiActivity uiActivity;

        @Override
        public void call(StateMachine stateMachine, StateManager stateManager,
                         AppEvents event, AppStates leavingState,
                         AppStates enterState,
                         EventParameters intentParameters) throws IOException, CoverageException {
            stateMachine.getStatesStack().pop();
            stateMachine.push(new WaitActivityInfo(enterState, event, null));
            stateManager.messages.createAccount();
        }
    }


    public class DialogInfo extends StateInfoBase {

        private StateManager.UiDialog uiDialog;
        private ActivityInfo activityInfo;

        public DialogInfo(AppStates state, AppEvents event, StateManager.UiDialog uiDialog, ActivityInfo activityInfo) {
            super(state, event);
            this.uiDialog = uiDialog;
            this.activityInfo = activityInfo;
        }
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }
}
