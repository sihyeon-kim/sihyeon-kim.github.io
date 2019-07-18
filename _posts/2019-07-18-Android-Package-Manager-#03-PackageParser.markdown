---
layout: post
title: "Android Package Manager #03 tmp"
date: 2019-07-18 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

# PackageParser.java

- [PackageParser.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java)  
- parsePermission(), parsePermissionGroup(), parserPermissionTree()  

### private Permission parsePermission() [#2221](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#2221)  

- permission은 App manifest file 안에 포함된다.  
- 다음과 같은 형태이다.  
```
<permission android:description="string resource"
            android:icon="drawable resource"
            android:label="string resource"
            android:name="string"
            android:permissionGroup="string"
            android:protectionLevel=["normal" | "dangerous" |
                                     "signature" | ...] />
```  
- 해당 어플 혹은 다른 어플의 구성요소에 대한 접근을 제한하는 security permission을 선언한다. 

- private Permission parsePermission(Package owner, Resources res, XmlPullParser parser, AttributeSet attrs, String[] outError) 
    - 매개변수  
        - Package owner  
        - Resources res  
        - XmlPullParser parser  
        - AttributeSet attrs  
        - String[] outError   
    - 반환값
        - Permission 객체  
        
- Permission 객체 생성  
- res에서 Attributes 가져와 TypedArray sa에 저장  
- parsePackageItemInfo() 호출 [#2985](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#2985)  
  - 패키지에 대한 정보(로고, 아이콘, 패키지 이름)를 파싱한다  

- parseAllMetaData() [#3901](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#3901)  
  - <meta-data> element에 있는 meta data를 파싱하여 저장한다  
  - 위 메소드는 반복문을 통해 모든 meta-data를 검색하고 실제 파싱은 parseMetaData에서 일어난다 [#3935](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#3935)  

### private PermissionGroup parsePermissionGroup() [#2177](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#2177)
- <permission-group>은 manifest 파일에서 아래와 같은 형태이다  
            
```
<permission-group android:description="string resource"
                  android:icon="drawable resource"
                  android:label="string resource"
                  android:name="string" />            
```
- 관련된 permission의 그룹에 대한 이름을 선언한다  
- 마찬가지로 parsePackageItemInfo()와 parseAllMetaData()를 호출한다  
- 호출할 때 String tag 매개변수 값만 바뀐다  


### private Permission parserPermissionTree() [#2291](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#2291)
- <permission-tree>는 manifest 파일에서 아래와 같은 형태이다  

```
<permission-tree android:icon="drawable resource"
                 android:label="string resource" ]
                 android:name="string" />
```

- permissiont tree의 base name을 선언한다  
- 어플은 tree에 있는 모든 name에 대한 ownership을 가진다  
- 마찬가지로 parsePackageItemInfo()와 parseAllMetaData()를 호출한다  
- 호출할 때 String tag 매개변수 값만 바뀐다  

---  
---  
---  


- public void recycle() [#1019 TypedArray.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/res/TypedArray.java#1019)

- [#4663 public final static class Permission extends Component](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#4663)  

- [#4250 public final static class Package](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#4250)  

---  

### 참고자료  
1. [Developers Android](https://developer.android.com/guide/topics/manifest/permission-tree-element)
