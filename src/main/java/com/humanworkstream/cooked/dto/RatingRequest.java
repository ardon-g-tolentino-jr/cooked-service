package com.humanworkstream.cooked.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RatingRequest(
        @NotNull @Min(1) @Max(5) Integer stars
) {
}
