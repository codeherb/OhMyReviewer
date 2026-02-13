import SwiftUI
import PhotosUI
import Shared

struct ContentView: View {
    // MARK: - State
    @State private var selectedItem: PhotosPickerItem?
    @State private var selectedImage: UIImage?
    @State private var isAnalyzing = false
    @State private var extractedKeywords: [String] = []
    @State private var generatedReview: String = ""
    @State private var showAlert = false
    @State private var alertMessage = ""

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // MARK: - Image Section
                    imageSection

                    // MARK: - Keywords Section
                    if !extractedKeywords.isEmpty {
                        keywordsSection
                    }

                    // MARK: - Review Section
                    if selectedImage != nil {
                        reviewSection
                    }

                    // MARK: - Action Buttons
                    if !generatedReview.isEmpty {
                        actionButtons
                    }

                    Spacer(minLength: 40)
                }
                .padding()
            }
            .navigationTitle("OhMyReviewer")
            .alert("알림", isPresented: $showAlert) {
                Button("확인", role: .cancel) {}
            } message: {
                Text(alertMessage)
            }
        }
    }

    // MARK: - Image Section
    private var imageSection: some View {
        VStack(spacing: 16) {
            if let image = selectedImage {
                // 선택된 이미지 표시
                Image(uiImage: image)
                    .resizable()
                    .scaledToFit()
                    .frame(maxHeight: 300)
                    .cornerRadius(12)
                    .shadow(radius: 4)

                // 이미지 변경 버튼
                HStack(spacing: 12) {
                    PhotosPicker(selection: $selectedItem, matching: .images) {
                        Label("다른 사진 선택", systemImage: "photo.on.rectangle")
                            .font(.subheadline)
                    }
                    .buttonStyle(.bordered)

                    Button(role: .destructive) {
                        clearAll()
                    } label: {
                        Label("삭제", systemImage: "trash")
                            .font(.subheadline)
                    }
                    .buttonStyle(.bordered)
                }
            } else {
                // 이미지 선택 영역
                PhotosPicker(selection: $selectedItem, matching: .images) {
                    VStack(spacing: 16) {
                        Image(systemName: "photo.badge.plus")
                            .font(.system(size: 60))
                            .foregroundColor(.accentColor)

                        Text("사진을 선택하세요")
                            .font(.headline)
                            .foregroundColor(.primary)

                        Text("이미지를 분석하여 리뷰를 자동으로 생성합니다")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 200)
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                }
            }

            // 로딩 인디케이터
            if isAnalyzing {
                HStack(spacing: 8) {
                    ProgressView()
                    Text("이미지 분석 중...")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .padding(.top, 8)
            }
        }
        .onChange(of: selectedItem) { oldValue, newValue in
            Task {
                await loadAndAnalyzeImage(from: newValue)
            }
        }
    }

    // MARK: - Keywords Section
    private var keywordsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "tag.fill")
                    .foregroundColor(.accentColor)
                Text("추출된 키워드")
                    .font(.headline)
            }

            FlowLayout(spacing: 8) {
                ForEach(extractedKeywords, id: \.self) { keyword in
                    Text(keyword)
                        .font(.caption)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color.accentColor.opacity(0.1))
                        .foregroundColor(.accentColor)
                        .cornerRadius(16)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    // MARK: - Review Section
    private var reviewSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "pencil.and.outline")
                    .foregroundColor(.accentColor)
                Text("리뷰 작성")
                    .font(.headline)

                Spacer()

                if ReviewService.shared.isFoundationModelsSupported {
                    Label("AI", systemImage: "sparkles")
                        .font(.caption)
                        .foregroundColor(.purple)
                }
            }

            TextEditor(text: $generatedReview)
                .frame(minHeight: 150)
                .padding(8)
                .background(Color(.systemBackground))
                .cornerRadius(8)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color(.systemGray4), lineWidth: 1)
                )

            // 글자 수 표시
            HStack {
                Spacer()
                Text("\(generatedReview.count)자")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            // 재생성 버튼
            Button {
                Task {
                    await regenerateReview()
                }
            } label: {
                Label("리뷰 다시 생성", systemImage: "arrow.clockwise")
                    .font(.subheadline)
            }
            .buttonStyle(.bordered)
            .disabled(isAnalyzing || extractedKeywords.isEmpty)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    // MARK: - Action Buttons
    private var actionButtons: some View {
        VStack(spacing: 12) {
            // 복사 버튼
            Button {
                copyToClipboard()
            } label: {
                Label("리뷰 복사하기", systemImage: "doc.on.doc")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)

            // 공유 버튼
            ShareLink(item: generatedReview) {
                Label("공유하기", systemImage: "square.and.arrow.up")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .controlSize(.large)
        }
    }

    // MARK: - Methods

    /// 이미지 로드 및 분석
    private func loadAndAnalyzeImage(from item: PhotosPickerItem?) async {
        guard let item = item else { return }

        isAnalyzing = true
        extractedKeywords = []
        generatedReview = ""

        do {
            // 이미지 로드
            if let data = try await item.loadTransferable(type: Data.self),
               let image = UIImage(data: data) {

                await MainActor.run {
                    selectedImage = image
                }

                // 이미지 분석
                let labels = await ImageAnalyzer.shared.analyzeImage(image)
                let texts = await ImageAnalyzer.shared.recognizeText(image)
                let keywords: [String]
                #if canImport(FoundationModels)
                if #available(iOS 26, *) {
                    keywords = await ImageAnalyzer.shared.enrichKeywords(labels: labels, texts: texts)
                } else {
                    keywords = labels + texts
                }
                #else
                keywords = labels + texts
                #endif

                await MainActor.run {
                    extractedKeywords = keywords
                }

                // 리뷰 생성
                let review = await ReviewService.shared.generateReview(keywords: keywords)

                await MainActor.run {
                    generatedReview = review
                    isAnalyzing = false
                }
            }
        } catch {
            await MainActor.run {
                isAnalyzing = false
                alertMessage = "이미지를 불러오는데 실패했습니다."
                showAlert = true
            }
        }
    }

    /// 리뷰 재생성
    private func regenerateReview() async {
        isAnalyzing = true

        let review = await ReviewService.shared.generateReview(keywords: extractedKeywords)

        await MainActor.run {
            generatedReview = review
            isAnalyzing = false
        }
    }

    /// 클립보드에 복사
    private func copyToClipboard() {
        UIPasteboard.general.string = generatedReview
        alertMessage = "리뷰가 클립보드에 복사되었습니다."
        showAlert = true
    }

    /// 모든 상태 초기화
    private func clearAll() {
        selectedItem = nil
        selectedImage = nil
        extractedKeywords = []
        generatedReview = ""
    }
}

