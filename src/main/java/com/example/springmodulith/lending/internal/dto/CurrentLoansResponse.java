package com.example.springmodulith.lending.internal.dto;

import com.example.springmodulith.catalog.BookResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentLoansResponse {

    @NotNull
    private BookResponse book;

    @NotNull
    private int daysLeft;

}
