package com.example.springmodulith.review.internal.service;

import com.example.springmodulith.catalog.BookDeletedEvent;
import com.example.springmodulith.catalog.BookService;
import com.example.springmodulith.review.internal.dto.ReviewRequest;
import com.example.springmodulith.review.internal.dto.ReviewResponse;
import com.example.springmodulith.review.internal.exception.ReviewException;
import com.example.springmodulith.review.internal.model.Review;
import com.example.springmodulith.review.internal.repository.ReviewRepository;
import com.example.springmodulith.review.internal.utils.ReviewConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final BookService bookService;

    public ReviewResponse createReview(String username, ReviewRequest reviewRequest) {

        bookService.getBookById(reviewRequest.getBookId());

        if (reviewRepository.findByUsernameAndBookId(username, reviewRequest.getBookId()).isPresent()) {
            throw new ReviewException("Review already exists");
        }

        Review review = ReviewConverter.convertToEntity(reviewRequest, username);
        Review savedReview = reviewRepository.save(review);

        return ReviewConverter.convertToDto(savedReview);
    }

    public List<ReviewResponse> getAllReviewsForBook(Long bookId) {
        List<Review> reviews = reviewRepository.findAll().stream()
                .filter(r -> r.getBookId().equals(bookId)).toList();

        return reviews.stream().map(ReviewConverter::convertToDto).toList();
    }

    public Page<ReviewResponse> getAllReviewsForBookPaginated(Long bookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewsPage = reviewRepository.findAllByBookId(bookId, pageable);
        return reviewsPage.map(ReviewConverter::convertToDto);
    }

    public boolean hasUserReviewed(String username, Long bookId) {
        return reviewRepository.existsByUsernameAndBookId(username, bookId);
    }

    @ApplicationModuleListener
    public void onBookDeleted(BookDeletedEvent event) {
        reviewRepository.deleteAllByBookId(event.bookId());
        log.info("Review module caught event! Deleted all reviews for book ID: \" + event.bookId()");
    }
}