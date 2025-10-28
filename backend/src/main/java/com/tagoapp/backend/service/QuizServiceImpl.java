package com.tagoapp.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import com.tagoapp.backend.dto.AnswerRequest;
import com.tagoapp.backend.dto.AnswerResponse;
import com.tagoapp.backend.dto.HintRequest;
import com.tagoapp.backend.dto.HintResponse;
import com.tagoapp.backend.dto.QuizRequest;
import com.tagoapp.backend.dto.QuizResponse;

import java.util.Map;

@Service
public class QuizServiceImpl implements QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizServiceImpl.class);

    private final ChatClient chatClient;

    // Constructor Injection
    public QuizServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 指定されたジャンルのクイズ問題を取得します。
     *
     * @param request ジャンル情報
     * @return クイズ問題レスポンス
     * @throws RuntimeException Gemini API呼び出し等でエラーが発生した場合
     */
    @Override
    public QuizResponse getQuizQuestion(QuizRequest request) {
        logger.info("Generating quiz question for genre: {}", request.genre());
        try {
            String questionPrompt = """
                    あなたはクイズマスターです。
                    {genre} に関するクイズ問題を1問、回答を含めずに生成してください。
                    問題文のみを返してください。
                    """;
            PromptTemplate promptTemplate = new PromptTemplate(questionPrompt);
            Prompt prompt = promptTemplate.create(Map.of("genre", request.genre()));

            logger.debug("Calling Gemini API for question generation...");
            String question = chatClient.prompt(prompt)
                    .call()
                    .content();
            logger.info("Successfully generated question.");
            logger.debug("Generated question: {}", question); // デバッグ用に問題文をログ出力

            if (question == null || question.isBlank()) {
                logger.error("Generated question is null or blank.");
                throw new RuntimeException("生成された問題文が空です。");
            }

            return new QuizResponse(question);
        } catch (Exception e) {
            logger.error("Error generating quiz question: {}", e.getMessage(), e);
            // Controller 層でハンドリングするために RuntimeException をスロー
            throw new RuntimeException("クイズ問題の生成中にエラーが発生しました。", e);
        }
    }

    /**
     * 指定されたクイズ問題に対するヒントを取得します。
     *
     * @param request クイズ問題文
     * @return ヒントレスポンス
     * @throws RuntimeException Gemini API呼び出し等でエラーが発生した場合
     */
    @Override
    public HintResponse getQuizHint(HintRequest request) {
        logger.info("Generating hint for question: {}", request.question());
        try {
            String hintPrompt = """
                    あなたはヒント提供者です。
                    以下のクイズ問題に対するヒントを1文で生成してください。
                    ただし、答えが直接わからないようにしてください。
                    ヒント文のみを返してください。

                    問題文:
                    {question}
                    """;
            PromptTemplate promptTemplate = new PromptTemplate(hintPrompt);
            Prompt prompt = promptTemplate.create(Map.of("question", request.question()));

            logger.debug("Calling Gemini API for hint generation...");
            String hint = chatClient.prompt(prompt)
                    .call()
                    .content();
            logger.info("Successfully generated hint.");
            logger.debug("Generated hint: {}", hint); // デバッグ用にヒントをログ出力

            if (hint == null || hint.isBlank()) {
                logger.error("Generated hint is null or blank.");
                throw new RuntimeException("生成されたヒントが空です。");
            }
            return new HintResponse(hint);
        } catch (Exception e) {
            logger.error("Error generating hint: {}", e.getMessage(), e);
            throw new RuntimeException("ヒントの生成中にエラーが発生しました。", e);
        }
    }

    /**
     * ユーザーの回答を判定し、解説を取得します。
     * BeanOutputConverter を使用して JSON 形式のレスポンスを取得します。
     *
     * @param request クイズ問題文とユーザーの回答
     * @return 回答結果レスポンス
     * @throws RuntimeException Gemini API呼び出し等でエラーが発生した場合
     */
    @Override
    public AnswerResponse checkQuizAnswer(AnswerRequest request) {
        logger.info("Checking answer for question: {}", request.question());
        try {
            var outputConverter = new BeanOutputConverter<>(AnswerResponse.class);

            String answerPrompt = """
                    あなたは採点者であり解説者です。
                    以下のクイズ問題とユーザーの回答を比較し、正誤判定 (isCorrect: boolean) と、正解・不正解に関わらずその問題に関する簡潔な解説 (explanation: String) をJSON形式で出力してください。
                    解説文は自然な日本語でお願いします。

                    問題文:
                    {question}

                    ユーザーの回答:
                    {answer}

                    出力形式:
                    {format}
                    """;

            PromptTemplate promptTemplate = new PromptTemplate(answerPrompt);
            Prompt prompt = promptTemplate.create(Map.of(
                    "question", request.question(),
                    "answer", request.answer(),
                    "format", outputConverter.getFormat()));

            logger.debug("Calling Gemini API for answer checking...");
            // .entity() を使用して BeanOutputConverter で直接 AnswerResponse に変換
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

            // レスポンスの内容をログに出力（デバッグ用）
            String rawResponse = response.getResult().getOutput().getContent();
            logger.debug("Raw response from Gemini API: {}", rawResponse);

            AnswerResponse answerResponse = outputConverter.convert(rawResponse);

            logger.info("Successfully checked answer. Correct: {}", answerResponse.isCorrect());
            logger.debug("Generated explanation: {}", answerResponse.explanation()); // デバッグ用に解説をログ出力

            if (answerResponse.explanation() == null || answerResponse.explanation().isBlank()) {
                logger.warn("Generated explanation is null or blank, providing default message.");
                // 解説が空の場合、デフォルトのメッセージを設定するなどのフォールバック処理
                return new AnswerResponse(answerResponse.isCorrect(), "(解説がありませんでした)");
            }

            return answerResponse;
        } catch (Exception e) {
            logger.error("Error checking quiz answer: {}", e.getMessage(), e);
            // APIからのレスポンス形式が期待通りでない場合などのパースエラーもここでキャッチされる可能性
            throw new RuntimeException("回答の判定中にエラーが発生しました。", e);
        }
    }
}
