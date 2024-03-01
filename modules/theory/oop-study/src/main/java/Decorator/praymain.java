package Decorator;

public class praymain {
    public static void main(String[] args) {
        SBL nonReligiousSBL = new SBL();
        nonReligiousSBL.haveAMill();

        Decorator christianSBL = new ChristianDecorator(new SBL());
        christianSBL.haveAMill();

    }
}
