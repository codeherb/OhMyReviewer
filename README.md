# OhMyReviewer
---
## 1. 개요 (Overview)
---
OhMyReviewer는 사용자가 입력한 키워드와 사진을 바탕으로 On-Device AI 기술을 활용하여 고품질의 리뷰 텍스트를 자동으로 생성하고, 이를 다양한 플랫폼에 공유할 수 있도록 돕는 서비스입니다.

## 2. 제품의 비전 (Product Vision)
---
OhMyReviewer는 사용자가 입력한 키워드와 사진을 바탕으로 On-Device AI 기술을 활용하여 고품질의 리뷰 텍스트를 자동으로 생성하고, 이를 다양한 플랫폼에 공유할 수 있도록 돕는 서비스입니다.

## 3. 핵심 기능 (Core Features)
---

### 3.1 AI 리뷰 생성 (AI Generation)
- 키워드 기반 생성: 사용자가 입력한 몇 가지 핵심 단어(예: 맛있다, 분위기 좋다, 친절함)를 조합하여 자연스러운 문장 생성.
- 이미지 분석: 업로드된 사진을 분석하여 상황에 맞는 리뷰 초안 작성 (예: 음식 사진 인식 후 맛에 대한 표현 추천).
- 멀티모달 입력: 사진과 키워드를 동시에 입력받아 맥락에 가장 적합한 리뷰 생성.

### 3.2 On-Device AI 처리
- 개인정보 보호: 데이터를 서버로 전송하지 않고 기기 내에서 처리하여 보안 강화.
- 오프라인 지원: 네트워크 연결 없이도 기본 기능 수행 가능.
- 빠른 응답 속도: 서버 통신 대기 시간 없는 즉각적인 텍스트 생성.

### 3.3 리뷰 관리 및 공유
- 공유하기: 생성된 리뷰를 클립보드에 복사하거나 인스타그램, 블로그, 지도 앱 등에 즉시 공유.
- 히스토리 저장: 이전에 생성했던 리뷰들을 로컬 DB에 저장하여 관리.

## 4. 기술 스택 (Tech Stack)
---

### 4.1 Cross-Platform Framework
- Kotlin Multiplatform (KMP): Android 및 iOS의 비즈니스 로직 및 UI 공유.
- Compose Multiplatform: 선언형 UI 프레임워크를 이용한 일관된 UX 제공.

### 4.2 On-Device AI
- Google ML Kit: 이미지 라벨링 및 텍스트 생성 (https://developers.google.com/ml-kit/genai?hl=ko)
  - genai-prompt - 온디바이스 텍스트 생성 (Gemini Nano) 
    - https://developers.google.com/ml-kit/genai/prompt/android?hl=ko
  - 이미지 라벨링 - 지원 안되는 기기에서 활용해야 할 듯
    - https://developers.google.com/ml-kit/vision/image-labeling?hl=ko
  - 이미지 설명 기능(BETA) - 실제 지원 가능 기기 제한이 있음 (에뮬의 경우 pixel10에서 가능)
    - https://developers.google.com/ml-kit/genai/image-description/android?hl=ko
- Foundation Models (MediaPipe): 온디바이스 텍스트 생성 및 멀티모달 추론.
  - iOS Vision - 이미지 라벨링
  - 텍스트 생성 기능은 파운데이션 모델을 통해 진행

### 4.3 기타
- Koin: 의존성 주입(DI).

## 5. 타겟 사용자 (Target Audience)
---
- 블로그나 SNS에 꾸준히 리뷰를 남기고 싶지만 글쓰기에 어려움을 느끼는 사용자.
- 배달 앱, 지도 앱 등에 빠른 피드백을 남기고 싶은 사용자.
- 개인정보 보호를 중시하며 빠르고 가벼운 앱을 선호하는 사용자.

## 6. 프로젝트 구성 (Project Structure)
---

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for the code that will be shared between all targets in the project.
  The most important subfolder is [commonMain](./shared/src/commonMain/kotlin). If preferred, you
  can add code to the platform-specific folders here too.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### 트러블 슈팅
- iOS Xcode 빌드 실패 해결법
  - Target > iosApp > BuildPhases > Compile Kotlin Framework 스크립트 내용 수정

```
if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
exit 0
fi

# 추가코드 START
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
# 추가코드 END

cd "$SRCROOT/.."
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

- 코루틴 디펜던시 문제 해결법 
  - TODO

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## 7. 로드맵 (Roadmap)
---
- [X] Phase 1: KMP 프로젝트 환경 설정 및 기본 UI 구성.
- [X] Phase 2: ML Kit과 Foundation Model 기능 확인
  - ML Kit - 해보긴 했는데 단말 제한이 있는 상황
  - Foundation Model - 셋팅 중
- [ ] 메인 화면 구성
  - 타일 형태로 해서 각 도구 혹은 예시들 화면으로 전환
- [ ] Phase 3: 이미지 분석 기능 구현.
  - 간단한 UI 구성
  - 각 SDK 연동
  - 키워드 추출
- [ ] Phase 4: 온디바이스 Foundation Model 연동 및 리뷰 생성 로직 최적화.
- [ ] Phase 5: 공유 기능 및 사용자 히스토리 기능 추가.
- [ ] Phase 6: Android/iOS 스토어 배포.