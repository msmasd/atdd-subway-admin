package nextstep.subway.line.dto;

import nextstep.subway.line.domain.Line;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponses;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LineResponse {

    private Long id;
    private String name;
    private String color;
    private StationResponses stations;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public LineResponse() {
    }

    public LineResponse(Long id, String name, String color, StationResponses stations, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.stations = stations;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    public static LineResponse of(Line line) {
        List<Station> stations = new ArrayList<>();
        line.getSections().getSections()
                .stream()
                .forEach(section -> {
                    stations.add(section.getUpStation());
                    stations.add(section.getDownStation());
                });
        return new LineResponse(line.getId(), line.getName(), line.getColor(), new StationResponses(stations), line.getCreatedDate(), line.getModifiedDate());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public StationResponses getStations() {
        return stations;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LineResponse) || o == null || getClass() != o.getClass()) {
            return false;
        }

        LineResponse that = (LineResponse) o;
        if (this.id.equals(that.getId()) && this.id == that.getId() && this.name.equals(that.getName())) {
            return true;
        }

        return false;
    }

}
