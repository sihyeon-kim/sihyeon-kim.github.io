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











