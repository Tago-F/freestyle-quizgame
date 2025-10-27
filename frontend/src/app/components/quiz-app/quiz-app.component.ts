import { Component, ChangeDetectionStrategy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { QuizService } from '../../services/quiz.service';
import { AnswerResponse } from '../../models/quiz.model'; // QuizResponse, HintResponse は不要

@Component({
    selector: 'app-quiz',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './quiz-app.component.html',
    styleUrl: './quiz-app.component.css',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class QuizAppComponent {
    private quizService = inject(QuizService);

    // --- State Signals ---
    genres = signal<string[]>(['歴史', '科学', 'エンタメ', '地理', 'おまかせ']).asReadonly();
    selectedGenre = signal<string>(this.genres()[0]);
    question = signal<string>(''); // 問題文 (string)
    hint = signal<string>(''); // ヒント文 (string)
    userAnswer = signal<string>('');
    answerResult = signal<AnswerResponse | null>(null); // 回答結果 (AnswerResponse)
    isLoading = signal<boolean>(false);
    error = signal<string | null>(null);

    // --- Computed Signals (Derived State) ---
    // resultText は不要になったため削除 (isCorrect を直接利用)
    explanationText = computed(() => this.answerResult()?.explanation || '');
    isCorrect = computed(() => this.answerResult()?.isCorrect || false); // AnswerResponse の isCorrect を直接参照

    // ボタンの状態管理
    canSubmit = computed(() => !!this.userAnswer() && !this.isLoading() && !this.answerResult());
    canGetHint = computed(() => !!this.question() && !this.isLoading() && !this.hint() && !this.answerResult());
    canGetNextQuiz = computed(() => !this.isLoading() || !!this.answerResult());
    nextQuizButtonText = computed(() => this.answerResult() || this.question() ? '次のクイズ' : 'クイズ');

    // --- Methods ---

    /** ジャンルを選択 */
    selectGenre(genre: string): void {
        this.selectedGenre.set(genre);
        this.resetStateForNewQuestion();
    }

    /** クイズを取得 */
    getQuiz(): void {
        this.resetStateForNewQuestion();
        this.isLoading.set(true);
        this.error.set(null);

        this.quizService.getQuestion({ genre: this.selectedGenre() })
            .pipe(finalize(() => this.isLoading.set(false)))
            .subscribe({
                next: (q) => { // レスポンスは string 型 (問題文)
                    if (q) {
                        this.question.set(q);
                    } else if (!this.error()) {
                        this.error.set('クイズの取得に失敗しました。');
                    }
                },
                error: (err) => {
                    console.error("GetQuiz Error:", err);
                    this.error.set('クイズの取得中に予期せぬエラーが発生しました。');
                }
            });
    }

    /** ヒントを取得 */
    getHint(): void {
        const currentQuestion = this.question();
        if (!currentQuestion || !this.canGetHint()) return;

        this.isLoading.set(true);
        this.error.set(null);

        this.quizService.getHint({ question: currentQuestion })
            .pipe(finalize(() => this.isLoading.set(false)))
            .subscribe({
                next: (h) => { // レスポンスは string 型 (ヒント文)
                    if (h) {
                        this.hint.set(h);
                    } else if (!this.error()) {
                        this.error.set('ヒントの取得に失敗しました。');
                    }
                },
                error: (err) => {
                    console.error("GetHint Error:", err);
                    this.error.set('ヒントの取得中に予期せぬエラーが発生しました。');
                }
            });
    }

    /** 回答を送信 */
    submitAnswer(): void {
        const currentAnswer = this.userAnswer();
        const currentQuestion = this.question();
        if (!currentAnswer || !currentQuestion || !this.canSubmit()) return;

        this.isLoading.set(true);
        this.error.set(null);

        this.quizService.submitAnswer({ question: currentQuestion, answer: currentAnswer })
            .pipe(finalize(() => this.isLoading.set(false)))
            .subscribe({
                next: (res) => { // レスポンスは AnswerResponse 型
                    if (res) { // res が null や undefined でないことを確認
                        this.answerResult.set(res);
                        // resultText を設定する処理は不要
                    } else if (!this.error()) {
                        // サービスがデフォルト値を返さなかった場合のフォールバック
                        this.error.set('回答の判定結果を正しく受け取れませんでした。');
                    }
                },
                error: (err) => {
                    console.error("SubmitAnswer Error:", err);
                    this.error.set('回答の判定中に予期せぬエラーが発生しました。');
                }
            });
    }

    /** 次の問題のために状態をリセット (ジャンルは維持) */
    private resetStateForNewQuestion(): void {
        this.question.set('');
        this.hint.set('');
        this.userAnswer.set('');
        this.answerResult.set(null);
        this.error.set(null);
        this.isLoading.set(false);
    }
}
