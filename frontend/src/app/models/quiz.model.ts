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

// [memo] TypeScript の Interface は Java における POJO の様にも使用できる。
// [memo] フロントエンド側だとほとんどこの使い方になる？