/*------------------------------scanPackageDirtyLI 함수------------------------------------------------------------------------------
인자로 받은 Package pkg를 분석 후 다시 Package 타입으로 리턴, 리턴타입 : PackageParser.Package
입력된 pkg 내용을 분석한 뒤에 pkg의 flags에 하나하나 체크한 뒤 리턴하는 형식임

(1)패키지 소스파일 로드
(2)parseFlags를 설정하는 부분
(3)DEBUG_PACKAGE_SCANNING 설정이 켜져있는 경우 패키지 정보를 로그로 출력하는 부분
  (3-1)패키지정보를 세팅, DEBUG_PACKAGE_SCANNING설정이 켜져있다면 계속 로그를 출력
  (3-2)패키지 관련 정보 로그 출력 부분
(4)입력했던 pkg 값 리턴

하는 순으로 진행됨
------------------------------------------------------------------------------------------------------------------------------------*/
private PackageParser.Package scanPackageDirtyLI(PackageParser.Package pkg, int parseFlags,
            int scanFlags, long currentTime, UserHandle user) throws PackageManagerException {
		//PackageParser.Package pkg 인자를 입력받아 이 내용을 분석하게 됨.
/*----------------------------------------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------1.패키지 소스파일 로드------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------------------*/
		final File scanFile = new File(pkg.codePath);
		//조건문 - 만약 pkg.applicationInfo의 코드경로 정보가 없다면, 또는 리소스파일 정보가 없다면 오류 예외처리구문 호출
        if (pkg.applicationInfo.getCodePath() == null ||
                pkg.applicationInfo.getResourcePath() == null) { 
            // Bail out. The resource and code paths haven't been set.
            throw new PackageManagerException(INSTALL_FAILED_INVALID_APK,
                    "Code and resource paths haven't been se2019-07-25t correctly"); //오류메세지 호출 후 오류처리구문으로 이동, 함수 정지
        }
/*----------------------------------------------------------------------------------------------------------------------------------*/
/*---------------------------------------------------2.parseFlags를 설정하는 부분---------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------------------*/
		//만약 parseFlags 부분을 읽어오도록 설정이 되어있고, PackageParser의 PARSE 정보에 시스템앱 부분이 true 상태인 경우
        if ((parseFlags&PackageParser.PARSE_IS_SYSTEM) != 0) { 
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_SYSTEM; //bit flag를 활용하여 pkg의 flags 인자에 FLAG_SYSTEM 정보를 추가
        } else {
            // Only allow system apps to be flagged as core apps.
            pkg.coreApp = false; //그 외의 경우엔 coreApp 설정을 false로 기본 설정.
        }

		//만약 parseFlags 부분을 읽어오도록 설정이 되어있고, PackageParser의 PARSE_IS_PRIVILEGED(권한 관련 허용정보) 정보가 true 상태인 경우
        if ((parseFlags&PackageParser.PARSE_IS_PRIVILEGED) != 0) {
            pkg.applicationInfo.privateFlags |= ApplicationInfo.PRIVATE_FLAG_PRIVILEGED; //bit flag를 활용하여 pkg의 flags 인자에 PARSE_IS_PRIVILEGED 정보를 추가
        }

		//mCustomResolverComponentName(전역변수, 현재 읽어온 컴포넌트의 이름을 관리하는 기능)을 사용하여 
		//현재 읽고있는 컴포넌트 이름과 패키지의 패키지이름이 같을 경우에 setUpCustomResolverActivity 함수 호출
        if (mCustomResolverComponentName != null &&
                mCustomResolverComponentName.getPackageName().equals(pkg.packageName)) {
            setUpCustomResolverActivity(pkg); //7782번라인 이하 setUpCustomResolverActivity 함수를 호출
        }

		//패키지가 android 패키지인 경우 mPlatformPackage, pkg.mVersionCode, mAndroidApplication, 
		//								 mResolveActivity와 mResolveInfo, mResolveComponentName 정보 초기화하고 로그 출력
        if (pkg.packageName.equals("android")) {
			//mPackages에 해당 정보 입력
			synchronized (mPackages) {
                if (mAndroidApplication != null) { //mAndroidApplication(전역변수, ApplicationInfo 내용을 담음) 변수에 이미 데이터가 담겨있는 경우 
					//코어 안드로이드 패키지가 현재 재정의중이니 정보 초기화 단계를 건너뛰겠다는 로그 출력 후 해당 예외처리구문으로 건너뜀
                    Slog.w(TAG, "*************************************************");
                    Slog.w(TAG, "Core android package being redefined.  Skipping.");
                    Slog.w(TAG, " file=" + scanFile);
                    Slog.w(TAG, "*************************************************");
                    throw new PackageManagerException(INSTALL_FAILED_DUPLICATE_PACKAGE,
                            "Core android package being redefined.  Skipping."); //해당 예외처리 구문으로 건너뜀
                }
                // Set up information for our fall-back user intent resolution activity.

				//불러온 pkg의 정보로 데이터 초기화
                mPlatformPackage = pkg;
                pkg.mVersionCode = mSdkVersion;
                mAndroidApplication = pkg.applicationInfo;
                if (!mResolverReplaced) {
                    mResolveActivity.applicationInfo = mAndroidApplication;
                    mResolveActivity.name = ResolverActivity.class.getName();
                    mResolveActivity.packageName = mAndroidApplication.packageName;
                    mResolveActivity.processName = "system:ui";
                    mResolveActivity.launchMode = ActivityInfo.LAUNCH_MULTIPLE;
                    mResolveActivity.documentLaunchMode = ActivityInfo.DOCUMENT_LAUNCH_NEVER;
                    mResolveActivity.flags = ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS;
                    mResolveActivity.theme = R.style.Theme_Holo_Dialog_Alert;
                    mResolveActivity.exported = true;
                    mResolveActivity.enabled = true;
                    mResolveInfo.activityInfo = mResolveActivity;
                    mResolveInfo.priority = 0;
                    mResolveInfo.preferredOrder = 0;
                    mResolveInfo.match = 0;
                    mResolveComponentName = new ComponentName(
                            mAndroidApplication.packageName, mResolveActivity.name);
                }
            }
        }
/*----------------------------------------------------------------------------------------------------------------------------------*/
/*-------------------------3.DEBUG_PACKAGE_SCANNING 설정이 켜져있는 경우 패키지 정보를 로그로 출력하는 부분-------------------------*/
/*----------------------------------------------------------------------------------------------------------------------------------*/
		//parseFlags정보가 null이 아니고, PackageParser의 PARSE_CHATTY 정보가 0이 아닐 경우 로그 출력
		if (DEBUG_PACKAGE_SCANNING) {
            if ((parseFlags & PackageParser.PARSE_CHATTY) != 0)
                Log.d(TAG, "Scanning package " + pkg.packageName); //Scanning package 로그 출력
        }

		//만약 mPackages에 현재 패키지 이름과 동일한 어플리케이션이 있는경우, 또는 SharedLibraries에 현재 패키지 이름과 동일한 어플리케이션이 있는 경우에는
        if (mPackages.containsKey(pkg.packageName)
                || mSharedLibraries.containsKey(pkg.packageName)) {
            throw new PackageManagerException(INSTALL_FAILED_DUPLICATE_PACKAGE,
                    "Application package " + pkg.packageName
                    + " already installed.  Skipping duplicate."); //예외처리 구문으로 건너뜀. "이미 설치된 패키지입니다."
        }
        // If we're only installing presumed-existing packages, require that the
        // scanned APK is both already known and at the path previously established
		// for it.  Previously unknown packages we pick up normally, but if we have an
		// a priori expectation about this package's install presence, enforce it.
		// With a singular exception for new system packages. When an OTA contains
        // a new system package, we allow the codepath to change from a system location
        // to the user-installed location. If we don't allow this change, any newer,
        // user-installed version of the application will be ignored.
        
		//scanFlags의 SCAN_REQUIRE_KNOWN 부분이 0이 아닐경우
		if ((scanFlags & SCAN_REQUIRE_KNOWN) != 0) {
            if (mExpectingBetter.containsKey(pkg.packageName)) { //mExpectingBetter (필요한 연관 패키지) 정보가 있을 경우
                logCriticalInfo(Log.WARN,
                        "Relax SCAN_REQUIRE_KNOWN requirement for package " + pkg.packageName); //경고 로그 출력
            } else {
                PackageSetting known = mSettings.peekPackageLPr(pkg.packageName); //미리 저장된 PackageSetting 정보가 있는지 읽어옴
                if (known != null) { //만약 미리 기록된 PackageSetting 정보가 있을경우
                    if (DEBUG_PACKAGE_SCANNING) { //DEBUG_PACKAGE_SCANNING 설정이 켜져있다면
                        Log.d(TAG, "Examining " + pkg.codePath
                                + " and requiring known paths " + known.codePathString
                                + " & " + known.resourcePathString); //로그 출력
                    }
                    if (!pkg.applicationInfo.getCodePath().equals(known.codePathString)
                            || !pkg.applicationInfo.getResourcePath().equals(known.resourcePathString)) { //기존 PackageSetting 정보와 입력했던 pkg의 경로정보가 다를경우
                        throw new PackageManagerException(INSTALL_FAILED_PACKAGE_CHANGED,
                                "Application package " + pkg.packageName
                                + " found at " + pkg.applicationInfo.getCodePath()
                                + " but expected at " + known.codePathString + "; ignoring."); //로그 출력
                    }
                }
            }
        }
/*----------------------------------------------------------------------------------------------------------------------------------*/
/*----------------3-1.아래는 패키지정보를 세팅하는 부분. DEBUG_PACKAGE_SCANNING설정이 켜져있다면 계속 로그를 출력한다---------------*/
/*----------------------------------------------------------------------------------------------------------------------------------*/
		// Initialize package source and resource directories
        //패키지 소스, 리소스 디렉토리 설정
		File destCodeFile = new File(pkg.applicationInfo.getCodePath()); // 파일타입 destCodeFile 변수에 pkg의 코드경로 정보 삽입
        File destResourceFile = new File(pkg.applicationInfo.getResourcePath()); //파일타입 destResourceFile 변수에 pkg의 리소스경로 정보 삽입
        SharedUserSetting suid = null; //suid 변수 초기화 선언
        PackageSetting pkgSetting = null; //pkgSetting 변수 초기화 선언
        if (!isSystemApp(pkg)) { //만약 pkg가 시스템앱이 아닌 경우 시스템앱에만 해당하는 아래 변수들을 null로 초기화
            // Only system apps can use these features.
            pkg.mOriginalPackages = null;
            pkg.mRealPackage = null;
            pkg.mAdoptPermissions = null;
        }

        // writer
        synchronized (mPackages) { //mPackages와 동기화하며 작업 진행
            if (pkg.mSharedUserId != null) { //pkg에 mSharedUserId 데이터가 있을 경우
                suid = mSettings.getSharedUserLPw(pkg.mSharedUserId, 0, 0, true); //suid에 mSettings의 getSharedUserLPw함수 활용하여 mSharedUserId데이터 입력
                if (suid == null) { //만약 입력된 값이 null이라면
                    throw new PackageManagerException(INSTALL_FAILED_INSUFFICIENT_STORAGE,
                            "Creating application package " + pkg.packageName
                            + " for shared user failed"); //에러 메세지 출력, 예외처리 구문으로 이동
                }
                if (DEBUG_PACKAGE_SCANNING) { //입력이 잘 된 경우
                    if ((parseFlags & PackageParser.PARSE_CHATTY) != 0)
                        Log.d(TAG, "Shared UserID " + pkg.mSharedUserId + " (uid=" + suid.userId
                                + "): packages=" + suid.packages); //suid 관련 정보 로그출력
                }
            }
            // Check if we are renaming from an original package name.
            PackageSetting origPackage = null; //origPackage 변수 초기화 선언
            String realName = null; //realName 변수 초기화 선언
            if (pkg.mOriginalPackages != null) { //pkg 데이터에 mOriginalPackages 관련 정보가 있을 경우
                // This package may need to be renamed to a previously
                // installed name.  Let's check on that...
                final String renamed = mSettings.mRenamedPackages.get(pkg.mRealPackage); //renamed 변수에 mSettings의 mRenamedPackages함수 활용하여 mRealPackage데이터 불러옴
                if (pkg.mOriginalPackages.contains(renamed)) { //pkg의 mOriginalPackages 리스트에 renamed 정보가 있는 경우
                    // This package had originally been installed as the
                    // original name, and we have already taken care of
                    // transitioning to the new one.  Just update the new
                    // one to continue using the old name.
                    realName = pkg.mRealPackage; //realName 변수에 mRealPackage 데이터 입력
                    if (!pkg.packageName.equals(renamed)) { //만약 pkg의 packageName이 renamed와 다르게 되어있는 경우
                        // Callers into this function may have already taken
                        // care of renaming the package; only do it here if
                        // it is not already done.
                        pkg.setPackageName(renamed); //setPackageName함수 활용하여 packageName을 renamed로 설정
                    }
                } else { //pkg의 mOriginalPackages 리스트에 renamed 정보가 없는 경우
                    //mOriginalPackages 정보 리스트를 한바퀴 돌면서 확인
					for (int i=pkg.mOriginalPackages.size()-1; i>=0; i--) {
						if ((origPackage = mSettings.peekPackageLPr(
                                pkg.mOriginalPackages.get(i))) != null) { //origPackage에 mSettings의 peekPackageLPr함수 활용하여 i번째 패키지를 가져오고, 만약 null이 아닐 시
                            // We do have the package already installed under its
                            // original name...  should we use it?
                            if (!verifyPackageUpdateLPr(origPackage, pkg)) { //verifyPackageUpdateLPr함수 활용하여 업데이트가 필요한 패키지인지 아닌지 확인
                                // New package is not compatible with original.
                                origPackage = null; //업데이트가 필요하지 않은 패키지일 경우 origPackage 정보 초기화 후
                                continue; // for문 계속 진행
                            } else if (origPackage.sharedUser != null) { //origPackage에 sharedUser 정보가 있는 경우
                                // Make sure uid is compatible between packages.
                                if (!origPackage.sharedUser.name.equals(pkg.mSharedUserId)) { //pkg의 mSharedUserId정보와 origPackage에 sharedUser정보가 같지 않다면
                                    Slog.w(TAG, "Unable to migrate data from " + origPackage.name
                                            + " to " + pkg.packageName + ": old uid "
                                            + origPackage.sharedUser.name
                                            + " differs from " + pkg.mSharedUserId); //로그 출력 후
                                    origPackage = null;
                                    continue; //for문 계속 진행
                                }
                            } else { //renamed 업데이트가 필요할 시
                                if (DEBUG_UPGRADE) Log.v(TAG, "Renaming new package "
                                        + pkg.packageName + " to old name " + origPackage.name); //업데이트 로그 출력
                            }
                            break; //업데이트가 될 경우 break로 for문 끝냄
                        }
                    }
                }
            }

			//mTransferedPackages에 현재 pkg의 packageName이 포함되어 있다면
            if (mTransferedPackages.contains(pkg.packageName)) {
                Slog.w(TAG, "Package " + pkg.packageName
                        + " was transferred to another, but its .apk remains"); //로그 출력
            }
            // Just create the setting, don't add it yet. For already existing packages
            // the PkgSetting exists already and doesn't have to be created.
            
			//pkgSetting의 내용을 mSettings의 getPackageLPw 함수를 사용하여 초기화
			pkgSetting = mSettings.getPackageLPw(pkg, origPackage, realName, suid, destCodeFile,
                    destResourceFile, pkg.applicationInfo.nativeLibraryRootDir,
                    pkg.applicationInfo.primaryCpuAbi,
                    pkg.applicationInfo.secondaryCpuAbi,
                    pkg.applicationInfo.flags, pkg.applicationInfo.privateFlags,
                    user, false);
            if (pkgSetting == null) { //만약 pkgSetting값이 제대로 불러와지지 않았다면
                throw new PackageManagerException(INSTALL_FAILED_INSUFFICIENT_STORAGE,
                        "Creating application package " + pkg.packageName + " failed"); //오류제어 구문으로 이동
            }
            if (pkgSetting.origPackage != null) { // pkgSetting값을 제대로 불러온 뒤
                // If we are first transitioning from an original package,
                // fix up the new package's name now.  We need to do this after
                // looking up the package under its new name, so getPackageLP
                // can take care of fiddling things correctly.
                pkg.setPackageName(origPackage.name); //pkg의 packageName을 setPackageName 함수를 사용해 origPackage.name로 변경
                // File a report about this.
                String msg = "New package " + pkgSetting.realName
                        + " renamed to replace old package " + pkgSetting.name; //로그 출력
                reportSettingsProblem(Log.WARN, msg);
                // Make a note of it.
                mTransferedPackages.add(origPackage.name); //mTransferedPackages 리스트에 origPackage.name를 추가
                // No longer need to retain this.
                pkgSetting.origPackage = null; //pkgSetting.origPackage에 할당된 내용 비활성화
            }
            if (realName != null) { //위에서 입력했던 realName에 값이 잘 들어간 경우
                // Make a note of it.
                mTransferedPackages.add(pkg.packageName); //packageName 값을 mTransferedPackages 리스트에 추가
            }
            if (mSettings.isDisabledSystemPackageLPr(pkg.packageName)) { // isDisabledSystemPackageLPr 함수를 활용하여 패키지 체크
                pkg.applicationInfo.flags |= ApplicationInfo.FLAG_UPDATED_SYSTEM_APP; //해당내용에 포함될 경우 FLAG_UPDATED_SYSTEM_APP 플래그 bit ON
            }
            if ((parseFlags&PackageParser.PARSE_IS_SYSTEM_DIR) == 0) { //PARSE_IS_SYSTEM_DIR에 대한 설정이 OFF인 경우
                // Check all shared libraries and map to their actual file path.
                // We only do this here for apps not on a system dir, because those
                // are the only ones that can fail an install due to this.  We
                // will take care of the system apps by updating all of their
                // library paths after the scan is done.
                updateSharedLibrariesLPw(pkg, null); //updateSharedLibrariesLPw함수를 호출하여 현재 pkg 내용을 라이브러리에 업데이트
            }
            if (mFoundPolicyFile) { //mFoundPolicyFile이 있는 경우
                SELinuxMMAC.assignSeinfoValue(pkg); //SELinuxMMAC의 assignSeinfoValue함수 활용하여 값 설정 (권한 정책에 관련된 구문으로 보임)
            }
            pkg.applicationInfo.uid = pkgSetting.appId; //pkg의 uid속성 값 설정
            pkg.mExtras = pkgSetting; //pkg의 mExtras속성 값 설정
            if (shouldCheckUpgradeKeySetLP(pkgSetting, scanFlags)) { //shouldCheckUpgradeKeySetLP함수의 리턴값이 true일 경우
                if (checkUpgradeKeySetLP(pkgSetting, pkg)) { //checkUpgradeKeySetLP함수의 리턴값이 true라면
                    // We just determined the app is signed correctly, so bring
                    // over the latest parsed certs.
                    pkgSetting.signatures.mSignatures = pkg.mSignatures; //pkgSetting의 mSignatures값을 pkg의 mSignatures값으로 설정
                } else { //checkUpgradeKeySetLP함수의 리턴값이 false라면
                    if ((parseFlags & PackageParser.PARSE_IS_SYSTEM_DIR) == 0) { //PARSE_IS_SYSTEM_DIR에 대한 설정이 OFF인 경우
                        throw new PackageManagerException(INSTALL_FAILED_UPDATE_INCOMPATIBLE,
                                "Package " + pkg.packageName + " upgrade keys do not match the "
                                + "previously installed version"); //오류제어 구문으로 이동
                    } else { ////PARSE_IS_SYSTEM_DIR에 대한 설정이 ON인 경우
                        pkgSetting.signatures.mSignatures = pkg.mSignatures;
                        String msg = "System package " + pkg.packageName
                            + " signature changed; retaining data.";
                        reportSettingsProblem(Log.WARN, msg); //시스템 패키지에 관련된 로그 출력
                    }
                }
            } else { //shouldCheckUpgradeKeySetLP함수의 리턴값이 false일 경우
                try { //오류제어 위한 try~ catch 구문 (오류 발생시 PackageManagerException e를 호출)
                    verifySignaturesLP(pkgSetting, pkg); //verifySignaturesLP함수 활용하여 패키지 검증
                    // We just determined the app is signed correctly, so bring
                    // over the latest parsed certs.
                    pkgSetting.signatures.mSignatures = pkg.mSignatures; //검증 후 pkgSetting의 mSignatures속성 값 설정
                } catch (PackageManagerException e) {
                    if ((parseFlags & PackageParser.PARSE_IS_SYSTEM_DIR) == 0) { //오류 발생시... PARSE_IS_SYSTEM_DIR에 대한 설정이 OFF인 경우 오류제어구문으로 이동
                        throw e;
                    }
                    // The signature has changed, but this package is in the system
                    // image...  let's recover!
                    pkgSetting.signatures.mSignatures = pkg.mSignatures; //pkgSetting의 mSignatures속성 값 설정
                    // However...  if this package is part of a shared user, but it
                    // doesn't match the signature of the shared user, let's fail.
                    // What this means is that you can't change the signatures
                    // associated with an overall shared user, which doesn't seem all
                    // that unreasonable.
                    if (pkgSetting.sharedUser != null) { //pkgSetting의 sharedUser 값이 null이 아닐 경우
                        if (compareSignatures(pkgSetting.sharedUser.signatures.mSignatures,
                                              pkg.mSignatures) != PackageManager.SIGNATURE_MATCH) { 
							//compareSignatures함수의 리턴값이 PackageManager.SIGNATURE_MATCH와 일치하지 않을 경우
                            throw new PackageManagerException(
                                    INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES,
                                            "Signature mismatch for shared user : "
                                            + pkgSetting.sharedUser); //오류제어 구문으로 이동
                        }
                    }
                    // File a report about this.
                    String msg = "System package " + pkg.packageName
                        + " signature changed; retaining data.";
                    reportSettingsProblem(Log.WARN, msg); //오류에 대한 로그 출력
                }
            }
            // Verify that this new package doesn't have any content providers
            // that conflict with existing packages.  Only do this if the
            // package isn't already installed, since we don't want to break
            // things that are installed.
            
			//SCAN_NEW_INSTALL 설정이 ON인 경우
			if ((scanFlags & SCAN_NEW_INSTALL) != 0) {
                final int N = pkg.providers.size(); //for문용 변수
                int i; //for문용 변수
                for (i=0; i<N; i++) {
                    PackageParser.Provider p = pkg.providers.get(i); //i번째 Provier p를 불러옴
                    if (p.info.authority != null) { //p의 authority 정보가 있을 경우
                        String names[] = p.info.authority.split(";"); //해당 내용을 names[] 변수에 ';'문자로 구분하여 추가 후
                        for (int j = 0; j < names.length; j++) { //names의 길이만큼 for문 돌입
                            if (mProvidersByAuthority.containsKey(names[j])) { //mProvidersByAuthority의 containsKey에 names에 해당하는 내용이 있다면
                                PackageParser.Provider other = mProvidersByAuthority.get(names[j]); //Provider other 값을 names의 내용으로 설정
                                final String otherPackageName =
                                        ((other != null && other.getComponentName() != null) ?
                                                other.getComponentName().getPackageName() : "?"); //other의 내용이 존재하고, other의 getComponentName함수 리턴값도 null이 아니라면
																								  //otherPackageName에 other의 getComponantName의 getPackageName함수 활용하여 값 입력
																								  //만약 other의 내용이 존재하지 않거나, getComponentName의 리턴값이 null이면 "?" 입력
                                throw new PackageManagerException(
                                        INSTALL_FAILED_CONFLICTING_PROVIDER,
                                                "Can't install because provider name " + names[j]
                                                + " (in package " + pkg.applicationInfo.packageName
                                                + ") is already used by " + otherPackageName); //이미 존재하는 provider라는 오류로그와 함께 오류제어 구문으로 이동
                            }
                        }
                    }
                }
            }
            if (pkg.mAdoptPermissions != null) { //pkg의 mAdoptPermissions값이 존재한다면
                // This package wants to adopt ownership of permissions from
                // another package.
                for (int i = pkg.mAdoptPermissions.size() - 1; i >= 0; i--) { //전체 mAdoptPermissions의 갯수만큼 for문 돌입
                    final String origName = pkg.mAdoptPermissions.get(i); //origName 변수에 i번째 mAdoptPermissions값 불러옴
                    final PackageSetting orig = mSettings.peekPackageLPr(origName); //불러온 origName변수값을 활용하여 peekPackageLPr함수로 PackageSetting값 불러옴
                    if (orig != null) { //불러온 PackageSetting orig에 값이 존재할 경우
                        if (verifyPackageUpdateLPr(orig, pkg)) { //orig 값을 verifyPackageUpdateLPr함수 활용하여 검증 후
                            Slog.i(TAG, "Adopting permissions from " + origName + " to "
                                    + pkg.packageName); //로그 출력
                            mSettings.transferPermissionsLPw(origName, pkg.packageName); //transferPermissionsLPw함수 활용하여 패키지의 Permission 값 설정
                        }
                    }
                }
            }
        }
        final String pkgName = pkg.packageName;						//pkgName값 설정
        final long scanFileTime = scanFile.lastModified();			//scanFileTime값 설정
        final boolean forceDex = (scanFlags & SCAN_FORCE_DEX) != 0;	//forceDex값 설정 (SCAN_FORCE_DEX설정이 ON이면 true, OFF면 false)
        pkg.applicationInfo.processName = fixProcessName(
                pkg.applicationInfo.packageName,
                pkg.applicationInfo.processName,
                pkg.applicationInfo.uid);							//processName값 설정
        File dataPath;												//dataPath값 설정
        if (mPlatformPackage == pkg) { //mPlatformPackage과 pkg가 같을 경우
            // The system package is special.
            dataPath = new File(Environment.getDataDirectory(), "system"); //dataPath를 system으로 설정
            pkg.applicationInfo.dataDir = dataPath.getPath(); //pkg의 dataDir을 system의 dir로 설정
        } else { //mPlatformPackage과 pkg가 다를 경우
            // This is a normal package, need to make its data directory.
            dataPath = Environment.getDataUserPackageDirectory(pkg.volumeUuid,
                    UserHandle.USER_OWNER, pkg.packageName); //dataPath를 Environment의 getDataUserPackageDirectory함수를 사용하여 설정
            boolean uidError = false; //uidError 변수 초기화
            if (dataPath.exists()) { //위에서 설정한 dataPath의 값이 정상적으로 존재할 경우
                int currentUid = 0;
                try {
                    StructStat stat = Os.stat(dataPath.getPath()); //stat 변수 값 초기화
                    currentUid = stat.st_uid; //currentUid 변수 값 초기화, 오류 발생시 아래 catch구문으로 이동
                } catch (ErrnoException e) {
                    Slog.e(TAG, "Couldn't stat path " + dataPath.getPath(), e); //오류 로그 출력
                }
                // If we have mismatched owners for the data path, we have a problem.
                if (currentUid != pkg.applicationInfo.uid) { //currentUid가 pkg의 uid와 다를 경우
                    boolean recovered = false; //recovered변수 값 초기화
                    if (currentUid == 0) {
                        // The directory somehow became owned by root.  Wow.
                        // This is probably because the system was stopped while
                        // installd was in the middle of messing with its libs
                        // directory.  Ask installd to fix that.
                        int ret = mInstaller.fixUid(pkg.volumeUuid, pkgName,
                                pkg.applicationInfo.uid, pkg.applicationInfo.uid); //mInstaller의 fixUid함수 활용하여 ret 변수값 초기화
                        if (ret >= 0) { //ret에 입력된 리턴값이 0보다 같거나 큰 경우
                            recovered = true; //recovered 변수 true값으로 설정
                            String msg = "Package " + pkg.packageName
                                    + " unexpectedly changed to uid 0; recovered to " +
                                    + pkg.applicationInfo.uid; 
                            reportSettingsProblem(Log.WARN, msg); //관련 로그 출력
                        }
                    }
                    if (!recovered && ((parseFlags&PackageParser.PARSE_IS_SYSTEM) != 0
                            || (scanFlags&SCAN_BOOTING) != 0)) { //recovered값이 false이고, PARSE_IS_SYSTEM설정이 ON이거나 SCAN_BOOTING설정이 ON이라면
                        // If this is a system app, we can at least delete its
                        // current data so the application will still work.
                        int ret = removeDataDirsLI(pkg.volumeUuid, pkgName); //ret 변수에 removeDataDirsLI함수의 리턴값 입력
                        if (ret >= 0) { //ret의 리턴값이 0보다 같거나 큰 경우
                            // TODO: Kill the processes first
                            // Old data gone!
                            String prefix = (parseFlags&PackageParser.PARSE_IS_SYSTEM) != 0
                                    ? "System package " : "Third party package "; //prefix의 값을 PARSE_IS_SYSTEM설정이 ON이라면 "System package", OFF라면 "hird party package"로.
                            String msg = prefix + pkg.packageName
                                    + " has changed from uid: "
                                    + currentUid + " to "
                                    + pkg.applicationInfo.uid + "; old data erased";
                            reportSettingsProblem(Log.WARN, msg); //로그메세지 출력
                            recovered = true; //recovered 값 true로 설정
                            // And now re-install the app.
                            ret = createDataDirsLI(pkg.volumeUuid, pkgName, pkg.applicationInfo.uid,
                                    pkg.applicationInfo.seinfo); //ret 변수에 createDataDirsLI함수의 리턴값 입력
                            if (ret == -1) { //ret의 리턴값이 -1인 경우... 이런 경우는 오류가 발생한 경우
                                // Ack should not happen!
                                msg = prefix + pkg.packageName
                                        + " could not have data directory re-created after delete.";
                                reportSettingsProblem(Log.WARN, msg); //오류 로그 출력 후
                                throw new PackageManagerException(
                                        INSTALL_FAILED_INSUFFICIENT_STORAGE, msg); //오류제어 구문으로 이동
                            }
                        }
                        if (!recovered) { //recovered값이 false인 경우
                            mHasSystemUidErrors = true; //mHasSystemUidErrors 변수값 true로 설정
                        }
                    } else if (!recovered) { //recovered값이 false인 경우
                        // If we allow this install to proceed, we will be broken.
                        // Abort, abort!
                        throw new PackageManagerException(INSTALL_FAILED_UID_CHANGED,
                                "scanPackageLI"); //오류제어 구문으로 이동
                    }
                    if (!recovered) { //recovered값이 false인 경우
                        pkg.applicationInfo.dataDir = "/mismatched_uid/settings_"
                            + pkg.applicationInfo.uid + "/fs_"
                            + currentUid; //pkg의 dataDir값 설정
                        pkg.applicationInfo.nativeLibraryDir = pkg.applicationInfo.dataDir; //pkg의 nativeLibraryDir값 설정
                        pkg.applicationInfo.nativeLibraryRootDir = pkg.applicationInfo.dataDir; //pkg의 nativeLibraryRootDir값 설정
                        String msg = "Package " + pkg.packageName
                                + " has mismatched uid: "
                                + currentUid + " on disk, "
                                + pkg.applicationInfo.uid + " in settings"; //관련 로그메세지 생성
                        // writer
                        synchronized (mPackages) { //mPackages에 내용 동기화하며 로그메세지 출력
                            mSettings.mReadMessages.append(msg); //
                            mSettings.mReadMessages.append('\n');
                            uidError = true;
                            if (!pkgSetting.uidError) {
                                reportSettingsProblem(Log.ERROR, msg);
                            }
                        }
                    }
                }
                pkg.applicationInfo.dataDir = dataPath.getPath(); //dataDir의 값을 위에서 설정했던 dataPath의 경로값으로 설정
                if (mShouldRestoreconData) { //mShouldRestoreconData에 값이 존재할 경우
                    Slog.i(TAG, "SELinux relabeling of " + pkg.packageName + " issued."); //관련 로그 출력
                    mInstaller.restoreconData(pkg.volumeUuid, pkg.packageName,
                            pkg.applicationInfo.seinfo, pkg.applicationInfo.uid); //mInstaller의 restoreconData함수 실행
                }
            } else { //dataPath.exists()의 값이 false인 경우
                if (DEBUG_PACKAGE_SCANNING) {
                    if ((parseFlags & PackageParser.PARSE_CHATTY) != 0) //PARSE_CHATTY설정이 ON인 경우
                        Log.v(TAG, "Want this data dir: " + dataPath); //관련 로그 출력
                }
                //invoke installer to do the actual installation
                int ret = createDataDirsLI(pkg.volumeUuid, pkgName, pkg.applicationInfo.uid,
                        pkg.applicationInfo.seinfo); //ret에 createDataDirsLI의 리턴값 입력 (해당 함수가 잘 작동했는지, 오류가 발생했는지)
                if (ret < 0) { //ret에 입력된 값이 0보다 작을 경우
                    // Error from installer
                    throw new PackageManagerException(INSTALL_FAILED_INSUFFICIENT_STORAGE,
                            "Unable to create data dirs [errorCode=" + ret + "]"); //오류제어 구문으로 이동
                }
                if (dataPath.exists()) { //위 함수 통해 dataPath가 잘 생성되었다면
                    pkg.applicationInfo.dataDir = dataPath.getPath(); //dataDir값 입력
                } else { //dataPath생성에 또 오류가 발생했다면
                    Slog.w(TAG, "Unable to create data directory: " + dataPath); //관련 로그 출력 후
                    pkg.applicationInfo.dataDir = null; //dataDir값 null로 설정
                }
            }
            pkgSetting.uidError = uidError; //pkgSetting.uidError값을 uidError값으로 설정
        }
        final String path = scanFile.getPath(); //path 변수의 값을 scanFile에 입력된 Path값으로 설정
        final String cpuAbiOverride = deriveAbiOverride(pkg.cpuAbiOverride, pkgSetting); //cpuAbiOverride 변수의 값을 deriveAbiOverride함수 활용하여 설정
        if ((scanFlags & SCAN_NEW_INSTALL) == 0) { //SCAN_NEW_INSTALL 설정이 OFF인 경우
            derivePackageAbi(pkg, scanFile, cpuAbiOverride, true /* extract libs */); //derivePackageAbi함수 실행
            // Some system apps still use directory structure for native libraries
            // in which case we might end up not detecting abi solely based on apk
            // structure. Try to detect abi based on directory structure.
            if (isSystemApp(pkg) && !pkg.isUpdatedSystemApp() &&
                    pkg.applicationInfo.primaryCpuAbi == null) { //pkg가 SystemApp이고 updatedSystemApp이 아닐 경우
                setBundledAppAbisAndRoots(pkg, pkgSetting); //setBundledAppAbisAndRoots함수 실행
                setNativeLibraryPaths(pkg); //setNativeLibraryPaths함수 실행
            }
        } else { //SCAN_NEW_INSTALL 설정이 ON인 경우
            if ((scanFlags & SCAN_MOVE) != 0) { //SCAN_MOVE 설정이 ON인 경우
                // We haven't run dex-opt for this move (since we've moved the compiled output too)
                // but we already have this packages package info in the PackageSetting. We just
                // use that and derive the native library path based on the new codepath.
                pkg.applicationInfo.primaryCpuAbi = pkgSetting.primaryCpuAbiString; //primaryCpuAbi값을 pkgSetting.primaryCpuAbiString값으로 설정
                pkg.applicationInfo.secondaryCpuAbi = pkgSetting.secondaryCpuAbiString; //secondaryCpuAbi값을 pkgSetting.secondaryCpuAbiString값으로 설정
            }
            // Set native library paths again. For moves, the path will be updated based on the
            // ABIs we've determined above. For non-moves, the path will be updated based on the
            // ABIs we determined during compilation, but the path will depend on the final
            // package path (after the rename away from the stage path).
            setNativeLibraryPaths(pkg); //setNativeLibraryPaths함수 실행
        }
        if (DEBUG_INSTALL) Slog.i(TAG, "Linking native library dir for " + path); //DEBUG_INSTALL설정이 ON인 경우 로그 출력
        final int[] userIds = sUserManager.getUserIds(); //userIds 리스트 초기화
        synchronized (mInstallLock) { //mInstallLock에 아래 내용 동기화하며 작업 진행
            // Make sure all user data directories are ready to roll; we're okay
            // if they already exist
            if (!TextUtils.isEmpty(pkg.volumeUuid)) { //pkg의 volumeUuid값이 존재할 경우 (TextUtils의 isEmpty 함수 활용)
                for (int userId : userIds) { //userIds 리스트에 들어있는 값들을 순차적으로 검색(for문 활용)
                    if (userId != 0) { //n번째 userId 값이 0이 아닌 경우
                        mInstaller.createUserData(pkg.volumeUuid, pkg.packageName,
                                UserHandle.getUid(userId, pkg.applicationInfo.uid), userId,
                                pkg.applicationInfo.seinfo); //해당 userId값을 사용하여 mInstaller의 createUserData함수 실행
                    }
                }
            }
            // Create a native library symlink only if we have native libraries
            // and if the native libraries are 32 bit libraries. We do not provide
            // this symlink for 64 bit libraries.
            if (pkg.applicationInfo.primaryCpuAbi != null &&
                    !VMRuntime.is64BitAbi(pkg.applicationInfo.primaryCpuAbi)) { //primaryCpuAbi가 존재하고 32Bit인 경우 (VMRuntime의 is64BitAbi함수 활용)
                final String nativeLibPath = pkg.applicationInfo.nativeLibraryDir; //nativeLibPath값 설정
                for (int userId : userIds) { //userIds 리스트에 들어있는 값들을 순차적으로 검색(for문 활용)
                    if (mInstaller.linkNativeLibraryDirectory(pkg.volumeUuid, pkg.packageName,
                            nativeLibPath, userId) < 0) { //userId값을 사용하여 mInstaller의 linkNativeLibraryDirectory함수 실행해서 실패할 경우
                        throw new PackageManagerException(INSTALL_FAILED_INTERNAL_ERROR,
                                "Failed linking native library dir (user=" + userId + ")"); //오류제어 구문으로 이동
                    }
                }
            }
        }
        // This is a special case for the "system" package, where the ABI is
        // dictated by the zygote configuration (and init.rc). We should keep track
        // of this ABI so that we can deal with "normal" applications that run under
        // the same UID correctly.
        if (mPlatformPackage == pkg) { //pkg가 system 패키지일 경우
            pkg.applicationInfo.primaryCpuAbi = VMRuntime.getRuntime().is64Bit() ?
                    Build.SUPPORTED_64_BIT_ABIS[0] : Build.SUPPORTED_32_BIT_ABIS[0]; //pkg의 primaryCpuAbi속성에 32비트, 64비트에 맞추어 해당 빌드 설정
        }
        // If there's a mismatch between the abi-override in the package setting
        // and the abiOverride specified for the install. Warn about this because we
        // would've already compiled the app without taking the package setting into
        // account.
        if ((scanFlags & SCAN_NO_DEX) == 0 && (scanFlags & SCAN_NEW_INSTALL) != 0) { //SCAN_NO_DEX설정이 OFF이고 SCAN_NEW_INSTALL설정이 ON인 경우
            if (cpuAbiOverride == null && pkgSetting.cpuAbiOverrideString != null) { //cpuAbiOverride값이 null이고 cpuAbiOverrideString값은 존재할 경우
                Slog.w(TAG, "Ignoring persisted ABI override " + cpuAbiOverride +
                        " for package: " + pkg.packageName); //해당 로그 출력
            }
        }
        pkgSetting.primaryCpuAbiString = pkg.applicationInfo.primaryCpuAbi;			//primaryCpuAbiString값 pkgSetting에 설정
        pkgSetting.secondaryCpuAbiString = pkg.applicationInfo.secondaryCpuAbi;		//secondaryCpuAbiString값 pkgSetting에 설정
        pkgSetting.cpuAbiOverrideString = cpuAbiOverride;							//cpuAbiOverrideString값 pkgSetting에 설정
        // Copy the derived override back to the parsed package, so that we can
        // update the package settings accordingly.
        pkg.cpuAbiOverride = cpuAbiOverride;										//cpuAbiOverride값 pkgSetting에 설정
        if (DEBUG_ABI_SELECTION) { //DEBUG_ABI_SELECTION설정이 ON인 경우
            Slog.d(TAG, "Resolved nativeLibraryRoot for " + pkg.applicationInfo.packageName
                    + " to root=" + pkg.applicationInfo.nativeLibraryRootDir + ", isa="
                    + pkg.applicationInfo.nativeLibraryRootRequiresIsa); //해당 로그 출력
        }
        // Push the derived path down into PackageSettings so we know what to
        // clean up at uninstall time.
        pkgSetting.legacyNativeLibraryPathString = pkg.applicationInfo.nativeLibraryRootDir; //legacyNativeLibraryPathString값 pkgSetting에 설정
        if (DEBUG_ABI_SELECTION) { //DEBUG_ABI_SELECTION설정이 ON인 경우
            Log.d(TAG, "Abis for package[" + pkg.packageName + "] are" +
                    " primary=" + pkg.applicationInfo.primaryCpuAbi +
                    " secondary=" + pkg.applicationInfo.secondaryCpuAbi); //해당 로그 출력
        }
        if ((scanFlags&SCAN_BOOTING) == 0 && pkgSetting.sharedUser != null) { //SCAN_BOOTING설정이 OFF이고 sharedUser값이 존재할 경우
            // We don't do this here during boot because we can do it all
            // at once after scanning all existing packages.
            //
            // We also do this *before* we perform dexopt on this package, so that
            // we can avoid redundant dexopts, and also to make sure we've got the
            // code and package path correct.
            adjustCpuAbisForSharedUserLPw(pkgSetting.sharedUser.packages,
                    pkg, forceDex, (scanFlags & SCAN_DEFER_DEX) != 0, true /* boot complete */); //adjustCpuAbisForSharedUserLPw함수 실행
        }
        if ((scanFlags & SCAN_NO_DEX) == 0) { //SCAN_NO_DEX설정이 OFF인 경우
            int result = mPackageDexOptimizer.performDexOpt(pkg, null /* instruction sets */,
                    forceDex, (scanFlags & SCAN_DEFER_DEX) != 0, false /* inclDependencies */,
                    (scanFlags & SCAN_BOOTING) == 0); //mPackageDexOptimizer의 performDexOpt 함수 실행
            if (result == PackageDexOptimizer.DEX_OPT_FAILED) { //performDexOpt함수의 실행에 오류가 생긴 경우
                throw new PackageManagerException(INSTALL_FAILED_DEXOPT, "scanPackageLI"); //오류제어 구문으로 이동
            }
        }
        if (mFactoryTest && pkg.requestedPermissions.contains(
                android.Manifest.permission.FACTORY_TEST)) { //mFactoryTest 설정이 ON이고 권한설정이 FACTORY_TEST로 되어있는 경우
            pkg.applicationInfo.flags |= ApplicationInfo.FLAG_FACTORY_TEST; //FLAG_FACTORY_TEST flags ON
        }

        ArrayList<PackageParser.Package> clientLibPkgs = null; //clientLibPkgs 리스트 초기화 선언
        // writer
        synchronized (mPackages) { //mPackages에 동기화하며 작업 진행
            if ((pkg.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM) != 0) { //FLAG_SYSTEM 설정이 ON인 경우
                // Only system apps can add new shared libraries.
                if (pkg.libraryNames != null) { //pkg에 libraryNames의 내용 정보가 있을 때
                    for (int i=0; i<pkg.libraryNames.size(); i++) { //libraryNames의 모든 내용을 for구문으로 순차 확인
                        String name = pkg.libraryNames.get(i); //i번째 libraryNames 데이터 불러옴
                        boolean allowed = false; //allowed 변수 false로 초기화
                        if (pkg.isUpdatedSystemApp()) { //pkg의 isUpdatedSystemApp가 true일 경우
                            // New library entries can only be added through the
                            // system image.  This is important to get rid of a lot
                            // of nasty edge cases: for example if we allowed a non-
                            // system update of the app to add a library, then uninstalling
                            // the update would make the library go away, and assumptions
                            // we made such as through app install filtering would now
                            // have allowed apps on the device which aren't compatible
                            // with it.  Better to just have the restriction here, be
                            // conservative, and create many fewer cases that can negatively
                            // impact the user experience.
                            final PackageSetting sysPs = mSettings
                                    .getDisabledSystemPkgLPr(pkg.packageName); //sysPs변수를 getDisabledSystemPkgLPr함수 통하여 설정
                            if (sysPs.pkg != null && sysPs.pkg.libraryNames != null) { //sysPs의 pkg내용과 sysPs의 pkg.libraryNames 내용이 null이 아니라면
                                for (int j=0; j<sysPs.pkg.libraryNames.size(); j++) { //sysPs에 입력된 pkg.libraryNames 내용 순차 확인하며
                                    if (name.equals(sysPs.pkg.libraryNames.get(j))) { //해당 내용이 name(i번째 libraryNames)과 같은 원소를 찾으면
                                        allowed = true; //allowed변수를 true로 설정한 뒤
                                        allowed = true;
                                        break; //for문 종료
                                    }
                                }
                            }
                        } else { //pkg의 isUpdatedSystemApp가 false일 경우
                            allowed = true; //allowed변수를 바로 true로 설정
                        }
                        if (allowed) { //위 구문에 이어서 진행. allowed가 정상적으로 true로 설정이 되었다면 아래 내용 실행
                            if (!mSharedLibraries.containsKey(name)) { //mSharedLibraries에 i번째 libraryNames에 해당하는 내용이 없다면
                                mSharedLibraries.put(name, new SharedLibraryEntry(null, pkg.packageName)); //mSharedLibraries에 i번째 libraryNames에 해당하는 내용 추가
                            } else if (!name.equals(pkg.packageName)) { //mSharedLibraries에 i번째 libraryNames에 해당하는 내용은 있으나, pkg.packageName과 같지 않다면
                                Slog.w(TAG, "Package " + pkg.packageName + " library "
                                        + name + " already exists; skipping"); //관련 로그 출력
                            }
                        } else { //allowed가 false 상태라면
                            Slog.w(TAG, "Package " + pkg.packageName + " declares lib "
                                    + name + " that is not declared on system image; skipping"); //관련 로그 출력
                        }
                    }
                    if ((scanFlags&SCAN_BOOTING) == 0) { //SCAN_BOOTING설정이 OFF라면 
                        // If we are not booting, we need to update any applications
                        // that are clients of our shared library.  If we are booting,
                        // this will all be done once the scan is complete.
                        clientLibPkgs = updateAllSharedLibrariesLPw(pkg); //clientLibPkgs변수를 updateAllSharedLibrariesLPw함수 통해 설정
                    }
                }
            }
        }
        // We also need to dexopt any apps that are dependent on this library.  Note that
        // if these fail, we should abort the install since installing the library will
        // result in some apps being broken.
        if (clientLibPkgs != null) { //clientLibPkgs변수가 null이 아니라면
            if ((scanFlags & SCAN_NO_DEX) == 0) { //SCAN_NO_DEX설정이 OFF라면
                for (int i = 0; i < clientLibPkgs.size(); i++) { //for구문으로 clientLibPkgs내용 확인
                    PackageParser.Package clientPkg = clientLibPkgs.get(i); //i번째 clientLibPkgs데이터를 불러오고
                    int result = mPackageDexOptimizer.performDexOpt(clientPkg,
                            null /* instruction sets */, forceDex,
                            (scanFlags & SCAN_DEFER_DEX) != 0, false,
                            (scanFlags & SCAN_BOOTING) == 0); //mPackageDexOptimizer.performDexOpt 함수 실행
                    if (result == PackageDexOptimizer.DEX_OPT_FAILED) { //mPackageDexOptimizer.performDexOpt함수 실행이 실패했을 경우
                        throw new PackageManagerException(INSTALL_FAILED_DEXOPT,
                                "scanPackageLI failed to dexopt clientLibPkgs"); //오류 제어 구문으로 이동
                    }
                }
            }
        }
        // Request the ActivityManager to kill the process(only for existing packages)
        // so that we do not end up in a confused state while the user is still using the older
        // version of the application while the new one gets installed.
        if ((scanFlags & SCAN_REPLACING) != 0) { //SCAN_REPLACING설정이 OFF라면
            killApplication(pkg.applicationInfo.packageName,
                        pkg.applicationInfo.uid, "replace pkg"); //killApplication 함수 활용해서 pkg 패키지 종료
        }
        // Also need to kill any apps that are dependent on the library.
        if (clientLibPkgs != null) { //clientLibPkgs변수가 null이 아니라면
            for (int i=0; i<clientLibPkgs.size(); i++) { //for구문으로 clientLibPkgs내용 확인
                PackageParser.Package clientPkg = clientLibPkgs.get(i); //i번째 clientLibPkgs데이터를 불러오고
                killApplication(clientPkg.applicationInfo.packageName,
                        clientPkg.applicationInfo.uid, "update lib"); //killApplication 함수 활용해서 clientPkg 패키지 종료
            }
        }
        // Make sure we're not adding any bogus keyset info
        KeySetManagerService ksms = mSettings.mKeySetManagerService; // KeySetManagerService ksms 객체 초기화
        ksms.assertScannedPackageValid(pkg); //ksms객체의 assertScannedPackageValid함수 실행
        // writer
        synchronized (mPackages) { //mPackages와 동기화하며 아래작업 진행
            // We don't expect installation to fail beyond this point
            // Add the new setting to mSettings
            mSettings.insertPackageSettingLPw(pkgSetting, pkg); //mSettings의 insertPackageSettingLPw함수 통해 PackageSetting값 설정
            // Add the new setting to mPackages
            mPackages.put(pkg.applicationInfo.packageName, pkg); //mPackages에 pkg 내용 추가
            // Make sure we don't accidentally delete its data.
            final Iterator<PackageCleanItem> iter = mSettings.mPackagesToBeCleaned.iterator(); //iterator iter 선언
            while (iter.hasNext()) { //while구문으로 mSettings의 mPackagesToBeCleaned 관련 데이터를 돌며 불필요한 데이터 삭제
                PackageCleanItem item = iter.next();
                if (pkgName.equals(item.packageName)) {
                    iter.remove();
                }
            }
            // Take care of first install / last update times.
            if (currentTime != 0) { //currentTime변수가 0이 아닐 경우
                if (pkgSetting.firstInstallTime == 0) { //firstInstallTime값이 0일 때
                    pkgSetting.firstInstallTime = pkgSetting.lastUpdateTime = currentTime; //firstInstallTime값을 currentTime으로 설정
                } else if ((scanFlags&SCAN_UPDATE_TIME) != 0) { //SCAN_UPDATE_TIME설정이 ON인 경우
                    pkgSetting.lastUpdateTime = currentTime; //lastUpdateTime값을 currentTime으로 설정
                }
            } else if (pkgSetting.firstInstallTime == 0) { //firstInstallTime값이 0일 때
                // We need *something*.  Take time time stamp of the file.
                pkgSetting.firstInstallTime = pkgSetting.lastUpdateTime = scanFileTime; //firstInstallTime값을 scanFileTime으로 설정
            } else if ((parseFlags&PackageParser.PARSE_IS_SYSTEM_DIR) != 0) { //PARSE_IS_SYSTEM_DIR설정이 ON인 경우
                if (scanFileTime != pkgSetting.timeStamp) { //scanFileTime과 timeStamp이 같지 않다면
                    // A package on the system image has changed; consider this
                    // to be an update.
                    pkgSetting.lastUpdateTime = scanFileTime; //lastUpdateTime값을 scanFileTime으로 설정
                }
            }
            // Add the package's KeySets to the global KeySetManagerService
            ksms.addScannedPackageLPw(pkg); //ksms의 addScannedPackageLPw함수 실행
/*----------------------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------3-2.이하 패키지 관련 정보 로그 출력 부분--------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------------------*/
			int N = pkg.providers.size(); //for문 전용 변수 N 선언
            StringBuilder r = null; //로그 메세지 출력용 변수 r 선언
            int i; //for문 전용 변수 i 선언
            for (i=0; i<N; i++) {
                PackageParser.Provider p = pkg.providers.get(i); //i번째 providers의 값을 불러옴
                p.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        p.info.processName, pkg.applicationInfo.uid); //i번째 providers의 processName 값을 fixProcessName함수 활용하여 설정
                mProviders.addProvider(p); //mProviders 리스트에 i번쨰 providers 추가
                p.syncable = p.info.isSyncable; //i번째 providers의 syncable 값을 isSyncable값으로 설정
                if (p.info.authority != null) { //i번째 providers에 authority 값이 존재할 경우
                    String names[] = p.info.authority.split(";"); //names에 authority정보 입력 (;로 구분)
                    p.info.authority = null; //authority값 초기화
                    for (int j = 0; j < names.length; j++) { //names에 입력된 정보 for문으로 순차 확인
                        if (j == 1 && p.syncable) { //j가 1이고 i번째 proviers의 syncable값이 true일 경우
                            // We only want the first authority for a provider to possibly be
                            // syncable, so if we already added this provider using a different
                            // authority clear the syncable flag. We copy the provider before
                            // changing it because the mProviders object contains a reference
                            // to a provider that we don't want to change.
                            // Only do this for the second authority since the resulting provider
                            // object can be the same for all future authorities for this provider.
                            p = new PackageParser.Provider(p); //p 객체를 새로운 Provider 생성하여 할당 
                            p.syncable = false; //syncable 값 false로 변경
                        }
                        if (!mProvidersByAuthority.containsKey(names[j])) { //mProvidersByAuthority에 names[j]에 해당하는 내용이 없는 경우
                            mProvidersByAuthority.put(names[j], p); //mProvidersByAuthority에 names[j]값 입력
                            if (p.info.authority == null) { //authority가 null이면
                                p.info.authority = names[j]; //authority에 names[j]값 입력
                            } else { //authority에 값이 존재하면
                                p.info.authority = p.info.authority + ";" + names[j]; //authority에 ";" 문자 활용하여 names[j]값 추가 입력
                            }
                            if (DEBUG_PACKAGE_SCANNING) { //DEBUG_PACKAGE_SCANNING설정이 ON이면
                                if ((parseFlags & PackageParser.PARSE_CHATTY) != 0) //PARSE_CHATTY설정도 ON이면
                                    Log.d(TAG, "Registered content provider: " + names[j]
                                            + ", className = " + p.info.name + ", isSyncable = "
                                            + p.info.isSyncable); //로그 출력
                            }
                        } else { //mProvidersByAuthority에 names[j]에 해당하는 내용이 있는 경우
                            PackageParser.Provider other = mProvidersByAuthority.get(names[j]);
                            Slog.w(TAG, "Skipping provider name " + names[j] +
                                    " (in package " + pkg.applicationInfo.packageName +
                                    "): name already used by "
                                    + ((other != null && other.getComponentName() != null)
                                            ? other.getComponentName().getPackageName() : "?")); //관련 로그 출력
                        }
                    }
                }
                if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) { //PARSE_CHATTY설정이 ON인 경우
                    if (r == null) { //r에 메모리할당이 안되어 있으면
                        r = new StringBuilder(256); //r에 메모리 할당
                    } else { //r에 할당이 되어있으면
                        r.append(' ');  //공백 입력 후
                    }
                    r.append(p.info.name); //r에 p의 name속성 값 입력
                }
            }


            if (r != null) { //r에 내용이 있는 경우
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Providers: " + r); //DEBUG_PACKAGE_SCANNING설정이 ON이라면, 로그 출력
            }

            N = pkg.services.size(); //for문 검색 위한 변수 N 재설정
            r = null; //로그메세지용 변수 r 초기화
            for (i=0; i<N; i++) {
                PackageParser.Service s = pkg.services.get(i); //i번째 services 불러옴
                s.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        s.info.processName, pkg.applicationInfo.uid); //i번째 services에 fixProcessName함수 활용하여 processName 설정
                mServices.addService(s); //mServices에 i번째 services 추가
                if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) { //PARSE_CHATTY설정이 ON인 경우
                    if (r == null) {  //r에 메모리할당이 안되어 있으면
                        r = new StringBuilder(256); //r에 메모리 할당
                    } else { //r에 할당이 되어있으면
                        r.append(' ');  //공백 입력 후
                    }
                    r.append(s.info.name); //r에 s의 name속성 값 입력
                }
            }
            if (r != null) { //r에 내용이 있는 경우
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Services: " + r); //DEBUG_PACKAGE_SCANNING설정이 ON이라면, 로그 출력
            }
            N = pkg.receivers.size(); //for문 검색 위한 변수 N 재설정
            r = null; //로그메세지용 변수 r 초기화
            for (i=0; i<N; i++) {
                PackageParser.Activity a = pkg.receivers.get(i); //i번째 receivers 불러옴
                a.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        a.info.processName, pkg.applicationInfo.uid); //i번째 receivers에 fixProcessName함수 활용하여 processName 설정
                mReceivers.addActivity(a, "receiver"); //mServices에 i번째 receivers 추가
                if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) { //PARSE_CHATTY설정이 ON인 경우
                    if (r == null) { //r에 메모리할당이 안되어 있으면
                        r = new StringBuilder(256); //r에 메모리 할당
                    } else { //r에 할당이 되어있으면
                        r.append(' '); //공백 입력 후
                    }
                    r.append(a.info.name); //r에 p의 name속성 값 입력
                }
            }
            if (r != null) { //r에 내용이 있는 경우
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Receivers: " + r); //DEBUG_PACKAGE_SCANNING설정이 ON이라면, 로그 출력
            }
            N = pkg.activities.size(); //for문 검색 위한 변수 N 재설정
            r = null; //로그메세지용 변수 r 초기화
            for (i=0; i<N; i++) {
                PackageParser.Activity a = pkg.activities.get(i); //i번째 activities 불러옴
                a.info.processName = fixProcessName(pkg.applicationInfo.processName,
                        a.info.processName, pkg.applicationInfo.uid); //i번째 activities에 fixProcessName함수 활용하여 processName 설정
                mActivities.addActivity(a, "activity"); //mActivities에 i번째 activities 추가
                if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) { //PARSE_CHATTY설정이 ON인 경우
                    if (r == null) { //r에 메모리할당이 안되어 있으면
                        r = new StringBuilder(256); //r에 메모리 할당
                    } else { //r에 할당이 되어있으면
                        r.append(' '); //공백 입력 후
                    }
                    r.append(a.info.name); //r에 a의 name속성 값 입력
                }
            }
            if (r != null) { //r에 내용이 있는 경우
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Activities: " + r); //DEBUG_PACKAGE_SCANNING설정이 ON이라면, 로그 출력
            }
            N = pkg.permissionGroups.size(); //for문 검색 위한 변수 N 재설정
            r = null; //로그메세지용 변수 r 초기화
            for (i=0; i<N; i++) {
                PackageParser.PermissionGroup pg = pkg.permissionGroups.get(i); //i번째 permissionGroups 불러옴
                PackageParser.PermissionGroup cur = mPermissionGroups.get(pg.info.name);  //i번째 permissionGroups의 mPermissionGroups데이터 불러옴
                if (cur == null) { //mPermissionGroups데이터가 null일 경우
                    mPermissionGroups.put(pg.info.name, pg); //mPermissionGroups에 i번째 permissionGroups 추가
                    if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) { //PARSE_CHATTY설정이 ON인 경우
                        if (r == null) { //r에 메모리할당이 안되어 있으면
                            r = new StringBuilder(256); //r에 메모리 할당
                        } else { //r에 할당이 되어있으면
                            r.append(' '); //공백 입력 후
                        }
                        r.append(pg.info.name); //r에 pg의 name속성 값 입력
                    }
                } else { //mPermissionGroups데이터가 존재할 경우
                    Slog.w(TAG, "Permission group " + pg.info.name + " from package "
                            + pg.info.packageName + " ignored: original from "
                            + cur.info.packageName); //관련 로그 출력
                    if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) { //PARSE_CHATTY설정이 ON인 경우
                        if (r == null) { //r에 메모리할당이 안되어 있으면
                            r = new StringBuilder(256); //r에 메모리 할당
                        } else { //r에 할당이 되어있으면
                            r.append(' '); //공백 입력 후
                        }
                        r.append("DUP:"); //r에 "DUP: 'pg의 name 속성 값' " 입력
                        r.append(pg.info.name); 
                    }
                }
            }
            if (r != null) { //r에 내용이 있는 경우
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Permission Groups: " + r); //DEBUG_PACKAGE_SCANNING설정이 ON이라면, 로그 출력
            }
            N = pkg.permissions.size(); //for문 검색 위한 변수 N 재설정
            r = null; //로그메세지용 변수 r 초기화
            for (i=0; i<N; i++) {
                PackageParser.Permission p = pkg.permissions.get(i); //i번째 permissions 불러옴
                // Assume by default that we did not install this permission into the system.
                p.info.flags &= ~PermissionInfo.FLAG_INSTALLED; //i번째 permission의 flags에 FLAG_INSTALLED의 값을 제거
                // Now that permission groups have a special meaning, we ignore permission
                // groups for legacy apps to prevent unexpected behavior. In particular,
                // permissions for one app being granted to someone just becuase they happen
                // to be in a group defined by another app (before this had no implications).
                if (pkg.applicationInfo.targetSdkVersion > Build.VERSION_CODES.LOLLIPOP_MR1) { //pkg의 targetSdkVersion이 LOLLIPOP_MR1보다 상위버전일 경우
                    p.group = mPermissionGroups.get(p.info.group); //p.group에 p.info.group값 입력
                    // Warn for a permission in an unknown group.
                    if (p.info.group != null && p.group == null) { //p.info.group은 null이 아닌데 p.group은 null인 경우
                        Slog.w(TAG, "Permission " + p.info.name + " from package "
                                + p.info.packageName + " in an unknown group " + p.info.group); //관련 로그 출력
                    }
                }
                ArrayMap<String, BasePermission> permissionMap =
                        p.tree ? mSettings.mPermissionTrees
                                : mSettings.mPermissions;	//permissionMap 값 설정, 
															//p.tree의 값이 true이면 mSettings.mPermissionTrees, 
															//p.tree의 값이 false면 mSettings.mPermissions로 설정
                BasePermission bp = permissionMap.get(p.info.name); //변수 bp 값 설정
                // Allow system apps to redefine non-system permissions
                if (bp != null && !Objects.equals(bp.sourcePackage, p.info.packageName)) { //bp값이 존재하고, bp.sourcePackage와 p.info.packageName가 일치하지 않을 시)
                    final boolean currentOwnerIsSystem = (bp.perm != null
                            && isSystemApp(bp.perm.owner)); //currentOwnerIsSystem의 값 설정
                    if (isSystemApp(p.owner)) { //p.owner가 SystemApp일 경우
                        if (bp.type == BasePermission.TYPE_BUILTIN && bp.perm == null) { //bp.type이 TYPE_BUILTIN타입과 같고, bp.perm이 null이라면
                            // It's a built-in permission and no owner, take ownership now
                            bp.packageSetting = pkgSetting; //bp의 packageSetting값 설정
                            bp.perm = p; //bp의 perm값 설정
                            bp.uid = pkg.applicationInfo.uid; //bp의 uid값 설정
                            bp.sourcePackage = p.info.packageName; //bp의 sourcePackage 값 설정
                            p.info.flags |= PermissionInfo.FLAG_INSTALLED; //FLAG_INSTALLED 플래그 ON
                        } else if (!currentOwnerIsSystem) { //currentOwnerIsSystem값이 false인 경우
                            String msg = "New decl " + p.owner + " of permission  "
                                    + p.info.name + " is system; overriding " + bp.sourcePackage;
                            reportSettingsProblem(Log.WARN, msg); //관련 로그 출력
                            bp = null; //bp 비할당
                        }
                    }
                }
                if (bp == null) { //bp가 null일 경우
                    bp = new BasePermission(p.info.name, p.info.packageName,
                            BasePermission.TYPE_NORMAL); //bp에 새로운 BasePermission객체 할당
                    permissionMap.put(p.info.name, bp); //permissionMap에 bp 추가
                }
                if (bp.perm == null) { //bp.perm이 null일 경우
                    if (bp.sourcePackage == null
                            || bp.sourcePackage.equals(p.info.packageName)) { //sourcePackage값이 null이거나 sourcePackage의 이름이 p의 packageName과 같을 경우
                        BasePermission tree = findPermissionTreeLP(p.info.name); //tree 변수를 findPermissionTreeLP함수 활용하여 설정
                        if (tree == null
                                || tree.sourcePackage.equals(p.info.packageName)) { //tree 변수가 null이거나 tree의 sourcePackage가 p의 packageName과 같을 경우
                            bp.packageSetting = pkgSetting; //bp의 packageSetting값 설정
                            bp.perm = p; //bp의 perm값 설정
                            bp.uid = pkg.applicationInfo.uid; //bp의 uid값 설정
                            bp.sourcePackage = p.info.packageName; //bp의 sourcePackage 값 설정
                            p.info.flags |= PermissionInfo.FLAG_INSTALLED; //FLAG_INSTALLED 플래그 ON
                            if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) { //PARSE_CHATTY설정이 ON인 경우
                                if (r == null) { //r에 메모리할당이 안되어 있으면
                                    r = new StringBuilder(256); //r에 메모리 할당
                                } else { //r에 할당이 되어있으면
                                    r.append(' '); //공백 입력 후
                                }
                                r.append(p.info.name); //r에 p의 name속성 값 입력
                            }
                        } else { //tree 변수가 null이 아니고, tree의 sourcePackage가 p의 packageName도 다를 경우
                            Slog.w(TAG, "Permission " + p.info.name + " from package "
                                    + p.info.packageName + " ignored: base tree "
                                    + tree.name + " is from package "
                                    + tree.sourcePackage); //관련 로그 출력
                        }
                    } else { //sourcePackage값이 null이 아니고, sourcePackage의 이름이 p의 packageName과 다를 경우
                        Slog.w(TAG, "Permission " + p.info.name + " from package "
                                + p.info.packageName + " ignored: original from "
                                + bp.sourcePackage); //관련 로그 출력
                    }
                } else if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) { //PARSE_CHATTY설정이 ON인 경우
                    if (r == null) { //r에 메모리할당이 안되어 있으면
                        r = new StringBuilder(256); //r에 메모리 할당
                    } else { //r에 할당이 되어있으면
                        r.append(' '); //공백 입력 후
                    }
                    r.append("DUP:"); //r에 "DUP: 'p의 name 속성 값' " 입력
                    r.append(p.info.name);
                }
                if (bp.perm == p) { //bp.perm이 p와 같은 경우
                    bp.protectionLevel = p.info.protectionLevel; //protectionLevel 속성 값 설정
                }
            }
            if (r != null) { //r에 내용이 있는 경우
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Permissions: " + r); //DEBUG_PACKAGE_SCANNING설정이 ON이라면, 로그 출력
            }
            N = pkg.instrumentation.size(); //for문 검색 위한 변수 N 재설정
            r = null; //로그메세지용 변수 r 초기화
            for (i=0; i<N; i++) {
                PackageParser.Instrumentation a = pkg.instrumentation.get(i); //i번째 Instrumentation 불러옴
                a.info.packageName = pkg.applicationInfo.packageName;						//i번째 Instrumentation의 packageName값 설정
                a.info.sourceDir = pkg.applicationInfo.sourceDir;							//i번째 Instrumentation의 sourceDir값 설정
                a.info.publicSourceDir = pkg.applicationInfo.publicSourceDir;				//i번째 Instrumentation의 publicSourceDir값 설정
                a.info.splitSourceDirs = pkg.applicationInfo.splitSourceDirs;				//i번째 Instrumentation의 splitSourceDirs값 설정
                a.info.splitPublicSourceDirs = pkg.applicationInfo.splitPublicSourceDirs;	//i번째 Instrumentation의 splitPublicSourceDirs값 설정
                a.info.dataDir = pkg.applicationInfo.dataDir;								//i번째 Instrumentation의 dataDir값 설정
                // TODO: Update instrumentation.nativeLibraryDir as well ? Does it
                // need other information about the application, like the ABI and what not ?
                a.info.nativeLibraryDir = pkg.applicationInfo.nativeLibraryDir;				//i번째 Instrumentation의 nativeLibraryDir값 설정
                mInstrumentation.put(a.getComponentName(), a);		//mInstrumentation에 i번째 Instrumentation 추가
                if ((parseFlags&PackageParser.PARSE_CHATTY) != 0) { //PARSE_CHATTY설정이 ON인 경우
                    if (r == null) { //r에 메모리할당이 안되어 있으면
                        r = new StringBuilder(256); //r에 메모리 할당
                    } else { //r에 할당이 되어있으면
                        r.append(' '); //공백 입력 후
                    }
                    r.append(a.info.name); //r에 a의 name속성 값 입력
                }
            }
            if (r != null) { //r에 내용이 있는 경우
                if (DEBUG_PACKAGE_SCANNING) Log.d(TAG, "  Instrumentation: " + r); //DEBUG_PACKAGE_SCANNING설정이 ON이라면, 로그 출력
            }
            if (pkg.protectedBroadcasts != null) { //pkg의 protectedBroadcasts값이 null이 아닌 경우
                N = pkg.protectedBroadcasts.size(); //for구문 돌기 위한 변수 N 설정
                for (i=0; i<N; i++) {
                    mProtectedBroadcasts.add(pkg.protectedBroadcasts.get(i)); //i번째 protectedBroadcasts값을 mProtectedBroadcasts 리스트에 추가
                }
            }
            pkgSetting.setTimeStamp(scanFileTime); //setTimeStamp 함수를 사용하여 scanFileTime값 입력
            // Create idmap files for pairs of (packages, overlay packages).
            // Note: "android", ie framework-res.apk, is handled by native layers.
            if (pkg.mOverlayTarget != null) { //mOverlayTarget값이 존재할 경우
                // This is an overlay package.
                if (pkg.mOverlayTarget != null && !pkg.mOverlayTarget.equals("android")) { //mOverlayTarget갓ㅂ이 존재하고 오버레이 타겟이 android일 경우
                    if (!mOverlays.containsKey(pkg.mOverlayTarget)) { //mOverlayTarget 값이 mOverlays 리스트에 존재하지 않는다면
                        mOverlays.put(pkg.mOverlayTarget,
                                new ArrayMap<String, PackageParser.Package>()); //mOverlays 리스트에 mOverlayTarget값 입력
                    }
                    ArrayMap<String, PackageParser.Package> map = mOverlays.get(pkg.mOverlayTarget); //mOverlays 리스트에 입력되어 있는 mOverlayTarget값을 호출
                    map.put(pkg.packageName, pkg); //불러온 map에 pkg 데이터 입력
                    PackageParser.Package orig = mPackages.get(pkg.mOverlayTarget); //orig에 mOverlayTarget값 불러와서 입력
                    if (orig != null && !createIdmapForPackagePairLI(orig, pkg)) { //orig값이 존재하나 createIdmapForPackagePairLI함수 실행이 실패했을 경우
                        throw new PackageManagerException(INSTALL_FAILED_UPDATE_INCOMPATIBLE,
                                "scanPackageLI failed to createIdmap"); //오류제어구문으로 이동
                    }
                }
            } else if (mOverlays.containsKey(pkg.packageName) &&
                    !pkg.packageName.equals("android")) { //mOverlayTarget값이 존재하지 않고, mOverlays에 pkg관련 내용이 있으며 안드로이드가 아닐 경우
                // This is a regular package, with one or more known overlay packages.
                createIdmapsForPackageLI(pkg); //createIdmapsForPackageLI함수 실행
            }
        }
/*----------------------------------------------------------------------------------------------------------------------------------*/
/*-----------------------------------------------4.함수 종료, 입력했던 패키지(pkg) 리턴---------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------------------*/		
		return pkg; //함수실행 완료, pkg 리턴
    }