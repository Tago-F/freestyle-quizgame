package com.tagoapp.backend.dto;

/**
 * ヒントを要求するリクエスト用の DTO. 
 * 質問文をプロンプトに組み込み、それを元にヒントを要求する。
 * 
 * @param question 質問文。
 */
public record HintRequest(String question) {
}