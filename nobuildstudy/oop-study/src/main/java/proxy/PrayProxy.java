package proxy;

public class PrayProxy implements IPerson{
    private SBL sbl;
    public PrayProxy(SBL sbl){
        this.sbl = sbl;
    }
    @Override
    public void haveAMill() {
        System.out.println("기도하기");
        sbl.haveAMill();
    }
}
