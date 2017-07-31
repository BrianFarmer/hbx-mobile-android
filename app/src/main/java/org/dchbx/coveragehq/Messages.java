package org.dchbx.coveragehq;

import android.net.Uri;

import org.dchbx.coveragehq.models.ridp.Answers;
import org.joda.time.LocalDate;

public interface Messages {
    void release();

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
    void stateAction(Events.StateAction.Action launchActivity, int id);
    void stateAction(Events.StateAction.Action launchActivity);
    void error(String str1, String str2);
    void appEvent(StateManager.AppEvents event, String s);
    void appEvent(StateManager.AppEvents event);
    void createAccount();
    void verifyUser();
}

