package com.beta.expedition.exception;

/** Запись с указанным ID не найдена. */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, long id) {
        super(entity + " с ID " + id + " не найден(а)");
    }
}
