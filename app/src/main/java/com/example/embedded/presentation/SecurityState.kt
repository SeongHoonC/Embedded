package com.example.embedded.presentation

import android.graphics.Bitmap

/*
  보안 상태를 나타내는 Sealed Interface
 */

sealed interface SecurityState {
    data object SecurityOn : SecurityState
    data class Thief(val bitmap: Bitmap, val isSirenOn: Boolean) : SecurityState
    data object SecurityOff : SecurityState
    data object ERROR : SecurityState
}
