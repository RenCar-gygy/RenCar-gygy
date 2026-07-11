package com.turkcell.rencarapp.ui.rental.delivery_photos

import android.net.Uri

data class DeliveryPhotosUiState(
    val vehicleName: String = "Renault Clio",
    val plate: String = "34 RNC 022",
    val photos: Map<PhotoDirection, Uri?> = PhotoDirection.entries.associateWith { null },
    val isLoading: Boolean = false,
    val isStartingRental: Boolean = false
) {
    val capturedCount: Int = photos.values.count { it != null }
    val isComplete: Boolean = capturedCount == PhotoDirection.entries.size
    val remainingCount: Int = PhotoDirection.entries.size - capturedCount
}

enum class PhotoDirection(val label: String) {
    FRONT("Ön"),
    BACK("Arka"),
    LEFT("Sol"),
    RIGHT("Sağ")
}

sealed interface DeliveryPhotosIntent {
    data class PhotoCaptured(val direction: PhotoDirection, val uri: Uri) : DeliveryPhotosIntent
    data object StartRentalClicked : DeliveryPhotosIntent
    data object BackClicked : DeliveryPhotosIntent
}

sealed interface DeliveryPhotosEffect {
    data object NavigateBack : DeliveryPhotosEffect
    data class NavigateToSummary(val rentalId: String) : DeliveryPhotosEffect
    data class ShowError(val message: String) : DeliveryPhotosEffect
}
