package adapter;

public class WithAdapter {

    public static void main(String[] args) {
        // 테디 베어 얼굴 작업
        EmptyBear emptyTeddyBear =  new EmptyTeddyBear();
        emptyTeddyBear.eyesTask();
        emptyTeddyBear.noseTask();
        emptyTeddyBear.mouthTask();



        // 괴물 javalin 얼굴 작업
        EmptyMonster emptyJavalin = new EmptyJavalin();
        MonsterAdapter monsterAdapter = new MonsterAdapter(emptyJavalin);
        monsterAdapter.eyesTask();
        monsterAdapter.noseTask();
        monsterAdapter.mouthTask();

    }
}
