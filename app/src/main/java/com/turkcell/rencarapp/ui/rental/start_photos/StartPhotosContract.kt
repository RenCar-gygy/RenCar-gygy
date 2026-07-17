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
    /** Günlük planda API yalnızca PREPARING'de foto kabul eder; ACTIVE'de yerel doğrulama. */
    val isDailyLocalPhotos: Boolean = false,
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
    data class PhotoCaptureRequested(val direction: PhotoDirection) : StartPhotosIntent
    data class PhotoCaptured(val direction: PhotoDirection, val bytes: ByteArray) : StartPhotosIntent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as PhotoCaptured
            if (direction != other.direction) return false
            if (!bytes.contentEquals(other.bytes)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = direction.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }
    data object CompletePhotosClicked : StartPhotosIntent
    data object BackClicked : StartPhotosIntent
}

sealed interface StartPhotosEffect {
    data object NavigateBack : StartPhotosEffect
    data class RideStarted(val rentalId: String) : StartPhotosEffect
    data class LaunchCamera(val direction: PhotoDirection) : StartPhotosEffect
    data class ShowError(val message: String) : StartPhotosEffect
}
