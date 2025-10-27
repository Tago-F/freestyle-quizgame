// APIリクエスト/レスポンスの型定義
export interface QuizRequest {
    genre: string;
}

export interface HintRequest {
    question: string;
}

export interface AnswerRequest {
    question: string;
    answer: string;
}

// 回答結果のレスポンスを想定
// バックエンドの DTO に合わせて修正を行う
export interface AnswerResponse {
    result: string;
    explanation: string;
    isCorrect: boolean;
}
