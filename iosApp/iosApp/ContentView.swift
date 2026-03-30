import SwiftUI
import PhotosUI
import Shared

// MARK: - Question Data

private struct ReviewQuestion {
    let text: String
    let icon: String
    let category: String
}

private let kReviewQuestions: [ReviewQuestion] = [
    .init(text: "맛있었나요?",            icon: "face.smiling.fill",      category: "맛"),
    .init(text: "배불렀나요?",            icon: "fork.knife.circle.fill", category: "양"),
    .init(text: "포장 상태는\n좋았나요?",  icon: "shippingbox.fill",        category: "포장상태"),
    .init(text: "배달은\n제때 됐나요?",   icon: "bicycle",                category: "배달상태"),
    .init(text: "추천하나요?",            icon: "hand.thumbsup.fill",     category: "총평"),
]

struct ContentView: View {

    // MARK: - Step & Answer State

    @State private var currentStep: Int = 0
    @State private var answers: [Bool?] = Array(repeating: nil, count: kReviewQuestions.count)

    // MARK: - Image & Review State

    @State private var selectedItem: PhotosPickerItem?
    @State private var selectedImage: UIImage?
    @State private var isAnalyzing = false
    @State private var extractedKeywords: [String] = []
    @State private var generatedReview: String = ""
    @State private var showAlert = false
    @State private var alertMessage = ""

    // MARK: - Body

