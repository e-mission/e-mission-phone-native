package edu.berkeley.eecs.e_mission;

/**
 * Created by RZarrabi on 6/5/2015.
 */
public class ScoreActivity {


    /**
     * Item text
     */
    @com.google.gson.annotations.SerializedName("text")
    public String Text;

    /**
     * Item Id
     */
    @com.google.gson.annotations.SerializedName("id")
    public String ID;



    @Override
    public String toString(){
        return Text;
    }

    public ScoreActivity() {
    }
}
