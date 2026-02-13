package io.yogiyo.ohmyreviewer.ui.image

import io.yogiyo.ohmyreviewer.ui.base.MviViewModel

class ImageViewModel : MviViewModel<ImageContract.State, ImageContract.Event, ImageContract.Effect>(
    initialState = ImageContract.State(),
) {

    override fun handleEvent(event: ImageContract.Event) {
        // TODO: 이벤트 처리 구현
    }
}
