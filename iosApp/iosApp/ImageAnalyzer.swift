import UIKit
import Vision
import CoreML

#if canImport(FoundationModels)
import FoundationModels
#endif

/// Vision Framework + Food101 모델을 사용한 이미지 분석 서비스
/// 음식 이미지에서 라벨(키워드)을 추출합니다
class ImageAnalyzer {
    
    /// 싱글톤 인스턴스
    static let shared = ImageAnalyzer()
    
    /// Food101 CoreML 모델 (있을 경우)
    private var food101Model: VNCoreMLModel?
    
    /// 음식 관련 키워드 (필터링용)
    private let foodRelatedKeywords: Set<String> = [
        // 음식 일반
        "food", "meal", "dish", "cuisine", "snack", "dessert", "appetizer",
        // 조리 방법
        "fried", "grilled", "baked", "steamed", "roasted", "boiled", "raw",
        // 재료
        "meat", "chicken", "beef", "pork", "fish", "seafood", "vegetable",
        "rice", "noodle", "bread", "cheese", "egg", "tofu", "mushroom",
        // 음식 종류
        "pizza", "pasta", "burger", "sandwich", "salad", "soup", "stew",
        "sushi", "ramen", "curry", "taco", "burrito", "dumpling",
        // 음료/디저트
        "drink", "beverage", "coffee", "tea", "juice", "cake", "ice cream",
        // 한식
        "korean", "kimchi", "bibimbap", "bulgogi", "japchae"
    ]
    
    /// 제외할 키워드 (테이블웨어, 주변 환경, 가전제품)
    private let excludeKeywords: Set<String> = [
        // 식기류
        "plate", "bowl", "cup", "glass", "fork", "spoon", "knife", "chopsticks",
        "table", "tablecloth", "napkin", "tray", "container", "box",
        // 환경
        "restaurant", "kitchen", "dining", "indoor", "outdoor",
        // 사람
        "hand", "person", "people", "finger",
        // 가전제품/조리기구
        "appliance", "grill", "oven", "stove", "microwave", "refrigerator",
        "cookware", "pan", "pot", "griddle", "burner"
    ]
    
    private init() {
        loadFood101Model()
    }
    
    /// Food101 모델 로드 시도
    private func loadFood101Model() {
        // Food101.mlmodel이 프로젝트에 있으면 로드
        if let modelURL = Bundle.main.url(forResource: "Food101", withExtension: "mlmodelc"),
           let model = try? MLModel(contentsOf: modelURL),
           let visionModel = try? VNCoreMLModel(for: model) {
            food101Model = visionModel
            print("###Food101 모델 로드 성공")
        } else {
            print("###Food101 모델 없음 - 기본 Vision + 필터링 사용")
        }
    }
    
    /// 이미지를 분석하여 음식 관련 라벨(키워드) 목록을 반환합니다
    /// Food101 모델이 있으면 우선 사용하고, 없으면 기본 Vision + 필터링 사용
    /// - Parameter image: 분석할 UIImage
    /// - Returns: 추출된 키워드 배열 (음식 우선)
    func analyzeImage(_ image: UIImage) async -> [String] {
        print("### [DEBUG] analyzeImage 시작")
        print("### [DEBUG] 이미지 크기: \(image.size)")
        
        guard let cgImage = image.cgImage else {
            print("### [DEBUG] cgImage 변환 실패!")
            return []
        }
        print("### [DEBUG] cgImage 변환 성공")
        
        // Food101 모델이 있으면 음식 분류 우선 실행
        var foodLabels: [String] = []
        if food101Model != nil {
            print("### [DEBUG] Food101 모델 있음 - 분석 시작")
            foodLabels = await analyzeWithFood101(cgImage: cgImage)
            print("### [DEBUG] Food101 결과: \(foodLabels)")
        } else {
            print("### [DEBUG] Food101 모델 없음!")
        }
        
        // 기본 Vision 분류 (필터링 적용)
        print("### [DEBUG] Vision 분류 시작")
        let visionLabels = await analyzeWithVision(cgImage: cgImage)
        print("### [DEBUG] Vision 결과: \(visionLabels)")
        
        // Food101 결과를 우선, Vision 결과로 보완
        var combinedLabels = foodLabels
        for label in visionLabels {
            if !combinedLabels.contains(label) {
                combinedLabels.append(label)
            }
        }
        
        print("### [DEBUG] 최종 결과: \(combinedLabels)")
        return Array(combinedLabels.prefix(10))
    }
    
