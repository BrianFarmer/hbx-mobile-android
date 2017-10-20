package org.dchbx.coveragehq.planshopping;

import android.content.Intent;
import android.util.Log;

import com.google.gson.GsonBuilder;

import org.dchbx.coveragehq.BrokerApplication;
import org.dchbx.coveragehq.ConfigurationStorageHandler;
import org.dchbx.coveragehq.CoverageConnection;
import org.dchbx.coveragehq.DateTimeDeserializer;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.IConnectionHandler;
import org.dchbx.coveragehq.IServiceManager;
import org.dchbx.coveragehq.LocalDateSerializer;
import org.dchbx.coveragehq.LocalTimeDeserializer;
import org.dchbx.coveragehq.Messages;
import org.dchbx.coveragehq.UrlHandler;
import org.dchbx.coveragehq.financialeligibility.FinancialEligibilityService;
import org.dchbx.coveragehq.models.fe.PersonForCoverage;
import org.dchbx.coveragehq.models.fe.UqhpDetermination;
import org.dchbx.coveragehq.models.planshopping.Plan;
import org.dchbx.coveragehq.models.planshopping.PlanChoice;
import org.dchbx.coveragehq.models.planshopping.Plans;
import org.dchbx.coveragehq.models.services.Service;
import org.dchbx.coveragehq.models.startup.EffectiveDate;
import org.dchbx.coveragehq.statemachine.EventParameters;
import org.dchbx.coveragehq.statemachine.StateManager;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Years;

import java.util.ArrayList;
import java.util.List;

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
public class PlanShoppingService {
    private static String TAG = "PlanShoppingService";
    public static String Plan = "Plan";
    public static String GetPlansResult = "GetPlanResults";
    public static String PremiumFilter = "PremiumFilter";
    public static String DeductibleFilter = "DeductibleFilter";


    private final IServiceManager serviceManager;
    private Messages messages;
    private ArrayList<Plan> plans;
    private ArrayList<Service> services;

