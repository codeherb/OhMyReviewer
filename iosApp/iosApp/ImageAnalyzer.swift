import UIKit
import Shared

#if canImport(FoundationModels)
import FoundationModels
#endif

/// KMP ImageAnalyzer를 통해 Cloud AI로 키워드를 추출하는 이미지 분석 서비스
class ImageAnalyzer {

    /// 싱글톤 인스턴스
    static let shared = ImageAnalyzer()

    /// 제외할 키워드 (enrichKeywords 필터링용)
    private let excludeKeywords: Set<String> = [
//        // 식기류
//        "plate", "bowl", "cup", "glass", "fork", "spoon", "knife", "chopsticks",
//        "table", "tablecloth", "napkin", "tray", "container", "box",
//        // 환경
//        "restaurant", "kitchen", "dining", "indoor", "outdoor",
//        // 사람
//        "hand", "person", "people", "finger",
//        // 가전제품/조리기구
//        "appliance", "grill", "oven", "stove", "microwave", "refrigerator",
//        "cookware", "pan", "pot", "griddle", "burner"
    ]
    
    private init() {}

    /// KMP ImageAnalyzer를 통해 이미지를 분석하여 키워드 목록을 반환합니다
    /// - Parameter image: 분석할 UIImage
    /// - Returns: 추출된 키워드 배열
    func analyzeImage(_ image: UIImage) async -> [String] {
        guard let imageData = image.jpegData(compressionQuality: 0.9) else {
            print("### [DEBUG] JPEG 변환 실패")
            return []
        }

        let imageBytes = imageData.toKotlinByteArray()
        let width = Int32(image.size.width)
        let height = Int32(image.size.height)

        do {
            let analyzer = Shared.ImageAnalyzer()
            let labels = try await analyzer.analyzeImage(imageBytes: imageBytes, width: width, height: height)
            let keywords = labels.map { $0.text }
            print("### [DEBUG] KMP 분석 결과: \(keywords)")
            return keywords
        } catch {
            print("### [DEBUG] KMP ImageAnalyzer 오류: \(error)")
            return []
        }
    }

    /// Vision 라벨 + OCR 텍스트를 FoundationModels로 한국어 키워드로 변환합니다
    /// - Parameters:
    ///   - labels: Vision 분류 라벨
    ///   - texts: OCR 인식 텍스트
    /// - Returns: 한국어 키워드 배열
    #if canImport(FoundationModels)
    @available(iOS 26, *)
    func enrichKeywords(labels: [String]) async -> [String] {
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
            return foodLabels
        }
    }
    #endif
    

}

// MARK: - Data 변환 헬퍼

private extension Data {
    /// Data를 KotlinByteArray로 변환 (KMP ImageAnalyzer 호출용)
    func toKotlinByteArray() -> KotlinByteArray {
        let result = KotlinByteArray(size: Int32(count))
        for (index, byte) in enumerated() {
            result.set(index: Int32(index), value: Int8(bitPattern: byte))
        }
        return result
    }
}
