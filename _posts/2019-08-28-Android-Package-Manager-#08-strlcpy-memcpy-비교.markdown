---
layout: post
title: "Android Package Manager #08 strlcpy memcpy 비교"
date: 2019-08-28 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

반복문 1억 번 돌면서 memcpy와 strlcpy 함수를 각각 호출하였을 때, memcpy는 약 0.160초 걸렸고 strlcpy는 약 4초 정도 걸렸다.  

![memcpy](/assets/memcpy-time.png)

![strlcpy](/assets/strlcpy-time.png)

memcpy는 주솟값을 반환한다.  
strlcpy는 문자열의 size 즉 길이를 반환한다.  
memcpy를 사용할 경우 문자열 크기를 계산하는 함수를 한 번 더 호출해야한다.  
