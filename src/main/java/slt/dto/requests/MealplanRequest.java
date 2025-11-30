package slt.dto.requests;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MealplanRequest {

    private Long id;

    private String title;

    private List<MealtimeRequest> mealtimes;

}