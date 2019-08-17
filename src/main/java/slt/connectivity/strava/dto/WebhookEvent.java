package slt.connectivity.strava.dto;

import lombok.*;

import java.util.HashMap;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@SuppressWarnings("all")
public class WebhookEvent {

    String object_type;
    Long object_id;
    String aspect_type;
    Long owner_id;
    Integer subscription_id;
    Long event_time;
    HashMap<String,String> updates;
}
