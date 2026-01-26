package br.com.gitflowhelper.dialog;

public class NameResponse {

    private String name;
    private Boolean pushOnFinish;

    public NameResponse(String name, Boolean pushOnFinish) {
        this.name = name;
        this.pushOnFinish = pushOnFinish;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Boolean getPushOnFinish() {
        return pushOnFinish;
    }
    public void setPushOnFinish(Boolean pushOnFinish) {
        this.pushOnFinish = pushOnFinish;
    }
}
