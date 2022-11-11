package com.softserve.itacademy.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;


@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NullEntityReferenceException.class)
    public ResponseEntity<ExceptionErrorResponse> nullEntityExceptionHandler(NullEntityReferenceException exception) {
        log.error(exception.getMessage());
        ExceptionErrorResponse response = new ExceptionErrorResponse(
                exception.getMessage(), System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionErrorResponse> entityNotFoundExceptionHandler(EntityNotFoundException exception) {
        log.error(exception.getMessage());
        ExceptionErrorResponse response = new ExceptionErrorResponse(
                exception.getMessage(), System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityNotCreatedException.class)
    public ResponseEntity<ExceptionErrorResponse> userNotCreatedExceptionHandler(EntityNotCreatedException exception) {
        log.error(exception.getMessage());
        ExceptionErrorResponse response = new ExceptionErrorResponse(
                exception.getMessage(), System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionErrorResponse> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {

        log.error(ex.getMessage());
        ExceptionErrorResponse response = new ExceptionErrorResponse(
                ex.getMessage(), System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage());
        ExceptionErrorResponse response = new ExceptionErrorResponse(
                ex.getMessage(), System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionErrorResponse> handleException(Exception ex) {
        log.error(ex.getMessage());
        ExceptionErrorResponse response = new ExceptionErrorResponse(
                ex.getMessage(), System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
