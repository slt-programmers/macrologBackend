package slt.connectivity.strava.dto;

import lombok.*;

import java.util.HashMap;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class WebhookEvent {

    private String object_type;
    private Long object_id;
    private String aspect_type;
    private Long owner_id;
    private Integer subscription_id;
    private Long event_time;
    private HashMap<String, String> updates;

}
