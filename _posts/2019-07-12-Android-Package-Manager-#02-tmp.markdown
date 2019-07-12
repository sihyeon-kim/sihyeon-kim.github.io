---
layout: post
title: "Android Package Manager #03 tmp"
date: 2019-07-12 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

- 아래 링크는 scanDirLI에서 scanPackageLI 호출하는 부분 [ #5645 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#5645)    
  - scanPackageLI는 overloading 되어 있다  
  - scanDirLI에서 호출하는 부분은 [ #5736 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#5732) 줄인 file을 변수로 받는 부분  
    - 위 코드 #5736 줄에서 호출하는 scanPackageLI는 scanPacakageDirtyLI를 호출하지 않는다  


- scanPackageLI(File scanFile, int parseFlags, int scanFlags, long currentTime, UserHandle user) [ #5736 ](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/services/core/java/com/android/server/pm/PackageManagerService.java#5732)    
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
