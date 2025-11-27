package slt.dto;


import java.util.ArrayList;
import java.util.List;

public class MealplanDto {

    private Long id;

    private String title;

    private List<MealtimeDto> mealtimes = new ArrayList<>();

}
