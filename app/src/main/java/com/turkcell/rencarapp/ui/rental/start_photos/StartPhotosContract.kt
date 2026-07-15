package com.turkcell.rencarapp.ui.rental.start_photos

import android.net.Uri
import com.turkcell.rencarapp.ui.rental.delivery_photos.PhotoDirection

data class StartPhotosUiState(
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val photos: Map<PhotoDirection, Uri?> = PhotoDirection.entries.associateWith { null },
    val isLoading: Boolean = false,
    val isSubmittingPhotos: Boolean = false,
    val error: String? = null,
) {
    val capturedCount: Int = photos.values.count { it != null }
    val isComplete: Boolean = capturedCount == PhotoDirection.entries.size

    val vehicleDisplayLabel: String
        get() {
            val namePart = listOf(brand, model).filter { it.isNotBlank() }.joinToString(" ")
            return when {
                namePart.isNotBlank() && plate.isNotBlank() -> "$namePart · $plate"
                namePart.isNotBlank() -> namePart
                plate.isNotBlank() -> plate
                else -> "—"
            }
        }
}

sealed interface StartPhotosIntent {
    data class PhotoBoxToggled(val direction: PhotoDirection) : StartPhotosIntent
    data object CompletePhotosClicked : StartPhotosIntent
    data object BackClicked : StartPhotosIntent
}

sealed interface StartPhotosEffect {
    data object NavigateBack : StartPhotosEffect
    data object RideStarted : StartPhotosEffect
    data class ShowError(val message: String) : StartPhotosEffect
}
