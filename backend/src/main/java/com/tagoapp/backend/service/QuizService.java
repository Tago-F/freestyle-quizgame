package com.tagoapp.backend.service;

import com.tagoapp.backend.dto.AnswerRequest;
import com.tagoapp.backend.dto.AnswerResponse;
import com.tagoapp.backend.dto.HintRequest;
import com.tagoapp.backend.dto.HintResponse;
import com.tagoapp.backend.dto.QuizRequest;
import com.tagoapp.backend.dto.QuizResponse;

/**
 * クイズに関連する Intarface 群。
 */
public interface QuizService {

    /**
     * 指定されたジャンルのクイズ問題を取得します。
     * 
     * @param request ジャンル情報
     * @return クイズ問題レスポンス
     * @throws Exception Gemini API呼び出し等でエラーが発生した場合
     */
    QuizResponse getQuizQuestion(QuizRequest request) throws Exception;

    /**
     * 指定されたクイズ問題に対するヒントを取得します。
     * 
     * @param request クイズ問題文
     * @return ヒントレスポンス
     * @throws Exception Gemini API呼び出し等でエラーが発生した場合
     */
    HintResponse getQuizHint(HintRequest request) throws Exception;

    /**
     * ユーザーの回答を判定し、解説を取得します。
     * 
     * @param request クイズ問題文とユーザーの回答
     * @return 回答結果レスポンス
     * @throws Exception Gemini API呼び出し等でエラーが発生した場合
     */
    AnswerResponse checkQuizAnswer(AnswerRequest request) throws Exception;

}