    public PlanShoppingService(IServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        messages = BrokerApplication.getBrokerApplication().getMessages(this);
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(Events.SubmitApplication submitApplication) {
        EventParameters eventParameters = submitApplication.getEventParameters();
        Plan plan = (org.dchbx.coveragehq.models.planshopping.Plan) eventParameters.getObject(Plan, org.dchbx.coveragehq.models.planshopping.Plan.class);
        FinancialEligibilityService feService = serviceManager.getFinancialEligibilityService();
        ConfigurationStorageHandler storageHandler = serviceManager.getConfigurationStorageHandler();
        UqhpDetermination uqhpDetermination = feService.getUqhpDetermination();

        PlanChoice planChoice = new PlanChoice();
        planChoice.applicants = uqhpDetermination.eligibleForQhp;
        planChoice.eaid = uqhpDetermination.eaid;
        planChoice.effective_date = storageHandler.readEffectiveDate().effectiveDate;
        planChoice.monthly_premium = plan.cost.monthlyPremium;
        planChoice.planHiosId = plan.hios.id;
        planChoice.planName = plan.name;


        UrlHandler.HttpRequest httpRequest = serviceManager.getUrlHandler().getPlanChoiceParameters(planChoice);
        try {
            serviceManager.getConnectionHandler().process(httpRequest, new IConnectionHandler.OnCompletion() {
                @Override
                public void onCompletion(IConnectionHandler.HttpResponse response) {
                    if (response.getResponseCode() >= 200
                        && response.getResponseCode() < 300){
                        Events.GetPlansResult getPlansResult = new Events.GetPlansResult(plans, -1, -1);
                        messages.appEvent(StateManager.AppEvents.ChoosePlanSucessful, EventParameters.build().add(GetPlansResult, getPlansResult));
                    } else {
                        messages.appEvent(StateManager.AppEvents.Error);
                    }
                }
            });
        } catch (Exception e) {
            messages.appEvent(StateManager.AppEvents.Error);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(Events.GetPlans getPlans){
        try {
            CoverageConnection coverageConnection = serviceManager.getCoverageConnection();

            ConfigurationStorageHandler configurationStorageHandler = serviceManager.getConfigurationStorageHandler();
            ArrayList<Integer> ages = new ArrayList<>();
            UqhpDetermination uqhpDetermination = configurationStorageHandler.readUqhpDetermination();
            LocalDate now = LocalDate.now();
            for (PersonForCoverage personForCoverage : uqhpDetermination.eligibleForQhp) {
                int years = Years.yearsBetween(now, personForCoverage.personDob).getYears();
                ages.add(years);
            }

            EffectiveDate effectiveDate = configurationStorageHandler.readEffectiveDate();
            final UrlHandler.HttpRequest getParameters = serviceManager.getUrlHandler().getPlansParameters(effectiveDate.effectiveDate, ages);
            serviceManager.getConnectionHandler().process(getParameters, new IConnectionHandler.OnCompletion() {
                @Override
                public void onCompletion(IConnectionHandler.HttpResponse response) {
                    if (response.getResponseCode() == 200){
                        List<Plan> plans = serviceManager.getParser().parsePlans(response.getBody());
                        Events.GetPlansResult getPlansResult = new Events.GetPlansResult(plans, -1, -1);
                        messages.appEvent(StateManager.AppEvents.GotPlans, EventParameters.build().add(GetPlansResult, getPlansResult));
                    } else {
                        messages.appEvent(StateManager.AppEvents.Error);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception getting plans: " + e.getMessage());
            messages.appEvent(StateManager.AppEvents.Error);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void doThis(Events.GetPlan getPlan) {
        try {
            final EventParameters eventParameters = getPlan.getEventParameters();
            Plans plans = (Plans) eventParameters.getObject("Plans", Plans.class);

            Plan plan = plans.getPlan(getPlan.getPlanId());

            List<Service> services = null;
            if (getPlan.isGetSummaryAndBenefits()){
                services = getSummaryForPlan(plan);
            }

            messages.getPlanResult(plan, services);
        } catch (Exception e) {
            // edit these exceptions since we are doing this asyncronously to the user.
        }
    }

    public List<Service> getSummaryForPlan(Plan plan) {
        DateTime now = DateTime.now();
        UrlHandler.HttpRequest request = serviceManager.getUrlHandler().getSummaryParameters(plan.links.servicesRates.substring(1));

        try {
            IConnectionHandler.HttpResponse response = serviceManager.getConnectionHandler().process(request);
            if (response.getResponseCode() == 200){
                return serviceManager.getParser().parseServices(response.getBody());
            }
        } catch (Exception e) {
            messages.appEvent(StateManager.AppEvents.Error);
        }
        return null;
    }

    /*
    public PlanShoppingParameters getPlanShoppingParameters(EventParameters eventParameters) {
        return serverConfiguration.planShoppingParameters;
    }

    public void updatePlanShopping(Events.UpdatePlanShopping updatePlanShopping) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, KeyStoreException, NoSuchProviderException, IllegalBlockSizeException {
        serverConfiguration.planShoppingParameters = updatePlanShopping.getPlanShoppingParameters();
        clearStorageHandler.store(serverConfiguration);
    }

    public void updatePlanFilters(double premiumFilter, double deductibleFilter) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, KeyStoreException, NoSuchProviderException, IllegalBlockSizeException {
        serverConfiguration.premiumFilter = premiumFilter;
        serverConfiguration.deductibleFilter = deductibleFilter;
        clearStorageHandler.store(serverConfiguration);
    }

    public double getPremiumFilter() {
        return serverConfiguration.premiumFilter;
    }

    public double getDeductibleFilter() {
        return serverConfiguration.deductibleFilter;
    }
    */

    public static Events.GetPlansResult getPlanResults(Intent intent) {
        String jsonString = intent.getStringExtra(GetPlansResult);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        gsonBuilder.registerTypeAdapter(LocalTime.class, new LocalTimeDeserializer());
        return gsonBuilder.create().fromJson(jsonString, Events.GetPlansResult.class);
    }

    public static double getPremiumFilterFromIntent(Intent intent) {
        return intent.getDoubleExtra(PremiumFilter, -1);
    }

    public static double getDeductibleFilterFromIntent(Intent intent) {
        return intent.getDoubleExtra(DeductibleFilter, -1);
    }

    public static Plan getPlanFromIntent(Intent intent) {
        String jsonString = intent.getStringExtra(Plan);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
        gsonBuilder.registerTypeAdapter(LocalTime.class, new LocalTimeDeserializer());
        return gsonBuilder.create().fromJson(jsonString, Plan.class);
    }
}
