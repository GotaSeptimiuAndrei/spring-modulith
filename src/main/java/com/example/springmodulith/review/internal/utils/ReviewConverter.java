package com.example.springmodulith.review.internal.utils;

import com.example.springmodulith.review.internal.dto.ReviewRequest;
import com.example.springmodulith.review.internal.dto.ReviewResponse;
import com.example.springmodulith.review.internal.model.Review;

import java.time.LocalDate;

public class ReviewConverter {
    public static Review convertToEntity(ReviewRequest request, String username) {

        Review review = new Review();
        review.setUsername(username);
        review.setBookId(request.getBookId());
        review.setRating(request.getRating());
        review.setReviewDescription(request.getReviewDescription());
        review.setDate(LocalDate.now());
        return review;
    }

    public static ReviewResponse convertToDto(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getReviewId());
        response.setUsername(review.getUsername());
        response.setBookId(review.getBookId());
        response.setRating(review.getRating());
        response.setReviewDescription(review.getReviewDescription());
        response.setDate(review.getDate());
        return response;
    }
}

