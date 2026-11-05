package com.finflow.application.exception;

public class DuplicateTransactionException extends BusinessException {
    public DuplicateTransactionException(String idempotencyKey) {
        super("Transação duplicada detectada para a chave de idempotência: " + idempotencyKey);
    }
}
