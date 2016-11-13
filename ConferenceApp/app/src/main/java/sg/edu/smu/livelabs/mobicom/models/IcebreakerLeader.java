package sg.edu.smu.livelabs.mobicom.models;


import java.util.Date;

/**
 * Created by johnlee on 22/2/16.
 */
public class IcebreakerLeader {

    private String avatar;
    private String name;
    private int counter;
    private Date lastModifiedTime;

    public IcebreakerLeader() {
    }

    public IcebreakerLeader(String avatar, String name, int counter) {
        this.avatar = avatar;
        this.name = name;
        this.counter = counter;
    }

    public IcebreakerLeader(String avatar, String name, int counter, Date lastModifiedTime) {
        this.avatar = avatar;
        this.name = name;
        this.counter = counter;
        this.lastModifiedTime = lastModifiedTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }
}
