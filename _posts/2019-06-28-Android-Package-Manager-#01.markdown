---
layout: post
title: "Android Package Manager #01"
date: 2019-06-28 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

# Android Package Manager #01
## 프로젝트 진행 현황
### 진행 현황 2019.06.28  
- 스마트폰 기기에 안드로이드 마시멜로 6.0.1버전 설치  
- adb를 통해 packagemanager dump 출력 시도  
- dump 출력 시 확인해야 하는 부분에 대해 멘토에게 문의(06.26) 후 답변 기다리는 중  
- pm(pacakage manager) 디렉토리에 포함된 소스코드 확인  

### 앞으로의 진행 계획    
- 멘토 답변을 받은 뒤 adb을 통해 package manager 확인  
- pm 디렉토리 내에서 구체적으로 어느 소스 코드를 봐야하는지 선정 후 학습

---

## Android Package Manager and Installer  
### Android Package Installer 
- Android에 Package를 설치하는 기본 애플리케이션이다.  
- Application/Package를 관리하는 사용자 인터페이스를 제공한다.  
- InstallAppProgress Activity를 호출해 사용자의 명령을 전달 받는다.  
  - InstallAppProgress는 installd를 통해 Package 설치를 Package Manager Service에 요청한다.  
  - installd는 root permission으로 APK 설치를 수행한다.  

### Android Package Manager  
- 애플리케이션 설치, 삭제, 업그레이드를 위한 API이다.  
- 설치할 때 PackageManager가 4개의 Parameter를 갖는 InstallPackage 메소드를 호출한다.  
- Package Manager는 Package Service를 실행하고 여기서 분산이 이루어진다.  
  - PackageInstallerActivity.java와 installAppProgress.java 확인  
  - Syster service process로 동작하는 Package Manager Service와 install daemon은 부팅 시점에 native process로 동작  

### APK file 저장 위치  
- 기본 설치되는 APK는 /system/app에 저장  
- 사용자가 설치한 APK는 /data/app에 저장  
- Package Manager는 데이터 디렉토리 /data/data/<package name>을 생성해 저장하고 native library와 cache data를 참조/공유한다.  
  
### 세부적인 APK 설치 과정  
Package Manager Service는 다음의 과정을 실행  

1. wating  
2. Installation Process Queue에 Package 추가  
3. Package 설치 경로를 결정  
4. 설치인지 업데이트인지 확인  
5. APK 파일을 주어진 디렉토리에 복사  
6. APP의 UID 결정  
7. Installd 데몬 프로세스에 요청  
8. Application 디렉토리를 만들고 permission 설정  
9. cache 디렉토리에서 dex code 추출  
10. package.list를 갱신하기 위해 /system/data/package.xml을 최산상태로 갱신  
11. 설치 완료를 system에 broadcast  
  - Intent.ACTION_PACKAGE_ADDED: 신규 패키지  
  - Intent.ACTION_PACKAGE_REPLACED: 업데이트  

### Package Manager 데이터 저장 방식  
- /data/system 내의 3개 파일에 애플리케이션 정보를 저장  
- packages.xml
  - Packages/Application과 permission 목록을 포함  
  - Permission은 <permission> 태그 하위에 표기  
  - Permission은 3가지 속성을 가진다.  
    - name: permission name을 말한다. AndroidManifest.xml에서 사용된다.  
    - package: package의 permission을 의미  
    - protection: 보안 레벨을 가리킨다.  
- packages.list  
  - Package Name, UID, flag, data directory 등의 정보를 갖는 텍스트 파일  
  - 중요 정보를 이용해 Package를 빠르게 찾을 수 있다.  
- packages-stopped.xml  
  - 정지된 상태의 package 목록을 갖는 파일 

---

## 안드로이드 6.0.1 r81 소스코드 참고 자료  
- [android 6.0.1 r81](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81)  
  - [core/java/content/pm](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/core/java/android/content/pm/)  
    - [PackageManager.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/core/java/android/content/pm/PackageManager.java)   
    - [PackageInstaller.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/core/java/android/content/pm/PackageInstaller.java)  
  - [services/core/java/com/android/server](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/services/core/java/com/android/server/pm)  
    - [PackageManagerService.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/services/core/java/com/android/server/pm/PackageManagerService.java)  
    - [PackageInstallerService.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r81/services/core/java/com/android/server/pm/PackageInstallerService.java)  

---

## 참고 자료 

- Pacakage Manager와 관련된 글
  - [Andorid PackageManager and PacakageInstaller Kor ver.](http://ghostkei.blogspot.com/2015/09/external-post-in-depth-android-package_32.html)
  - [Andorid PackageManager and PacakageInstaller Eng ver.](http://kpbird.blogspot.com/2012/10/in-depth-android-package-manager-and.html)
  - [Wikipedia: Android Application Package](https://en.wikipedia.org/wiki/Android_application_package)
  - [Quora: Android Package Installer](https://www.quora.com/What-is-an-Android-package-installer)

- about scanning  
  - [stackoverflow: scanning in spring and androind ](https://stackoverflow.com/questions/11421085/implementing-spring-like-package-scanning-in-android)
  - [blog: scanning in spring](https://hamait.tistory.com/322)
  - [scanning android package](https://www.opswat.com/blog/scanning-android-package-files-apks-metadefender-cloud)

- others  
  - [android boot process](https://hackernoon.com/the-android-boot-process-2ce4c498615b)
