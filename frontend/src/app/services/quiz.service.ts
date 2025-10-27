import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
    AnswerRequest,
    AnswerResponse,
    HintRequest,
    HintResponse, // HintResponse をインポート
    QuizRequest,
    QuizResponse // QuizResponse をインポート
} from '../models/quiz.model';

@Injectable({
    providedIn: 'root'
})
export class QuizService {
    private http = inject(HttpClient);
    private readonly API_URL = 'http://localhost:8080/api/quiz'; // [TODO] 環境変数化

    /**
     * クイズの問題を取得します。
     * @param payload ジャンル情報
     * @returns 問題文のObservable
     */
    getQuestion(payload: QuizRequest): Observable<string> {
        // レスポンスが { "question": "..." } 形式であることを期待
        return this.http.post<QuizResponse>(`${this.API_URL}/question`, payload)
            .pipe(
                map(response => response.question), // レスポンスオブジェクトから question プロパティを抽出
                catchError(this.handleError<string>('クイズの取得に失敗しました。', '')) // エラー時は空文字を返す
            );
    }

    /**
     * ヒントを取得します。
     * @param payload 質問文
     * @returns ヒント文のObservable
     */
    getHint(payload: HintRequest): Observable<string> {
        // レスポンスが { "hint": "..." } 形式であることを期待
        return this.http.post<HintResponse>(`${this.API_URL}/hint`, payload)
            .pipe(
                map(response => response.hint), // レスポンスオブジェクトから hint プロパティを抽出
                catchError(this.handleError<string>('ヒントの取得に失敗しました。', '')) // エラー時は空文字を返す
            );
    }

    /**
     * 回答を送信し、結果を取得します。
     * @param payload 質問文と回答
     * @returns 回答結果のObservable
     */
    submitAnswer(payload: AnswerRequest): Observable<AnswerResponse> {
        // レスポンスが { "isCorrect": boolean, "explanation": "..." } 形式であることを期待
        // responseType の指定は不要 (デフォルトでJSON)
        return this.http.post<AnswerResponse>(`${this.API_URL}/answer`, payload)
            .pipe(
                // map オペレータは不要。HttpClient が自動でJSONをパースし、AnswerResponse 型にしてくれる。
                // テキストをパースする parseAnswerResponse メソッドも不要になったため削除。
                catchError(this.handleError<AnswerResponse>(
                    '回答の判定に失敗しました。',
                    // エラー時のデフォルト値を AnswerResponse の型に合わせる
                    { isCorrect: false, explanation: 'エラーにより判定できませんでした。' }
                ))
            );
    }

    /**
     * HTTP エラーハンドリング
     * @param operation 実行した操作名
     * @param result エラー時に返すデフォルト値
     * @returns エラーハンドリングを含むObservable
     */
    private handleError<T>(operation = 'operation', result?: T) {
        return (error: HttpErrorResponse): Observable<T> => {
            console.error(`${operation} failed: ${error.message}`);
            // [TODO] ユーザーフレンドリーなエラー表示を検討
            // サーバーからのエラーメッセージがあればそれを表示するなどの処理も可能
            // if (error.error && error.error.error) {
            //   console.error(`Server returned error: ${error.error.error}`);
            // }
            return of(result as T); // アプリの実行を続けるためにデフォルト値を返す
        };
    }
}
