---
layout: post
title: "Android Package Manager #06 AOSP 빌드 시 APK를 이용하여 AVD에 어플 설치"
date: 2019-08-11 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

# AVD에 어플 설치하기  
- 환경: Ubuntu 16.04, Android 6.0.1_r77, emulator  
- 참고자료: [AOSP AVD에 앱 설치 - stackoverflow](https://stackoverflow.com/questions/10579827/how-do-i-add-apks-in-an-aosp-build), [APK 설치](https://bottlecok.tistory.com/100)  
- 결과  
  - 안드로이드 앱을 APK로 만들어 AVD에 설치 가능  
  - /system/app 폴더에 설치 가능  
  - /data/app 폴더에 앱을 설치하려고 시도하였으나 안됨, AOSP build 자체에서 user app 설치가 불가능하다는 [스택오버플로우 답변](https://stackoverflow.com/questions/11984572/how-do-i-install-an-app-into-the-data-app-folder-instead-of-the-system-app-folde) 있음    
  - 구글 플레이 스토어 같은 어플의 APK를 받아 시도하였으나 안됨, 다운받는 곳의 출처가 불명확한 문제가 있으며 APK와 AVD의 안드로이드 버전이 안 맞을 가능성 있음    

![result](/assets/avd-app-test.png)  

![result](/assets/avd-data-app-test.png)  

- 방법([참고자료](https://stackoverflow.com/questions/10579827/how-do-i-add-apks-in-an-aosp-build))  
1. *< aosp root >/packages/apps/< your app folder >*  
  위와 같은 경로가 되도록 */packages/apps/*에 폴더 생성  
2. 위에서 생성한 폴더 안에 *< yourapp.apk >* 와 *Android.mk* 파일을 만든다.  
  apk 파일은 안드로이드 스튜디오를 통해 만들 수 있다.  
3. *Android.mk* 파일은 다음을 포함한다.  
  ```
  LOCAL_PATH := $(call my-dir)

  include $(CLEAR_VARS)

  LOCAL_MODULE_TAGS := optional

  LOCAL_MODULE := < your app folder name >

  LOCAL_CERTIFICATE := < desired key >

  LOCAL_SRC_FILES := < app apk filename >

  LOCAL_MODULE_CLASS := APPS

  LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

  include $(BUILD_PREBUILT)
  ```  
4. *< aosp root >/build/target/product/core.mk*에 다음을 포함한다.  
  ```
  PRODUCT_PACKAGES += < what you have defined in LOCAL_MODULE, it should be your app folder name >
  ```
5. 다시 make하고 실행한다.  
