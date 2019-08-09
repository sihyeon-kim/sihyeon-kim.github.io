---
layout: post
title: "Android Package Manager #06 AVD"
date: 2019-08-11 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

# AVD에 어플 설치하기  
- Ubuntu 16.04, Android 6.0.1_r77, emulator  
- 참고자료: [AOSP AVD에 앱 설치 - stackoverflow](https://stackoverflow.com/questions/10579827/how-do-i-add-apks-in-an-aosp-build), [APK 설치](https://bottlecok.tistory.com/100)  
- 결과  
  - 안드로이드 앱을 APK로 만들어 AVD에 설치 가능  
  - /system/app 폴더에 설치 가능  
  - /data/app 폴더에 설치 시도하였으나 안됨, AOSP build 자체에서 user app 설치가 불가능하다는 [스택오버플로우 답변](https://stackoverflow.com/questions/11984572/how-do-i-install-an-app-into-the-data-app-folder-instead-of-the-system-app-folde) 있음    
  - 구글 플레이 스토어 같은 어플의 APK를 받아 시도하였으나 안됨, 다운받는 곳의 출처가 불명확한 문제가 있으며 APK와 AVD의 안드로이드 버전이 안 맞을 가능성 있음    

![result](/assets/avd-app-test.png)
