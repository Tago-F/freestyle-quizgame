import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
// import { App } from './app/app'; // Appコンポーネントを直接起動しない場合
import { QuizAppComponent } from './app/components/quiz-app/quiz-app.component'; // QuizAppComponent をインポート

// bootstrapApplication(App, appConfig) // Appの代わりにQuizAppComponentを起動する場合
bootstrapApplication(QuizAppComponent, appConfig)
  .catch((err) => console.error(err));

// もし App コンポーネント (app.component.ts) をルートとして使い、
// その中で <app-quiz></app-quiz> を表示したい場合は、
// bootstrapApplication(App, appConfig) のままにし、
// app.component.ts の imports に QuizAppComponent を追加し、
// app.component.html に <app-quiz></app-quiz> を記述します。
