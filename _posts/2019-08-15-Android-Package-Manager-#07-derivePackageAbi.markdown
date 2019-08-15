---
layout: post
title: "Android Package Manager #07 derivePackageAbi"
date: 2019-08-15 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

- [PackageManagerService.java #7554](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#7554)  
- #6967에서 derivePackageAbi를 호출한다. 이 부분의 시간이 오래 걸린다.  
- #7554에 derivePackageAbi 메소드가 선언돼 있다.  

### ABI, Application Binary Interface
- ABI는 두 개의 바이너리 프로그램 모듈 사이의 interface 이다. 보통 한 쪽은 라이브러리 혹은 운영체제이고, 다른 한 쪽은 사용자가 동작시키는 프로그램이다.  
- API는 source code에서 접근 가능하며, 상대적으로 high-level, hardware-independent, human-readable 형식이다.  
- ABI는 machine code에서 접근을 정의한다. low-level, hardware-dependeant 하다.  
- 여러 개의 프로그래밍 언어를 혼합하여 사용할 때 처리한다. 보통은 운영체제, 컴파일러가 수행하며, 프로그래머가 작업을 하는 경우도 있다.  

### .asec, Android secure encrypted file extension  
