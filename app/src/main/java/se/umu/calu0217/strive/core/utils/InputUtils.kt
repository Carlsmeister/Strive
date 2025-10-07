package se.umu.calu0217.strive.core.utils

/**
 * Small text helpers for input normalization.
 */
fun String.digits(maxLen: Int): String = this.filter { it.isDigit() }.take(maxLen)
