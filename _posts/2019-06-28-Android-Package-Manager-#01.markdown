---
layout: post
title: "Android Package Manager #01"
date: 2019-06-28 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

# Android Packagemanager #01
## 프로젝트 진행 현황
### 진행 현황 2019.06.28  
- 스마트폰 기기에 안드로이드 마시멜로 6.0.1버전 설치  
- adb를 통해 packagemanager dump 출력 시도  
- dump 출력 시 확인해야 하는 부분에 대해 멘토에게 문의(06.26) 후 답변 기다리는 중  
- pm(pacakage manager) 디렉토리에 포함된 소스코드 확인  
### 앞으로의 진행 계획    
- 멘토 답변을 받은 뒤 adb을 통해 package manager 확인  
- pm 디렉토리 내에서 구체적으로 어느 소스 코드를 봐야하는지 선정 후 확습

---

## 안드로이드 6.0.1 r81 소스코드 참고 자료  
- [android 6.0.1 r81](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81)  
  - [core/java/content/pm](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/core/java/android/content/pm/)  
    - [Packagemanager.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/core/java/android/content/pm/PackageManager.java)   
    - [Packageinstaller.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/core/java/android/content/pm/PackageInstaller.java)  
  - [services/core/java/com/android/server](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/services/core/java/com/android/server/pm)  
    - [PackageManagerService.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/services/core/java/com/android/server/pm/PackageManagerService.java)  
    - [PackageInstallerService.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/services/core/java/com/android/server/pm/PackageInstallerService.java)  

---

## 참고 자료 

- Pacakage Manager와 관련된 글
  - [Andorid PackageManager and PacakageInstaller eng ver.](http://ghostkei.blogspot.com/2015/09/external-post-in-depth-android-package_32.html)
  - [Andorid PackageManager and PacakageInstaller kor ver.](http://kpbird.blogspot.com/2012/10/in-depth-android-package-manager-and.html)
  - [Wikipedia: Android Application Package](https://en.wikipedia.org/wiki/Android_application_package)
  - [Quora: Android Package Installer](https://www.quora.com/What-is-an-Android-package-installer)

- about scanning  
  - [stackoverflow: scanning in spring and androind ](https://stackoverflow.com/questions/11421085/implementing-spring-like-package-scanning-in-android)
  - [blog: scanning in spring](https://hamait.tistory.com/322)
  - [scanning android package](https://www.opswat.com/blog/scanning-android-package-files-apks-metadefender-cloud)

- others  
  - [android boot process](https://hackernoon.com/the-android-boot-process-2ce4c498615b)
