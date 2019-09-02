---
layout: post
title: "Android Package Manager #09 abcde"
date: 2019-09-02 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

### sizeof

`sizeof`는 바이트 형식으로 반환한다. C++에서 `char`의 크기는 1바이트이다. 따라서 문자열 길이로 생각해도 된다.  

---
---

### ScopedUtfChars

[ScopedUtfChars.h](https://android.googlesource.com/platform/libnativehelper/+/idea133/include/nativehelper/ScopedUtfChars.h) 코드를 보면 `c_str()`의 반환 값 타입은 `char *`이다.  
`char *`의 크기는 32bit CPU에서는 32 비트, 즉 4바이트이고, 64bit CPU에서는 64비트, 즉 8바이트이다. 따라서, 넥서스 5에서 `sizeof(---.c_str())`의 출력은 4이다.  

---
---

### 포인터

```
*(localFileName + nativeLibPath.size()) = '/';
```

---
---

### strlcpy

[strlcpy.c](https://android.googlesource.com/platform/system/core/+/refs/tags/android-6.0.1_r81/libcutils/strlcpy.c) 코드  

```
/*
 * Copy src to string dst of size siz.  At most siz-1 characters
 * will be copied.  Always NUL terminates (unless siz == 0).
 * Returns strlen(src); if retval >= siz, truncation occurred.
 */
size_t
strlcpy(char *dst, const char *src, size_t siz)
```

---
---

### memcpy

---
---

### C++ 배열 크기 할당: 런타임 vs 컴파일타임  


[variable-length-array-vla-in-c-compilers](https://stackoverflow.com/questions/39334435/variable-length-array-vla-in-c-compilers)  

[array-size-at-run-time-without-dynamic-allocation-is-allowed](https://stackoverflow.com/questions/737240/array-size-at-run-time-without-dynamic-allocation-is-allowed)  

