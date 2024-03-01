package polymorphism_class;

public class java extends language{
    @Override
    public void whatICanDo() {
        System.out.println("web, app");
    }
    public java() {
        this.lang = "java";
    }
    @Override
    public void solve(String prob) {
        thinkOfLogic(prob);
        if(prob == "정렬"){
            System.out.println("list.sort((a1,a2)->{ \n" +
                    "   if(a1[0]==a2[0])\n" +
                    "       return a1[1] - a2[1];\n" +
                    "   return a1[0]-a2[0];");
        }
    }
}
