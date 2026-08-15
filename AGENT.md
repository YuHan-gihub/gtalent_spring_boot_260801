# 專案架構紀錄

## 技術與分層

此專案是 Spring Boot 3 / Java 17 的書籍管理 API。主要請求流程如下：

```text
HTTP Request
  -> Controller（路由、請求驗證、流程協調）
  -> Repository（資料庫交易與 CRUD）
  -> MySQL

Controller
  -> MailService（組裝並發送郵件）
  -> Gmail SMTP
```

主要套件職責：

- `controller`：公開 API；`BookController` 協調書籍異動與通知，`MailController` 提供手動寄信 API。
- `request`：接收及驗證 API JSON，例如 `BookCreateRequest`、`MailSendRequest`。
- `dto`：一般資料傳輸物件；`EmailRequest` 用於原有 `/mail/send` JSON API。
- `repository`：資料庫存取及手動交易控制；介面為 `BookRepository`，實作為 `BookRepositoryImpl`。
- `service`：可重用的商業服務；`MailService` 負責寄送一般郵件與書籍異動通知。
- `entity`：JPA 實體，例如 `Book`。
- `constant` / `response` / `exception`：統一錯誤碼、回應格式與例外處理。

## 本次調整：書籍異動通知

`POST /books`、`PUT /books/{id}`、`DELETE /books/{id}` 的執行順序為：

```text
BookController
  -> BookRepository 寫入／更新／軟刪除資料庫
  -> 交易 commit 成功後回傳 Book
  -> MailService.sendBookNotification(action, book)
  -> Gmail 寄送給 book.notification.email
```

設計重點：

- 先完成資料庫操作，成功後才寄通知；書籍不存在或交易失敗時不會寄成功信。
- `BookRepository.delete` 的回傳型別已由 `void` 改為 `Book`，讓刪除通知仍能包含 ID、書名及價格。
- `BookRepositoryImpl.update` 回傳實際更新的 `existingBook`，確保通知具有正確的資料庫 ID 與新欄位值。
- 寄信細節集中在 `MailService`，控制器只決定何時觸發，避免三個 CRUD API 重複組裝郵件。
- 郵件寄送失敗會由既有例外處理轉為錯誤回應，錯誤碼為 `50000`（`MAIL_SEND_FAILED`）。目前資料庫交易不會因為 commit 後的寄信失敗而回滾。

通知信主旨為 `書籍{新增|修改|刪除}通知`，內文包含操作、書籍 ID、書名與價格。

## 郵件 API

### 建議使用：POST `/send/gmail`

請求 Content-Type：`application/json`

```json
{
  "toMailAddress": "recipient@example.com",
  "subject": "測試主旨",
  "content": "測試內容"
}
```

`MailSendRequest` 驗證規則：

- `toMailAddress`：必填，且必須是 Email 格式。
- `subject`：必填，最多 60 個字。
- `content`：必填，最多 1000 個字。

驗證錯誤使用 `ResponseMessages` 的 10004–10009 錯誤碼，經由全域例外處理轉為統一 API 回應。

### 保留的練習 API：`/mail/send`

- `GET /mail/send?to=...&subject=...&text=...`：可直接用瀏覽器測試；不應放敏感資訊在網址。
- `POST /mail/send`：接受下列 JSON，使用 `dto.EmailRequest`：

```json
{
  "to": "recipient@example.com",
  "subject": "測試主旨",
  "text": "測試內容"
}
```

## 設定與安全性

`src/main/resources/application.properties` 會載入根目錄 `.env`。敏感資訊只放在 `.env`，不要提交 Git；可依 `.env.example` 建立本機設定。

```properties
GMAIL_USERNAME=your-gmail-address
GMAIL_PASSWORD=your-gmail-app-password
BOOK_NOTIFICATION_EMAIL=book-admin@example.com
```

- Gmail SMTP 使用 `smtp.gmail.com:587`、SMTP AUTH 與 STARTTLS。
- `BOOK_NOTIFICATION_EMAIL` 未設定時，預設寄給 `GMAIL_USERNAME`。
- 測試環境使用 `src/test/resources/application.properties` 的 localhost SMTP 假設定及 `example.test` 地址，避免測試使用真實帳密或實際寄信。
- Maven 編譯來源編碼已設定為 UTF-8，確保中文註解與訊息可正常編譯。

## 維護注意事項

- 新增書籍異動類型時，應由 `BookController` 在資料庫成功後呼叫 `MailService`，不要在 Repository 內直接寄信。
- 若調整通知格式，優先修改 `MailService.sendBookNotification`。
- 新增 API 請求欄位時，將驗證放在 `request` DTO，並於 `ResponseMessages` 補上對應錯誤碼與繁中訊息。
- 不要將 Gmail 密碼、應用程式密碼或真實收件者硬編碼至 Java 原始碼、`application.properties` 或本文件。
