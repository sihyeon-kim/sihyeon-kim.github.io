---
layout: post
title: "Android Package Manager #10 memcpy"
date: 2019-09-6 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

strlcpy 부분은 안드로이드 최신 버전에도 그대로 남아있다.  

### memcpy.c 소스 확인  

먼저, glibc (GNU C Library) 의 git-mirror 페이지 링크: [git-mirror/glibc](https://github.molgen.mpg.de/git-mirror/glibc)  
glibc의 memcpy.c 소스코드: [memcpy.c](https://github.molgen.mpg.de/git-mirror/glibc/blob/master/sysdeps/tile/tilegx/memcpy.c)  

#### 다른 참고 링크  

1. [strlen.c](https://github.molgen.mpg.de/git-mirror/glibc/blob/master/string/strlen.c)  
2. [https://sourceware.org/git/?p=glibc.git](https://sourceware.org/git/?p=glibc.git)  

### AOSP에서 memcpy 소스가 있는지 확인  

먼저, 안드로이드 6.0.1_r77에는 다음의 경로를 따라가면 memcpy.S 파일이 있다.   
.S 파일 확장자는 어셈블리어 소스코드이다.  
`platform/bionic/+/refs/tags/android-6.0.1_r77/libc/arch-mips/string`  
링크는 다음과 같다: [memcpy.S](https://android.googlesource.com/platform/bionic/+/refs/tags/android-6.0.1_r77/libc/arch-mips/string)  

<br>

안드로이드 8.0.0_r1 부터는 같은 경로에 memcpy.c가 포함되어 있다.  
링크는 다음과 같다: [memcpy.c](https://android.googlesource.com/platform/bionic/+/refs/tags/android-8.0.0_r1/libc/arch-mips/string/)  

<br>

glibc와 AOSP에 있는 memcpy.c 소스코드 구현이 다르다.  

### glibc에 있는 memcpy.c 소스코드 보기  

함수 반환형과 이름 사이에 있는 inhibit_loop_to_libcall, 변수 자료형과 이름 사이에 있는 `__restrict`에 대해서는 아래의 참고자료 보기.  
attribute라 부른다.  
컴파일러가 최적화하도록 어떤 것을 설정하는 기능을 한다.   
여기서는 macro로 attribute이 정의되어 있는 듯하다.  

#### 참고자료
[attribute-syntax](https://gcc.gnu.org/onlinedocs/gcc-4.7.2/gcc/Attribute-Syntax.html#Attribute-Syntax)  
[function-attribute](https://gcc.gnu.org/onlinedocs/gcc-4.7.2/gcc/Function-Attributes.html)  