    /// Food101 모델로 음식 분류
    private func analyzeWithFood101(cgImage: CGImage) async -> [String] {
        guard let model = food101Model else { 
            print("### [DEBUG] Food101 모델이 nil!")
            return [] 
        }
        
        return await withCheckedContinuation { continuation in
            let request = VNCoreMLRequest(model: model) { request, error in
                if let error = error {
                    print("### [DEBUG] Food101 분석 오류: \(error.localizedDescription)")
                    continuation.resume(returning: [])
                    return
                }
                
                guard let observations = request.results as? [VNClassificationObservation] else {
                    print("### [DEBUG] Food101 observations 변환 실패")
                    continuation.resume(returning: [])
                    return
                }
                
                print("### [DEBUG] Food101 전체 결과 수: \(observations.count)")
                // 상위 5개 출력 (신뢰도 상관없이)
                for (index, obs) in observations.prefix(5).enumerated() {
                    print("### [DEBUG] Food101 [\(index)]: \(obs.identifier) - \(obs.confidence)")
                }
                
                // 신뢰도 0.7 이상인 상위 5개 음식 라벨
                let labels = observations
                    .filter { $0.confidence > 0.5 }
                    .prefix(5)
                    .map { self.formatFoodLabel($0.identifier) }
                
                print("### [DEBUG] Food101 필터링 후 결과: \(labels)")
                continuation.resume(returning: Array(labels))
            }
            
            let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
            
            do {
                try handler.perform([request])
            } catch {
                print("### [DEBUG] Food101 요청 실행 오류: \(error.localizedDescription)")
                continuation.resume(returning: [])
            }
        }
    }
    
    /// 기본 Vision으로 분류 (음식 관련 필터링 적용)
    private func analyzeWithVision(cgImage: CGImage) async -> [String] {
        return await withCheckedContinuation { continuation in
            let request = VNClassifyImageRequest { [weak self] request, error in
                guard let self = self else {
                    print("### [DEBUG] Vision self가 nil!")
                    continuation.resume(returning: [])
                    return
                }
                
                if let error = error {
                    print("### [DEBUG] Vision 분석 오류: \(error.localizedDescription)")
                    continuation.resume(returning: [])
                    return
                }
                
                guard let observations = request.results as? [VNClassificationObservation] else {
                    print("### [DEBUG] Vision observations 변환 실패")
                    continuation.resume(returning: [])
                    return
                }
                
                print("### [DEBUG] Vision 전체 결과 수: \(observations.count)")
                // 상위 5개 출력 (신뢰도 상관없이)
                for (index, obs) in observations.prefix(5).enumerated() {
                    print("### [DEBUG] Vision [\(index)]: \(obs.identifier) - \(obs.confidence)")
                }
                
                // 결과 필터링 및 정렬 (음식 관련 우선)
                let filteredLabels = observations
                    .filter { $0.confidence > 0.15 }
                    .map { ($0.identifier, $0.confidence, self.calculateFoodScore($0.identifier)) }
                    .sorted { $0.2 > $1.2 } // 음식 점수로 정렬
                    .prefix(10)
                    .map { $0.0 }
                
                print("### [DEBUG] Vision 필터링 후 결과: \(filteredLabels)")
                continuation.resume(returning: Array(filteredLabels))
            }
            
            let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
            
            do {
                try handler.perform([request])
            } catch {
                print("### [DEBUG] Vision 요청 실행 오류: \(error.localizedDescription)")
                continuation.resume(returning: [])
            }
        }
    }

