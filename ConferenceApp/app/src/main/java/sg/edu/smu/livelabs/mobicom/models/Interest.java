package sg.edu.smu.livelabs.mobicom.models;


import me.kaede.tagview.Tag;

/**
 * Created by johnlee on 22/2/16.
 */
public class Interest extends Tag {

    private Long id;
    private String interest;

    public Interest(Long id, String interest) {
        super(interest);
        this.id = id;
        this.interest = interest;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Interest){
            return id == ((Interest)o).id;
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }
}
