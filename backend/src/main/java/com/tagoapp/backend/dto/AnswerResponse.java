package com.tagoapp.backend.dto;

/**
 * Gemini API からの正誤判定レスポンス用の DTO.
 * @param isCorrect 正解か不正解か。
 * @param explanation 問題の解説。
 */
public record AnswerResponse(boolean isCorrect, String explanation) {

}
