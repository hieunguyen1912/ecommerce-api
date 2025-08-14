package com.hieu.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(groups = OnCreate.class, message = "Name is required")
    @Size(max = 100, groups = {OnCreate.class, OnUpdate.class}, message = "Description must be less than 255 characters")
    private String name;

    private String description;
    private Long parentId;

    public interface OnCreate {}
    public interface OnUpdate {}
}
