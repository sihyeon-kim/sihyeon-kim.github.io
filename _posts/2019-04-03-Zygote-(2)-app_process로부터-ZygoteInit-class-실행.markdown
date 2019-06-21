---
layout: post
title: "Zygote (2) app_process로부터 ZygoteInit class 실행"
date: 2019-04-03 19:00:00
author: Sihyeon Kim
categories: android-framework-study
---

최종 수정일 : 2019-04-03

# Zygote가 실행되는 과정을 실제 코드를 통해 확인
### app_process로부터 ZygoteInit class 실행
- app_process : 달빅 가상 머신 생성 -> 가상 머신 위에 ZygoteInit 클래스를 로딩하고 실행  

#### (1) init 프로세스가 app_process 실행  
```
// android froyo
// system/core/rootdir/init.rc 중 service zygote

service zygote /system/bin/app_process -Xzygote /system/bin --zygote --start-system-server
    socket zygote stream 666
    onrestart write /sys/android_power/request_state wake
    onrestart write /sys/power/state on
    onrestart restart media
```

```
app_process -Xzygote /system/bin --zygote --start-system-server
```

- app_process 실행 규칙  

```
app_process [java-options] cmd-dir start-class-name [options]
```

- java-options : 가상머신으로 전달되는 옵션  
- cmd-dir : 프로세스가 실행될 디렉토리  
- start-class-name : 가상 머신에서 생성할 클래스 이름, app_process는 전달받은 클래스를 가상 머신으로 로딩한 후 해당 클래스의 main() 메소드 호출  
- options : 실행될 클래스로 전달될 옵션  

---
### AppRuntime 객체 생성 및 실행

```
// android froyo
// app_process/app_main.cpp 중 main 함수

int main(int argc, const char* const argv[])
{
    // These are global variables in ProcessState.cpp
    mArgC = argc;
    mArgV = argv;
    
    mArgLen = 0;
    for (int i=0; i<argc; i++) {
        mArgLen += strlen(argv[i]) + 1;
    }
    mArgLen--;
    AppRuntime runtime; // (2)
    const char *arg;
    const char *argv0;
    argv0 = argv[0];
    // Process command line arguments
    // ignore argv[0]
    argc--;
    argv++;
    // Everything up to '--' or first non '-' arg goes to the vm
    
    int i = runtime.addVmArguments(argc, argv); // (3)
    // Next arg is parent directory
    if (i < argc) {
        runtime.mParentDir = argv[i++]; // (4)
    }
    // Next arg is startup classname or "--zygote"
    if (i < argc) {
        arg = argv[i++];
        if (0 == strcmp("--zygote", arg)) { // (5)
            bool startSystemServer = (i < argc) ? 
                    strcmp(argv[i], "--start-system-server") == 0 : false;
            setArgv0(argv0, "zygote");
            set_process_name("zygote");
            runtime.start("com.android.internal.os.ZygoteInit",
                startSystemServer); // (6)
        } else {
            set_process_name(argv0);
            runtime.mClassName = arg;
            // Remainder of args get passed to startup class main()
            runtime.mArgC = argc-i;
            runtime.mArgV = argv+i;
            LOGV("App process is starting with pid=%d, class=%s.\n",
                 getpid(), runtime.getClassName());
            runtime.start();
        }
    } else {
        LOG_ALWAYS_FATAL("app_process: no class name or --zygote supplied.");
        fprintf(stderr, "Error: no class name or --zygote supplied.\n");
        app_usage();
        return 10;
    }
}
```

#### (2) AppRuntime 객체 생성  
AppRuntime 객체는 AndroidRuntime 클래스를 상속한다.  
AndroidRuntime 클래스는 달빅 가상 머신을 초기화하고 실행한다.  

#### (3) main() 함수에 전달된 인자 값을 분석해서 AppRuntime 객체에 전달한다.  
-Xzygote 값은 AppRuntime의 mOption 변수에 저장  

#### (4) 실행 디렉토리 경로 /system/bin은 AppRuntime의 mParentDir 변수에 저장  

#### (5) 가상 머신에서 생성할 클래스 이름 확인  
클래스 이름이 --zygote인가 아니가에 따라 처리 과정이 달라지지만 결국 가상 머신 상에서 주어진 클래스를 로딩한다.

#### (6) AppRuntime의 start() 멤버 함수를 호출  
가상 머신이 생성되고 초기화된다.   
가상 머신에서 ZygoteInit 클래스를 로딩하고 main() 메소드로 실행 흐름이 바뀐다.  
runtime()의 첫 번째 인자로 클래스의 패키지 이름과 클래스 이름을 명시  
패키지를 경로로 해석해 ZygoteInit 클래스를 찾아 로딩한다.  
runtime.start() 두 번째 인자는 --start-system-server가 전달됐으므로 true가 된다.  

