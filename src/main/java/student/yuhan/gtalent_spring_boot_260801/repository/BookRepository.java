package student.yuhan.gtalent_spring_boot_260801.repository;

import student.yuhan.gtalent_spring_boot_260801.entity.Book;

import java.util.List;

public interface BookRepository {

    // 取得所有書籍
    public List<Book> findAll(int page, int size);

    // 取得一本書籍by Id
    public Book findOneById(Long id);

    // 取得一本書籍by Name
    public List<Book> findOneByName(String name);

    // 取得書籍總筆數
    public long countAll();

    // 新增一本書籍
    public Book create(Book book);

    // 修改一本書籍
    public Book update(Long id, Book book);

    // 改寫步驟 16：軟刪除後回傳被刪除的 Book，而非只回傳 void。
    // 原因：刪除後控制器仍要在通知信中列出該書的 ID、書名與價格，不能只剩下傳入的 ID。
    public Book delete(Long id);

}
