package org.example;

import polymorphism_class.*;

public class Main {
	public static void main(String[] args) {

		Sports sports = new Sports();
		System.out.println(sports.play());

		Sports soccer = new Soccer();
		Sports basketball = new basketball();

		System.out.println(basketball.play());
		System.out.println(soccer.play());
		System.out.println(soccer.play("아주 세게"));

		Soccer realSoccer = new Soccer();
		System.out.println(realSoccer.play("아주 세게"));

		System.out.println('\n');

		//
		//
		//        language python = new python();
		//        language java = new java();
		//        language cpp = new cpp();
		//
		//        language[] langs = new language[3];
		//        langs[0] = python;
		//        langs[1] = java;
		//        langs[2] = cpp;
		//
		//
		//        // 아래는 다형성의 이점을 100%활용하지 않은 사용
		//        langs[0].whatICanDo();
		//        langs[0].solve("정렬");
		//        System.out.println('\n');
		//        langs[1].whatICanDo();
		//        langs[1].solve("정렬");
		//        System.out.println('\n');
		//        langs[2].whatICanDo();
		//        langs[2].solve("정렬");
		//
		//
		//        // 다형성의 이점을 잘 활용한 사용
		//        for(int i = 0; i < 3 ; i ++){
		//            langs[i].whatICanDo();
		//            langs[i].solve("정렬");
		//            System.out.println('\n');
		//        }

	}
}