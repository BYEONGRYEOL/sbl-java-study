package Decorator;

interface IPerson{
    void haveAMill();
}

class SBL implements IPerson{

    @Override
    public void haveAMill() {
        System.out.println("식사 하기");
    }
}

abstract class Decorator implements IPerson{
    IPerson wrappee; // 싸여질 원본 객체라는 뜻으로 wrappee

    Decorator(IPerson wrappee){
        this.wrappee = wrappee;
    }

    public void haveAMill(){
        wrappee.haveAMill(); // 들고 있는 원본 객체가 수행하도록 위임하는 구조
    }
}

class ChristianDecorator extends Decorator{
    ChristianDecorator(IPerson wrappee){
        super(wrappee); // 부모클래스의 생성자 호출로
        // wrappee 원본 객체를 들고있도록 함
    }

    @Override
    public void haveAMill() {
        pray(); //데코레이터 클래스만의 추가적인 메서드 실행
        super.haveAMill(); // 원본 객체의 해당 메서드를 실행
    }

    private void pray(){
        System.out.println("기도 하기");
    }
}