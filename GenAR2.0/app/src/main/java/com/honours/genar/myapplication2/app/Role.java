package com.honours.genar.myapplication2.app;

public class Role implements Comparable<Role>{

    private String name, description, password;
    private boolean active;

    public Role(String name, String description, String password) {
        this.name = name;
        this.description = description;
        this.password = password;
        active = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int compareTo(Role role) {
        if (role==null) throw new NullPointerException();

        return name.compareTo(role.getName());
    }


}
