package com.abircode.cruddp.entities;

public class User {
    private int id;
    private String email;
    private String password; // This will store the hashed password
    private String name;
    private String lastname;
    private String image;
    private boolean blok;
    private Boolean accepted;
    private Integer phone;
    private String roles; // In Symfony it's an array, we'll store as JSON or comma-separated
    public User() {}
    public User(int id, String email, String password, String name, String lastname, String image, boolean blok, Boolean accepted, Integer phone, String roles) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.lastname = lastname;
        this.image = image;
        this.blok = blok;
        this.accepted = accepted;
        this.phone = phone;
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", image='" + image + '\'' +
                ", blok=" + blok +
                ", accepted=" + accepted +
                ", phone=" + phone +
                ", roles='" + roles + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur ne peut pas être vide.");
        }
        this.name = name;
    }
    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {


        this.lastname = lastname;


    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isBlok() {
        return blok;
    }

    public void setBlok(boolean blok) {
        this.blok = blok;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {

        this.phone = phone;
    }
    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
