package entity;

public class Values {

    private String string1;
    private String string2;
    private String string3;
    private String string4;
    private String string5;
    private String string6;
    private String string7;
    private int temp;
    private int counter;
    private SubValues subValues;

    public Values(String string1, String string2, String string3, SubValues subValues) {
        this.string1 = string1;
        this.string2 = string2;
        this.string3 = string3;
        this.subValues = subValues;
    }

    public Values(String string1, String string2, SubValues subValues) {
        this.string1 = string1;
        this.string2 = string2;
        this.subValues = subValues;
    }

    public Values(int counter, String string1, String string2, String string3, String string4, SubValues subValues){

        this.counter = counter;
        this.string1 = string1;
        this.string2 = string2;
        this.string3 = string3;
        this.string4 = string4;
        this.subValues = subValues;
    }

    public SubValues getSubValues() {
        return subValues;
    }

    public void setSubValues(SubValues subValues) {
        this.subValues = subValues;
    }

    public Values(String string1, String string2, String string3) {
        this.string1 = string1;
        this.string2 = string2;
        this.string3 = string3;
    }

    public Values(String string1, int temp) {
        this.string1 = string1;
        this.temp = temp;
    }

    public Values(String string1, int temp, String string2) {
        this.string1 = string1;
        this.temp = temp;
        this.string2 = string2;
    }

    public Values(String string1, int temp, int counter) {
        this.string1 = string1;
        this.temp = temp;
        this.counter = counter;
    }

    public int getCounter() {
        return counter;
    }

    public Values(String string1, String string2, String string3, String string4, int temp, int counter) {
        this.string1 = string1;
        this.string2 = string2;
        this.string3 = string3;
        this.string4 = string4;
        this.temp = temp;
        this.counter = counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public Values(String string1, String string2, String string3, String string4, int temp) {
        this.string1 = string1;
        this.string2 = string2;
        this.string3 = string3;
        this.string4 = string4;
        this.temp = temp;
    }

    public String getString1() {

        return string1;

    }

    public void setString1(String string1) {

        this.string1 = string1;

    }

    public String getString2() {

        return string2;

    }

    public void setString2(String string2) {

        this.string2 = string2;

    }

    public Values(String string1, String string2) {

        this.string1 = string1;
        this.string2 = string2;

    }

    public String getString3() {
        return string3;
    }

    public void setString3(String string3) {
        this.string3 = string3;
    }

    public String getString4() {
        return string4;
    }

    public void setString4(String string4) {
        this.string4 = string4;
    }

    public Values(String string1, String string2, String string3, String string4) {
        this.string1 = string1;
        this.string2 = string2;
        this.string3 = string3;
        this.string4 = string4;
    }

    public String getString5() {
        return string5;
    }

    public void setString5(String string5) {
        this.string5 = string5;
    }

    public String getString6() {
        return string6;
    }

    public void setString6(String string6) {
        this.string6 = string6;
    }

    public String getString7() {
        return string7;
    }

    public void setString7(String string7) {
        this.string7 = string7;
    }

}
