package com.medialab.tasksystem.model;

public class PriorityLevel {
    private String id;
    private String name;
    private boolean isDefault;

    public PriorityLevel() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public PriorityLevel(String name, boolean isDefault) {
        this();
        this.name = name;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    @Override
    public String toString() {
        return name;
    }
}