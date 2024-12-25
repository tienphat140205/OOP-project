package sourceCode.Models;

import java.time.LocalDate;

public class User {

    private String userId;

    private String name;

    private String identityNumber;

    private LocalDate birth;

    private String gender;

    private String phoneNumber;

    private String email;

    private String address;

    private String password;

    private String role;

    public User() {
        userId = "";
        name = "";
        identityNumber = "";
        birth = LocalDate.now();
        gender = "";
        phoneNumber = "";
        email = "";
        address = "";
        password = "";
    }

    public User(String userId, String name, String identityNumber, LocalDate birth, String gender,
            String phoneNumber, String email, String address, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.identityNumber = identityNumber;
        this.birth = birth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.password = password;
        this.role = role;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getBirth() {
        return birth;
    }

    public void setBirth(LocalDate birth) {
        this.birth = birth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
