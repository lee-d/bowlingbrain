package de.europace.bowlingbrain.game;

import de.europace.bowlingbrain.api.dto.GameDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GameMapper {

    GameDto toDto(Game game);
}
