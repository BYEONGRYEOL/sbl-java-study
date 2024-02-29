package adapter;

public class MonsterAdapter implements EmptyBear{

    EmptyMonster monster;
    public MonsterAdapter(EmptyMonster monster){
        this.monster = monster;
    }
    @Override
    public void eyesTask() {
        monster.attachThreeEyes();
    }

    @Override
    public void noseTask() {
        monster.hollowOutNose();
    }

    @Override
    public void mouthTask() {
        monster.drawMouth();
    }
}
