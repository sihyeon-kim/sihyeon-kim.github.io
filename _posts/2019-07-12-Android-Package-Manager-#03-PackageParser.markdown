---
layout: post
title: "Android Package Manager #03 tmp"
date: 2019-07-18 20:00:00
author: Sihyeon Kim
categories: android-packagemanager
---

# PackageParser.java

- [PackageParser.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java)  

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
    - private boolean parsePackageItemInfo 호출 [#2985](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#2985)  

```
    private Permission parsePermission(Package owner, Resources res,
            XmlPullParser parser, AttributeSet attrs, String[] outError)
        throws XmlPullParserException, IOException {
        Permission perm = new Permission(owner);
        TypedArray sa = res.obtainAttributes(attrs,
                com.android.internal.R.styleable.AndroidManifestPermission);

        // 패키지 정보를 파싱한다  
        // 
        if (!parsePackageItemInfo(owner, perm.info, outError,
                "<permission>", sa,
                com.android.internal.R.styleable.AndroidManifestPermission_name,
                com.android.internal.R.styleable.AndroidManifestPermission_label,
                com.android.internal.R.styleable.AndroidManifestPermission_icon,
                com.android.internal.R.styleable.AndroidManifestPermission_logo,
                com.android.internal.R.styleable.AndroidManifestPermission_banner)) {
            sa.recycle();
            mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        
        // Note: don't allow this value to be a reference to a resource
        // that may change.
        perm.info.group = sa.getNonResourceString(
                com.android.internal.R.styleable.AndroidManifestPermission_permissionGroup);
        if (perm.info.group != null) {
            perm.info.group = perm.info.group.intern();
        }
        
        perm.info.descriptionRes = sa.getResourceId(
                com.android.internal.R.styleable.AndroidManifestPermission_description,
                0);
        perm.info.protectionLevel = sa.getInt(
                com.android.internal.R.styleable.AndroidManifestPermission_protectionLevel,
                PermissionInfo.PROTECTION_NORMAL);
        perm.info.flags = sa.getInt(
                com.android.internal.R.styleable.AndroidManifestPermission_permissionFlags, 0);
        sa.recycle();
        if (perm.info.protectionLevel == -1) {
            outError[0] = "<permission> does not specify protectionLevel";
            mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        perm.info.protectionLevel = PermissionInfo.fixProtectionLevel(perm.info.protectionLevel);
        if ((perm.info.protectionLevel&PermissionInfo.PROTECTION_MASK_FLAGS) != 0) {
            if ((perm.info.protectionLevel&PermissionInfo.PROTECTION_MASK_BASE) !=
                    PermissionInfo.PROTECTION_SIGNATURE) {
                outError[0] = "<permission>  protectionLevel specifies a flag but is "
                        + "not based on signature type";
                mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return null;
            }
        }
        
        if (!parseAllMetaData(res, parser, attrs, "<permission>", perm,
                outError)) {
            mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        owner.permissions.add(perm);
        return perm;
    }
```

### private PermissionGroup parsePermissionGroup() [#2177](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#2177)

### private Permission parserPermissionTree() [#2291](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#2291)

- public void recycle() [#1019 TypedArray.java](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/res/TypedArray.java#1019)

- permission permissiongroup permissiontree  

- private Permission parsePermission(Package owner, Resources res, XmlPullParser parser, AttributeSet attrs, String[] outError)  

- [#4663 public final static class Permission extends Component](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#4663)  

- [#4250 public final static class Package](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-6.0.1_r77/core/java/android/content/pm/PackageParser.java#4250)  

### 참고자료  
1. [Developers Android](https://developer.android.com/guide/topics/manifest/permission-tree-element)
