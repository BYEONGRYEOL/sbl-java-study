package singleton;
public class Singleton {
    static Singleton Instance;
    private Singleton(){};
    public static Singleton getInstance(){
        if(Instance == null)
            Instance = new Singleton();
        return Instance;
    }
    public static void destroyInstance() {
        Instance = null;
    }
    
    public static void staticDoSomething(){
        System.out.println(Instance.toString());
    }

    public  void nonStaticDoSomething(){
        System.out.println(Instance.toString());
    }
}
