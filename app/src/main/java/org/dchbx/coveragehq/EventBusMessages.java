package org.dchbx.coveragehq;

import android.net.Uri;
import android.util.Log;

import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.models.fe.FinancialAssistanceApplication;
import org.dchbx.coveragehq.models.fe.Schema;
import org.dchbx.coveragehq.models.ridp.Answers;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.LocalDate;

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

public class EventBusMessages implements Messages {
    private Object object;
    private EventBus eventBus;

    public EventBusMessages(Object object){
        this.object = object;
        eventBus = EventBus.getDefault();
        try {
            eventBus.register(object);
        } catch (Throwable e){
            Log.d("EventBusMessages", "exception initializing: " + e.getMessage());
        }
    }

    @Override
    public EventBus getEventBus(){
        return eventBus;
    }

    @Override
    public int getEmployer(String employerId) {
        Events.GetEmployer getEmployer = new Events.GetEmployer(employerId);
        eventBus.post(getEmployer);
        return getEmployer.getId();
    }

    @Override
    public void getEmployer() {
        eventBus.post(new Events.GetEmployer(null));
    }

    @Override
    public void getLogin() {
        eventBus.post(new Events.GetLogin());
    }

    @Override
    public void securityAnswer(String securityAnswer) {
        eventBus.post(new Events.SecurityAnswer(securityAnswer));
    }

    @Override
    public void loginRequest(Events.LoginRequest loginRequest) {
        eventBus.post(loginRequest);
    }

    @Override
    public void getBrokerAgency() {
        eventBus.post(new Events.GetBrokerAgency());
    }

    @Override
    public void logoutRequest() {
        logoutRequest(true);
    }

    @Override
    public void logoutRequest(boolean clearAccount) {
        eventBus.post(new Events.LogoutRequest(clearAccount));
    }

    @Override
    public void release() {
        eventBus.unregister(object);
        eventBus = null;
    }

    @Override
    public void getRoster(String employerId) {
        eventBus.post(new Events.GetRoster(employerId));
    }

    @Override
    public void getRoster() {
        eventBus.post(new Events.GetRoster(null));
    }

    @Override
    public void getEmployee(String employeeId, String employerId) {
        eventBus.post(new Events.GetEmployee(employeeId, employerId));
    }

    @Override
    public void getInsured() {
        eventBus.post(new Events.GetEmployee(null, null));
    }

    @Override
    public void coverageYearChanged(LocalDate year) {
        eventBus.post(new Events.CoverageYear(year));
    }

    @Override
    public void getGitAccounts(String s) {
        eventBus.post(new Events.GetGitAccounts(s));
    }

    @Override
    public void startSessioniTimeoutCountdown() {
        eventBus.post(new Events.StartSessionTimeout());
    }

    @Override
    public void sessionTimeoutCountdownTick(int secondsLeft) {
        eventBus.post(new Events.SessionTimeoutCountdownTick(secondsLeft));
    }

    @Override
    public void sessionAboutToTimeout() {
        eventBus.post(new Events.SessionAboutToTimeout());
    }

    @Override
    public void sessionTimedout() {
        eventBus.post(new Events.SessionTimedOut());
    }

    @Override
    public void stayLoggedIn() {
        eventBus.post(new Events.StayLoggedIn());
    }

    @Override
    public void getFingerprintStatus() {
        eventBus.post(new Events.GetFingerprintStatus());
    }

    @Override
    public void relogin(String account, String password) {
        eventBus.post(new Events.Relogin(account, password));
    }

    @Override
    public void validateLogin(){
        eventBus.post(new Events.AuthenticateFingerprintEncrypt());
    }

    @Override
    public void decryptAccountAndPassword() {
        eventBus.post(new Events.AuthenticateFingerprintDecrypt());
    }

    @Override
    public void EmployerActivityReady() {
        eventBus.postSticky(new Events.EmployerActivityReady());
    }

    @Override
    public void testTimeOut() {
        eventBus.post(new Events.TestTimeout());
    }

    @Override
    public void getCarriers() {
        eventBus.post(new Events.GetCarriers());
    }

    @Override
    public void getUserEmployee() {
        eventBus.postSticky(new Events.GetUserEmployee());
    }

    @Override
    public void capturePhoto(boolean front) {
        eventBus.post(new Events.CapturePhoto(front));
    }

    @Override
    public void updateInsurancyCard() {
        eventBus.postSticky(new Events.UpdateInsuranceCard());
    }

    @Override
    public void moveImageToData(boolean frontOfCard, Uri uri) {
        eventBus.post(new Events.MoveImageToData(frontOfCard, uri));
    }

    @Override
    public void removeInsuraceCardImage(boolean front) {
        eventBus.post(new Events.RemoveInsuraceCardImage(front));
    }

    @Override
    public void getInsuredAndBenefits(LocalDate currentDate) {
        eventBus.post(new Events.GetInsuredAndBenefits(currentDate));
    }

