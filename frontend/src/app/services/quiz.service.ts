import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AnswerRequest, AnswerResponse, HintRequest, QuizRequest } from '../models/quiz.model';

@Injectable({
  providedIn: 'root' // アプリケーション全体でシングルトンとして提供
})
export class QuizService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/quiz'; // 環境変数などから取得するのが望ましい

  /**
   * クイズの問題を取得します。
   * @param payload ジャンル情報
   * @returns 問題文のObservable
   */
  getQuestion(payload: QuizRequest): Observable<string> {
    return this.http.post(`${this.API_URL}/question`, payload, { responseType: 'text' })
      .pipe(
        catchError(this.handleError<string>('クイズの取得に失敗しました。', ''))
      );
  }

  /**
   * ヒントを取得します。
   * @param payload 質問文
   * @returns ヒント文のObservable
   */
  getHint(payload: HintRequest): Observable<string> {
    return this.http.post(`${this.API_URL}/hint`, payload, { responseType: 'text' })
      .pipe(
        catchError(this.handleError<string>('ヒントの取得に失敗しました。', ''))
      );
  }

  /**
   * 回答を送信し、結果を取得します。
   * @param payload 質問文と回答
   * @returns 回答結果のObservable
   */
  submitAnswer(payload: AnswerRequest): Observable<AnswerResponse> {
    // バックエンドのレスポンスが 'text' のため、ここでパース処理を行う
    return this.http.post(`${this.API_URL}/answer`, payload, { responseType: 'text' })
      .pipe(
        map(response => this.parseAnswerResponse(response)),
        catchError(this.handleError<AnswerResponse>('回答の判定に失敗しました。', { result: '判定不能', explanation: '', isCorrect: false }))
      );
  }

  /**
   * テキスト形式の回答レスポンスをAnswerResponseオブジェクトにパースします。
   * @param response バックエンドからのテキストレスポンス
   * @returns パースされたAnswerResponse
   */
  private parseAnswerResponse(response: string): AnswerResponse {
    const lines = response.split('\n');
    const result = lines.shift() || '判定不能';
    const explanation = lines.join('\n').trim();
    const isCorrect = result.includes('正解'); // '正解'という文字列が含まれるかで判定
    return { result, explanation, isCorrect };
  }

  /**
   * HTTPエラーハンドリング
   * @param operation 実行した操作名
   * @param result エラー時に返すデフォルト値
   * @returns エラーハンドリングを含むObservable
   */
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: HttpErrorResponse): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`);
      // ユーザー向けのより良いエラーメッセージに変換することも可能
      // ここでは簡易的にコンソールに出力し、デフォルト値を返す
      // `throwError` を使ってエラーを上位に伝播させることもできます
      // return throwError(() => new Error(message));
      return of(result as T); // エラーが発生してもアプリを継続させるためにデフォルト値を返す
    };
  }
}
