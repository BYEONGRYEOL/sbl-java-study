package adapter;

public class EmptyTeddyBear implements EmptyBear{
    @Override
    public void eyesTask() {
        System.out.println("눈 두개 붙이기");
    }
    @Override
    public void noseTask() {
        System.out.println("코 하나 붙이기");
    }
    @Override
    public void mouthTask(){
        System.out.println("입 하나 붙이기");
    }
}