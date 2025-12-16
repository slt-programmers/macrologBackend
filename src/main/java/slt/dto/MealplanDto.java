package slt.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class MealplanDto {

    private Long id;

    private String title;

    @Builder.Default
    private List<MealtimeDto> mealtimes = new ArrayList<>();

}
