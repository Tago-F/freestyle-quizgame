
# なんかいろんなメモ書き

## リファレンス

- Spring AI（Vertex AI 導入）

    - https://spring.pleiades.io/spring-ai/reference/api/chat/vertexai-gemini-chat.html

    - pom.xml の dependency の記述はここ。

- Spring AI（BOM 設定）

    - https://spring.pleiades.io/spring-ai/reference/getting-started.html#dependency-management

    - pom.xml の dependencyManagement（BOM）の記述はここ。このプロジェクトでは 1.0.3 を使用。

- ChatClient の JavaDoc

    - https://docs.spring.io/spring-ai/docs/current/api/org/springframework/ai/chat/client/ChatClient.html

## 知らなかったことなど殴り書き

- BeanOutputConverter

    - Spring AI に型定義を宣言し、レスポンスを型定義に合わせた JSON で出力してくれる便利クラス。

    - chatClient.prompt(prompt).call().entity(outputConverter) のような使用イメージ。


## 参考資料

- Java, TypeScript の intarface の違い

    - https://shironeko.hateblo.jp/entry/2023/10/07/161950
 
   - Java の intarface の実装方法の基本とか

    - TypeScript の intarface は他に型脚注としても使えますよー とか書いてある。