package com.hieu.ecommerce.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateReviewRequest {
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;

    @NotNull(message = "rating is required")
    @Min(1)
    @Max(5)
    private Integer rating;

    private List<String> imageUrls;
}
