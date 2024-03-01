package com.sbl.webflux.study0117;

public class DynamicObject {
	String name;

	public DynamicObject() {

	}

	public void setDynamicObject(String name) {
		this.name = name;
		System.out.println(name + "이라는 동적 객체 생성됨");
	}
}
