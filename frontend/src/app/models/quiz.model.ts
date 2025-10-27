// APIリクエスト/レスポンスの型定義
// --- リクエスト ---
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


// --- レスポンス ---
// クイズ問題取得APIのレスポンス
export interface QuizResponse {
    question: string;
}

// ヒント取得APIのレスポンス
export interface HintResponse {
    hint: string;
}

// 回答判定APIのレスポンス
export interface AnswerResponse {
    isCorrect: boolean; // 正解かどうか (true/false)
    explanation: string; // 解説文
}

// エラーレスポンス (必要に応じて)
export interface ErrorResponse {
    error: string;
}

// [memo] TypeScript の Interface は Java における POJO (DTO) の様にも使用できる。
// [memo] DTO のデータ構造を定義する際に便利。
