package com.example.springmodulith.review.internal.controller;

import com.example.springmodulith.common.APIResponse;
import com.example.springmodulith.common.JwtUtils;
import com.example.springmodulith.review.internal.dto.ReviewRequest;
import com.example.springmodulith.review.internal.dto.ReviewResponse;
import com.example.springmodulith.review.internal.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = { "http://localhost:3000", "https://booknestlibrary.netlify.app" })
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    public static final String SUCCESS = "success";

    public static final String ERROR = "error";

    @PostMapping
    public ResponseEntity<APIResponse<ReviewResponse>> createReview(@RequestHeader("Authorization") String token,
                                                                    @Valid @RequestBody ReviewRequest reviewRequest) {

        String username = JwtUtils.extractUsername(token);

        ReviewResponse created = reviewService.createReview(username, reviewRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.<ReviewResponse>builder().status(SUCCESS).results(created).build());
    }

    @GetMapping("/book/{bookId}/user")
    public ResponseEntity<APIResponse<Boolean>> hasReviewed(
            @RequestHeader("Authorization") String token,
            @PathVariable Long bookId) {

        String username = JwtUtils.extractUsername(token);
        boolean exists = reviewService.hasUserReviewed(username, bookId);

        return ResponseEntity.ok(
                APIResponse.<Boolean>builder().status(SUCCESS).results(exists).build());
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<APIResponse<List<ReviewResponse>>> getReviewsForBook(@PathVariable Long bookId) {
        List<ReviewResponse> reviews = reviewService.getAllReviewsForBook(bookId);
        return ResponseEntity.ok(APIResponse.<List<ReviewResponse>>builder().status(SUCCESS).results(reviews).build());
    }

    @GetMapping("/book/{bookId}/paginated")
    public ResponseEntity<APIResponse<Page<ReviewResponse>>> getReviewsForBookPaginated(@PathVariable Long bookId,
                                                                                        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        Page<ReviewResponse> reviewPage = reviewService.getAllReviewsForBookPaginated(bookId, page, size);
        return ResponseEntity
                .ok(APIResponse.<Page<ReviewResponse>>builder().status(SUCCESS).results(reviewPage).build());
    }
}
