package com.hieu.ecommerce.model.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @Size(max = 1000, message = "Mô tả sản phẩm không được vượt quá 1000 ký tự")
    private String description;

    // Giá chỉ dùng khi sản phẩm không có variants
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;

    // Stock chỉ dùng khi sản phẩm không có variants
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;

    private Boolean hasVariants;

    // Danh sách ID ảnh muốn giữ lại
    private List<Long> keepImageIds;

    // Danh sách URL ảnh mới muốn thêm
    @Valid
    private List<UpdateProductImageRequest> newImages;

    private List<Long> categoryIds;

    // Danh sách variants (chỉ khi hasVariants = true)
    @Valid
    private List<UpdateProductVariantRequest> variants;
}
