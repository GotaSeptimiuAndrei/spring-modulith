package com.example.springmodulith.lending.internal.service;

import com.example.springmodulith.catalog.*;
import com.example.springmodulith.lending.internal.dto.CurrentLoansResponse;
import com.example.springmodulith.lending.internal.exception.BookLoanException;
import com.example.springmodulith.lending.internal.model.BookLoan;
import com.example.springmodulith.lending.internal.repository.BookLoanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookLoanService {

    private final BookLoanRepository bookLoanRepository;

    private final BookService bookService;

    public BookLoan loanBook(String username, Long bookId) {
        Optional<BookLoan> validateLoan = bookLoanRepository.findByUsernameAndBookId(username, bookId);

        if (validateLoan.isPresent()) {
            throw new BookLoanException("Book is already loaned by current user");
        }

        // Synchronous API Call: Safely decrement quantity via Catalog module
        // This automatically throws an exception if the book doesn't exist or if copies are <= 0
        bookService.updateBookQuantity(bookId, new BookQuantityUpdateRequest(-1));

        BookLoan bookLoan = new BookLoan();
        bookLoan.setUsername(username);
        bookLoan.setBookId(bookId);
        bookLoan.setLoanDate(LocalDate.now());
        bookLoan.setReturnDate(LocalDate.now().plusDays(3));

        return bookLoanRepository.save(bookLoan);
    }

    public int currentLoansCount(String username) {
        return bookLoanRepository.findByUsername(username).size();
    }

    public boolean isBookLoanedByUser(String username, Long bookId) {
        return bookLoanRepository.findByUsernameAndBookId(username, bookId).isPresent();
    }

    public List<CurrentLoansResponse> currentLoansByUser(String username) {
        List<CurrentLoansResponse> currentLoans = new ArrayList<>();
        List<BookLoan> bookLoans = bookLoanRepository.findByUsername(username);
        LocalDate today = LocalDate.now();

        for (BookLoan loan : bookLoans) {
            // Synchronous API Call: Get book details directly from the Catalog API
            BookResponse bookResponse = bookService.getBookById(loan.getBookId());

            LocalDate returnDate = LocalDate.parse(loan.getReturnDate().toString());
            int daysLeft = (int) ChronoUnit.DAYS.between(today, returnDate);

            // CurrentLoansResponse now natively accepts the public BookResponse
            currentLoans.add(new CurrentLoansResponse(bookResponse, daysLeft));
        }

        return currentLoans;
    }

    public void returnBook(String username, Long bookId) {
        Optional<BookLoan> validateLoan = bookLoanRepository.findByUsernameAndBookId(username, bookId);

        if (validateLoan.isEmpty()) {
            throw new BookLoanException("Book is not loaned by current user");
        }

        // Synchronous API Call: Restore the book quantity
        bookService.updateBookQuantity(bookId, new BookQuantityUpdateRequest(1));

        bookLoanRepository.delete(validateLoan.get());
    }

    public void renewLoan(String username, Long bookId) {
        Optional<BookLoan> bookLoan = bookLoanRepository.findByUsernameAndBookId(username, bookId);

        if (bookLoan.isEmpty()) {
            throw new BookLoanException("Book is not loaned by current user");
        }

        LocalDate today = LocalDate.now();
        LocalDate returnDate = LocalDate.parse(bookLoan.get().getReturnDate().toString());

        if (returnDate.isAfter(today)) {
            bookLoan.get().setReturnDate(LocalDate.now().plusDays(3));
            bookLoanRepository.save(bookLoan.get());
        }
        else {
            throw new BookLoanException("Cannot renew loan. The return date has passed.");
        }
    }

    @ApplicationModuleListener
    @Transactional
    public void onBookDeleted(BookDeletedEvent event) {
        bookLoanRepository.deleteAllByBookId(event.bookId());
        log.info("Lending module caught event! Deleted all loans for book ID: {}", event.bookId());
    }
}