---
### 달빅 가상 머신 생성

```
// android froyo
// framework/base/core/jni/AndroidRuntime.cpp 중 startVM() 함수, start() 함수

/*
 * Start the Dalvik Virtual Machine.
 *
 * Various arguments, most determined by system properties, are passed in.
 * The "mOptions" vector is updated.
 *
 * Returns 0 on success.
 */
int AndroidRuntime::startVm(JavaVM** pJavaVM, JNIEnv** pEnv)
{
    
    // 생략
    
    enum {
      kEMDefault,
      kEMIntPortable,
      kEMIntFast,
#if defined(WITH_JIT)
      kEMJitCompiler,
#endif
    } executionMode = kEMDefault;
    
    property_get("dalvik.vm.checkjni", propBuf, ""); // (8)
    if (strcmp(propBuf, "true") == 0) {
        checkJni = true;
    } else if (strcmp(propBuf, "false") != 0) {
        /* property is neither true nor false; fall back on kernel parameter */
        property_get("ro.kernel.android.checkjni", propBuf, "");
        if (propBuf[0] == '1') {
            checkJni = true;
        }
    }
    property_get("dalvik.vm.execution-mode", propBuf, "");

    // 생략

    if (JNI_CreateJavaVM(pJavaVM, pEnv, &initArgs) < 0) { // (9)
        LOGE("JNI_CreateJavaVM failed\n");
        goto bail;
    }
    result = 0;
bail:
    free(stackTraceFile);
    return result;
}



/*
 * Start the Android runtime.  This involves starting the virtual machine
 * and calling the "static void main(String[] args)" method in the class
 * named by "className".
 */
void AndroidRuntime::start(const char* className, const bool startSystemServer)
{
    LOGD("\n>>>>>>>>>>>>>> AndroidRuntime START <<<<<<<<<<<<<<\n");
    char* slashClassName = NULL;
    char* cp;
    JNIEnv* env;
    blockSigpipe();
    /* 
     * 'startSystemServer == true' means runtime is obslete and not run from 
     * init.rc anymore, so we print out the boot start event here.
     */
    if (startSystemServer) {
        /* track our progress through the boot sequence */
        const int LOG_BOOT_PROGRESS_START = 3000;
        LOG_EVENT_LONG(LOG_BOOT_PROGRESS_START, 
                       ns2ms(systemTime(SYSTEM_TIME_MONOTONIC)));
    }
    const char* rootDir = getenv("ANDROID_ROOT");
    if (rootDir == NULL) {
        rootDir = "/system";
        if (!hasDir("/system")) {
            LOG_FATAL("No root directory specified, and /android does not exist.");
            goto bail;
        }
        setenv("ANDROID_ROOT", rootDir, 1);
    }
    //const char* kernelHack = getenv("LD_ASSUME_KERNEL");
    //LOGD("Found LD_ASSUME_KERNEL='%s'\n", kernelHack);
    /* start the virtual machine */
    if (startVm(&mJavaVM, &env) != 0) // (7)
        goto bail;
    /*
     * Register android functions.
     */
    if (startReg(env) < 0) { // (10)
        LOGE("Unable to register all android natives\n");
        goto bail;
    }
    /*
     * We want to call main() with a String array with arguments in it.
     * At present we only have one argument, the class name.  Create an
     * array to hold it.
     */
    jclass stringClass;
    jobjectArray strArray;
    jstring classNameStr;
    jstring startSystemServerStr;
    stringClass = env->FindClass("java/lang/String");
    assert(stringClass != NULL);
    strArray = env->NewObjectArray(2, stringClass, NULL);
    assert(strArray != NULL);
    classNameStr = env->NewStringUTF(className);
    assert(classNameStr != NULL);
    env->SetObjectArrayElement(strArray, 0, classNameStr);
    startSystemServerStr = env->NewStringUTF(startSystemServer ? 
                                                 "true" : "false");
    env->SetObjectArrayElement(strArray, 1, startSystemServerStr);
    /*
     * Start VM.  This thread becomes the main thread of the VM, and will
     * not return until the VM exits.
     */
    jclass startClass;
    jmethodID startMeth;
    slashClassName = strdup(className);
    for (cp = slashClassName; *cp != '\0'; cp++) // (11)
        if (*cp == '.')
            *cp = '/';
    startClass = env->FindClass(slashClassName); // (12)
    if (startClass == NULL) {
        LOGE("JavaVM unable to locate class '%s'\n", slashClassName);
        /* keep going */
    } else {
        startMeth = env->GetStaticMethodID(startClass, "main", // (13)
            "([Ljava/lang/String;)V");
        if (startMeth == NULL) {
            LOGE("JavaVM unable to find main() in '%s'\n", className);
            /* keep going */
        } else {
            env->CallStaticVoidMethod(startClass, startMeth, strArray); // (14)
#if 0
            if (env->ExceptionCheck())
                threadExitUncaughtException(env);
#endif
        }
    }
    LOGD("Shutting down VM\n");
    if (mJavaVM->DetachCurrentThread() != JNI_OK)
        LOGW("Warning: unable to detach main thread\n");
    if (mJavaVM->DestroyJavaVM() != 0)
        LOGW("Warning: VM did not shut down cleanly\n");
bail:
    free(slashClassName);
}



static const RegJNIRec gRegJNI[] = {
    REG_JNI(register_android_debug_JNITest),
    REG_JNI(register_com_android_internal_os_RuntimeInit),
    REG_JNI(register_android_os_SystemClock),
    REG_JNI(register_android_util_EventLog),
    REG_JNI(register_android_util_Log),
    
    // 생략
    
};



/*
 * Register android native functions with the VM.
 */
/*static*/ int AndroidRuntime::startReg(JNIEnv* env)
{
    /*
     * This hook causes all future threads created in this process to be
     * attached to the JavaVM.  (This needs to go away in favor of JNI
     * Attach calls.)
     */
    androidSetCreateThreadFunc((android_create_thread_fn) javaCreateThreadEtc);
    LOGD("--- registering native functions ---\n");
    /*
     * Every "register" function calls one or more things that return
     * a local reference (e.g. FindClass).  Because we haven't really
     * started the VM yet, they're all getting stored in the base frame
     * and never released.  Use Push/Pop to manage the storage.
     */
    env->PushLocalFrame(200);
    if (register_jni_procs(gRegJNI, NELEM(gRegJNI), env) < 0) {
        env->PopLocalFrame(NULL);
        return -1;
    }
    env->PopLocalFrame(NULL);
    //createJavaThread("fubar", quickTest, (void*) "hello");
    return 0;
}

```

