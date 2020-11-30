package com.twoilya.lonelyboardgamer

abstract class WithCodeException(message: String) : Exception(message) {
    abstract val code: Int
}

class AuthorizationException(message: String) : WithCodeException(message) {
    override val code: Int = 1
}

class InfoMissingException(message: String) : WithCodeException(message) {
    override val code: Int = 2
}

class ElementWasNotFoundException(message: String) : WithCodeException(message) {
    override val code: Int = 3
}

class WrongDataFormatException(message: String) : WithCodeException(message) {
    override val code: Int = 4
}

class BadDataException(message: String) : WithCodeException(message) {
    override val code: Int = 5
}