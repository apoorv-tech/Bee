package com.devanant.bee.UI.Home;

import java.io.Serializable;
import java.util.ArrayList;

public class UserModel implements Serializable {
    private String Bio;
    private String Facebook;
    private String Instagram;
    private ArrayList<String> Interest;
    private String Organisation;
    private String PhoneNumber;
    private String ProfilePic;
    private String Username;

    public UserModel(String bio, String facebook, String instagram, ArrayList<String> interest, String organisation, String phoneNumber, String ProfilePic, String Username) {
        Bio = bio;
        Facebook = facebook;
        Instagram = instagram;
        Interest = interest;
        Organisation = organisation;
        PhoneNumber = phoneNumber;
        this.ProfilePic = ProfilePic;
        this.Username = Username;
    }
    public UserModel()
    {
        //empty constructor
    }

    public String getBio() {
        return Bio;
    }

    public void setBio(String bio) {
        Bio = bio;
    }

    public String getFacebook() {
        return Facebook;
    }

    public void setFacebook(String facebook) {
        Facebook = facebook;
    }

    public String getInstagram() {
        return Instagram;
    }

    public void setInstagram(String instagram) {
        Instagram = instagram;
    }

    public ArrayList<String> getInterest() {
        return Interest;
    }

    public void setInterest(ArrayList<String> interest) {
        Interest = interest;
    }

    public String getOrganisation() {
        return Organisation;
    }

    public void setOrganisation(String organisation) {
        Organisation = organisation;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getProfilePic() {
        return ProfilePic;
    }

    public void setProfilePic(String profilePic) {
        ProfilePic = profilePic;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String UserName) {
        Username = UserName;
    }
}
