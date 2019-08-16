package slt.connectivity.strava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Slf4j
@Builder
public class SubscriptionInformation {

    Integer id;
    String callback_url;
    DateTime created_at;
    DateTime updated_at;

}
