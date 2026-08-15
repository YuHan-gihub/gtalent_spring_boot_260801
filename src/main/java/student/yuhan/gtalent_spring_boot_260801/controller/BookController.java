package student.yuhan.gtalent_spring_boot_260801.controller;

import student.yuhan.gtalent_spring_boot_260801.entity.Book;
import student.yuhan.gtalent_spring_boot_260801.repository.BookRepository;
// 改寫步驟 1：匯入 MailService，讓書籍 API 可在資料庫操作成功後呼叫寄信功能。
// 原因：通知屬於書籍新增、修改、刪除流程的一部分，控制器需要能協調這兩項工作。
import student.yuhan.gtalent_spring_boot_260801.service.MailService;

import student.yuhan.gtalent_spring_boot_260801.request.BookCreateRequest;

import student.yuhan.gtalent_spring_boot_260801.response.ApiResponse;
import student.yuhan.gtalent_spring_boot_260801.response.BookResponse;
import student.yuhan.gtalent_spring_boot_260801.response.PageResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository repository;

    // 改寫步驟 2：保留一份 MailService 參考，專門處理 Gmail 通知。
    // 原因：控制器只決定「何時」通知；信件組裝與 SMTP 傳送仍集中在 MailService，避免重複程式碼。
    private final MailService mailService;

    // 改寫步驟 3：使用建構子注入 Repository 與 MailService。
    // 原因：Spring 建立 BookController 時會提供這兩個已管理的 Bean，測試時也能輕易替換成 mock 物件。
    public BookController(BookRepository repository, MailService mailService) {
        this.repository = repository;
        this.mailService = mailService;
    }

    // 取得所有的書籍
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<BookResponse> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 預設頁碼從1開始
        if (page < 1) {
            page = 1;
        }

        // 每頁最少數量不能為0
        // 如果帶0進來, 自動呈現1頁10組
        if (size < 1) {
            size = 10;
        }

        // 每頁最大不能超過50組
        if (size > 50) {
            size = 50;
        }

        List<Book> books = repository.findAll(page, size);

        // API 不直接回傳 Book Entity，避免把 status、deletedAt 暴露給前端。
        // books.stream()：把 List<Book> 轉成串流，準備逐筆處理。
        // map(BookResponse::new)：每一筆 Book 都執行 new BookResponse(book)，轉成只包含id、name、price
        // 的 DTO。
        // toList()：把轉換後的 BookResponse 收集回 List<BookResponse>。
        List<BookResponse> bookResponses = books.stream()
                .map(BookResponse::new)
                .toList();

        long totalElements = repository.countAll();

        return new PageResponse<>(bookResponses, page, size, totalElements);

    }

    // 取得單一書籍By Id
    @GetMapping("/search-id/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Book getOneById(@PathVariable Long id) {
        Book book = repository.findOneById(id);
        return book;
    }

    // 取得單一書籍By Name
    @GetMapping("search-name/{name}")
    @ResponseStatus(HttpStatus.OK)
    public List<Book> getOneByName(@PathVariable String name) {
        return repository.findOneByName(name);
    }

    // 新增一本書籍
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse create(@Valid @RequestBody BookCreateRequest request) {
        Book book = new Book(request.getName(), request.getPrice());

        // 改寫步驟 4：先完成並確認新增交易，再取得帶有資料庫 ID 的書籍物件。
        // 原因：只有資料真正新增成功才可以寄「新增成功」通知；create 回傳的物件包含新產生的 ID，可放入信件。
        Book createdBook = repository.create(book);

        // 改寫步驟 5：新增完成後寄送 Gmail 通知。
        // 原因：通知放在 repository.create 後面，資料庫新增失敗時會直接拋出例外，不會寄出錯誤的成功信。
        mailService.sendBookNotification("新增", createdBook);
        return new ApiResponse("新增書籍成功");
    }

    // 修改一本書籍
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse update(@PathVariable Long id, @Valid @RequestBody BookCreateRequest request) {
        Book book = new Book(request.getName(), request.getPrice());

        // 改寫步驟 6：先更新資料庫，並取得已套用新書名與新價格的既有書籍。
        // 原因：若找不到書籍或更新失敗，repository 會拋出例外，流程不會誤寄修改成功通知。
        Book updatedBook = repository.update(id, book);

        // 改寫步驟 7：資料庫更新成功後，寄出包含最新書籍資料的 Gmail 通知。
        // 原因：讓管理者在信中可直接看到本次被修改的 ID、書名與價格。
        mailService.sendBookNotification("修改", updatedBook);
        return new ApiResponse("修改書籍成功");
    }

    // 軟刪除一本書籍
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse delete(@PathVariable Long id) {
        // 改寫步驟 8：執行軟刪除後取得被刪除書籍的原始資料。
        // 原因：軟刪除後資料不會出現在一般查詢結果中，因此必須由 delete 的回傳值保留書名、價格供通知使用。
        Book deletedBook = repository.delete(id);

        // 改寫步驟 9：刪除交易成功後寄送 Gmail 通知。
        // 原因：若書籍不存在或刪除失敗，這行不會執行，避免寄出與實際資料不一致的成功通知。
        mailService.sendBookNotification("刪除", deletedBook);
        return new ApiResponse("刪除書籍成功");
    }
}
