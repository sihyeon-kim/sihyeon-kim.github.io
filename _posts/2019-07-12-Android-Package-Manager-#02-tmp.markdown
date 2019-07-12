---
layout: post
title: "Android Package Manager #03 tmp"
date: 2019-07-12 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

- scanDirLI에서 scanPackageLI 호출하는 부분 [ #5645 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#5645)    
  - scanPackageLI는 overloading 되어 있다  
  - scanDirLI에서 호출하는 scanPackageLI 부분은 [ #5732 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#5732) 번째 줄인 file을 parameter로 받는 부분  
    - 위 코드 #5732 에서는 PackageParser.Package pkg를 parameter로 가지는 scanPacakageDirtyLI 를 호출한다. [ #5941 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#5941)   


- scanPackageLI(File scanFile, int parseFlags, int scanFlags, long currentTime, UserHandle user) [ #5732 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#5732)    
```
    private PackageParser.Package scanPackageLI(File scanFile, int parseFlags, int scanFlags,
            long currentTime, UserHandle user) throws PackageManagerException
```



- scanPackageLI(PackageParser.Package pkg, int parseFlags, int scanFlags, long currentTime, UserHandle user)
[ #6467 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#6467)    
```
    private PackageParser.Package scanPackageLI(PackageParser.Package pkg, int parseFlags,
            int scanFlags, long currentTime, UserHandle user) throws PackageManagerException
```
