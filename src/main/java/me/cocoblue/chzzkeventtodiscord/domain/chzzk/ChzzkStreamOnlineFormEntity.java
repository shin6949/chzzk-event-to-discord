package me.cocoblue.chzzkeventtodiscord.domain.chzzk;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity(name = "chzzk_subscription_stream_online_form")
@SuperBuilder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChzzkStreamOnlineFormEntity extends ChzzkSubscriptionFormEntity {
    @Column(name = "show_detail", nullable = false, length = 1)
    @ColumnDefault("0")
    private boolean showDetail;

    @Column(name = "show_thumbnaill", nullable = false, length = 1)
    @ColumnDefault("1")
    private boolean showThumbnail;
}