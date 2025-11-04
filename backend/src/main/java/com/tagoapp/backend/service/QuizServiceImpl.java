package com.tagoapp.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
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

    /** slf4j ロガー */
    private static final Logger logger = LoggerFactory.getLogger(QuizServiceImpl.class);

    /** Gemini とチャット（リクエスト・レスポンス）を行う為のクライアント */
    private final ChatClient chatClient;

    /**
     * コンストラクタ。
     * chatClientBuilder を使用し、chatClient のビルド（初期化）を行う。
     * 
     * @param chatClientBuilder
     */
    public QuizServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public QuizResponse getQuizQuestion(QuizRequest request) {
        // 選択したジャンルの情報を [INFO] に追加
        logger.info("Generating quiz question for genre: {}", request.genre());
        try {
            // プロンプト用の文字列
            // ステークホルダー {genre} は PromptTemplate.create() メソッドで Map を渡すことで組み立て。
            // [memo] 制約をつけることで客観的な質問を防止したり、詳細なルールを認識してくれる。
            String questionPrompt = """
                    あなたはクイズマスターです。
                    {genre} に関するジャンルのクイズ問題を1問、回答を含めずに生成してください。
                    できるだけ多様な視点からの問題を生成してください。
                    【制約】
                    1.  **回答が必ず一つに定まる、客観的な事実**（例：特定の名称、数値、年号、記録、定義など）を問う問題にしてください。
                    2.  人気、知名度、評価、重要度、美しさなど、**回答者の主観や解釈によって答えが変わる可能性のある問題は、絶対に生成しないでください**。
                    3.  問題文は50文字以内にしてください。
                    4.  問題文のみを返してください。
                    """;
            // 文字列を元にプロンプトテンプレートを作成
            PromptTemplate promptTemplate = new PromptTemplate(questionPrompt);
            // ステークホルダーにジャンルの文字列を追加しプロンプトを作成
            Prompt prompt = promptTemplate.create(Map.of("genre", request.genre()));

            // ChatClient を使用して Gemini API にプロンプトをコール
            // content() メソッドをは応答を単純な String として受け取る
            String question = chatClient.prompt(prompt)
                    .call()
                    .content();

            // 成功時ログ
            logger.info("Successfully generated question.");
            logger.info("Generated question: {}", question);

            // AI からのレスポンスが空の場合
            if (question == null || question.isBlank()) {
                // [ERROR] ログを残して例外を throw
                logger.error("Generated question is null or blank.");
                throw new RuntimeException("生成された問題文が空です。");
            }
            // 問題文を String 型で返す
            return new QuizResponse(question);
        } catch (Exception e) {
            logger.error("Error generating quiz question: {}", e.getMessage(), e);
            throw new RuntimeException("クイズ問題の生成中にエラーが発生しました。", e);
        }
    }

    @Override
    public HintResponse getQuizHint(HintRequest request) {
        // 問題文の情報を [INFO] に追加
        logger.info("Generating hint for question: {}", request.question());
        try {
            // ヒント用プロンプト
            String hintPrompt = """
                    あなたはヒント提供者です。
                    以下のクイズ問題に対するヒントを1文で生成してください。
                    ただし、答えが直接わからないようにしてください。
                    ヒント文のみを返してください。

                    問題文:
                    {question}
                    """;
            // プロンプトテンプレ作成
            PromptTemplate promptTemplate = new PromptTemplate(hintPrompt);
            // 問題文を元にプロンプト組み立て
            Prompt prompt = promptTemplate.create(Map.of("question", request.question()));

            // プロンプトを Gemini API に投げる
            String hint = chatClient.prompt(prompt)
                    .call()
                    .content();

            // ログ出力
            logger.info("Successfully generated hint.");
            logger.info("Generated hint: {}", hint);

            // 空チェック
            if (hint == null || hint.isBlank()) {
                logger.error("Generated hint is null or blank.");
                throw new RuntimeException("生成されたヒントが空です。");
            }
            // ヒントを String 型で返す
            return new HintResponse(hint);
        } catch (Exception e) {
            logger.error("Error generating hint: {}", e.getMessage(), e);
            throw new RuntimeException("ヒントの生成中にエラーが発生しました。", e);
        }
    }

    @Override
    public AnswerResponse checkQuizAnswer(AnswerRequest request) {
        // 問題文をログ出力
        logger.info("Checking answer for question: {}", request.question());
        // 回答文をログ出力
        logger.info("Checking user answer: {}", request.answer());
        try {
            // BeanOutputConverter を使用して AI からの最終的な出力の型を Spring AI に宣言
            // 最終的に AnswerResponse（isCorrect と explanation）にしてもらう。
            var outputConverter = new BeanOutputConverter<>(AnswerResponse.class);

            // プロンプトの文字列
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

            // プロンプトテンプレ作成
            PromptTemplate promptTemplate = new PromptTemplate(answerPrompt);
            // プロンプト作成
            Prompt prompt = promptTemplate.create(Map.of(
                    "question", request.question(),
                    "answer", request.answer(),
                    // format は BeanOutputConverter で宣言した型のフィールド変数を JSON のキーとしたもの
                    "format", outputConverter.getFormat()));

            // entity(outputConverter) で自動的に AnswerResponse に変換するように指示
            AnswerResponse answerResponse = chatClient.prompt(prompt)
                    .call()
                    .entity(outputConverter);

            // ログ書き書き。
            logger.info("Parsed AnswerResponse: {}", answerResponse);
            logger.info("Successfully checked answer. Correct: {}", answerResponse.isCorrect());

            // 説明文が無い場合
            if (answerResponse.explanation() == null || answerResponse.explanation().isBlank()) {
                // [WARN] 出力
                logger.warn("Generated explanation is null or blank, providing default message.");
                // 正誤判定のみでも返却できるようにする
                return new AnswerResponse(answerResponse.isCorrect(), "(解説がありませんでした)");
            }
            // AnswerResponse を返却
            return answerResponse;

        } catch (Exception e) {
            logger.error("Error checking quiz answer or parsing response: {}", e.getMessage(), e);
            throw new RuntimeException("回答の判定またはレスポンスの解析中にエラーが発生しました。", e);
        }
    }
}
