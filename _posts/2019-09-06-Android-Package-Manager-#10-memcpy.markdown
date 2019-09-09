---
layout: post
title: "Android Package Manager #10 memcpy"
date: 2019-09-6 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

strlcpy 부분은 안드로이드 최신 버전에도 그대로 남아있다.  

### memcpy.c 소스 확인  

먼저, glibc (GNU C Library) 의 git-mirror 페이지 링크: [git-mirror/glibc](https://github.molgen.mpg.de/git-mirror/glibc)  
glibc의 memcpy.c 소스코드: [memcpy.c](https://github.molgen.mpg.de/git-mirror/glibc/blob/master/sysdeps/tile/tilegx/memcpy.c)  

#### 다른 참고 링크  

1. [strlen.c](https://github.molgen.mpg.de/git-mirror/glibc/blob/master/string/strlen.c)  
2. [https://sourceware.org/git/?p=glibc.git](https://sourceware.org/git/?p=glibc.git)  

### AOSP에서 memcpy 소스가 있는지 확인  

먼저, 안드로이드 6.0.1_r77에는 다음의 경로를 따라가면 memcpy.S 파일이 있다.   
.S 파일 확장자는 어셈블리어 소스코드이다.  
`platform/bionic/+/refs/tags/android-6.0.1_r77/libc/arch-mips/string`  
링크는 다음과 같다: [memcpy.S](https://android.googlesource.com/platform/bionic/+/refs/tags/android-6.0.1_r77/libc/arch-mips/string)  

<br>

안드로이드 8.0.0_r1 부터는 같은 경로에 memcpy.c가 포함되어 있다.  
링크는 다음과 같다: [memcpy.c](https://android.googlesource.com/platform/bionic/+/refs/tags/android-8.0.0_r1/libc/arch-mips/string/)  

<br>

glibc와 AOSP에 있는 memcpy.c 소스코드 구현이 다르다.  

### glibc에 있는 memcpy.c 소스코드 보기  

함수 반환형과 이름 사이에 있는 inhibit_loop_to_libcall, 변수 자료형과 이름 사이에 있는 `__restrict`에 대해서는 아래의 참고자료 보기.  
attribute라 부른다.  
컴파일러가 최적화하도록 어떤 것을 설정하는 기능을 한다.   
여기서는 macro로 attribute이 정의되어 있는 듯하다.  
<br>
29번째 줄,   
`size_t`는 unsigned data type이다.  
32bit 머신에서는 32bit로 표현 가능한 가장 큰 정수, 64bit machine에서는 64bit로 표현가능한 가장 큰 정수이다.  
`sizeof()`함수의 반환형이다.  
<br>
31,32번째 줄에서,  
`void *`형 매개변수를 `char *`로 형변환한다: 결국, char를 복사한다.  
`restrict` 관련 [위키피디아](https://en.wikipedia.org/wiki/Restrict) 참고: pointer aliasing 막아 compiling optimization 한다.    
<br>
35번째 줄,  
[이 사이트](https://code.woboq.org/userspace/glibc/sysdeps/generic/memcopy.h.html)를 보면 `unsigned long int`로 정의되어 있다.  
주석보면 destination memory에 대한 8바이트 포인터라고 표시되어 있다.  
`unsigned long int`는 64bit machine에서 8byte이다.  
<br>
39-44번째 줄,  
<span style="color:red">*왜 있는거지?*</span>  
char 개수가 16개보다 작으면 그대로 복사하고 반환한다.  
char는 자료형 크기가 1byte이고 16개가 있으면 128bit.  

<br>  

48-57번째 줄,  
48줄에서 src1의 끝 위치 찾는다.  
프로세서는 cache line을 거쳐 data를 cache로 가져온다.  
cache line은 크기는 프로세서마다 다르며 16~128바이트이다. 보통 64바이트이다. 이러한 cache line을 몇 개씩 가지고 있다.      
[관련 스택오버플로우](https://stackoverflow.com/questions/3928995/how-do-cache-lines-work)  
결국 미리 정의한 PREFETCH_LINES_AHEAD 수를 넘어가지 않는 범위에서, L2 cache line size만큼 prefetch에 더하면서 src1의 끝을 넘어가지 않도록 한다.  
즉 src1_end 넘어가지 않는 범위에서 얼마만큼 prefetch할지 결정한다.  
<br>
60번째 줄,  
`uintptr_t`은 포인터 타입이다.  
[관련 스택오버플로우-uintptr](https://stackoverflow.com/questions/1845482/what-is-uintptr-t-data-type)   
<span style="color:red">word-aligned???????????????</span>  
[stackoverflow-word-alignmnet](https://stackoverflow.com/questions/1584267/understanding-word-alignment)  
[quora-word-alignment](https://www.quora.com/What-is-word-Alignment)  
alignment는 machine이 access할 수 있는 block size.  
예를 들어, word가 32bit인 machine에서, instruction이 16bit일때,  
하나의 word씩 읽어 instruction을 실행하면, 두 개의 16bit instruction이 실행되어 에러발생한다.  
그래서 word-alignment해야한다.  
<br>
`op_t`가 8바이트(64bit, 1word)이니깐, bit and해서 null이 아니면 반복문 돌리면서, src를 dst에 복사하는데 word-aligned되어 복사된다.<span style="color:red">???????????</span>    
<br>
66번째 줄부터,  
`__builtin_expect`: branch prediction cache에 영향을 주어 성능 최적화, thrashing 줄인다.  
[스택오버플로우](https://stackoverflow.com/questions/7346929/what-is-the-advantage-of-gccs-builtin-expect-in-if-else-statements)  
두 번째 인자가 0이면, unlikely이고, else가 실행된다고 기대한다.  
<br>  
161번째 줄부터,  
위의 66번째 줄의 else 문이다.  
src를 8바이트 포인터로 바꿔주고,  
<span style="color:red">cache-line-aligned???????????????</span>  
[stackoverflow-cacheline-aligned](https://stackoverflow.com/questions/39971639/what-does-cacheline-aligned-mean)  
<br>
165-212줄,  
n이 chip line size보다 크면 8바이트 단위로 tmp에 저장한 뒤 dst에 옮겨준다.  
그리고 n을 64씩 줄여가며 진행  
<br>
200번째 줄,  
write hint instruction (WH)  
[write-hint](https://www.coursehero.com/file/pksdf5/Instruction-Descriptions-I-4143-41110-Write-Hint-Format-WH64-Rbab-Misc-format/)  
218번째 줄에서, 
나머지 부분 복사한 뒤 222번째 줄에서 반환  
<br>
228번째 줄부터,  
위에서 복사를 다 못하고, 남아있는 경우 이 줄부터 진행  
n < 8으로 남는 경우에 진행  
big endian, little endian 나눠서 진행...  
<br>

---

```
// memcpyTest.cpp: 콘솔 응용 프로그램의 진입점을 정의합니다.
//

#include "stdafx.h"

void * memcpy(void * dst, void const * src, size_t len)
{
	long * plDst = (long *)dst;
	long const * plSrc = (long const *)src;

	if (!((uintptr_t)src & 0xFFFFFFFC) && !( (uintptr_t)dst & 0xFFFFFFFC))
	{
		while (len >= 4)
		{
			*plDst++ = *plSrc++;
			len -= 4;
		}
	}

	char * pcDst = (char *)plDst;
	char const * pcSrc = (char const *)plSrc;
	while (len--)
	{
		*pcDst++ = *pcSrc++;
	}

	return (dst);
	}

#include <iostream>
using namespace std;

int main()
{
	char a[] = "abc";
	char b[] = "def";

	cout << (char *) memcpy(a, b, sizeof(a)) << endl;


	printf("hello\n");
    return 0;
}
```

[memcpy 최적화 관련 블로그](https://hypermin.tistory.com/entry/%EC%BD%94%EB%93%9C-%EC%98%B5%ED%8B%B0%EB%A7%88%EC%9D%B4%EC%A6%88memcpy-source-optimize)   



#### 참고자료
[attribute-syntax](https://gcc.gnu.org/onlinedocs/gcc-4.7.2/gcc/Attribute-Syntax.html#Attribute-Syntax)  
[function-attribute](https://gcc.gnu.org/onlinedocs/gcc-4.7.2/gcc/Function-Attributes.html)  












