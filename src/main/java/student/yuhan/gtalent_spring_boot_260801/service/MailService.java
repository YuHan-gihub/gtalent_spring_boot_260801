package student.yuhan.gtalent_spring_boot_260801.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
// 改寫步驟 10：@Value 用於讀取 application.properties 或 .env 中的設定值。
// 原因：寄件者與通知收件者是環境設定，不應硬編碼在 Java 程式內。
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import student.yuhan.gtalent_spring_boot_260801.entity.Book;

@Service
public class MailService {

  private final JavaMailSender mailSender;

  // 改寫步驟 11：保存 Gmail 寄件帳號，並在每封信設定 From。
  // 原因：Gmail SMTP 通常只允許使用已驗證的帳號寄信；明確指定寄件者也能讓收件者辨識信件來源。
  private final String from;

  // 改寫步驟 12：保存書籍異動通知的專用收件者。
  // 原因：手動寄信 API 的收件者由呼叫者決定；系統通知則必須固定寄給管理者設定的信箱。
  private final String bookNotificationEmail;

  // 注入 JavaMailSender
  public MailService(
      JavaMailSender mailSender,
      @Value("${spring.mail.username}") String from,
      @Value("${book.notification.email:${spring.mail.username}}") String bookNotificationEmail) {
    this.mailSender = mailSender;
    this.from = from;
    this.bookNotificationEmail = bookNotificationEmail;
  }

  public void sendEmail(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();

    // 改寫步驟 13：每封信都設定 Gmail 帳號為寄件者。
    // 原因：同時適用於既有的 /mail/send API 與新的書籍通知，確保寄件設定一致。
    message.setFrom(from);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    mailSender.send(message);
  }

  /**
   * 改寫步驟 14：集中產生書籍異動通知的主旨與內文，並寄給管理者。
   * 原因：新增、修改、刪除三個 API 都使用同一種通知格式，集中處理可避免三處內容逐漸不一致。
   */
  public void sendBookNotification(String action, Book book) {
    String subject = "書籍" + action + "通知";
    String text = String.format(
        "書籍%s成功。%n書籍 ID：%d%n書名：%s%n價格：%d",
        action, book.getId(), book.getName(), book.getPrice());

    // 改寫步驟 15：重用既有 sendEmail 方法實際送至 Gmail SMTP。
    // 原因：不重複建立 SimpleMailMessage 與呼叫 mailSender.send 的程式碼，日後調整寄信行為只需修改一處。
    sendEmail(bookNotificationEmail, subject, text);
  }
}
