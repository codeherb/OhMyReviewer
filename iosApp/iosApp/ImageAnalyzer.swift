import UIKit
import Vision

#if canImport(FoundationModels)
import FoundationModels
#endif

/// Vision Framework를 사용한 이미지 분석 서비스
/// 이미지에서 라벨(키워드)을 추출합니다
class ImageAnalyzer {
    
    /// 싱글톤 인스턴스
    static let shared = ImageAnalyzer()
    
    private init() {}
    
    /// 이미지를 분석하여 라벨(키워드) 목록을 반환합니다
    /// - Parameter image: 분석할 UIImage
    /// - Returns: 추출된 키워드 배열
    func analyzeImage(_ image: UIImage) async -> [String] {
        guard let cgImage = image.cgImage else {
            return []
        }
        
        return await withCheckedContinuation { continuation in
            let request = VNClassifyImageRequest { request, error in
                if let error = error {
                    print("Vision 분석 오류: \(error.localizedDescription)")
                    continuation.resume(returning: [])
                    return
                }
                
                guard let observations = request.results as? [VNClassificationObservation] else {
                    continuation.resume(returning: [])
                    return
                }
                
                // 신뢰도 0.3 이상인 상위 10개 라벨 추출
                let labels = observations
                    .filter { $0.confidence > 0.3 }
                    .prefix(10)
                    .map { $0.identifier }
                
                continuation.resume(returning: Array(labels))
            }
            
            let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
            
            do {
                try handler.perform([request])
            } catch {
                print("Vision 요청 실행 오류: \(error.localizedDescription)")
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
        do {
            let session = LanguageModelSession()
            let prompt = """
            다음은 배달 음식 사진에서 추출한 정보입니다.
            분류 라벨: \(labels.joined(separator: ", "))
            인식된 텍스트: \(texts.joined(separator: ", "))
            
            위 정보를 바탕으로 배달 음식 리뷰에 사용할 수 있는 키워드 5-8개를 쉼표로 구분하여 나열해주세요.
            음식 맛, 양, 포장 상태, 배달 상태 등 배달 음식 리뷰에 적합한 키워드를 추출해주세요.
            반드시 한글로만 출력하세요.
            키워드만 출력하세요.
            """
            let response = try await session.respond(to: prompt)
            return response.content
                .split(separator: ",")
                .map { $0.trimmingCharacters(in: .whitespaces) }
                .filter { !$0.isEmpty }
        } catch {
            print("FoundationModels 키워드 생성 오류: \(error.localizedDescription)")
            return labels + texts
        }
    }
    #endif
    
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
            }
        }
    }
}
