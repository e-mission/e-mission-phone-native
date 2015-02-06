package edu.berkeley.eecs.e_mission;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import android.content.Context;

public class DataUtils {
	File privateFileDir;
	
	public DataUtils(Context context) {
		privateFileDir = context.getFilesDir();
	}
	
	public void saveUserName(String userName) {
        try {
        	File userNameFile = new File(privateFileDir, "userName");
        	PrintWriter out = new PrintWriter(userNameFile);
            out.println(userName);
            out.close();               	
        } catch (FileNotFoundException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
	}
	
	public String getUserName() {
        try {
        	File userNameFile = new File(privateFileDir, "userName");
        	BufferedReader in = new BufferedReader(new FileReader(userNameFile));
            String userName = in.readLine();
            in.close();
            return userName;
        } catch (FileNotFoundException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}
	
	public String toString() {
		return "DataUtils(privateFileDir = "+privateFileDir+")";
	}
}
