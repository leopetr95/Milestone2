package entity;

public class Buggyness {

    private String index;
    private String nameOfClass;
    private String buggy;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getNameOfClass() {
        return nameOfClass;
    }

    public void setNameOfClass(String nameOfClass) {
        this.nameOfClass = nameOfClass;
    }

    public String getBuggy() {
        return buggy;
    }

    public void setBuggy(String buggy) {
        this.buggy = buggy;
    }

    public Buggyness(String index, String nameOfClass, String buggy) {
        this.index = index;
        this.nameOfClass = nameOfClass;
        this.buggy = buggy;
    }

}

