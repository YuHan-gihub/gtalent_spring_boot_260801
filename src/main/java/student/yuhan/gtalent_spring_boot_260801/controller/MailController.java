package student.yuhan.gtalent_spring_boot_260801.controller;

// MediaType 用來限制 POST 請求的 Content-Type 必須是 application/json。
// 原因：避免使用者誤傳表單資料，讓此 API 明確只接受 JSON。
import org.springframework.http.MediaType;

// 改寫步驟 1：GetMapping 對應 GET 請求。
// 原因：保留瀏覽器可直接測試的功能，GET 仍使用網址參數。
import org.springframework.web.bind.annotation.GetMapping;

// 改寫步驟 2：PostMapping 對應 POST 請求。
// 原因：這次要讓 POST 從 JSON request body 讀取寄信資料。
import org.springframework.web.bind.annotation.PostMapping;

// 改寫步驟 3：匯入 RequestParam，從網址參數或表單欄位取得呼叫者傳入的資料。
// 原因：收件者、主旨與內文不應寫死在程式中，而應由每次 API 呼叫提供。
import org.springframework.web.bind.annotation.RequestParam;

// 改寫步驟 4：RequestBody 將 POST 的 JSON 內容轉換成 Java 物件。
// Spring Boot 會使用 Jackson，自動把 JSON 的 to、subject、text 對應到 EmailRequest。
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import student.yuhan.gtalent_spring_boot_260801.service.MailService;
import student.yuhan.gtalent_spring_boot_260801.dto.EmailRequest;
// 錯誤修正步驟：匯入 /send/gmail API 使用的 MailSendRequest DTO。
// 原因：MailSendRequest 位於 request 套件；未匯入時編譯器只會在 controller 套件尋找該類別，
// 因而出現「cannot find symbol: class MailSendRequest」錯誤，導致 Maven 無法編譯及執行測試。
import student.yuhan.gtalent_spring_boot_260801.request.MailSendRequest;
import student.yuhan.gtalent_spring_boot_260801.response.ApiResponse;

@RestController
public class MailController {
  private MailService mailService;

  // 建構子注入：Spring 建立控制器時，會將 MailService 提供給此控制器使用。
  public MailController(MailService mailService) {
    this.mailService = mailService;
  }

  @PostMapping("/send/gmail")
  public ApiResponse sendEmail(@Valid @RequestBody MailSendRequest request) {
    mailService.sendEmail(request.getToMailAddress(), request.getSubject(), request.getContent());
    return new ApiResponse("寄送Gmail成功");
  }

  /*
   * 改寫步驟 5：保留 GET 寄信 API，方便直接在瀏覽器測試。
   *
   * API 網址：/mail/send
   * GET 測試範例：
   * /mail/send?to=test@example.com&subject=Hello&text=Test%20message
   *
   * GET 方便直接在瀏覽器測試；實際傳送資料通常建議使用下方的 JSON POST API。
   * 不要透過 GET 傳送敏感資料，因為網址可能會保留在瀏覽紀錄或伺服器日誌中。
   */
  @GetMapping("/mail/send")
  public String sendEmailByGet(
      // 改寫步驟 6：讀取名稱為 to 的必要參數，作為收件者的 Email。
      @RequestParam String to,

      // 改寫步驟 7：讀取名稱為 subject 的必要參數，作為信件主旨。
      @RequestParam String subject,

      // 改寫步驟 8：讀取名稱為 text 的必要參數，作為信件內文。
      // 在 GET 網址中，空白通常必須轉成 %20 或 +。
      @RequestParam String text) {

    // 改寫步驟 9：將呼叫者傳入的資料交給 MailService 寄信。
    // 改寫前，三個值都是固定文字；現在每次呼叫 API 都能寄出不同內容。
    mailService.sendEmail(to, subject, text);

    // 改寫步驟 10：MailService 正常執行後，回傳成功訊息給 API 呼叫者。
    // 此訊息只表示應用程式已處理寄信請求，不代表收件者已經讀取信件。
    return "Email sent successfully!";
  }

  /*
   * 改寫步驟 11：建立專門處理 JSON 的 POST API。
   *
   * Postman 設定：
   * 1. Method 選擇 POST。
   * 2. URL 輸入 http://localhost:8080/mail/send。
   * 3. Body 選擇 raw，再選擇 JSON。
   * 4. 輸入下列 JSON：
   * {
   * "to": "test@example.com",
   * "subject": "Hello",
   * "text": "Test message"
   * }
   *
   * @RequestBody 的作用：讀取 HTTP body 中的 JSON，並建立 EmailRequest 物件。
   * JSON 的欄位名稱必須與 EmailRequest 的 to、subject、text 屬性相同。
   */
  @PostMapping(value = "/mail/send", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String sendEmailByPost(@RequestBody EmailRequest emailRequest) {
    // 改寫步驟 12：從已轉換完成的 Java 物件取出 JSON 中的三個欄位，再寄送信件。
    mailService.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getText());

    // 改寫步驟 13：回傳 JSON POST 呼叫成功的結果。
    return "Email sent successfully!";
  }
}
