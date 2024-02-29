package polymorphism_class;

public class cpp extends language {
    public cpp() {
        this.lang = "cpp";
    }

    @Override
    public void solve(String prob) {
        thinkOfLogic(prob);
        if(prob == "정렬"){
            System.out.println("sort(vector.begin(), vector.end(), compare)\n" +
                    "bool compare(vector<int> a, vector<int> b) {\n" +
                    "    if (a[0] == b[0]) return a[1] < b[1];\n" +
                    "    return a[0] < b[0];\n" +
                    "}");
        }
    }

    @Override
    public void whatICanDo() {
        System.out.println("simulation");
    }
}
