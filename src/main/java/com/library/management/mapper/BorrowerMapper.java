package com.library.management.mapper;

import com.library.management.api.dto.BorrowerResponse;
import com.library.management.domain.model.Borrower;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BorrowerMapper {
    BorrowerResponse toBorrowerResponse(Borrower borrower);
}
