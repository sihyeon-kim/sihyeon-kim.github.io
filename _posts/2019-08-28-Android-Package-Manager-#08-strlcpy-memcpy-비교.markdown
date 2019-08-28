---
layout: post
title: "Android Package Manager #08 strlcpy memcpy 비교"
date: 2019-08-28 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

소스 코드: [NativeLibraryHelper.cpp](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/jni/com_android_internal_content_NativeLibraryHelper.cpp?autodive=0%2F%2F%2F%2F%2F%2F%2F)  

### findSupportedAbi와 copyNativeBinaries 속도 측정
findSupportedAbi가 호출하는 cpp 코드에서 findSupportedAbi와 copyNativeBinaries에 해당하는 cpp 코드인 copyFileIfChanged 부분의 시간을 측정한 결과는 아래와 같다.  

![이미지](https://cdn.discordapp.com/attachments/563317164296372236/616227606387359754/unknown.png)  

![이미지](https://cdn.discordapp.com/attachments/563317164296372236/616228540215722013/unknown.png)

find 보다 copy에서 시간이 많이 걸렸다.  
단위는 밀리세컨드이다.  
cpp에서 시간을 측정했는데 이상하게 시간이 오래 걸렸다.  
부팅 과정 중 혹은 부팅이 끝난 다음 앱 최적화 과정때문에 시간이 오래걸리는 것으로 추정된다.

### strlcpy와 memcpy  
코드만 보면 strlcpy와 memcpy는 큰 차이가 없지만 기계어 코드를 보면 다르다.  
memcpy는 레지스터를 사용해 고속 복사가 가능하다.  
strlcpy의 경우 string의 끝을 체크해야하므로 고속 복사가 불가능하다.  
따라서 string의 복사라 하더라도 string 크기가 정해져 있을 경우 memcpy를 사용하는 것이 좋다.  

### 반복문을 이용한 strlcpy와 memcpy 실행시간 측정

반복문 1억 번 돌면서 memcpy와 strlcpy 함수를 각각 호출하였을 때, memcpy는 약 0.160초 걸렸고 strlcpy는 약 4초 정도 걸렸다.  

![memcpy](/assets/memcpy-time.png)

![strlcpy](/assets/strlcpy-time.png)

### strlcpy 코드를 memcpy로 바꾸기  
memcpy는 주솟값을 반환한다.  
strlcpy는 문자열의 size 즉 길이를 반환한다.  
memcpy를 사용할 경우 문자열 크기를 계산하는 함수를 한 번 더 호출해야한다.  

##### 변경 전
```
if (strlcpy(localTmpFileName, nativeLibPath.c_str(), sizeof(localTmpFileName)) != nativeLibPath.size()) {

}
```

##### 변경 후
```
memcpy(localTmpFileName, nativeLibPath.c_str(), sizeof(localTmpFileName)
if( sizeof(localTmpFileName) != nativeLibPath.size()) {

}
```

![이미지](https://cdn.discordapp.com/attachments/563317164296372236/616265622783000579/memcpy1.JPG)

변경 후 크기를 비교하는 조건문 부분에서 문제가 발생하여 fail error가 발생할 시 로그를 출력하고 반환하는 조건문 부분을 전부 삭제해 주었다. 변경 전에 에러를 출력하는 로그가 나오지 않았으므로 올바르게 동작한다고 가정한다.

![이미지](https://cdn.discordapp.com/attachments/563317164296372236/616265649043537958/code.JPG)  

코드 변경 후 PackageManagerService.java의 derivePackageAbi 메소드 실행 시간을 측정해 보았을 때 약 0.01초 정도의 성능 향상을 보였다. 하지만 프로젝트를 진행하는 과정에서 로그를 무분별하게 찍어 놓아서 해당 로그가 맞는지 정확히 다시 확인하고 시간을 측정할 필요가 있다.  

### 의문점  
![이미지](https://cdn.discordapp.com/attachments/563317164296372236/616234445799817220/unknown.png)

cpp 코드에서 시간을 측정하는 로그를 출력하는 과정에서 위와 같은 로그 코드를 작성하였는데 시간 측정하는 로그는 나오고, 원래 있던 로그는 나오지 않는 문제가 발생했다.
