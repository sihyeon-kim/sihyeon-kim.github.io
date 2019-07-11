---
layout: post
title: "Android Package Manager #02 starting from system server"
date: 2019-07-02 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

- 안드로이드 버전: android 6.0.1 r77  
- [[ code ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77)]   

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
  - Framework 로딩: .apk와 .jar 파일을 로딩한다. [ code: PackageManagerService.java #1998 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#1998)  
  - collect packages [ code: PackageManagerService.java #2063 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#2063)  
    - *여기서 scanDirLI 함수를 자세히 살펴보자. PackageParser가 눈에 띈다. 여기서 스캐닝이 일어나고, 이후의 코드에서 불필요한 패키지 리스트를 제거하는 등의 작업이 이루어진다고 생각된다.*  
```
            // Collect vendor overlay packages.
            // (Do this before scanning any apps.)
            // For security and version matching reason, only consider
            // overlay packages if they reside in VENDOR_OVERLAY_DIR.
            File vendorOverlayDir = new File(VENDOR_OVERLAY_DIR);
            scanDirLI(vendorOverlayDir, PackageParser.PARSE_IS_SYSTEM
                    | PackageParser.PARSE_IS_SYSTEM_DIR, scanFlags | SCAN_TRUSTED_OVERLAY, 0);
            // Find base frameworks (resource packages without code).
            scanDirLI(frameworkDir, PackageParser.PARSE_IS_SYSTEM
                    | PackageParser.PARSE_IS_SYSTEM_DIR
                    | PackageParser.PARSE_IS_PRIVILEGED,
                    scanFlags | SCAN_NO_DEX, 0);
            // Collected privileged system packages.
            final File privilegedAppDir = new File(Environment.getRootDirectory(), "priv-app");
            scanDirLI(privilegedAppDir, PackageParser.PARSE_IS_SYSTEM
                    | PackageParser.PARSE_IS_SYSTEM_DIR
                    | PackageParser.PARSE_IS_PRIVILEGED, scanFlags, 0);
```
```
// Prune any system packages that no longer exist.
            final List<String> possiblyDeletedUpdatedSystemApps = new ArrayList<String>();
            if (!mOnlyCore) {
                Iterator<PackageSetting> psit = mSettings.mPackages.values().iterator();
                while (psit.hasNext()) {
                    PackageSetting ps = psit.next();
                    /*
                     * If this is not a system app, it can't be a
                     * disable system app.
                     */
                    if ((ps.pkgFlags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        continue;
                    }
                    /*
                     * If the package is scanned, it's not erased.
                     */
                    final PackageParser.Package scannedPkg = mPackages.get(ps.name);
                    if (scannedPkg != null) {
                        /*
                         * If the system app is both scanned and in the
                         * disabled packages list, then it must have been
                         * added via OTA. Remove it from the currently
                         * scanned package so the previously user-installed
                         * application can be scanned.
                         */
                        if (mSettings.isDisabledSystemPackageLPr(ps.name)) {
                            logCriticalInfo(Log.WARN, "Expecting better updated system app for "
                                    + ps.name + "; removing system app.  Last known codePath="
                                    + ps.codePathString + ", installStatus=" + ps.installStatus
                                    + ", versionCode=" + ps.versionCode + "; scanned versionCode="
                                    + scannedPkg.mVersionCode);
                            removePackageLI(ps, true);
                            mExpectingBetter.put(ps.name, ps.codePath);
                        }
                        continue;
                    }
                    if (!mSettings.isDisabledSystemPackageLPr(ps.name)) {
                        psit.remove();
                        logCriticalInfo(Log.WARN, "System package " + ps.name
                                + " no longer exists; wiping its data");
                        removeDataDirsLI(null, ps.name);
                    } else {
                        final PackageSetting disabledPs = mSettings.getDisabledSystemPkgLPr(ps.name);
                        if (disabledPs.codePath == null || !disabledPs.codePath.exists()) {
                            possiblyDeletedUpdatedSystemApps.add(ps.name);
                        }
                    }
                }
```

### scanDirLI method in PackageManagerService.java  
-  [ code: PackageManagerService.java #5625 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#5625)
```

    private void scanDirLI(File dir, int parseFlags, int scanFlags, long currentTime) {
        final File[] files = dir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            Log.d(TAG, "No files in app dir " + dir);
            return;
        }
        if (DEBUG_PACKAGE_SCANNING) {
            Log.d(TAG, "Scanning app dir " + dir + " scanFlags=" + scanFlags
                    + " flags=0x" + Integer.toHexString(parseFlags));
        }
        
        ...
        
    }
```
- 위 메소드에서 DEBUG_PACKAGE_SCANNING flag가 보인다.

---

### 로그 출력 7월 2일  
- 소스 빌드 및 DEBUG_PACKAGE_SCANNING 로그 출력 참고  
[blog: kyungsoo](https://rudtn082.github.io/android/PackageManager2-post)  
[blog: jung geun](https://im8768.github.io/15th-post/)  

---

# 참고 자료  
1. [웹사이트: Android Framework analysis-PackageManager analysis](https://www.programering.com/a/MzN5QzNwATk.html)  
2. [Naver Blog: Package Scanning의 간략한 과정](http://blog.naver.com/PostView.nhn?blogId=hyup8509&logNo=130150211745)  
3. [Google Books: Android Security Internals](https://books.google.co.kr/books?id=-QcvDwAAQBAJ&pg=PA64&dq=packagemanagerservice&hl=ko&sa=X&ved=0ahUKEwidvsHFppbjAhVtEqYKHc_oCiwQ6AEIKDAA#v=onepage&q=packagemanagerservice&f=false)  
