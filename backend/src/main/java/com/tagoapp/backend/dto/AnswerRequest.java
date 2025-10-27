package com.tagoapp.backend.dto;

/**
 * Gemini API への正誤判定リクエスト用の DTO.
 * 問題文と回答を送信し、プロンプトに組み込むことで正誤判定を行う。
 * @param question 問題文。
 * @param answer ユーザーの回答。
 */
public record AnswerRequest(String question, String answer) {

}
