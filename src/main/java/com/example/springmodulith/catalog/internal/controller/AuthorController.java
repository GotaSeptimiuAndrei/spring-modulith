package com.example.springmodulith.catalog.internal.controller;

import com.example.springmodulith.catalog.AuthorService;
import com.example.springmodulith.catalog.internal.dto.AuthorResponse;
import com.example.springmodulith.common.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Slf4j
public class AuthorController {

    public static final String SUCCESS = "success";

    public static final String ERROR = "error";

    private final AuthorService authorService;

    @GetMapping("/paginated")
    public ResponseEntity<APIResponse<Page<AuthorResponse>>> getAllAuthorsPaginated(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {

        Page<AuthorResponse> authorsPage = authorService.getAllAuthorsPaginated(page, size);
        return ResponseEntity
                .ok(APIResponse.<Page<AuthorResponse>>builder().status(SUCCESS).results(authorsPage).build());
    }

    @GetMapping("/search")
    public ResponseEntity<APIResponse<Page<AuthorResponse>>> searchAuthors(@RequestParam String query,
                                                                           @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {

        Page<AuthorResponse> authorsPage = authorService.searchAuthorsByName(query, page, size);
        return ResponseEntity
                .ok(APIResponse.<Page<AuthorResponse>>builder().status(SUCCESS).results(authorsPage).build());
    }

    @GetMapping("/{fullName}")
    public ResponseEntity<APIResponse<AuthorResponse>> getAuthorByFullName(@PathVariable String fullName) {

        AuthorResponse author = authorService.getAuthorByFullName(fullName);
        return ResponseEntity.ok(APIResponse.<AuthorResponse>builder().status(SUCCESS).results(author).build());
    }
}
