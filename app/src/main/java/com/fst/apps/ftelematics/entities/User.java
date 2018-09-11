package com.fst.apps.ftelematics.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("accountID")
    @Expose
    private String accountID;
    @SerializedName("userID")
    @Expose
    private String userID;
    @SerializedName("userType")
    @Expose
    private Integer userType;
    @SerializedName("roleID")
    @Expose
    private String roleID;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("isSchool")
    @Expose
    private Integer isSchool;
    @SerializedName("gender")
    @Expose
    private Object gender;
    @SerializedName("contactName")
    @Expose
    private String contactName;
    @SerializedName("contactPhone")
    @Expose
    private String contactPhone;
    @SerializedName("contactEmail")
    @Expose
    private String contactEmail;
    @SerializedName("timeZone")
    @Expose
    private String timeZone;
    @SerializedName("firstLoginPageID")
    @Expose
    private Object firstLoginPageID;
    @SerializedName("preferredDeviceID")
    @Expose
    private Object preferredDeviceID;
    @SerializedName("passwdQueryTime")
    @Expose
    private Object passwdQueryTime;
    @SerializedName("lastLoginTime")
    @Expose
    private Object lastLoginTime;
    @SerializedName("isActive")
    @Expose
    private Integer isActive;
    @SerializedName("displayName")
    @Expose
    private Object displayName;
    @SerializedName("description")
    @Expose
    private Object description;
    @SerializedName("notes")
    @Expose
    private Object notes;
    @SerializedName("isMobAppUser")
    @Expose
    private String isMobAppUser;
    @SerializedName("lastUpdateTime")
    @Expose
    private String lastUpdateTime;
    @SerializedName("creationTime")
    @Expose
    private String creationTime;
    @SerializedName("dealer")
    @Expose
    private String dealer;
    @SerializedName("tz")
    @Expose
    private String tz;

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getRoleID() {
        return roleID;
    }

    public void setRoleID(String roleID) {
        this.roleID = roleID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getIsSchool() {
        return isSchool;
    }

    public void setIsSchool(Integer isSchool) {
        this.isSchool = isSchool;
    }

    public Object getGender() {
        return gender;
    }

    public void setGender(Object gender) {
        this.gender = gender;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Object getFirstLoginPageID() {
        return firstLoginPageID;
    }

    public void setFirstLoginPageID(Object firstLoginPageID) {
        this.firstLoginPageID = firstLoginPageID;
    }

    public Object getPreferredDeviceID() {
        return preferredDeviceID;
    }

    public void setPreferredDeviceID(Object preferredDeviceID) {
        this.preferredDeviceID = preferredDeviceID;
    }

    public Object getPasswdQueryTime() {
        return passwdQueryTime;
    }

    public void setPasswdQueryTime(Object passwdQueryTime) {
        this.passwdQueryTime = passwdQueryTime;
    }

    public Object getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Object lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public Object getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Object displayName) {
        this.displayName = displayName;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Object getNotes() {
        return notes;
    }

    public void setNotes(Object notes) {
        this.notes = notes;
    }

    public String getIsMobAppUser() {
        return isMobAppUser;
    }

    public void setIsMobAppUser(String isMobAppUser) {
        this.isMobAppUser = isMobAppUser;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getDealer() {
        return dealer;
    }

    public void setDealer(String dealer) {
        this.dealer = dealer;
    }

    public String getTz() {
        return tz;
    }

    public void setTz(String tz) {
        this.tz = tz;
    }

}