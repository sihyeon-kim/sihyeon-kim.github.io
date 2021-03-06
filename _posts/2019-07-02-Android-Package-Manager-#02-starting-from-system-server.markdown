---
layout: post
title: "Android Package Manager #02 starting from system server"
date: 2019-07-02 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

- 안드로이드 버전: android 6.0.1 r77  
- [[ code ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77)]   

### 로그 출력 7월 2일  
- 소스 빌드 및 DEBUG_PACKAGE_SCANNING 로그 출력 참고  
[blog: kyungsoo](https://rudtn082.github.io/android/PackageManager2-post)  
[blog: jung geun](https://im8768.github.io/15th-post/)  

### 7월 11일  
- 세 부분으로 나누어 진행  
  - 넥서스 5에 소스를 올려 스캐닝관련 로그 출력: 기기에 직접 올리면 로그가 더 많이 나올 것이라 생각    
  - DEBUG_PACKAGE_SCANNING 플래그 관련 코드 분석: 로그 출력과 관련된 플래그 주변 코드가 결국 중요할 것이라 생각    
  - PackageManagerService 생성자 코드 분석: 부팅 과정에 따라 SystemServer에서 호출하는 생성자를 보면 스캐닝 동작 과정 이해할 수 있을 것이라 생각  

---

# SystemServer.java에서 시작  
- 먼저 SystemServer.java 에서 PackageManagerService를 생성한다  
- [ code: SystemServer.java #364 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/java/com/android/server/SystemServer.java#364)  
```
        // Start the package manager.
        Slog.i(TAG, "Package Manager");
        mPackageManagerService = PackageManagerService.main(mSystemContext, installer,
                mFactoryTestMode != FactoryTest.FACTORY_TEST_OFF, mOnlyCore);
        mFirstBoot = mPackageManagerService.isFirstBoot();
        mPackageManager = mSystemContext.getPackageManager();
```  
- 그 다음 PackageManagerService.main 함수를 살펴본다.  

## PackageManagerService.java  
- [ code: PackageManagerService.java ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java)  
- [ code: PackageManagerService.java #1766 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#1766)  
```
    public static PackageManagerService main(Context context, Installer installer,
            boolean factoryTest, boolean onlyCore) {
        PackageManagerService m = new PackageManagerService(context, installer,
                factoryTest, onlyCore);
        ServiceManager.addService("package", m);
        return m;
    }
```   
- 위 코드에서 PackageManagerService 인스턴스를 생성한다.  
- 그리고 ServiceManager의 addService를 통해 PackageManagerService를 등록한다.   
- 이제 PacakageManagerService 클래스의 생성자 코드를 찾아본다.  

### PackageManagerService Constructor in PackageManagerService.java  
- [ code: PackageManagerService.java #1802 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#1802)  
- 이 생성자에서 sharedUserID 설정, App Directory 초기화, Framework 로딩, Packages 수집 등 다양한 역할을 한다.  
- 생성자 기능을 코드를 통해 개략적으로 살펴보면 아래와 같다.  
  - sharedUserID 설정 [ code: PackageManagerService.java #1816 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#1816)  
  - Handler설정, App Directory 초기화 [ code: PackageManagerService.java #1872 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#1872)    
    - File( parent directory, child file ): parent directory의 child file에 대한 객체 생성  
  - mSettings.readLPw() [ code: PackageManagerService.java #1913 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#1913)  
    - readLPw method [ code: Settings.java #2500 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/Settings.java#2500)  
    - packages.xml을 읽는다. packages.xml에는 permission, permission-trees, shared-user 등과 같은 애플리케이션의 정보가 있다.  
    - 아래는 Setting.java에서 mSettingsFilename 변수  
    ```
    mSettingsFilename = new File(mSystemDir, "packages.xml");
    ```
  - SCAN START [ code: PackageManagerService.java #1925 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#1925)  
  - BOOTCLASSPATH 가져옴 [ code: PackageManagerService.java #1936 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#1936)  
  - ensure external libraries have had dexopt run on them [ code: PackageManagerService.java #1967 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#1967)  
    - 주석: For now, we're compiling these system "shared libraries" (and framework jars) into all available architectures. It's possible to compile them only when we come across an app that uses them (there's already logic for that in scanPackageLI) but that adds some complexity.  
  - Framework 로딩: .apk와 .jar 파일을 로딩한다. [ code: PackageManagerService.java #1998 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#1998)  
    - .apk와 .jar을 불러와 alreadyDexOpted에 넣는다.  
    - ABI(Appication Binary Interface) 
  - collect packages [ code: PackageManagerService.java #2063 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#2063)  
    - 여기서 scanDirLI 함수 살펴보자. PackageParser가 눈에 띈다. 여기서 스캐닝이 일어나고, 이후의 코드에서 불필요한 패키지 리스트를 제거하는 등의 작업이 이루어진다.  
    - 해당 디렉토리의 파일 객체를 생성해 스캔을 진행한다.  
    - OEM(Original Equipment Manufacturer)  
##### *중간에 코드 아직 안봤음!!!!!!!*
  - SCAN END [ code: PackageManagerService.java #2271 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#2271)  

- 아래는 nexus5 로그 중 time to scan 부분
![log](/assets/log-nexus5.png)

### scanDirLI method in PackageManagerService.java  
-  [ code: PackageManagerService.java #5625 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#5625)   

### scanPackageLI method    
- [ code: PackageManagerService.java #5736 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#5736)   

---

# 참고 자료  
1. [웹사이트: Android Framework analysis-PackageManager analysis](https://www.programering.com/a/MzN5QzNwATk.html)  
2. [Naver Blog: Package Scanning의 간략한 과정](http://blog.naver.com/PostView.nhn?blogId=hyup8509&logNo=130150211745)  
3. [Google Books: Android Security Internals](https://books.google.co.kr/books?id=-QcvDwAAQBAJ&pg=PA64&dq=packagemanagerservice&hl=ko&sa=X&ved=0ahUKEwidvsHFppbjAhVtEqYKHc_oCiwQ6AEIKDAA#v=onepage&q=packagemanagerservice&f=false)  
