package com.example.mathquest;

public class User {
    private String id;
    private String email;
    private String nom;
    private String password;

    private int score;
    private int coeur;
    private int argent;

    public User() {}

    public User(String email, String nom, String password, String id) {
        this.email = email;
        this.nom = nom;
        this.password = password;
        this.id = id;
        this.score = 0;
        this.coeur = 3;
        this.argent = 0;
    }

    // GETTERS
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getNom() { return nom; }
    public String getPassword() { return password; }
    public int getScore() { return score; }
    public int getCoeur() { return coeur; }
    public int getArgent() { return argent; }

    // SETTERS
    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setNom(String nom) { this.nom = nom; }
    public void setPassword(String password) { this.password = password; }
    public void setScore(int score) { this.score = score; }
    public void setCoeur(int coeur) { this.coeur = coeur; }
    public void setArgent(int argent) { this.argent = argent; }
}