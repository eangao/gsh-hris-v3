package com.gshhris.app.service.mapper;

import com.gshhris.app.domain.Designation;
import com.gshhris.app.service.dto.DesignationDTO;
import java.util.Set;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Designation} and its DTO {@link DesignationDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface DesignationMapper extends EntityMapper<DesignationDTO, Designation> {
    @Named("nameSet")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    Set<DesignationDTO> toDtoNameSet(Set<Designation> designation);
}
