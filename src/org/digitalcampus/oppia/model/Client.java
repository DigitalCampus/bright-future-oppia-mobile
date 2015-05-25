package org.digitalcampus.oppia.model;

import java.io.Serializable;

public class Client implements Serializable, SearchOutput{

    private long clientId;

    private String clientName;
    private long clientMobileNumber;
    private String clientGender;
    private String clientMaritalStatus;
    private int clientAge;
    private String clientParity;
    private String clientLifeStage;
    private int ageYoungestChild; //  take the input in no. of months
    private String husbandName;
    private String methodName;

    private String healthWorker; // User or ASHA worker
    private long clientServerId;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getAgeYoungestChild() {
        return ageYoungestChild;
    }

    public void setAgeYoungestChild(int ageYoungestChild) {
        this.ageYoungestChild = ageYoungestChild;
    }

    public String getHusbandName() {
        return husbandName;
    }

    public void setHusbandName(String husbandName) {
        this.husbandName = husbandName;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public long getClientMobileNumber() {
        return clientMobileNumber;
    }

    public void setClientMobileNumber(long clientMobileNumber) {
        this.clientMobileNumber = clientMobileNumber;
    }

    public String getClientGender() {
        return clientGender;
    }

    public void setClientGender(String clientGender) {
        this.clientGender = clientGender;
    }

    public String getClientMaritalStatus() {
        return clientMaritalStatus;
    }

    public void setClientMaritalStatus(String clientMaritalStatus) {
        this.clientMaritalStatus = clientMaritalStatus;
    }

    public int getClientAge() {
        return clientAge;
    }

    public void setClientAge(int clientAge) {
        this.clientAge = clientAge;
    }

    public String getClientParity() {
        return clientParity;
    }

    public void setClientParity(String clientParity) {
        this.clientParity = clientParity;
    }

    public String getClientLifeStage() {
        return clientLifeStage;
    }

    public void setClientLifeStage(String clientLifeStage) {
        this.clientLifeStage = clientLifeStage;
    }

    public String getHealthWorker() {
        return healthWorker;
    }

    public void setHealthWorker(String healthWorker) {
        this.healthWorker = healthWorker;
    }

    public long getClientServerId() {
        return clientServerId;
    }

    public void setClientServerId(long clientServerId) {
        this.clientServerId = clientServerId;
    }
}