    @Override
    public void getInsuredAndServices(LocalDate currentDate) {
        eventBus.post(new Events.GetInsuredAndServices(currentDate));
    }

    @Override
    public void signUp() {
        eventBus.post(new Events.SignUp(null));
    }

    @Override
    public void signUp(String planShoppingPath) {
        eventBus.post(new Events.SignUp(planShoppingPath));
    }

    @Override
    public void updateAnswers(Answers answers) {
        eventBus.post(new Events.UpdateAnswers(answers));
    }

    @Override
    public void getVerificationResponse() {
        eventBus.post(new Events.GetVerificationResponse());
    }

    @Override
    public void getCurrentActivity() {
        eventBus.post(new Events.GetCurrentActivity());
    }

    @Override
    public void stateAction(Events.StateAction.Action action, int id, EventParameters eventParameters) {
        eventBus.post(new Events.StateAction(action, id, eventParameters));
    }

    @Override
    public void stateAction(Events.StateAction.Action action, int id) {
        eventBus.post(new Events.StateAction(action, id, null));
    }

    @Override
    public void stateAction(Events.StateAction.Action action) {
        stateAction(action, 0);
    }

    @Override
    public void error(String str1, String str2) {
        BrokerWorker.eventBus.post(new Events.Error(str1, str2));
    }

    @Override
    public void appEvent(StateManager.AppEvents event, EventParameters intentParameters){
        eventBus.post(new Events.AppEvent(event, intentParameters));
    }

    @Override
    public void appEvent(StateManager.AppEvents event) {
        eventBus.post(new Events.AppEvent(event, null));
    }

    @Override
    public void createAccount() {
        eventBus.post(new Events.CreateAccount());
    }

    @Override
    public void verifyUser() {
        eventBus.post(new Events.VerifyUser());
    }

    @Override
    public void getPlanShopping() {
        eventBus.post(new Events.GetPlanShopping());
    }

    @Override
    public void resetPlanShopping() {
        eventBus.post(new Events.ResetPlanShopping());
    }

    @Override
    public void updatePlanShopping(PlanShoppingParameters planShoppingParameters) {
        eventBus.post(new Events.UpdatePlanShopping(planShoppingParameters));
    }

    @Override
    public void getPlans() {
        eventBus.post(new Events.GetPlans());
    }

    @Override
    public void updateFilters(int premiumFilter, int deductibleFilter) {
        eventBus.post(new Events.SetPlanFilter(premiumFilter, deductibleFilter));
    }

    @Override
    public void getPlan(String planId) {
        eventBus.post(new Events.GetPlan(planId, false));
    }

    @Override
    public void getAppConfig() {
        eventBus.post(new Events.GetAppConfig());
    }

    @Override
    public void updateAppConfig(ServiceManager.AppConfig appConfig) {
        eventBus.post(new Events.UpdateAppConfig(appConfig));
    }

    @Override
    public void updateInsuredFragment(LocalDate currentDate) {
        eventBus.post(new Events.EmployeeFragmentUpdate(null, currentDate));
    }

    @Override
    public void getPlan(String planId, boolean getSummaryAndBenefits) {
        eventBus.post(new Events.GetPlan(planId, true));
    }

    @Override
    public void getRidpQuestions() {
        eventBus.post(new Events.GetRidpQuestions());
    }

    @Override
    public void buttonClicked(StateManager.AppEvents appEvent) {
        eventBus.post(new Events.AppEvent(appEvent, null));
    }

    @Override
    public void accountButtonClicked(StateManager.AppEvents appEvent, Account account, Answers usersAnswers) {
        eventBus.post(new Events.AccountButtonClicked(appEvent, account, usersAnswers));
    }

    @Override
    public void getCreateAccountInfo() {
        eventBus.post(new Events.GetCreateAccountInfo());
    }

    @Override
    public void getGlossary(){
        eventBus.post(new Events.GetGlossary());
    }

    @Override
    public void getGlossaryItem(String name){
        eventBus.post(new Events.GetGlossaryItem(name));
    }

    @Override
    public void getFinancialEligibilityJson() {
        eventBus.post(new Events.GetFinancialEligibilityJson());

    }

    @Override
    public void getFinancialEligibilityJsonResponse(Schema schema) {
        eventBus.post(new Events.GetFinancialEligibilityJsonResponse(schema));
    }

    @Override
    public void getFinancialAssistanceApplication() {
        eventBus.post(new Events.GetFinancialAssistanceApplication());
    }

    @Override
    public void getFinancialAssistanceApplicationResponse(FinancialAssistanceApplication financialAssistanceApplication) {
        eventBus.post(new Events.GetFinancialAssistanceApplicationResponse(financialAssistanceApplication));
    }

    @Override
    public void getFinancialApplicationPerson(String eapersonid) {
        eventBus.post(new Events.GetApplicationPerson(eapersonid));
    }

    @Override
    public void getFinancialApplicationPersonResponse(HashMap<String, Object> person) {
        eventBus.post(new Events.GetApplicationPersonResponse(person));
    }
}