    /// Vision 라벨 + OCR 텍스트를 FoundationModels로 한국어 키워드로 변환합니다
    /// - Parameters:
    ///   - labels: Vision 분류 라벨
    ///   - texts: OCR 인식 텍스트
    /// - Returns: 한국어 키워드 배열
    #if canImport(FoundationModels)
    @available(iOS 26, *)
    func enrichKeywords(labels: [String], texts: [String]) async -> [String] {
        // 음식 관련 라벨만 필터링 (테이블웨어 등 제외)
        let foodLabels = labels.filter { label in
            let lowercased = label.lowercased()
            return !excludeKeywords.contains(where: { lowercased.contains($0) })
        }
        
        // 음식명 후보 추출 (food, meat 같은 일반 단어 제외)
        let genericWords = ["food", "meal", "dish", "cuisine", "meat", "structure", "wood_processed"]
        let specificFoodLabels = foodLabels.filter { !genericWords.contains($0.lowercased()) }
        
        do {
            let session = LanguageModelSession()
            let prompt = """
            배달 음식 사진에서 인식된 음식들입니다:
            \(specificFoodLabels.joined(separator: ", "))
            
            위 음식명을 모두 한글로 번역하고, 특징 키워드를 추가해주세요.
            
            규칙:
            1. 인식된 음식명을 모두 한글로 번역 (예: spaghetti→스파게티, egg→계란, soup→수프)
            2. 음식명 번역 후 특징 키워드 추가 (예: 푸짐한, 맛있어 보이는)
            3. 총 5-8개, 쉼표로 구분
            4. 한글 키워드만 출력
            
            예시 입력: spaghetti, egg, soup
            예시 출력: 스파게티, 계란, 수프, 푸짐한, 따뜻해 보이는
            """
            let response = try await session.respond(to: prompt)
            let keywords = response.content
                .split(separator: ",")
                .map { $0.trimmingCharacters(in: .whitespaces) }
                .filter { !$0.isEmpty }
            
            print("### [enrichKeywords] 입력 라벨: \(specificFoodLabels)")
            print("### [enrichKeywords] 생성된 키워드: \(keywords)")
            
            return keywords
        } catch {
            print("FoundationModels 키워드 생성 오류: \(error.localizedDescription)")
            return foodLabels + texts
        }
    }
    #endif
    
    /// 음식 관련도 점수 계산
    private func calculateFoodScore(_ label: String) -> Float {
        let lowercased = label.lowercased()
        
        // 제외 키워드면 매우 낮은 점수
        if excludeKeywords.contains(where: { lowercased.contains($0) }) {
            return 0.1
        }
        
        // 음식 관련 키워드면 높은 점수
        if foodRelatedKeywords.contains(where: { lowercased.contains($0) }) {
            return 1.0
        }
        
        // 기본 점수
        return 0.5
    }
    
    /// Food101 라벨 포맷팅 (언더스코어를 공백으로)
    private func formatFoodLabel(_ label: String) -> String {
        return label.replacingOccurrences(of: "_", with: " ").capitalized
    }
    
    /// 이미지에서 텍스트를 인식합니다 (OCR)
    /// - Parameter image: 분석할 UIImage
    /// - Returns: 인식된 텍스트 배열
    func recognizeText(_ image: UIImage) async -> [String] {
        guard let cgImage = image.cgImage else {
            return []
        }
        
        return await withCheckedContinuation { continuation in
            let request = VNRecognizeTextRequest { request, error in
                if let error = error {
                    print("텍스트 인식 오류: \(error.localizedDescription)")
                    continuation.resume(returning: [])
                    return
                }
                
                guard let observations = request.results as? [VNRecognizedTextObservation] else {
                    continuation.resume(returning: [])
                    return
                }
                
                let texts = observations.compactMap { observation in
                    observation.topCandidates(1).first?.string
                }
                
                continuation.resume(returning: texts)
            }
            
            // 한국어 지원
            request.recognitionLanguages = ["ko-KR", "en-US"]
            request.recognitionLevel = .accurate
            
            let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
            
            do {
                try handler.perform([request])
            } catch {
                print("텍스트 인식 요청 실행 오류: \(error.localizedDescription)")
                continuation.resume(returning: [])
            }
        }
    }
}
