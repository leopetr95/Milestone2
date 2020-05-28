package entity;

public class Values {

    private String string1;
    private String string2;
    private String string3;
    private String string4;
    private String string5;
    private String string6;
    private String string7;
    private String string8;
    private String string9;
    private String string10;
    private int temp;
    private int counter;

    public Values(String string1, String string2, String string3, String string4, String string5, String string6, String string7, String string8, String string9) {
        this.string1 = string1;
        this.string2 = string2;
        this.string3 = string3;
        this.string4 = string4;
        this.string5 = string5;
        this.string6 = string6;
        this.string7 = string7;
        this.string8 = string8;
        this.string9 = string9;
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

    public String getString8() {
        return string8;
    }

    public void setString8(String string8) {
        this.string8 = string8;
    }

    public String getString9() {
        return string9;
    }

    public void setString9(String string9) {
        this.string9 = string9;
    }

    public String getString10() {
        return string10;
    }

    public void setString10(String string10) {
        this.string10 = string10;
    }

    public Values(String string1, String string2, String string3, String string4, String string5, String string6, String string7, String string8, String string9, String string10) {
        this.string1 = string1;
        this.string2 = string2;
        this.string3 = string3;
        this.string4 = string4;
        this.string5 = string5;
        this.string6 = string6;
        this.string7 = string7;
        this.string8 = string8;
        this.string9 = string9;
        this.string10 = string10;
    }
}
