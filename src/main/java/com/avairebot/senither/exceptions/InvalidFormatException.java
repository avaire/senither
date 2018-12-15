package com.avairebot.senither.exceptions;

public class InvalidFormatException extends RuntimeException {

    public InvalidFormatException(String message, String time) {
        super(String.format(message, time));
    }
}
