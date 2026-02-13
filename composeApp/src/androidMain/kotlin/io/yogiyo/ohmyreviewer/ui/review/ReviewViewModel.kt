package io.yogiyo.ohmyreviewer.ui.review

import io.yogiyo.ohmyreviewer.ui.base.MviViewModel

class ReviewViewModel : MviViewModel<ReviewContract.State, ReviewContract.Event, ReviewContract.Effect>(
    initialState = ReviewContract.State(),
) {

    override fun handleEvent(event: ReviewContract.Event) {
        // TODO: 이벤트 처리 구현
    }
}
