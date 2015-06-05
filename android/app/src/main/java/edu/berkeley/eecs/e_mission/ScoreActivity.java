package edu.berkeley.eecs.e_mission;

/**
 * Created by RZarrabi on 6/5/2015.
 */
public class ScoreActivity {


    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("text")
    private String mText;

    /**
     * Item Id
     */
    @com.google.gson.annotations.SerializedName("id")
    private int mId;

    @Override
    public String toString(){
        return mText;
    }

    public ScoreActivity(String User, int score) {
        this.mText = User;
        this.mId = score;
    }
}
