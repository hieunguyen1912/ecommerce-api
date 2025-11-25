package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.request.CreateCategoryRequest;
import com.hieu.ecommerce.model.dto.request.UpdateCategoryRequest;
import com.hieu.ecommerce.model.dto.response.CategoryResponse;
import com.hieu.ecommerce.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    Category toEntity(CreateCategoryRequest createCategoryRequest);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    CategoryResponse toResponse(Category category);

    void update(UpdateCategoryRequest updateCategoryRequest, @MappingTarget Category category);
}
