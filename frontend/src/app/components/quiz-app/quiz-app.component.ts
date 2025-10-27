import { Component, ChangeDetectionStrategy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { QuizService } from '../../services/quiz.service';
import { AnswerResponse } from '../../models/quiz.model';

@Component({
    selector: 'app-quiz', // セレクタを 'app-root' から変更（app.component.html などで使うことを想定）
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './quiz-app.component.html', // HTMLを別ファイルに分離
    styleUrl: './quiz-app.component.css',     // CSSを別ファイルに分離
    changeDetection: ChangeDetectionStrategy.OnPush // OnPushでパフォーマンス向上
})
export class QuizAppComponent {
    private quizService = inject(QuizService);

    // --- State Signals ---
    genres = signal<string[]>(['歴史', '科学', 'エンタメ', '地理', 'おまかせ']).asReadonly();
    selectedGenre = signal<string>(this.genres()[0]);
    question = signal<string>('');
    hint = signal<string>('');
    userAnswer = signal<string>('');
    answerResult = signal<AnswerResponse | null>(null); // 回答結果全体を保持
    isLoading = signal<boolean>(false);
    error = signal<string | null>(null);

    // --- Computed Signals (Derived State) ---
    // 回答結果から個別の情報を取得するためのComputed Signal
    resultText = computed(() => this.answerResult()?.result || '');
    explanationText = computed(() => this.answerResult()?.explanation || '');
    isCorrect = computed(() => this.answerResult()?.isCorrect || false);

    // ボタンの無効状態などを管理するComputed Signal
    canSubmit = computed(() => !!this.userAnswer() && !this.isLoading() && !this.answerResult());
    canGetHint = computed(() => !!this.question() && !this.isLoading() && !this.hint() && !this.answerResult());
    canGetNextQuiz = computed(() => !this.isLoading() || !!this.answerResult()); // ローディング中でも結果があれば次へ
    nextQuizButtonText = computed(() => this.answerResult() || this.question() ? '次のクイズ' : 'クイズ');

    // --- Methods ---

    /** ジャンルを選択 */
    selectGenre(genre: string): void {
        this.selectedGenre.set(genre);
        this.resetStateForNewQuestion(); // ジャンル変更時にも状態をリセット
    }

    /** クイズを取得 */
    getQuiz(): void {
        this.resetStateForNewQuestion();
        this.isLoading.set(true);
        this.error.set(null);

        this.quizService.getQuestion({ genre: this.selectedGenre() })
            .pipe(finalize(() => this.isLoading.set(false)))
            .subscribe({
                next: (q) => {
                    if (q) { // サービスでエラー時に空文字が返る可能性があるためチェック
                        this.question.set(q);
                    } else if (!this.error()) { // サービス側でエラーがハンドルされなかった場合
                        this.error.set('クイズの取得に失敗しました。');
                    }
                },
                error: (err) => { // サービス側で throwError した場合など
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
                next: (h) => {
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
                next: (res) => {
                    if (res && res.result !== '判定不能') { // サービスのエラー時のデフォルト値でないかチェック
                        this.answerResult.set(res);
                    } else if (!this.error()) {
                        this.error.set('回答の判定に失敗しました。');
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
        this.isLoading.set(false); // 開始前にローディング解除
    }
}
