package com.tagoapp.backend.dto;

/**
 * Gemini からのエラーレスポンス用 DTO.
 */
public record ErrorResponse(String error) {
}