// MARK: - Flow Layout for Keywords

struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let sizes = subviews.map { $0.sizeThatFits(.unspecified) }
        return layoutSizes(sizes: sizes, proposal: proposal).size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let sizes = subviews.map { $0.sizeThatFits(.unspecified) }
        let offsets = layoutSizes(sizes: sizes, proposal: proposal).offsets

        for (subview, offset) in zip(subviews, offsets) {
            subview.place(at: CGPoint(x: bounds.minX + offset.x, y: bounds.minY + offset.y), proposal: .unspecified)
        }
    }

    private func layoutSizes(sizes: [CGSize], proposal: ProposedViewSize) -> (offsets: [CGPoint], size: CGSize) {
        let maxWidth = proposal.width ?? .infinity
        var offsets: [CGPoint] = []
        var currentX: CGFloat = 0
        var currentY: CGFloat = 0
        var lineHeight: CGFloat = 0
        var totalHeight: CGFloat = 0

        for size in sizes {
            if currentX + size.width > maxWidth && currentX > 0 {
                currentX = 0
                currentY += lineHeight + spacing
                lineHeight = 0
            }

            offsets.append(CGPoint(x: currentX, y: currentY))
            currentX += size.width + spacing
            lineHeight = max(lineHeight, size.height)
            totalHeight = currentY + lineHeight
        }

        return (offsets, CGSize(width: maxWidth, height: totalHeight))
    }
}

// MARK: - Preview

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
