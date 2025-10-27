import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http'; // HttpClientModule の代わりに provideHttpClient をインポート

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient() // HttpClient をアプリケーション全体で利用可能にする
    // provideBrowserGlobalErrorListeners() は不要であれば削除
  ]
};
