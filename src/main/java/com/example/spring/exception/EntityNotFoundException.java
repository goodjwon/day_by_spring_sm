package com.example.spring.exception;

public class EntityNotFoundException extends BusinessException  {

    public EntityNotFoundException(String entityName, Long id) {
        super("ENTITY_NOT_FOUND", entityName + "을(를) 찾을 수 없습니다. ID: " + id);
    }

    public EntityNotFoundException(String message) {
        super("ENTITY_NOT_FOUND", message);
    }

}
