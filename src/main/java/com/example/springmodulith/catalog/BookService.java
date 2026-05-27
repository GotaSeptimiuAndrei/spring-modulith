package com.example.springmodulith.catalog;

import com.example.springmodulith.catalog.internal.dto.BookRequest;
import com.example.springmodulith.catalog.internal.exception.BookNotFoundException;
import com.example.springmodulith.catalog.internal.exception.BookValidationException;
import com.example.springmodulith.catalog.internal.repository.BookRepository;
import com.example.springmodulith.catalog.internal.utils.BookConverter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(BookConverter::convertToDto).toList();
    }

    public Page<BookResponse> getAllBooksPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookId"));
        Page<Book> booksPage = bookRepository.findAll(pageable);

        return booksPage.map(BookConverter::convertToDto);
    }

    public Page<BookResponse> searchBooks(String query, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Book> booksPage = "All".equalsIgnoreCase(category)
                ? bookRepository.findByTitleIgnoreCaseContainingOrAuthorIgnoreCaseContaining(query, query, pageable)
                : bookRepository
                .findByCategoryIgnoreCaseAndTitleIgnoreCaseContainingOrCategoryIgnoreCaseAndAuthorIgnoreCaseContaining(
                        category, query, category, query, pageable);

        return booksPage.map(BookConverter::convertToDto);
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return BookConverter.convertToDto(book);
    }

    @Transactional
    public BookResponse saveBook(BookRequest bookRequest) {
        String mockCoverUrl = "https://via.placeholder.com/400x600.png?text=";

        Book book = BookConverter.convertToEntity(bookRequest, mockCoverUrl);
        Book savedBook = bookRepository.save(book);
        return BookConverter.convertToDto(savedBook);
    }

    @Transactional
    public BookResponse updateBookQuantity(Long id, BookQuantityUpdateRequest dto) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        int delta = dto.getDelta();

        int newCopies = book.getCopies() + delta;
        int newCopiesAvailable = book.getCopiesAvailable() + delta;

        if (newCopies < 0 || newCopiesAvailable < 0) {
            throw new BookValidationException("Cannot reduce quantity below zero.");
        }

        if (newCopiesAvailable > newCopies) {
            throw new BookValidationException("Available copies cannot exceed total copies.");
        }

        book.setCopies(newCopies);
        book.setCopiesAvailable(newCopiesAvailable);

        Book saved = bookRepository.save(book);
        return BookConverter.convertToDto(saved);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        bookRepository.delete(book);

        eventPublisher.publishEvent(new BookDeletedEvent(id));
        log.info("Catalog published BookDeletedEvent for book ID: {}", id);
    }

}

