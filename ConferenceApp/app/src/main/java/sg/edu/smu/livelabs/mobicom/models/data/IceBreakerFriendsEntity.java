package sg.edu.smu.livelabs.mobicom.models.data;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "ICE_BREAKER_FRIENDS_ENTITY".
 */
public class IceBreakerFriendsEntity {

    private Long id;
    private Long userId;
    private String name;
    private String emailId;
    private String desig;
    private String avatarId;
    private String qrCode;

    public IceBreakerFriendsEntity() {
    }

    public IceBreakerFriendsEntity(Long id) {
        this.id = id;
    }

    public IceBreakerFriendsEntity(Long id, Long userId, String name, String emailId, String desig, String avatarId, String qrCode) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.emailId = emailId;
        this.desig = desig;
        this.avatarId = avatarId;
        this.qrCode = qrCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getDesig() {
        return desig;
    }

    public void setDesig(String desig) {
        this.desig = desig;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

}
