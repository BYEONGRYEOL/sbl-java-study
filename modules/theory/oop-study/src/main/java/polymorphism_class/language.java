package polymorphism_class;


public abstract class language {
    public void whatICanDo(){
        System.out.println("anything!");
    }
    protected String lang;
    public language(){
        this.lang = "None";
    }
    protected String thinkOfLogic(String prob){
        return lang + "언어로는 " + prob + "을(를) 어떻게 하더라?";
    }
    public abstract void solve(String prob);
}
