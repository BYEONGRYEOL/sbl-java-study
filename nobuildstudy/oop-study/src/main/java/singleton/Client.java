package singleton;

public class Client {
	public static void main(String[] args) {

		Singleton s = Singleton.getInstance(); // 처음으로 getInstance를 실행한 시점에 유일 객체가 동적할당된다.

		System.out.println(s.Instance.toString()); //
		Singleton.destroyInstance();
		System.out.println(s.Instance.toString());

	}
}