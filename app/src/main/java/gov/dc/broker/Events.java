package gov.dc.broker;

import android.widget.ImageView;

/**
 * Created by plast on 7/26/2016.
 */
public class Events {
    static int lastCancelableRequestId = 0;

    static public class RequestError {

    }

    static public class CancelRequest {
        private int id;

        public CancelRequest(int id){
            this.id = id;
        }
    }

    static public class CancelableRequest {
        private int id;

        public int getId (){
            return id;
        }
    }

    static public class LoginRequest extends CancelableRequest{
        private String accountName;
        private String password;

        public LoginRequest(String accountName, String password){
            this.accountName = accountName;
            this.password = password;
        }

        public String getAccountName() {
            return accountName;
        }

        public String getPassword() {
            return password;
        }
    }

    static public class GetAccount extends CancelableRequest {

    }

    static public class GetEmployerList extends CancelableRequest {

    }

    static public class GetEmployer extends CancelableRequest {
        private int id;

        public GetEmployer(int id){
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    static public class GetCarrierImage extends CancelableRequest {
        private String url;
        private ImageView imageView;

        public GetCarrierImage(String url, ImageView imageView){
            this.url = url;
            this.imageView = imageView;
        }


    }

    static public class GetCarriers extends CancelableRequest{
        // https://dchealthlink.com/shared/json/carriers.json
    }

    static public class ResponseToRequest{
        private int id;

        ResponseToRequest(int id){
            this.id = id;
        }

        public int getId(){
            return id;
        }
    }

    static public class Account extends ResponseToRequest {
        private gov.dc.broker.Account account;

        Account(int id) {
            super(id);
        }

        public gov.dc.broker.Account getAccount() {
            return account;
        }

        public void setAccount(gov.dc.broker.Account account) {
            this.account = account;
        }
    }

    static public class EmployerList extends ResponseToRequest{
        private gov.dc.broker.EmployerList employerList;

        public EmployerList(int id, gov.dc.broker.EmployerList employerList) {
            super(id);
            this.employerList = employerList;
        }

        public gov.dc.broker.EmployerList getEmployerList() {
            return employerList;
        }

        public void setEmployerList(gov.dc.broker.EmployerList employerList) {
            this.employerList = employerList;
        }
    }

    static public class BrokerClient extends ResponseToRequest{
        private final gov.dc.broker.BrokerClient brokerClient;
        private final gov.dc.broker.BrokerClientDetails brokerClientDetails;

        public BrokerClient(int id, gov.dc.broker.BrokerClient brokerClient,
                            gov.dc.broker.BrokerClientDetails brokerClientDetails) {
            super(id);
            this.brokerClient = brokerClient;
            this.brokerClientDetails = brokerClientDetails;
        }

        public gov.dc.broker.BrokerClient getBrokerClient() {
            return brokerClient;
        }

        public BrokerClientDetails getBrokerClientDetails() {
            return brokerClientDetails;
        }
    }

    static public class Carriers extends ResponseToRequest {
        private final gov.dc.broker.Carriers carriers;

        Carriers(int id, gov.dc.broker.Carriers carriers) {
            super(id);
            this.carriers = carriers;
        }

        public gov.dc.broker.Carriers getCarriers() {
            return carriers;
        }
    }
}
