package org.dchbx.coveragehq;

import android.net.Uri;

import org.dchbx.coveragehq.models.planshopping.PlanShoppingParameters;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.LocalDate;

/**
 * Created by plast on 10/27/2016.
 */

public class EventBusMessages implements Messages {
    private Object object;
    private EventBus eventBus;

    public EventBusMessages(Object object){
        this.object = object;
        eventBus = EventBus.getDefault();
        eventBus.register(object);
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
        eventBus.post(new Events.SignUp());
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
}

