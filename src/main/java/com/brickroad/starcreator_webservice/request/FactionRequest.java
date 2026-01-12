package com.brickroad.starcreator_webservice.request;

public class FactionRequest {

    private String name;
    private String type;
    private String alignment;
    private boolean ai_created;
    private int count;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isAi_created() {
        return ai_created;
    }

    public void setAi_created(boolean ai_created) {
        this.ai_created = ai_created;
    }
}
