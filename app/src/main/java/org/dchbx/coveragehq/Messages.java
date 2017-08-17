package org.dchbx.coveragehq;

import android.net.Uri;

import org.dchbx.coveragehq.models.fe.FinancialAssistanceApplication;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.models.ridp.Answers;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.LocalDate;

import java.util.HashMap;

public interface Messages {
    void release();

    EventBus getEventBus();

    void getEmployer();
    int getEmployer(String employerId);
    void getLogin();
    void securityAnswer(String securityAnswer);
    void loginRequest(Events.LoginRequest loginRequest);
    void getBrokerAgency();
    void logoutRequest();
    void logoutRequest(boolean clearAccount);
    void getRoster();
    void getRoster(String employerId);
    void getEmployee(String employeeId, String employerId);
    void getInsured();
    void coverageYearChanged(LocalDate coverageYear);
    void getGitAccounts(String s);
    void startSessioniTimeoutCountdown();
    void sessionTimeoutCountdownTick(int secondsLeft);
    void sessionAboutToTimeout();
    void sessionTimedout();
    void stayLoggedIn();
    void getFingerprintStatus();
    void relogin(String account, String password);
    void validateLogin();
    void decryptAccountAndPassword();
    void EmployerActivityReady();
    void testTimeOut();
    void getCarriers();
    void getUserEmployee();
    void capturePhoto(boolean front);
    void updateInsurancyCard();
    void moveImageToData(boolean frontOfCard, Uri uri);
    void removeInsuraceCardImage(boolean front);
    void getInsuredAndBenefits(LocalDate currentDate);
    void getInsuredAndServices(LocalDate currentDate);
    void signUp();
    void getPlanShopping();
    void resetPlanShopping();
    void updatePlanShopping(PlanShoppingParameters planShoppingParameters);
    void getPlans();
    void updateFilters(int premiumFilter, int deductibleFilter);
    void getPlan(String planId);
    void getAppConfig();
    void updateAppConfig(ServiceManager.AppConfig appConfig);
    void updateInsuredFragment(LocalDate currentDate);
    void getPlan(String planId, boolean getSummaryAndBenefits);
    void getRidpQuestions();
    void buttonClicked(StateManager.AppEvents appEvent);
    void accountButtonClicked(StateManager.AppEvents appEvent, org.dchbx.coveragehq.models.account.Account account, Answers usersAnswers);
    void getCreateAccountInfo();
    void signUp(String planShoppingPath);
    void updateAnswers(Answers usersAnswers);
    void getVerificationResponse();
    void getCurrentActivity();
    void stateAction(Events.StateAction.Action action, int id, EventParameters eventParameters);
    void stateAction(Events.StateAction.Action action, int id);
    void stateAction(Events.StateAction.Action action);
    void error(String str1, String str2);
    void appEvent(StateManager.AppEvents event, EventParameters intentParameters);
    void appEvent(StateManager.AppEvents event);
    void createAccount();
    void verifyUser();
    void getGlossary();
    void getGlossaryItem(String name);
    void getFinancialEligibilityJson();
    void getFinancialEligibilityJsonResponse(Schema schema);
    void getFinancialAssistanceApplication();
    void getFinancialAssistanceApplicationResponse(FinancialAssistanceApplication financialAssistanceApplication);
    void getFinancialApplicationPerson(String eapersonid);
    void getFinancialApplicationPersonResponse(HashMap<String, Object> personHashMap);
}

