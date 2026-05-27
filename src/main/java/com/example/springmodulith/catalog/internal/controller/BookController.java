package com.example.springmodulith.catalog.internal.controller;

import com.example.springmodulith.catalog.BookService;
import com.example.springmodulith.catalog.BookQuantityUpdateRequest;
import com.example.springmodulith.catalog.internal.dto.BookRequest;
import com.example.springmodulith.catalog.BookResponse;
import com.example.springmodulith.common.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    public static final String SUCCESS = "success";

    public static final String ERROR = "error";

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<APIResponse<List<BookResponse>>> getAllBooks() {
        List<BookResponse> books = bookService.getAllBooks();
        return ResponseEntity.ok(APIResponse.<List<BookResponse>>builder().status(SUCCESS).results(books).build());
    }

    @GetMapping("/paginated")
    public ResponseEntity<APIResponse<Page<BookResponse>>> getAllBooksPaginated(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        Page<BookResponse> booksPage = bookService.getAllBooksPaginated(page, size);

        return ResponseEntity.ok(APIResponse.<Page<BookResponse>>builder().status(SUCCESS).results(booksPage).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<BookResponse>> getBookById(@PathVariable Long id) {
        BookResponse bookResponse = bookService.getBookById(id);
        return ResponseEntity.ok(APIResponse.<BookResponse>builder().status(SUCCESS).results(bookResponse).build());
    }

    @GetMapping("/search")
    public ResponseEntity<APIResponse<Page<BookResponse>>> searchBooksPaginated(@RequestParam String query,
                                                                                @RequestParam(defaultValue = "All") String category, @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "5") int size) {

        Page<BookResponse> booksPage = bookService.searchBooks(query, category, page, size);

        return ResponseEntity
                .ok(APIResponse.<Page<BookResponse>>builder().status("success").results(booksPage).build());
    }

    @PostMapping
    public ResponseEntity<APIResponse<BookResponse>> createBook(@Valid @ModelAttribute BookRequest bookRequest) {
        BookResponse created = bookService.saveBook(bookRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.<BookResponse>builder().status(SUCCESS).results(created).build());
    }

    @PatchMapping("/{id}/quantity")
    public ResponseEntity<APIResponse<BookResponse>> updateBookQuantity(@PathVariable Long id,
                                                                        @Valid @RequestBody BookQuantityUpdateRequest dto) {

        BookResponse updated = bookService.updateBookQuantity(id, dto);

        return ResponseEntity.ok(APIResponse.<BookResponse>builder().status(SUCCESS).results(updated).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