#### (7) startVM()  
(8)과 (9)를 실행한다.  

#### (8) property_get() 함수 호출   
가상 머신의 실행 옵션을 설정한다.  

#### (9) JNI_CreateJavaVM() 함수를 호출  
달빅 가상 머신을 생성하고 실행한다.  
`JavaVM **pVm` : 생성된 JavaVM 클래스의 인스턴스에 대한 포인터  
`JNIEnv **p_env` : 가상 머신에 접근하기 위한 JNIEnv 클래스의 인스턴스에 대한 포인터  
`void *vm_args` : 지금까지 설정한 가상 머신의 옵션  

---
### 생성된 가상머신에서 사용할 JNI 함수 등록   
#### (10) startReg() 함수  
gRegJNI[] 배열에 저장되어 있는 함수를 호출한다.  
가상 머신 상에서 사용할 JNI 함수를 등록하고 나면, 가상 머신 상에서 동작하는 자바 클래스에서 이 네이티브 함수들을 호출 할 수 있다.  

---
### ZygoteInit 클래스의 실행
생성된 VM에서 동작할 클래스를 로딩하는 부분이다.  
app_process는 전달된 인자에 따라 Zygote 외의 다른 클래스를 호출할 수 있다.  
여기서는 Zygote 클래스를 호출한다.  
여기서 인자로 전달된 className은 `com.android.internal.os.ZygoteInit`이다.

#### (11) 클래스 이름의 `.`을 `/`로 치환한다.  
아래의 코드 (12)에서 경로에 들어 있는 클래스를 읽어들기이 때문이다.

#### (12) FindClass() 함수  
실행할 클래스를 로딩한다.  

#### (13) GetStaticMethodID() 함수  
해당 클래스에서 매개변수가 String 배열이고 반환값이 Void이며, 정적 메서드인 main()을 찾는다.  

#### (14) main method 호출  
메인 메서드를 호출하면 실행 흐름은 가상 머신 위에서 동작하는 자바 애플리케이션으로 바뀐다.  
여기서는 ZygoteInit 클래스로 실행 흐름이 넘어간다.  
이후 네이티브 영역에서 진행되어온 C++ 코드의 실행흐름은 가상 머신이 종료될 때 까지 진행되지 않는다.  

여기까지 가상 머신을 생성하고 ZygoteInit 클래스를 로딩 완료.  
