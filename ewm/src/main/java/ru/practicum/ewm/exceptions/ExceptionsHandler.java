package ru.practicum.ewm.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ExceptionsHandler {
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ApiError handleConflict(Exception exception) {
        exception.printStackTrace();
        return new ApiError(
                exception.getClass().toString(),
                exception.getMessage(),
                "Объект с указанными параметрами уже существует",
                HttpStatus.CONFLICT.toString(),
                LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class, NoSuchElementException.class})
    public ApiError handleNoSuchElement(NoSuchElementException exception) {
        exception.printStackTrace();
        return new ApiError(
                exception.getClass().toString(),
                exception.getMessage(),
                "Объект не найден в базе данных",
                HttpStatus.NOT_FOUND.toString(),
                LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class, NumberFormatException.class,
            InvalidDataAccessApiUsageException.class})
    public ApiError handleValidation(Exception exception) {
        exception.printStackTrace();
        return new ApiError(
                exception.getClass().toString(),
                exception.getMessage(),
                "Параметры объекта не соответствуют условиям валидации",
                HttpStatus.BAD_REQUEST.toString(),
                LocalDateTime.now());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    public ApiError handleException(Throwable throwable) {
        throwable.printStackTrace();
        return new ApiError(
                throwable.getClass().toString(),
                throwable.getMessage(),
                "Внутренняя ошибка сервера",
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                LocalDateTime.now());
    }
}
