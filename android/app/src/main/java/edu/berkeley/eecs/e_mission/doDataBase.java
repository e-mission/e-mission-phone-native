package edu.berkeley.eecs.e_mission;

import org.apache.cordova.CordovaActivity;

import java.util.Objects;

/**
 * Created by RZarrabi on 6/9/2015.
 */
public class doDataBase extends CordovaActivity {

    private boolean azure;
    private boolean couch;
    private boolean aws;
    private Object comp;
    private String option;

    public doDataBase() {
        if (option.equals("azure")){
            this.azure = true;
            comp = new CompetitionActivity_Azure();
        } else if (option.equals("couch")) {
            this.couch = true;
        } else if (option.equals("aws")) {
            this.aws = true;
        }
    }

    public void addItem(ScoreActivity score) {
    }
}
