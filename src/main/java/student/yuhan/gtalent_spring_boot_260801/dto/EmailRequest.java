package student.yuhan.gtalent_spring_boot_260801.dto;

/*
 * 此類別是 POST /mail/send 的 JSON 資料模型（DTO）。
 * DTO 的用途是接收 Controller 與外部呼叫者之間傳遞的資料。
 *
 * JSON 範例：
 * {
 *   "to": "test@example.com",
 *   "subject": "Hello",
 *   "text": "Test message"
 * }
 */
public class EmailRequest {
  // JSON 的 "to" 會對應到此欄位。
  private String to;

  // JSON 的 "subject" 會對應到此欄位。
  private String subject;

  // JSON 的 "text" 會對應到此欄位。
  private String text;

  // Jackson 轉換 JSON 時需要無參數建構子來建立物件。
  public EmailRequest() {
  }

  // 以下 Getter 讓 Controller 可以讀取 JSON 轉換後的資料。
  public String getTo() {
    return to;
  }

  public String getSubject() {
    return subject;
  }

  public String getText() {
    return text;
  }

  // 以下 Setter 讓 Jackson 能把 JSON 欄位的值放入對應的 Java 欄位。
  public void setTo(String to) {
    this.to = to;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setText(String text) {
    this.text = text;
  }
}
