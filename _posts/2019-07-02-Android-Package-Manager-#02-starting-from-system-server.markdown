---
layout: post
title: "Android Package Manager #02 starting from system server"
date: 2019-07-02 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

- 안드로이드 버전: android 6.0.1 r77  
- [[ code ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77)]   

---

# SystemServer.java에서 시작  
- 먼저 SystemServer.java 에서 PackageManagerService를 생성한다  
- [ code ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/java/com/android/server/SystemServer.java#364)  
```
        // Start the package manager.
        Slog.i(TAG, "Package Manager");
        mPackageManagerService = PackageManagerService.main(mSystemContext, installer,
                mFactoryTestMode != FactoryTest.FACTORY_TEST_OFF, mOnlyCore);
        mFirstBoot = mPackageManagerService.isFirstBoot();
        mPackageManager = mSystemContext.getPackageManager();
```  




