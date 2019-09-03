---
layout: post
title: "Android Package Manager #09 strlcpy"
date: 2019-09-02 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

### sizeof

`sizeof`는 바이트 형식으로 반환한다. C++에서 `char`의 크기는 1바이트이다. 따라서 문자열 길이로 생각해도 된다.  

---

### ScopedUtfChars

[ScopedUtfChars.h](https://android.googlesource.com/platform/libnativehelper/+/idea133/include/nativehelper/ScopedUtfChars.h) 코드를 보면 `c_str()`의 반환 값 타입은 `char *`이다.  
`char *`의 크기는 32bit CPU에서는 32 비트, 즉 4바이트이고, 64bit CPU에서는 64비트, 즉 8바이트이다. 따라서, 넥서스 5에서 `sizeof(nativeLibPath.c_str())`의 출력은 4이다.  
자료형의 크기에 관한 자세한 사항은 [이 블로그](https://sckllo7.tistory.com/entry/32bit%EC%99%80-64bit%EC%9D%98-C-%EC%9E%90%EB%A3%8C%ED%98%95Data-Type-%ED%81%AC%EA%B8%B0-%EC%B0%A8%EC%9D%B4)를 참고하자.  

---

### 포인터

```
*(localFileName + nativeLibPath.size()) = '/';
```

배열 포인터 [관련된 글](https://dojang.io/mod/page/view.php?id=509)들을 참고하자.  
또는 이 글의 제일 아랫 부분 참고.  

---

### strlcpy

strlcpy는 c 표준 라이브러리에 포함되어 있지 않다. 코드는 다음 링크 [strlcpy.c](https://android.googlesource.com/platform/system/core/+/refs/tags/android-6.0.1_r81/libcutils/strlcpy.c)를 참고하자.  
l은 length를 의미한다.  

```
/*
 * Copy src to string dst of size siz.  At most siz-1 characters
 * will be copied.  Always NUL terminates (unless siz == 0).
 * Returns strlen(src); if retval >= siz, truncation occurred.
 */
size_t
strlcpy(char *dst, const char *src, size_t siz)
```
src에서 dst로 siz-1개의 문자열을 복사한다.  
그리고 siz번째에 null을 붙인다.  
반환 값은 생성하려고 한 문자열의 길이, 즉 src의 길이이다. 이때 길이는 null을 포함하지 않는다.  
예를 들어 char * src = '123'이라면, siz가 1이든 2이든 상관 없이 반환 값은 3이다. null을 포함한 문자열 길이인 4를 반환하는 것은 아니다.  

다음 실행 결과들을 참고하자.  
![pic](/assets/0902-01.png)  
![pic](/assets/0902-02.png)

---

### strncpy

```
char* strncpy(char* destination, const char* source, size_t num)
```
source에서 destination으로 num개의 문자열을 복사한다.  
destination끝에 항상 null이 붙는 것이 아니다.
반환 값은 destination이다.  

---

### memcpy

```
void* memcpy( void* dest, const void* src, std::size_t count );
```
반환 값은 dest이다.  

---

### C++ 배열 크기 할당: 런타임 vs 컴파일타임  


[variable-length-array-vla-in-c-compilers](https://stackoverflow.com/questions/39334435/variable-length-array-vla-in-c-compilers)  

[array-size-at-run-time-without-dynamic-allocation-is-allowed](https://stackoverflow.com/questions/737240/array-size-at-run-time-without-dynamic-allocation-is-allowed)  

---

### 217번째 줄 부터 동작 과정

[NativeLibraryHelper.cpp](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/jni/com_android_internal_content_NativeLibraryHelper.cpp?autodive=0%2F%2F%2F%2F%2F%2F%2F) 217번째 줄 부터 동작과정 예시    

예를 들어   
char *fileName = "kyungsoo"; // 8  
nativeLibPath.c_str()이 "jungle" 이라 가정 // 6  

strlen()은 null은 포함하지 않는 문자열 길이 반환한다.   
nativeLibPath.size()도 null을 포함하지 않는 문자열 길이 반환한다.  

218번째 줄에서    
localFileName[]의 크기는 16으로 초기화된다.     

220번재 줄에서 
localFileName은 "jungle\0"이 된다.  
이때 nativeLibPath 길이가 localFileName보다 길다면 localFileName에 맞춰서 null을 포함한 문자열을 복사하기 위해 strlcpy를 이용한다. 이 경우 strncpy는 null이 포함 안된다.  
strlcpy는 copy해서 생성하려는 문자열 길이를 반환한다. 이 경우 6을 반환. 이는 nativeLibPath 문자열 길이(크기)와 같다. 따라서 조건문 안으로 안들어간다.   

225번째 줄에서  
배열 localFileName에서 nativeLibPath.size() 번째 인덱스 자리에 character '/' 를 넣는다.  
그러면 배열 localFileName은 "jungle/"이 된다. 물론 마지막은 null ('\0')로 채워진다.  
(파일, 디렉토리 경로를 만들어주는 과정이다.)  

227번째 줄에서  
이제 localFileName + nativeLibPath.size() + 1 자리에, 즉 "jungle/" 그 다음 인덱스, 다시말해 인덱스 7부터 복사해 넣는다. fileName을 세 번째 매개변수 크기만큼 배열 localFileName 인덱스 7부터 넣는다.   

그럼 세 번째 매개 변수는 왜 sizeof(localFileName) - nativeLibPath.size() - 1 일까?  
이미 배열 localFileName에는 jungle/ 이 채워져있다. 즉 nativeLibPath와 '/'이 채워져 있으므로 해당하는 크기 nativeLibPath.size()와 1을 빼준다. 배열의 남은 공간에만 문자열을 복사할 수 있다.  

227번째 줄이 실행되면 localFileName은 "jungle/kyungsoo"가 된다. 이때에도 마지막은 null이다. null까지 포함한 문자열 길이는 16이다. 그래서 218번째 줄을 다시 보면, 배열을 초기화할 때 +2가 필요한 것이다. '/'과 null을 위한 2개이다.  

이제 nativeLibPath 경로와 fileName(파일 이름)로 이루어진 경로 "jungle/kyungsoo"가 완성되었다.    

<b>그럼 조건문 속으로 언제 들어갈까????</b>  
조건문 안으로 들어가지 않는다.  
strlcpy는 두 번째 매개변수 source의 길이를 반환한다.  
그리고 조건문에서 이 두 번째 매개변수 길이들을 비교한다. 그래서 조건문 안으로 들어갈 수 없다.  
