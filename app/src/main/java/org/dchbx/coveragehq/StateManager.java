package org.dchbx.coveragehq;

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
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
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

    private final ServiceManager serviceManager;

    private StateMachine2<AppEvents, AppStates> stateMachine;
    private Messages messages;

    public StateManager(ServiceManager serviceManager){
        this.serviceManager = serviceManager;
        init();
        start();
        messages = BrokerApplication.getBrokerApplication().getMessages(this);
    }

    public void start(){
        stateMachine.start(new StateMachine2.ActivityInfo(AppStates.Login, LoginActivity.uiActivity));
        //launchActivity(LoginActivity.uiActivity);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void doThis(Events.AppEvent appEvent){
        try {
            process(appEvent.getEvent());
        } catch (Exception e){

        }
    }

    // This is for processing an external event.
    public void process(AppEvents appEvent) throws IOException, CoverageException {
        stateMachine.process(appEvent);
    }


    public void launchActivity(StateManager.UiActivity uiActivity) {
        messages.stateAction(Events.StateAction.Action.LaunchActivity, uiActivity.getId());
    }

    public void launchDialog(StateManager.UiDialog uiDialog) {
        messages.stateAction(Events.StateAction.Action.LaunchDialog);
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
        StateMachine2.StateInfoBase<AppEvents,AppStates> peek = stateMachine.getStatesStack().peek();
        return ((StateMachine2.ActivityInfo<AppEvents,AppStates>)peek).getUiActivity();
    }


    public enum AppStates {
        Any,
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
        GetQuestions, VerifyingUser, CreatingAccount, UqhpConfirm
    }

    public enum AppEvents {
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
        Details,
        BuyPlan,
        ReturningSignUpIndividual,
        OperationComplete,
        ServiceErrorHappened,
        GetQuestionsOperationComplete,
        UserVerifiedFoundYou,
        UserVerifiedSsnWithEmployer,
        UserVerifiedOkToCreate,
        AccountCreated,
        CreateAccountInCuramAced, ErrorHappened
    }

    public void init() {
        final StateManager stateManager = this;
        stateMachine = new StateMachine2<>(this);


        // default actions that could happen at any time. If a state doesn't want one of
        // these to happen in needs to handle the event.

        stateMachine.from(AppStates.Any).on(AppEvents.Back).doThis(new StateMachine2.Back());


        // Initial states not associated with any major section of the app.

        stateMachine.from(AppStates.Login)
            .processEvent(AppEvents.Login, new UserStatusProcessor())
                .on(AppEvents.BrokerLoggedIn).to(AppStates.Broker, null)
                .on(AppEvents.IndividualLoggedIn).to(AppStates.Broker, null)
                .on(AppEvents.ReturningSignUpIndividual).to(AppStates.PlanShoppingChoices, new StateMachine2.StateMachineAction<AppEvents,AppStates>() {
            @Override
            public void call(StateMachine2<AppEvents, AppStates> stateMachine, StateManager stateManager, AppEvents appEvents, AppStates leavingState, AppStates enterState) throws IOException, CoverageException {
                serviceManager.getAppStatusService().setUserStatus(ServerConfiguration.UserType.SignUpIndividual);
                serviceManager.getCoverageConnection().getEndPoints();
                stateMachine.launchActivity(new StateMachine2.ActivityInfo<AppEvents,AppStates>(enterState, PlanShoppingChoicesActivity.uiActivity));
            }
        });

        stateMachine.from(AppStates.Login).on(AppEvents.SignUpIndividual).to(AppStates.PlanShoppingChoices, new StateMachine2.LaunchActivity(PlanShoppingChoicesActivity.uiActivity));

        stateMachine.from(AppStates.PlanShoppingChoices)
                .on(AppEvents.StartShopping)
                    .to(AppStates.AcctCreate, new StateMachine2.LaunchActivity(AcctCreate.uiActivity));


        initRidpStates(stateMachine);
        initBasicPlanShoppingStates(stateMachine);
        initWalletStates(stateMachine);

    }

    private void initWalletStates(StateMachine2<AppEvents, AppStates> stateMachine) {

    }


    private void initBasicPlanShoppingStates(StateMachine2<AppEvents, AppStates> stateMachine){
        stateMachine.from(AppStates.PlanShoppingFamilyMembers).on(AppEvents.Continue).to(AppStates.PlanShoppingPremiumAndDeductible, new StateMachine2.LaunchActivity(PremiumAndDeductibleActivity.uiActivity));
        stateMachine.from(AppStates.PlanShoppingPremiumAndDeductible).on(AppEvents.SeePlans).to(AppStates.PlanSelector, new StateMachine2.LaunchActivity(PlanSelector.uiActivity));
        stateMachine.from(AppStates.PlanSelector).on(AppEvents.Details).to(AppStates.PlanShoppingDetails, new StateMachine2.LaunchActivity(PlanDetailsActivity.uiActivity));
        stateMachine.from(AppStates.PlanSelector).on(AppEvents.BuyPlan).to(AppStates.PlanShoppingPremiumAndDeductible, new StateMachine2.LaunchActivity(PremiumAndDeductibleActivity.uiActivity));
    }

    private void initRidpStates(StateMachine2<AppEvents, AppStates> stateMachine) {

        stateMachine.from(AppStates.AcctCreate).on(AppEvents.Continue).to(AppStates.AcctPreAuth, new StateMachine2.LaunchActivity(AcctPreAuthActivity.uiActivity));
        stateMachine.from(AppStates.AcctCreate).on(AppEvents.Skip).to(AppStates.PlanShoppingFamilyMembers, new StateMachine2.LaunchActivity(FamilyActivity.uiActivity));
        stateMachine.from(AppStates.AcctPreAuth).on(AppEvents.Continue).to(AppStates.AcctAddress, new StateMachine2.LaunchActivity(AcctAddress.uiActivity));
        stateMachine.from(AppStates.AcctAddress).on(AppEvents.Continue).to(AppStates.AcctDateOfBirth, new StateMachine2.LaunchActivity(AcctGenderActivity.uiActivity));
        stateMachine.from(AppStates.AcctGender).on(AppEvents.Continue).to(AppStates.AcctSsn, new StateMachine2.LaunchActivity(AcctDateOfBirth.uiActivity));
        stateMachine.from(AppStates.AcctDateOfBirth).on(AppEvents.Continue).to(AppStates.AcctSsn, new StateMachine2.LaunchActivity(AcctSsn.uiActivity));
        stateMachine.from(AppStates.AcctSsn).on(AppEvents.Continue).to(AppStates.AcctAuthConsent, new StateMachine2.LaunchActivity(AcctAuthConsent.uiActivity));
        stateMachine.from(AppStates.AcctAuthConsent).on(AppEvents.ConsentGiven).to(AppStates.GetQuestions, new StateManager.GetQuestions());
        stateMachine.from(AppStates.GetQuestions).on(AppEvents.GetQuestionsOperationComplete).to(AppStates.RidpQuestions, new StateMachine2.PopAndLaunchActivity(RidpQuestionsActivity.uiActivity));
        stateMachine.from(AppStates.AcctAuthConsent).on(AppEvents.ConsentDenied).to(AppStates.Login, new StateMachine2.LaunchActivity(AcctSystemFoundYouAceds.uiActivity));
        stateMachine.from(AppStates.RidpQuestions).on(AppEvents.Continue).to(AppStates.VerifyingUser, new StateManager.verifyUser());
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.UserVerifiedFoundYou).to(AppStates.AcctSystemFoundYou, new StateMachine2.LaunchActivity(AcctSystemFoundYou.uiActivity));
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.UserVerifiedSsnWithEmployer).to(AppStates.AcctSsnWithEmployer, new StateMachine2.LaunchActivity(AcctSsnWithEmployer.uiActivity));
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.UserVerifiedOkToCreate).to(AppStates.CreatingAccount, new CreateAccount());
        stateMachine.from(AppStates.CreatingAccount).on(AppEvents.CreateAccountInCuramAced).to(AppStates.AcctSystemFoundYouInCuramAceds, new StateMachine2.LaunchActivity(AcctSystemFoundYouAceds.uiActivity));
        stateMachine.from(AppStates.CreatingAccount).on(AppEvents.AccountCreated).to(AppStates.Login, new StateMachine2.PopAndLaunchActivity(LoginActivity.uiActivity));
    }


    class UserStatusProcessor implements StateMachine2.EventProcessor<StateManager.AppEvents> {

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
            Class<?> cls;
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

    public class UiDialog extends  Process {
        @Override
        void doThis(BrokerActivity brokerActivity) {

        }
    }

    // Get the Experian questions.

    public static class GetQuestions implements StateMachine2.StateMachineAction<StateManager.AppEvents,AppStates> {
        private StateManager.UiActivity uiActivity;

        @Override
        public void call(final StateMachine2<AppEvents, AppStates> stateMachine, final StateManager stateManager, AppEvents appEvents, AppStates leavingState, AppStates enterState) throws IOException, CoverageException {
            stateMachine.showWait(new StateMachine2.WaitActivityInfo<AppEvents, AppStates>(AppStates.GetQuestions, null));
            stateManager.messages.getVerificationResponse();
        }
    }

    // Verifiy user / send answers to server.

    public static class verifyUser implements StateMachine2.StateMachineAction<StateManager.AppEvents,AppStates> {
        @Override
        public void call(StateMachine2<AppEvents, AppStates> stateMachine, StateManager stateManager, AppEvents appEvents, AppStates leavingState, AppStates enterState) throws IOException, CoverageException {
            stateMachine.showWait(new StateMachine2.WaitActivityInfo<AppEvents, AppStates>(AppStates.GetQuestions, null));
            stateManager.messages.verifyUser();
        }
    }

        // Do the actual account creation.

    public static class CreateAccount implements StateMachine2.StateMachineAction<StateManager.AppEvents,AppStates> {
        private StateManager.UiActivity uiActivity;

        @Override
        public void call(final StateMachine2<AppEvents, AppStates> stateMachine, final StateManager stateManager, AppEvents appEvents, AppStates leavingState, AppStates enterState) throws IOException, CoverageException {
            stateMachine.showWait(new StateMachine2.WaitActivityInfo<AppEvents, AppStates>(AppStates.GetQuestions, null));
            stateManager.messages.createAccount();
        }
    }
}
