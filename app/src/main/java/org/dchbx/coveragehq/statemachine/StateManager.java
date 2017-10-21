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
import org.dchbx.coveragehq.GlossaryDialog;
import org.dchbx.coveragehq.HelloActivity;
import org.dchbx.coveragehq.IServiceManager;
import org.dchbx.coveragehq.InsuredDetailsActivity;
import org.dchbx.coveragehq.Intents;
import org.dchbx.coveragehq.LoginActivity;
import org.dchbx.coveragehq.Messages;
import org.dchbx.coveragehq.PlanDetailsActivity;
import org.dchbx.coveragehq.PlanShoppingChoicesActivity;
import org.dchbx.coveragehq.ServerConfiguration;
import org.dchbx.coveragehq.ServiceManager;
import org.dchbx.coveragehq.StateProcessor;
import org.dchbx.coveragehq.financialeligibility.AttestationActivity;
import org.dchbx.coveragehq.financialeligibility.CheckedListDialog;
import org.dchbx.coveragehq.financialeligibility.EditPersonActivity;
import org.dchbx.coveragehq.financialeligibility.EditRelationshipActivity;
import org.dchbx.coveragehq.financialeligibility.EligibleResultsActivity;
import org.dchbx.coveragehq.financialeligibility.IneligibleResultsActivity;
import org.dchbx.coveragehq.financialeligibility.RelationshipsActivity;
import org.dchbx.coveragehq.financialeligibility.SectionActivity;
import org.dchbx.coveragehq.planshopping.FamilyActivity;
import org.dchbx.coveragehq.planshopping.PlanSelector;
import org.dchbx.coveragehq.planshopping.PremiumAndDeductibleActivity;
import org.dchbx.coveragehq.planshopping.SelectedPlanActivity;
import org.dchbx.coveragehq.planshopping.SubmitApplicationActivity;
import org.dchbx.coveragehq.planshopping.ThanksApplicationActivity;
import org.dchbx.coveragehq.ridp.AcctAddress;
import org.dchbx.coveragehq.ridp.AcctAuthConsent;
import org.dchbx.coveragehq.ridp.AcctCreate;
import org.dchbx.coveragehq.ridp.AcctCreateNewPassword;
import org.dchbx.coveragehq.ridp.AcctDateOfBirth;
import org.dchbx.coveragehq.ridp.AcctGenderActivity;
import org.dchbx.coveragehq.ridp.AcctPhone;
import org.dchbx.coveragehq.ridp.AcctPreAuthActivity;
import org.dchbx.coveragehq.ridp.AcctRidpConnectionFailure;
import org.dchbx.coveragehq.ridp.AcctRidpUserNotFound;
import org.dchbx.coveragehq.ridp.AcctRidpWrongAnswersLockout;
import org.dchbx.coveragehq.ridp.AcctRidpWrongAnswersRecoverable;
import org.dchbx.coveragehq.ridp.AcctSsn;
import org.dchbx.coveragehq.ridp.AcctSsnWithEmployer;
import org.dchbx.coveragehq.ridp.AcctSystemFoundYou;
import org.dchbx.coveragehq.ridp.AcctSystemFoundYouAceds;
import org.dchbx.coveragehq.ridp.RidpQuestionsActivity;
import org.dchbx.coveragehq.ridp.RidpService;
import org.dchbx.coveragehq.startup.CongratsActivity;
import org.dchbx.coveragehq.startup.CoverageThisYearActivity;
import org.dchbx.coveragehq.startup.DentalCoverageActivity;
import org.dchbx.coveragehq.startup.FullPricePlanActivity;
import org.dchbx.coveragehq.startup.HelpPayingActivity;
import org.dchbx.coveragehq.startup.IWantToActivity;
import org.dchbx.coveragehq.startup.MobilePasswordActivity;
import org.dchbx.coveragehq.startup.OpenEnrollmentClosedActivity;
import org.dchbx.coveragehq.startup.ResumeApplicationActivity;
import org.dchbx.coveragehq.uqhp.FamilyRelationshipsActivity;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.SubscriberExceptionEvent;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.Cancel;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.ClearedPII;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.Continue;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.No;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.Ok;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.ReceivedUqhpDeterminationOnlyEligible;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.SeePlans;
import static org.dchbx.coveragehq.statemachine.StateManager.AppEvents.Yes;

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
    private final IServiceManager serviceManager;

    private StateMachine stateMachine;
    private Messages messages;
    private boolean inUse;
    private ArrayList<Events.AppEvent> queuedEvents;

    public StateManager(IServiceManager serviceManager){
        this.serviceManager = serviceManager;
        inUse = false;
        queuedEvents = new ArrayList<>();

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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(SubscriberExceptionEvent exceptionEvent){
        try {
            process(AppEvents.Error, null);
        } catch (Exception e){
            Log.e(TAG, "exception handling exception from another service, NOT GOOD!!!");
            Log.e(TAG, e.getMessage());
            Log.e(TAG, e.getStackTrace().toString());
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(Events.AppEvent appEvent){
        try {
            if (inUse){
                queuedEvents.add(appEvent);
                return;
            }
            inUse = true;
            process(appEvent.getEvent(), appEvent.getIntentParameters());
            while (queuedEvents.size() > 0){
                Events.AppEvent topEvent = queuedEvents.remove(0);
                process(topEvent.getEvent(), appEvent.getIntentParameters());
            }
            inUse = false;
        } catch (Throwable t){
            Log.e(TAG, "throwable in state manager: " + t);
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


    public void popActivity(StateManager.UiActivity uiActivity, EventParameters eventParameters) {
        messages.stateAction(Events.StateAction.Action.Pop, uiActivity.getId(), eventParameters);
    }

    public void popAndLaunchActivity(StateManager.UiActivity uiActivity, EventParameters eventParameters) {
        messages.stateAction(Events.StateAction.Action.PopAndLaunchActivity, uiActivity.getId(), eventParameters);
    }

    public void launchActivity(StateManager.UiActivity uiActivity, EventParameters eventParameters) {
        messages.stateAction(Events.StateAction.Action.LaunchActivity, uiActivity.getId(), eventParameters);
    }

    public void launchDialog(StateManager.UiDialog uiDialog, EventParameters eventParameters) {
        messages.stateAction(Events.StateAction.Action.LaunchDialog, uiDialog.getId(), eventParameters);
    }

    public void restartApp() {

    }

    public void back(EventParameters eventParameters){
        messages.stateAction(Events.StateAction.Action.Finish, eventParameters);
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

    public enum ActivityResultCodes {
        Saved,
        Canceled
    }

    // You are discouraged from removing or reordring this enum. It is used in serialized objects.

    public enum AppStates {
        Any,
        Previous,
        AcctAddress,
        AcctPhone,
        AcctAuthConsent,
        AcctCreate,
        AcctGender,
        AcctDateOfBirth,
        AcctPreAuth,
        AcctSsn,
        FoundEmployers,
        PlanShoppingChoices,
        Login,
        Initialized,
        PlanShoppingFamilyMembers,
        PlanShoppingPremiumAndDeductible,
        PlanSelector,
        PlanShoppingDetails,
        Broker,
        UqhpConfirm,
        WalletLogin,
        Hello,
        YourMobilePassword,
        WelcomeBack,
        ChooseFinancialAssistance,
        FamilyRelationships,
        GlossaryDialog,
        CreatingAccount, VerifyingUser, RidpQuestions, GetQuestions, AcctRidpUserNotFound, AcctRidpClosing,
        AcctRidpConnectionFailure, AcctRidpWrongAnswersRecoverable, AcctRidpWrongAnswersLockout, AcctRidpCheckingOverride,
        AcctSystemFoundYouInCuramAceds, AcctSsnWithEmployer, AcctSystemFoundYouReturningToLogin, AcctSystemFoundYouClosing,
        FinancialAssitanceQuestions, FeDropDown,
        Wallet, SectionQuestions, EditFamilyRelationShip, Attestation,
        UqhpDetermination, Ineligible, Eligible, Saved,
        OpenEnrollmentClosed, DentalCoverage, IWantTo, CoverageNextYear, HelpPaying, MobilePassword,
        ResumeApplication, ResumingAppliedUqhp, ResumingApplying, LoggingIn, GettingEffectiveDate,
        GettingStatus, AcctNewPassword, AcctPreAuthNP, AcctAddressNP, AcctGenderNP, AcctDateOfBirthNP,
        AcctSsnNP, AcctAuthConsentNP, GetQuestionsNP, RidpQuestionsNP, AcctSystemFoundYou, FamilyMembers,
        GettingUqhpDetermination, PremiumAndDeductible, GettingPlans, SelectedPlan, ApplicationSubmitted, SubmittingApplication, ThanksApplication, Congrats, GettingEffectiveDateForIWant, LastStep, ClearPIIMobilePassword, CoverageThisYear
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
        RidpConnectionFailure,
        RidpWrongAnswersRecoverable,
        RidpWrongAnswersLockout,
        RidpUserNotFound,
        ReviewRidpResponses,
        UserVerifiedFoundYou,
        UserVerifiedSsnWithEmployer,
        UserVerifiedOkToCreate,
        SignUpSuccessful,
        SignUpUserInAceds,
        ViewMyAccount,
        StartApplication,
        ResumeApplication,
        Yes, No,
        ShowGlossaryItem,
        ShowStateInfo,
        Init,
        EditFamilyMember,
        OpenSection,
        Goto, // Special case for dev to goto specific state.
        ShowDropDown, DropdownSaved, UserSaved, EditRelationship, ReceivedUqhpDeterminationOnlyEligible,
        ShowEligible, ShowIneligible, ErrorHappened, GetCoverageThisYear, GetCoverageNextYear,
        StatusAppliedUqhp, StatusEnrollingUqhp, StatusApplying, StatusEnrolled,
        ReceivedEffectiveDate, InOpenEnrollment, OpenEnrollmentClosed, GetDentalCoverage,
        ForgotPassword, ReceivedUqhpDeterminationHasIneligible, ChoosePlan,
        ContinueMultipleMemberFamily, ContinueSingleMemberFamily, GotPlans,

        RidpCheckOverride, ClearedPII,
        BuyPlanConfirmed, ChoosePlanSucessful, CheckStatusNow, ComeBackLater, SubmitMyApplicationClicked, PlanShoppingEditFamilyMember
    }

    public void configStates() {
        final StateManager stateManager = this;
        stateMachine = new StateMachine(this);


        // default actions that could happen at any time. If a state doesn't want one of
        // these to happen in needs to handle the event.

        stateMachine.from(AppStates.Any).on(AppEvents.Back).doThis(new Back());
        stateMachine.from(AppStates.Any).on(AppEvents.ShowGlossaryItem).to(AppStates.GlossaryDialog, new LaunchDialog(GlossaryDialog.uiDialog));

        stateMachine.from(AppStates.Any).on(AppEvents.Goto).to(AppStates.PlanShoppingFamilyMembers, new LaunchActivity(FamilyActivity.uiActivity));


        // Initial states not associated with any major section of the app.


        stateMachine.from(AppStates.GlossaryDialog).on(AppEvents.Back).doThis(new Back());
        stateMachine.from(AppStates.GlossaryDialog).on(AppEvents.Close).doThis(new Back());

        stateMachine.from(AppStates.YourMobilePassword).on(AppEvents.Ok).to(AppStates.ChooseFinancialAssistance, new LaunchActivity(ChooseFinancialAssistanceActivity.uiActivity));
        stateMachine.from(AppStates.YourMobilePassword).on(AppEvents.Cancel).to(AppStates.Hello, new LaunchActivity(HelloActivity.uiActivity));

        //stateMachine.from(AppStates.ChooseFinancialAssistance).on(AppEvents.Yes).to(AppStates.AcctCreateFe, new LaunchActivity(AcctCreate.uiActivity));
        stateMachine.from(AppStates.ChooseFinancialAssistance).on(No).to(AppStates.AcctCreate, new LaunchActivity(AcctCreate.uiActivity));

        stateMachine.from(AppStates.Login)
            .processEvent(AppEvents.Login, new UserStatusProcessor())
                .on(AppEvents.BrokerLoggedIn).to(AppStates.Broker, null)
                .on(AppEvents.IndividualLoggedIn).to(AppStates.Wallet, new PopAndLaunchActivity(InsuredDetailsActivity.uiActivity))
                .on(AppEvents.ReturningSignUpIndividual).to(AppStates.PlanShoppingChoices, new StateMachineAction() {
            @Override
            public boolean call(StateMachine stateMachine, StateManager stateManager, AppEvents appEvents, AppStates leavingState, AppStates enterState, EventParameters intentParameters) throws IOException, CoverageException {
                serviceManager.getAppStatusService().setUserStatus(ServerConfiguration.UserType.SignUpIndividual);
                serviceManager.getCoverageConnection().getEndPoints();
                stateMachine.push(new ActivityInfo(enterState, appEvents, PlanShoppingChoicesActivity.uiActivity));
                return false;
            }
        });

        stateMachine.from(AppStates.Login).on(AppEvents.SignUpIndividual).to(AppStates.PlanShoppingChoices, new LaunchActivity(PlanShoppingChoicesActivity.uiActivity));
        stateMachine.from(AppStates.Login).on(AppEvents.Cancel).to(AppStates.Hello, new LaunchActivity(HelloActivity.uiActivity));

        stateMachine.from(AppStates.PlanShoppingChoices)
                .on(AppEvents.StartShopping)
                    .to(AppStates.AcctCreate, new LaunchActivity(AcctCreate.uiActivity));


        initStartupStates(stateMachine);
        initRidpStates(stateMachine);
        initUQHPStates(stateMachine);
        initBasicPlanShoppingStates(stateMachine);
        initWalletStates(stateMachine);
    }

    private void initStartupStates(StateMachine stateMachine){
        stateMachine.from(AppStates.Hello).on(AppEvents.ViewMyAccount).to(AppStates.Login, new LaunchActivity(LoginActivity.uiActivity));
        stateMachine.from(AppStates.Hello).on(AppEvents.StartApplication).to(AppStates.GettingEffectiveDateForIWant, new StateManager.BackgroundProcess(Events.GetEffectiveDate.class));
        stateMachine.from(AppStates.GettingEffectiveDateForIWant).on(AppEvents.ReceivedEffectiveDate).to(AppStates.IWantTo, new LaunchActivity(IWantToActivity.uiActivity));
        stateMachine.from(AppStates.Hello).on(AppEvents.ResumeApplication).to(AppStates.ResumeApplication, new LaunchActivity(ResumeApplicationActivity.uiActivity));
        stateMachine.from(AppStates.IWantTo).on(AppEvents.GetCoverageNextYear).to(AppStates.CoverageNextYear, new LaunchActivity(FullPricePlanActivity.uiActivity));
        stateMachine.from(AppStates.IWantTo).on(AppEvents.GetCoverageThisYear).to(AppStates.CoverageThisYear, new LaunchActivity(CoverageThisYearActivity.uiActivity));
        stateMachine.from(AppStates.IWantTo).on(AppEvents.GetDentalCoverage).to(AppStates.DentalCoverage, new LaunchActivity(DentalCoverageActivity.uiActivity));
        stateMachine.from(AppStates.OpenEnrollmentClosed).on(Continue).to(AppStates.OpenEnrollmentClosed, new LaunchActivity(OpenEnrollmentClosedActivity.uiActivity));
        stateMachine.from(AppStates.DentalCoverage).on(Continue).to(AppStates.DentalCoverage, new LaunchActivity(OpenEnrollmentClosedActivity.uiActivity));
        stateMachine.from(AppStates.CoverageNextYear).on(Yes).to(AppStates.MobilePassword, new LaunchActivity(MobilePasswordActivity.uiActivity));
        stateMachine.from(AppStates.CoverageNextYear).on(No).to(AppStates.HelpPaying, new LaunchActivity(HelpPayingActivity.uiActivity));
        stateMachine.from(AppStates.MobilePassword).on(Ok).to(AppStates.ClearPIIMobilePassword, new BackgroundProcess(Events.ClearPIIRequest.class));
        stateMachine.from(AppStates.ClearPIIMobilePassword)
            .on(ClearedPII)
                .to(AppStates.AcctCreate, new InitAndLaunchActivity(AcctCreate.uiActivity, new InitAndLaunchActivity.InitEventParameter() {
                    @Override
                    public void init(EventParameters eventParameters) {
                        eventParameters.add("Account", RidpService.getNewAccount());
                    }
                }));


        stateMachine.from(AppStates.MobilePassword).on(Cancel).to(AppStates.Hello, new LaunchActivity(HelloActivity.uiActivity));
        stateMachine.from(AppStates.ResumeApplication).on(AppEvents.ForgotPassword).to(AppStates.AcctNewPassword, new LaunchActivity(AcctCreateNewPassword.uiActivity));
        stateMachine.from(AppStates.ResumeApplication).on(AppEvents.ResumeApplication).to(AppStates.LoggingIn, new StateManager.BackgroundProcess(Events.IvlLoginRequest.class));
        stateMachine.from(AppStates.LoggingIn).on(AppEvents.IndividualLoggedIn).to(AppStates.GettingEffectiveDate, new StateManager.BackgroundProcess(Events.GetEffectiveDate.class));
        stateMachine.from(AppStates.GettingEffectiveDate).on(AppEvents.ReceivedEffectiveDate).to(AppStates.GettingStatus, new StateManager.BackgroundProcess(Events.ResumeApplication.class));
        stateMachine.from(AppStates.GettingStatus).on(AppEvents.StatusAppliedUqhp).to(AppStates.GettingUqhpDetermination, new StateManager.BackgroundProcess(Events.GetUqhpDeterminationFromServer.class));

        stateMachine.from(AppStates.GettingUqhpDetermination)
                .on(AppEvents.ReceivedUqhpDeterminationHasIneligible)
                .to(AppStates.Ineligible, new LaunchActivity(IneligibleResultsActivity.uiActivity));
        stateMachine.from(AppStates.GettingUqhpDetermination).on(ReceivedUqhpDeterminationOnlyEligible).to(AppStates.Eligible, new LaunchActivity(EligibleResultsActivity.uiActivity));


        stateMachine.from(AppStates.ResumingAppliedUqhp).on(AppEvents.InOpenEnrollment).to(AppStates.ResumingAppliedUqhp, new StateManager.BackgroundProcess(Events.CheckOpenEnrollment.class));
        stateMachine.from(AppStates.ResumingAppliedUqhp).on(AppEvents.OpenEnrollmentClosed).to(AppStates.ResumingAppliedUqhp, new StateManager.BackgroundProcess(Events.CheckOpenEnrollment.class));
        stateMachine.from(AppStates.GettingStatus).on(AppEvents.StatusEnrollingUqhp).to(AppStates.ThanksApplication, new LaunchActivity(ThanksApplicationActivity.uiActivity));
        stateMachine.from(AppStates.GettingStatus).on(AppEvents.StatusEnrolled).to(AppStates.Congrats, new LaunchActivity(CongratsActivity.uiActivity));
        stateMachine.from(AppStates.GettingStatus).on(AppEvents.StatusApplying).to(AppStates.ResumingApplying, new StateManager.BackgroundProcess(Events.CheckOpenEnrollment.class));
        stateMachine.from(AppStates.ResumingApplying).on(AppEvents.InOpenEnrollment).to(AppStates.FamilyMembers, new LaunchActivity(org.dchbx.coveragehq.financialeligibility.FamilyActivity.uiActivity));
        stateMachine.from(AppStates.ResumingApplying).on(AppEvents.OpenEnrollmentClosed).to(AppStates.OpenEnrollmentClosed, new LaunchActivity(OpenEnrollmentClosedActivity.uiActivity));
    }

    private void initUQHPStates(StateMachine stateMachine) {
        stateMachine.from(AppStates.PlanShoppingFamilyMembers).on(AppEvents.Continue).to(AppStates.FamilyRelationships, new LaunchActivity(FamilyRelationshipsActivity.uiActivity));
        //stateMachine.from(AppStates.FamilyRelationships).on(AppEvents.Continue).to(AppStates.UqhpConfirm, new LaunchActivity(UqhpConfirm.uiActivity));
        stateMachine.from(AppStates.UqhpConfirm).on(AppEvents.Continue).to(AppStates.UqhpConfirm, new LaunchActivity(LoginActivity.uiActivity));
    }

    private void initWalletStates(StateMachine stateMachine) {

    }


    private void initBasicPlanShoppingStates(StateMachine stateMachine){
        stateMachine.from(AppStates.AcctCreate).on(AppEvents.Skip).to(AppStates.PlanShoppingFamilyMembers, new LaunchActivity(FamilyActivity.uiActivity));
        stateMachine.from(AppStates.PlanShoppingFamilyMembers).on(AppEvents.Continue).to(AppStates.PlanShoppingPremiumAndDeductible, new LaunchActivity(PremiumAndDeductibleActivity.uiActivity));
        stateMachine.from(AppStates.PlanShoppingFamilyMembers).on(AppEvents.PlanShoppingEditFamilyMember).to(AppStates.FinancialAssitanceQuestions, new LaunchActivity(EditPersonActivity.uiActivity));
        stateMachine.from(AppStates.PlanShoppingPremiumAndDeductible).on(AppEvents.SeePlans).to(AppStates.PlanSelector, new LaunchActivity(PlanSelector.uiActivity));
        stateMachine.from(AppStates.PlanSelector).on(AppEvents.ShowPlanDetails).to(AppStates.PlanShoppingDetails, new LaunchActivity(PlanDetailsActivity.uiActivity));
        stateMachine.from(AppStates.PlanSelector).on(AppEvents.BuyPlan).to(AppStates.SelectedPlan, new LaunchActivity(SelectedPlanActivity.uiActivity));
        stateMachine.from(AppStates.SelectedPlan).on(AppEvents.BuyPlanConfirmed).to(AppStates.LastStep, new LaunchActivity(SubmitApplicationActivity.uiActivity));
        stateMachine.from(AppStates.LastStep).on(AppEvents.SubmitMyApplicationClicked).to(AppStates.SubmittingApplication, new BackgroundProcess(Events.SubmitApplication.class));
        stateMachine.from(AppStates.SubmittingApplication).on(AppEvents.ChoosePlanSucessful).to(AppStates.ThanksApplication, new LaunchActivity(ThanksApplicationActivity.uiActivity));
        stateMachine.from(AppStates.ApplicationSubmitted).on(AppEvents.ChoosePlanSucessful).to(AppStates.Hello, new LaunchActivity(ThanksApplicationActivity.uiActivity));
        stateMachine.from(AppStates.ThanksApplication).on(AppEvents.CheckStatusNow).to(AppStates.GettingStatus, new StateManager.BackgroundProcess(Events.ResumeApplication.class));
        stateMachine.from(AppStates.ThanksApplication).on(AppEvents.ComeBackLater).to(AppStates.Hello, new LaunchActivity(HelloActivity.uiActivity));
    }


    private void initRidpStates(StateMachine stateMachine) {

        // Auth for new account.

        stateMachine.from(AppStates.AcctCreate).on(AppEvents.Continue).to(AppStates.AcctPreAuth, new LaunchActivity(AcctPreAuthActivity.uiActivity));
        stateMachine.from(AppStates.AcctPreAuth).on(AppEvents.Continue).to(AppStates.AcctAddress, new LaunchActivity(AcctAddress.uiActivity));
        stateMachine.from(AppStates.AcctAddress).on(AppEvents.Continue).to(AppStates.AcctPhone, new LaunchActivity(AcctPhone.uiActivity));
        stateMachine.from(AppStates.AcctPhone).on(AppEvents.Continue).to(AppStates.AcctGender, new LaunchActivity(AcctGenderActivity.uiActivity));
        stateMachine.from(AppStates.AcctGender).on(AppEvents.Continue).to(AppStates.AcctDateOfBirth, new LaunchActivity(AcctDateOfBirth.uiActivity));
        stateMachine.from(AppStates.AcctDateOfBirth).on(AppEvents.Continue).to(AppStates.AcctSsn, new LaunchActivity(AcctSsn.uiActivity));
        stateMachine.from(AppStates.AcctSsn).on(AppEvents.Continue).to(AppStates.AcctAuthConsent, new LaunchActivity(AcctAuthConsent.uiActivity));
        stateMachine.from(AppStates.AcctAuthConsent).on(AppEvents.ConsentGiven).to(AppStates.GetQuestions, new BackgroundProcess(Events.GetRidpQuestions.class));
        stateMachine.from(AppStates.AcctAuthConsent).on(AppEvents.ConsentDenied).to(AppStates.Login, new LaunchActivity(AcctSystemFoundYouAceds.uiActivity));
        stateMachine.from(AppStates.GetQuestions).on(AppEvents.RidpUserNotFound).to(AppStates.AcctRidpUserNotFound, new PopAndLaunchActivity(AcctRidpUserNotFound.uiActivity));
        stateMachine.from(AppStates.GetQuestions).on(AppEvents.RidpConnectionFailure).to(AppStates.AcctRidpConnectionFailure, new PopAndLaunchActivity(AcctRidpConnectionFailure.uiActivity));
        stateMachine.from(AppStates.GetQuestions).on(AppEvents.GetQuestionsOperationComplete).to(AppStates.RidpQuestions, new LaunchActivity(RidpQuestionsActivity.uiActivity));
        stateMachine.from(AppStates.RidpQuestions).on(AppEvents.Continue).to(AppStates.VerifyingUser, new BackgroundProcess(Events.VerifyUser.class));
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.UserVerifiedFoundYou).to(AppStates.AcctSystemFoundYou, new LaunchActivity(AcctSystemFoundYou.uiActivity));
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.UserVerifiedSsnWithEmployer).to(AppStates.AcctSsnWithEmployer, new LaunchActivity(AcctSsnWithEmployer.uiActivity));
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.UserVerifiedOkToCreate).to(AppStates.CreatingAccount, new BackgroundProcess(Events.CreateAccount.class));
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.RidpConnectionFailure).to(AppStates.AcctRidpConnectionFailure, new PopAndLaunchActivity(AcctRidpConnectionFailure.uiActivity));
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.RidpWrongAnswersRecoverable).to(AppStates.AcctRidpWrongAnswersRecoverable, new PopAndLaunchActivity(AcctRidpWrongAnswersRecoverable.uiActivity));
        stateMachine.from(AppStates.VerifyingUser).on(AppEvents.RidpWrongAnswersLockout).to(AppStates.AcctRidpWrongAnswersLockout, new PopAndLaunchActivity(AcctRidpWrongAnswersLockout.uiActivity));

        stateMachine.from(AppStates.CreatingAccount).on(AppEvents.SignUpUserInAceds).to(AppStates.AcctSystemFoundYouInCuramAceds, new LaunchActivity(AcctSystemFoundYouAceds.uiActivity));
        stateMachine.from(AppStates.CreatingAccount).on(AppEvents.SignUpSuccessful).to(AppStates.GettingStatus, new StateManager.BackgroundProcess(Events.ResumeApplication.class));
        stateMachine.from(AppStates.CreatingAccount).on(AppEvents.SignUpSuccessful).to(AppStates.FamilyMembers, new PopAndLaunchActivity(org.dchbx.coveragehq.financialeligibility.FamilyActivity.uiActivity));

        stateMachine.from(AppStates.AcctRidpUserNotFound).on(AppEvents.ReviewRidpResponses).to(AppStates.AcctCreate, new PopAndLaunchActivity(AcctCreate.uiActivity));
        stateMachine.from(AppStates.AcctRidpUserNotFound).on(AppEvents.Close).to(AppStates.AcctRidpClosing, new StateManager.BackgroundProcess(Events.ClearPIIRequest.class));
        stateMachine.from(AppStates.AcctRidpClosing).on(AppEvents.ClearedPII).to(AppStates.Hello, new PopAndLaunchActivity(HelloActivity.uiActivity));

        stateMachine.from(AppStates.AcctRidpConnectionFailure).on(AppEvents.Close).to(AppStates.AcctRidpClosing, new StateManager.BackgroundProcess(Events.ClearPIIRequest.class));
        stateMachine.from(AppStates.AcctRidpClosing).on(AppEvents.ClearedPII).to(AppStates.Hello, new PopAndLaunchActivity(HelloActivity.uiActivity));

        stateMachine.from(AppStates.AcctRidpWrongAnswersRecoverable).on(AppEvents.RidpCheckOverride).to(AppStates.AcctRidpCheckingOverride, new StateManager.BackgroundProcess(Events.RidpCheckOverride.class));
        stateMachine.from(AppStates.AcctRidpWrongAnswersRecoverable).on(AppEvents.Close).to(AppStates.AcctRidpClosing, new StateManager.BackgroundProcess(Events.ClearPIIRequest.class));
        stateMachine.from(AppStates.AcctRidpClosing).on(AppEvents.ClearedPII).to(AppStates.Hello, new PopAndLaunchActivity(HelloActivity.uiActivity));

        stateMachine.from(AppStates.AcctRidpCheckingOverride).on(AppEvents.UserVerifiedFoundYou).to(AppStates.AcctSystemFoundYou, new LaunchActivity(AcctSystemFoundYou.uiActivity));
        stateMachine.from(AppStates.AcctRidpCheckingOverride).on(AppEvents.UserVerifiedSsnWithEmployer).to(AppStates.AcctSsnWithEmployer, new LaunchActivity(AcctSsnWithEmployer.uiActivity));
        stateMachine.from(AppStates.AcctRidpCheckingOverride).on(AppEvents.UserVerifiedOkToCreate).to(AppStates.CreatingAccount, new BackgroundProcess(Events.CreateAccount.class));
        stateMachine.from(AppStates.AcctRidpCheckingOverride).on(AppEvents.RidpConnectionFailure).to(AppStates.AcctRidpConnectionFailure, new PopAndLaunchActivity(AcctRidpConnectionFailure.uiActivity));
        stateMachine.from(AppStates.AcctRidpCheckingOverride).on(AppEvents.RidpWrongAnswersLockout).to(AppStates.AcctRidpWrongAnswersLockout, new PopAndLaunchActivity(AcctRidpWrongAnswersLockout.uiActivity));

        stateMachine.from(AppStates.AcctRidpWrongAnswersLockout).on(AppEvents.Close).to(AppStates.AcctRidpClosing, new StateManager.BackgroundProcess(Events.ClearPIIRequest.class));

        // Auth for new password.

        stateMachine.from(AppStates.AcctNewPassword).on(AppEvents.Continue).to(AppStates.AcctPreAuth, new LaunchActivity(AcctPreAuthActivity.uiActivity));

        stateMachine.from(AppStates.FamilyMembers).on(AppEvents.EditFamilyMember).to(AppStates.FinancialAssitanceQuestions, new LaunchActivity(EditPersonActivity.uiActivity));
        stateMachine.from(AppStates.FamilyMembers).on(AppEvents.ContinueSingleMemberFamily).to(AppStates.Attestation, new LaunchActivity(AttestationActivity.uiActivity));
        stateMachine.from(AppStates.FamilyMembers).on(AppEvents.ContinueMultipleMemberFamily).to(AppStates.FamilyRelationships, new LaunchActivity(RelationshipsActivity.uiActivity));
        stateMachine.from(AppStates.FinancialAssitanceQuestions).on(AppEvents.ShowDropDown).to(AppStates.FeDropDown, new LaunchActivity(CheckedListDialog.uiActivity));
        stateMachine.from(AppStates.FinancialAssitanceQuestions).on(AppEvents.UserSaved).doThis(new Back());
        stateMachine.from(AppStates.FinancialAssitanceQuestions).on(AppEvents.OpenSection).to(AppStates.SectionQuestions, new LaunchActivity(SectionActivity.uiActivity));
        stateMachine.from(AppStates.SectionQuestions).on(AppEvents.ShowDropDown).to(AppStates.FeDropDown, new LaunchActivity(CheckedListDialog.uiActivity));
        stateMachine.from(AppStates.SectionQuestions).on(AppEvents.UserSaved).doThis(new Back());
        stateMachine.from(AppStates.SectionQuestions).on(AppEvents.OpenSection).to(AppStates.SectionQuestions, new LaunchActivity(SectionActivity.uiActivity));
        stateMachine.from(AppStates.FeDropDown).on(AppEvents.DropdownSaved).doThis(new Back());
        stateMachine.from(AppStates.FamilyRelationships).on(AppEvents.EditRelationship).to(AppStates.EditFamilyRelationShip, new LaunchActivity(EditRelationshipActivity.uiActivity));
        stateMachine.from(AppStates.FamilyRelationships).on(AppEvents.Continue).to(AppStates.Attestation , new LaunchActivity(AttestationActivity.uiActivity));
        stateMachine.from(AppStates.EditFamilyRelationShip).on(AppEvents.ShowDropDown).to(AppStates.FeDropDown, new LaunchActivity(CheckedListDialog.uiActivity));
        stateMachine.from(AppStates.EditFamilyRelationShip).on(AppEvents.UserSaved).doThis(new Back());
        stateMachine.from(AppStates.Attestation).on(AppEvents.Continue).to(AppStates.UqhpDetermination, new BackgroundProcess(Events.SendHavenApplication.class));
        stateMachine.from(AppStates.UqhpDetermination)
            .on(AppEvents.ReceivedUqhpDeterminationHasIneligible)
                .to(AppStates.Ineligible, new LaunchActivity(IneligibleResultsActivity.uiActivity));
        stateMachine.from(AppStates.UqhpDetermination).on(ReceivedUqhpDeterminationOnlyEligible).to(AppStates.Eligible, new LaunchActivity(EligibleResultsActivity.uiActivity));
        stateMachine.from(AppStates.Ineligible).on(AppEvents.ShowEligible).to(AppStates.Eligible, new LaunchActivity(EligibleResultsActivity.uiActivity));
        stateMachine.from(AppStates.Eligible).on(AppEvents.ShowIneligible).to(AppStates.Ineligible, new LaunchActivity(IneligibleResultsActivity.uiActivity));
        stateMachine.from(AppStates.Eligible).on(AppEvents.ChoosePlan).to(AppStates.GettingPlans, new BackgroundProcess(Events.GetPlans.class));
        stateMachine.from(AppStates.GettingPlans).on(AppEvents.GotPlans).to(AppStates.PremiumAndDeductible, new LaunchActivity(PremiumAndDeductibleActivity.uiActivity));
        stateMachine.from(AppStates.PremiumAndDeductible).on(SeePlans).to(AppStates.PlanSelector, new LaunchActivity(PlanSelector.uiActivity));
        stateMachine.from(AppStates.AcctSystemFoundYou).on(AppEvents.ShowLogin).to(AppStates.AcctSystemFoundYouReturningToLogin, new StateManager.BackgroundProcess(Events.ClearPIIRequest.class));
        stateMachine.from(AppStates.AcctSystemFoundYouReturningToLogin).on(AppEvents.ClearedPII).to(AppStates.Login, new PopAndLaunchActivity(LoginActivity.uiActivity));
        stateMachine.from(AppStates.AcctSystemFoundYou).on(AppEvents.SignUpIndividual).to(AppStates.AcctCreate, new PopAndLaunchActivity(AcctCreate.uiActivity));
        stateMachine.from(AppStates.AcctSystemFoundYou).on(AppEvents.Close).to(AppStates.AcctSystemFoundYouClosing, new StateManager.BackgroundProcess(Events.ClearPIIRequest.class));
        stateMachine.from(AppStates.AcctSystemFoundYouClosing).on(AppEvents.ClearedPII).to(AppStates.Hello, new PopAndLaunchActivity(HelloActivity.uiActivity));
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
            Intents.launchActivity(info.cls, brokerActivity, new EventParameters());
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

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public class BackgroundProcess extends StateMachineAction {
        private final Class c;

        public BackgroundProcess(Class c){
            this.c = c;
        }

        @Override
        public boolean call(StateMachine stateMachine, StateManager stateManager, AppEvents event,
                            AppStates leavingState, AppStates enterState,
                            EventParameters intentParameters) throws IOException, CoverageException {
            stateMachine.push(new WaitActivityInfo<AppEvents, AppStates>(enterState, event, null));
            stateManager.showWait();
            try {
                Object o = c.newInstance();
                Events.BackgroundProcess eBP = (Events.BackgroundProcess) o;
                eBP.setEventParameters(intentParameters);
                stateManager.messages.sendBackgroundProcess(eBP);
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

    public interface PopulateEventParameters{
        void fill(EventParameters eventParameters);
    }
}
