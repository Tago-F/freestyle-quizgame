package com.tagoapp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.tagoapp.backend.dto.*; // DTOパッケージをインポート
import com.tagoapp.backend.service.QuizService;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "http://127.0.0.1:4200 , http://localhost:4200 , http://127.0.0.1:4200/") // Angular開発サーバーからのアクセスを許可
                                                                                                 // [TODO] 本番環境は URL
                                                                                                 // が決定次第追加
public class QuizController {

    private static final Logger logger = LoggerFactory.getLogger(QuizController.class);

    private final QuizService quizService;

    // Constructor InjectionでQuizServiceを注入
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/question")
    public ResponseEntity<?> getQuestion(@RequestBody QuizRequest request) {
        try {
            logger.info("Received request for quiz question with genre: {}", request.genre());
            QuizResponse response = quizService.getQuizQuestion(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting quiz question", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("クイズ問題の取得に失敗しました: " + e.getMessage()));
        }
    }

    @PostMapping("/hint")
    public ResponseEntity<?> getHint(@RequestBody HintRequest request) {
        try {
            logger.info("Received request for hint for question: {}", request.question());
            HintResponse response = quizService.getQuizHint(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting hint", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("ヒントの取得に失敗しました: " + e.getMessage()));
        }
    }

    @PostMapping("/answer")
    public ResponseEntity<?> submitAnswer(@RequestBody AnswerRequest request) {
        try {
            logger.info("Received answer submission for question: {}", request.question());
            AnswerResponse response = quizService.checkQuizAnswer(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("回答の判定に失敗しました: " + e.getMessage()));
        }
    }
}
