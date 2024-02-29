package polymorphism_class;

public class python extends language {
    @Override
    public void whatICanDo() {
        System.out.println("ai, data analysis");
    }
    public python() {
        this.lang = "python";
    }

    @Override
    public void solve(String prob) {
        //thinkOfLogic(prob);
        System.out.println(this.lang + "은 그냥 하면 된다.");
        if(prob == "정렬"){
            System.out.println("list.sort()");
        }
    }
}
