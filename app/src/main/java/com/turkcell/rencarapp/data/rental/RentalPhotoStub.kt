package com.turkcell.rencarapp.data.rental

import android.util.Base64

/**
 * API `start` öncesi zorunlu foto upload'u için minimal JPEG (1x1 px).
 * Gerçek kamera entegrasyonu gelene kadar test/yolculuk başlatma amaçlıdır.
 */
object RentalPhotoStub {
    val JPEG_BYTES: ByteArray by lazy {
        Base64.decode(MINIMAL_JPEG_BASE64, Base64.DEFAULT)
    }

    val REQUIRED_SIDES: List<String> = listOf("FRONT", "BACK", "LEFT", "RIGHT")

    private const val MINIMAL_JPEG_BASE64 =
        "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRof" +
            "Hh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwh" +
            "MjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAAR" +
            "CAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAA" +
            "AAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIR" +
            "AxEAPwCdABmX/9k="
}
