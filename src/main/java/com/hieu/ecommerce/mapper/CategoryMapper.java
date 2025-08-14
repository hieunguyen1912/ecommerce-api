package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.request.CategoryRequest;
import com.hieu.ecommerce.model.dto.response.CategoryResponse;
import com.hieu.ecommerce.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parent", expression = "java(createCategoryRequest.getParentId() != null ? new Category(createCategoryRequest.getParentId()) : null)")
    Category toEntity(CategoryRequest createCategoryRequest);

    @Mapping(target = "parentId", expression = "java(category.getParent() != null ? category.getParent().getId() : null)")
    @Mapping(target = "parentName", expression = "java(category.getParent() != null ? category.getParent().getName() : null)")
    CategoryResponse toResponse(Category category);
}
