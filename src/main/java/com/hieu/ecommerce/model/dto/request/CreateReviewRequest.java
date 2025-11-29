package com.hieu.ecommerce.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {
    @NotNull(message = "Product is required")
    private Long productId;

    @NotNull(message = "Order Item is required")
    private Long orderItemId;

    @NotNull(message = "rating is required")
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;

    private List<String> imageUrls;
}