    var body: some View {
        NavigationView {
            Group {
                if currentStep < kReviewQuestions.count {
                    questionStepView
                } else {
                    imageAndReviewView
                }
            }
            .navigationTitle("OhMyReviewer")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                if currentStep > 0 {
                    ToolbarItem(placement: .navigationBarLeading) {
                        Button { goBack() } label: {
                            Image(systemName: "chevron.left")
                                .fontWeight(.semibold)
                        }
                    }
                }
                if currentStep >= kReviewQuestions.count {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button("다시 시작") { restart() }
                            .font(.subheadline)
                    }
                }
            }
            .alert("알림", isPresented: $showAlert) {
                Button("확인", role: .cancel) {}
            } message: {
                Text(alertMessage)
            }
        }
    }

    // MARK: - Question Step View

    private var questionStepView: some View {
        VStack(spacing: 0) {
            progressSection
                .padding(.horizontal, 24)
                .padding(.top, 24)

            Spacer()

            questionCard
                .id(currentStep)
                .transition(.asymmetric(
                    insertion: .move(edge: .trailing).combined(with: .opacity),
                    removal: .move(edge: .leading).combined(with: .opacity)
                ))

            Spacer()

            answerButtons
                .padding(.horizontal, 32)
                .padding(.bottom, 52)
        }
    }

    private var progressSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("\(currentStep + 1) / \(kReviewQuestions.count)")
                .font(.subheadline)
                .foregroundColor(.secondary)

            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Capsule().fill(Color(.systemGray5))
                    Capsule()
                        .fill(Color.accentColor)
                        .frame(
                            width: geo.size.width * CGFloat(currentStep + 1) / CGFloat(kReviewQuestions.count)
                        )
                        .animation(.spring(response: 0.4, dampingFraction: 0.8), value: currentStep)
                }
            }
            .frame(height: 6)
        }
    }

    private var questionCard: some View {
        let question = kReviewQuestions[currentStep]
        return VStack(spacing: 28) {
            Image(systemName: question.icon)
                .font(.system(size: 72))
                .foregroundStyle(Color.accentColor)

            Text(question.text)
                .font(.system(size: 34, weight: .bold))
                .multilineTextAlignment(.center)
        }
        .padding(40)
    }

    private var answerButtons: some View {
        HStack(spacing: 16) {
            Button { advance(with: false) } label: {
                Text("아니오")
                    .font(.title3.weight(.semibold))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 20)
                    .background(Color(.systemGray5))
                    .foregroundColor(.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 18))
            }

            Button { advance(with: true) } label: {
                Text("예")
                    .font(.title3.weight(.semibold))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 20)
                    .background(Color.accentColor)
                    .foregroundColor(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 18))
            }
        }
    }

    // MARK: - Image & Review View

    private var imageAndReviewView: some View {
        ScrollView {
            VStack(spacing: 24) {
                answerSummarySection
                imageSection

                if !extractedKeywords.isEmpty {
                    keywordsSection
                }

                if selectedImage != nil {
                    reviewSection
                }

                if !generatedReview.isEmpty {
                    actionButtons
                }

                Spacer(minLength: 40)
            }
            .padding()
        }
    }

    private var answerSummarySection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("내 평가")
                .font(.caption)
                .foregroundColor(.secondary)

            FlowLayout(spacing: 8) {
                ForEach(kReviewQuestions.indices, id: \.self) { index in
                    let question = kReviewQuestions[index]
                    let answer = answers[index] ?? true
                    HStack(spacing: 4) {
                        Image(systemName: answer ? "checkmark.circle.fill" : "xmark.circle.fill")
                            .font(.caption2)
                        Text(question.category)
                            .font(.caption)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(answer ? Color.green.opacity(0.12) : Color(.systemRed).opacity(0.08))
                    .foregroundColor(answer ? .green : Color(.systemRed))
                    .clipShape(Capsule())
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    // MARK: - Image Section

    private var imageSection: some View {
        VStack(spacing: 16) {
            if let image = selectedImage {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFit()
                    .frame(maxHeight: 300)
                    .cornerRadius(12)
                    .shadow(radius: 4)

                HStack(spacing: 12) {
                    PhotosPicker(selection: $selectedItem, matching: .images) {
                        Label("다른 사진 선택", systemImage: "photo.on.rectangle")
                            .font(.subheadline)
                    }
                    .buttonStyle(.bordered)

                    Button(role: .destructive) {
                        clearImageState()
                    } label: {
                        Label("삭제", systemImage: "trash")
                            .font(.subheadline)
                    }
                    .buttonStyle(.bordered)
                }
            } else {
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

            HStack {
                Spacer()
                Text("\(generatedReview.count)자")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            Button {
                Task { await regenerateReview() }
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
            Button {
                copyToClipboard()
            } label: {
                Label("리뷰 복사하기", systemImage: "doc.on.doc")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)

            ShareLink(item: generatedReview) {
                Label("공유하기", systemImage: "square.and.arrow.up")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .controlSize(.large)
        }
    }

    // MARK: - Navigation

    private func advance(with answer: Bool) {
        answers[currentStep] = answer
        withAnimation(.easeInOut(duration: 0.3)) {
            currentStep += 1
        }
    }

    private func goBack() {
        withAnimation(.easeInOut(duration: 0.25)) {
            currentStep -= 1
        }
    }

    private func restart() {
        withAnimation(.easeInOut(duration: 0.3)) {
            currentStep = 0
            answers = Array(repeating: nil, count: kReviewQuestions.count)
            clearImageState()
        }
    }

    // MARK: - Computed

    private var userReviewAnswers: UserReviewAnswers {
        UserReviewAnswers(
            taste: answers[0] ?? true,
            quantity: answers[1] ?? true,
            packaging: answers[2] ?? true,
            delivery: answers[3] ?? true,
            recommend: answers[4] ?? true
        )
    }

    // MARK: - Methods

    private func loadAndAnalyzeImage(from item: PhotosPickerItem?) async {
        print("### [ContentView] loadAndAnalyzeImage 시작")
        guard let item = item else {
            print("### [ContentView] item이 nil - 종료")
            return
        }
        print("### [ContentView] item 있음 - 분석 시작")

        isAnalyzing = true
        extractedKeywords = []
        generatedReview = ""

        do {
            print("### [ContentView] 이미지 데이터 로드 시도...")
            if let data = try await item.loadTransferable(type: Data.self),
               let image = UIImage(data: data) {
                print("### [ContentView] 이미지 로드 성공! 크기: \(image.size)")

                await MainActor.run {
                    selectedImage = image
                }

                print("### [ContentView] ImageAnalyzer.analyzeImage 호출 전")
                let labels = await ImageAnalyzer.shared.analyzeImage(image)
                print("### [ContentView] analyzeImage 완료 - labels: \(labels)")
                let keywords: [String]
                #if canImport(FoundationModels)
                if #available(iOS 26, *) {
                    keywords = await ImageAnalyzer.shared.enrichKeywords(labels: labels)
                } else {
                    keywords = labels
                }
                #else
                keywords = labels
                #endif

                await MainActor.run {
                    extractedKeywords = keywords
                }

                let review = await ReviewService.shared.generateReview(
                    keywords: keywords,
                    userAnswers: userReviewAnswers
                )

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

    private func regenerateReview() async {
        isAnalyzing = true

        let review = await ReviewService.shared.generateReview(
            keywords: extractedKeywords,
            userAnswers: userReviewAnswers
        )

        await MainActor.run {
            generatedReview = review
            isAnalyzing = false
        }
    }

    private func copyToClipboard() {
        UIPasteboard.general.string = generatedReview
        alertMessage = "리뷰가 클립보드에 복사되었습니다."
        showAlert = true
    }

    private func clearImageState() {
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
