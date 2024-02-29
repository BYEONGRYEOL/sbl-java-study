package polymorphism_class;

public class Sports {
    public String play(){
        return "안다치게 한다.";
    }
    public String play(String how){
        return how + this.play();
    }
}
