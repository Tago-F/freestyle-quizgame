package com.tagoapp.backend.dto;

/**
 * AI にクイズの出題を依頼するためのリクエスト DTO.
 * ジャンルを指定してプロンプトに組み込む。
 * @param genre 指定ジャンル。
 */
public record QuizRequest(String genre) {
}