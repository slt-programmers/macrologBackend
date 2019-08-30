package slt.connectivity.strava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@SuppressWarnings("all")
public class SubscriptionInformation {

    Integer id;
    String callback_url;
    DateTime created_at;
    DateTime updated_at;

}
