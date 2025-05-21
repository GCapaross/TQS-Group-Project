package nikev.group.project.chargingplatform.DTOs;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class BookingRequestDTO {
    private Long stationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}