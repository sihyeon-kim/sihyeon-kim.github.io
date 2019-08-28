---
layout: post
title: "Android Package Manager #08 strlcpy memcpy 비교"
date: 2019-08-28 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

소스 코드: [NativeLibraryHelper.cpp](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/jni/com_android_internal_content_NativeLibraryHelper.cpp?autodive=0%2F%2F%2F%2F%2F%2F%2F)  

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

변경 후 크기를 비교하는 조건문 부분에서 문제가 발생하여 fail error가 발생할 시 로그를 출력하고 반환하는 조건문 부분을 전부 삭제해 주었다. 변경 전에 에러를 출력하는 로그가 나오지 않았으므로 올바르게 동작한다고 가정한다.

![이미지](https://media.discordapp.net/attachments/563317164296372236/616265649043537958/code.JPG?width=500&height=276)
