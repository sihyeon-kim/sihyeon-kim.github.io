---
layout: post
title: "Miscellaneous Java"
date: 2019-07-15 20:00:00
author: Sihyeon Kim
categories: java
---

[목차1](#static-메소드)

# static variables, static method  
## static 변수
한 클래스의 모든 인스턴스가 공유하는 변수를 `static`으로 선언한다.  
`static` 변수 즉 클래스 변수는 클래스 이름 또는 인스턴스의 이름으로 접근 가능하다. 보통 클래스 변수에 접근하는지 인스턴스 변수에 접근하는지 구분하기 위해 클래스 변수에 접근할 때는 클래스 이름을 사용한다.   
`static` 변수는 인스턴스가 생성되기 이전에 별도의 메모리 공간에 할당되어 초기화된다. 따라서 인스턴스를 생성하지 않아도 `static` 변수에 접근할 수 있다. `static` 변수가 초기화되는 시점은 JVM에 의해서 클래스가 메모리 공간에 올라가는 순간이다. `static` 변수는 모든 인스턴스가 공유하므로 생성자를 통해 초기화해서는 안된다.      
변경되지 않는 값은 `final`로 선언해 상수화한다.  
클래스 내부 또는 외부에서 참조의 용도로만 선언된 변수는 `static final`로 선언한다. 이때 `static`과 `final`의 선언 위치는 상관없다.  
그리고 접근 제어자<sup>access specifier</sup>를 이용하는 경우가 많다. 이비 `static`과 더불어 `final`로 선언된 변수는 변경이 불가능하므로 외부에서 접근을 허용한다고 해서 문제가 되지 않는다.  
혹은 외부 접근을 허용해서 상수 값이 프로그램상에서 하나문 존재할 수 있다는 의미로 사용할 수 있다.  

## static 메소드  
클래스의 모든 인스턴스는 `static` 메소드에 접근할 수 있다.  
`static` 메소드는 클래스의 이름 또는 인스턴스의 이름으로 접근할 수 있다.  
인스턴스 이름으로 static 메소드를 부르는 것은 좋은 습관이 아니다. eclipse에서 경고 메시지를 발생시킨다.  
static 메소드는 polymorphic 하지 않고 인스턴스 메소드는 polymorphic 하다.
인스턴스를 생성하지 않아도 `static` 메소드를 호출할 수 있다.  
메소드가 단순히 외부에 기능만을 제공할 때는 인스턴스의 생성 없이 호출할 수 있도록 `static`으로 선언하는 것이 좋다.  
`static` 메소드는 인스턴스 변수에 접근이 불가능하다.  

static 메소드는 왜 오버라이딩 할 수 없는가?  
컴파일 시간에 statically resolved 되기 때문이다.  


---

# 접근 제어자<sup>Access specifier</sup>  

|지시자|클래스 내부|동일 패키지|상속받은 클래스|이외의 영역|
|---|:---:|:---:|:---:|:---:|
|private|o|x|x|x|
|default|o|o|x|x|
|protected|o|o|o|x|
|public|o|o|o|o|

---

# final과 static
final 클래스를 상속할 수 없다.  
final 메소드를 오버라이드 하거나 hidden 할 수 없다. 즉 subclasss에서 final method를 수정할 수 없다.    
static 블록: static 초기화 블록이라 한다.  
- 클래스 초기화 블록: 클래스가 처음 로딩될 때 한 번만 수행한다.  
- 인스턴스 초기화 블록: 인스턴스가 생성될 때 마다 수행된다. 생성자보다 먼저 수행된다.  

인스턴스 변수의 초기화는 주로 생성자를 사용하기 때문에, 인스턴스 초기화 블록은 잘 사용하지 않는다.  
대신 클래스의 모든 생성자에서 공통적으로 수행되어야 하는 코드가 있는 경우 생성자에 넣지 않고 인스턴스 초기화 블록에 넣어 두면 코드의 중복을 줄일 수 있다.  
static 클래스: 일반적인 top-level 클래스에 적용하면 문법 오류이다.  
중첩 클래스<sup>nested class</sup>에만 사용할 수 있다.  

---

# Overriding vs hiding
오버라이딩<sup>Overriding</sup>은 late-binding을 지원한다.  
즉 어떤 메소드가 호출될지 런타임에 결정한다.  
Hiding은 early-binding을 기반으로 한다. 즉 호출되거나 사용되는 메소드 또는 메버를 컴파일 시간에 결정한다.  

||Superclass Instance Method|Superclass Static Method|
|---|:---:|:---:|
|Subclass Instance Method|Overrides|Generates a compile-time error|
|Subclass Static Method|Generates a compile-time error|Hides|

---

# Early binding vs late binding

---

# Runtime vs Compile time

---


#### 참고 자료  
[1](https://hashcode.co.kr/questions/654/%EC%9E%90%EB%B0%94%EC%97%90%EC%84%9C-static-%EB%B8%94%EB%A1%9D%EC%9D%80-%EB%AC%B4%EC%97%87%EC%9D%84-%EC%9D%98%EB%AF%B8%ED%95%98%EB%82%98%EC%9A%94)  
[2](https://stackoverflow.com/questions/10594052/overriding-vs-hiding-java-confused)  
[3](https://stackoverflow.com/questions/16313649/what-is-method-hiding-in-java-even-the-javadoc-explanation-is-confusing)  
[4. polymorphism](https://stackoverflow.com/questions/1031273/what-is-polymorphism-what-is-it-for-and-how-is-it-used)

