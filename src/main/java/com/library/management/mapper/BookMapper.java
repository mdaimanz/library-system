package com.library.management.mapper;

import com.library.management.api.dto.BookResponse;
import com.library.management.domain.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    BookResponse toBookResponse(Book Book);
}
