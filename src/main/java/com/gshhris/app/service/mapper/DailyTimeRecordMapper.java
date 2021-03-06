package com.gshhris.app.service.mapper;

import com.gshhris.app.domain.DailyTimeRecord;
import com.gshhris.app.service.dto.DailyTimeRecordDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DailyTimeRecord} and its DTO {@link DailyTimeRecordDTO}.
 */
@Mapper(componentModel = "spring", uses = { EmployeeMapper.class })
public interface DailyTimeRecordMapper extends EntityMapper<DailyTimeRecordDTO, DailyTimeRecord> {
    @Mapping(target = "employee", source = "employee", qualifiedByName = "employeeId")
    DailyTimeRecordDTO toDto(DailyTimeRecord s);
}
