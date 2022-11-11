package com.softserve.itacademy.exception;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

public class EntityNotCreatedException extends RuntimeException {
    public EntityNotCreatedException(String message) {
        super(message);
    }

    public static String errorMessage(BindingResult bindingResult) {
        StringBuilder errorMessage = new StringBuilder();
        List<FieldError> errorList = bindingResult.getFieldErrors();
        for (FieldError error : errorList) {
            errorMessage.append(error.getField())
                    .append(" - ")
                    .append(error.getDefaultMessage())
                    .append(";");
        }
        return errorMessage.toString();
    }
}
