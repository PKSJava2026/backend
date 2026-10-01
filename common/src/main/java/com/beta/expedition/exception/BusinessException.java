package com.beta.expedition.exception;

/** Нарушено бизнес-правило (некорректные данные, запрещённый переход статуса и т.п.). */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
