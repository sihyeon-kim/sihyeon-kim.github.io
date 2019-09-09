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
<br>
29번째 줄,   
`size_t`는 unsigned data type이다.  
32bit 머신에서는 32bit로 표현 가능한 가장 큰 정수, 64bit machine에서는 64bit로 표현가능한 가장 큰 정수이다.  
`sizeof()`함수의 반환형이다.  
<br>
31,32번째 줄에서,  
`void *`형 매개변수를 `char *`로 형변환한다: 결국, char를 복사한다.  
`restrict` 관련 [위키피디아](https://en.wikipedia.org/wiki/Restrict) 참고: pointer aliasing 막아 compiling optimization 한다.    
<br>
35번째 줄,  
[이 사이트](https://code.woboq.org/userspace/glibc/sysdeps/generic/memcopy.h.html)를 보면 `unsigned long int`로 정의되어 있다.  
주석보면 destination memory에 대한 8바이트 포인터라고 표시되어 있다.  
`unsigned long int`는 64bit machine에서 8byte이다.  
<br>
39~44번째 줄,  
<span style="color:red;">왜 있는거지?</span>  
char 개수가 16개보다 작으면 그대로 복사하고 반환한다.  
char는 자료형 크기가 1byte이고 16개가 있으면 128bit.  
<br>
48~57번째 줄,  
48줄: src1의 끝 위치 찾는다.  
프로세서는 cache line을 거쳐 data를 cache로 가져온다.  
cache line은 크기는 프로세서마다 다르며 16~128바이트이다. 보통 64바이트이다. 이러한 cache line을 몇 개씩 가지고 있다.      
[관련 스택오버플로우](https://stackoverflow.com/questions/3928995/how-do-cache-lines-work)  
결국 미리 정의한 PREFETCH_LINES_AHEAD 수를 넘어가지 않는 범위에서, L2 cache line size만큼 prefetch에 더하면서 src1의 끝을 넘어가지 않도록 한다.  
즉 src1_end 넘어가지 않는 범위에서 얼마만큼 prefetch할지 결정한다.  
<br>
60번째 줄,  
`uintptr_t`은 포인터 타입이다.  



#### 참고자료
[attribute-syntax](https://gcc.gnu.org/onlinedocs/gcc-4.7.2/gcc/Attribute-Syntax.html#Attribute-Syntax)  
[function-attribute](https://gcc.gnu.org/onlinedocs/gcc-4.7.2/gcc/Function-Attributes.html)  












