package proxy;

public class HaveAMill {
    public static void main(String[] args) {

        IPerson sbl = new PrayProxy(new SBL());
        sbl.haveAMill();
    }
}
