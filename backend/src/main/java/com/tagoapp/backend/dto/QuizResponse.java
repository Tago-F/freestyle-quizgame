package com.tagoapp.backend.dto;

/**
 * AI からのクイズ出題レスポンス用 DTO.
 * @param question AI が考えてくれたクイズ。
 */
public record QuizResponse(String question) {
}