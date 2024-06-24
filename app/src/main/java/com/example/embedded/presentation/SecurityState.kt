package com.example.embedded.presentation

import android.graphics.Bitmap

sealed interface SecurityState {
    data object SecurityOn : SecurityState
    data class Thief(val bitmap: Bitmap, val isSirenOn: Boolean) : SecurityState
    data object SecurityOff : SecurityState
    data object ERROR : SecurityState
}
