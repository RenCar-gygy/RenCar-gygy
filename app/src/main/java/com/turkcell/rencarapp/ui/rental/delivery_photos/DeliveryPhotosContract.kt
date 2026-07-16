package com.turkcell.rencarapp.ui.rental.delivery_photos

import android.net.Uri

/** Yerel işaretleme modu; gerçek kamera/upload entegrasyonu sonraya bırakıldı. */
val MARKED_PHOTO_URI: Uri = Uri.parse("rencar://local/marked")

data class DeliveryPhotosUiState(
    val brand: String = "",
    val model: String = "",
    val plate: String = "",
    val photos: Map<PhotoDirection, Uri?> = PhotoDirection.entries.associateWith { null },
    val isLoading: Boolean = false,
    val isSubmittingPhotos: Boolean = false
) {
    val capturedCount: Int = photos.values.count { it != null }
    val isComplete: Boolean = capturedCount == PhotoDirection.entries.size
    val remainingCount: Int = PhotoDirection.entries.size - capturedCount

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

enum class PhotoDirection(val label: String) {
    FRONT("Ön"),
    BACK("Arka"),
    LEFT("Sol"),
    RIGHT("Sağ")
}

sealed interface DeliveryPhotosIntent {
    data class PhotoCaptureRequested(val direction: PhotoDirection) : DeliveryPhotosIntent
    data class PhotoCaptured(val direction: PhotoDirection, val previewUri: Uri) : DeliveryPhotosIntent
    data object CompletePhotosClicked : DeliveryPhotosIntent
    data object BackClicked : DeliveryPhotosIntent
}

sealed interface DeliveryPhotosEffect {
    data object NavigateBack : DeliveryPhotosEffect
    data class NavigateToSummary(val rentalId: String) : DeliveryPhotosEffect
    data class LaunchCamera(val direction: PhotoDirection) : DeliveryPhotosEffect
    data class ShowError(val message: String) : DeliveryPhotosEffect
}
