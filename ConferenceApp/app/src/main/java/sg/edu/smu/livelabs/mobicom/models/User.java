package sg.edu.smu.livelabs.mobicom.models;


import java.io.Serializable;

/**
 * Created by smu on 22/1/16.
 */
public class User implements Serializable {

    private long  UID;
    private String email;
    private String password;
    private String name;
    private String status;
    private String cover;
    private String avatar;//avatar server id
    private String roleStr;
    private String[] role;//keynote, speaker
    private String designation;
    private String school;
    private String interestsStr;
    private String[] interests;
    private String sessionToken;
    private String userHandle;
    private String qrcode;
    private int totalPoints;
    private boolean isFirstTimeUpdate;
    private boolean isModerator = false;

    public long getUID() {
        return UID;
    }

    public void setUID(long UID) {
        this.UID = UID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String[] getRole() {
        return role;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String[] getInterests() {
        return interests;
    }

    public String getRoleStr() {
        return roleStr;
    }

    public void setRoleStr(String roleStr) {
        this.roleStr = roleStr;
        if (roleStr != null && roleStr.length() > 0){
            this.role = roleStr.split(",");
            for (String role1 : role){
                if (role1.trim().toLowerCase().equals("moderator")){
                    isModerator = true;
                    break;
                }
            }
        }
    }

    public String getInterestsStr() {
        return interestsStr;
    }

    public void setInterestsStr(String interestsStr) {
        this.interestsStr = interestsStr;
        if (interestsStr != null && interestsStr.length() > 0){
            this.interests = interestsStr.split(",");
        }
    }

    public boolean isFirstTimeUpdate() {
        return isFirstTimeUpdate;
    }

    public void setIsFirstTimeUpdate(boolean isFirstTimeUpdate) {
        this.isFirstTimeUpdate = isFirstTimeUpdate;
    }

    public void setQrCode(String qrcode){
        this.qrcode = qrcode;
    }

    public String getQrCode(){
        return qrcode;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getUserHandle() {
        return userHandle;
    }

    public void setUserHandle(String userHandle) {
        this.userHandle = userHandle;
    }

    public boolean isModerator(){
        return isModerator;
    }
}
