package adapter;

public class EmptyJavalin implements EmptyMonster{
    @Override
    public void attachThreeEyes() {
        System.out.println("눈 세개를 붙이기");
    }

    @Override
    public void hollowOutNose() {
        System.out.println("코 모양대로 파내기");
    }

    @Override
    public void drawMouth() {
        System.out.println("입을 무섭게 그리기");
    }
}
