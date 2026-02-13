import UIKit
import Vision

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
