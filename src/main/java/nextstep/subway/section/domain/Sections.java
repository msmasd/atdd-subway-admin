package nextstep.subway.section.domain;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import nextstep.subway.station.domain.Station;

@Embeddable
public class Sections {

    private static final int LAST_STATION_COUNT = 1;

    @OneToMany(mappedBy = "line", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> values = new ArrayList<>();

    protected Sections() {
    }

    private Sections(List<Section> values) {
        this.values = values;
    }

    public void connect(Section section) {
        Optional<Section> optExistedSection = upBoundSection(section);
        if (optExistedSection.isPresent()) {
            Section existedSection = optExistedSection.get();
            validateAlreadyExistedStations(section);
            validateDistanceWhenConnectInExistedSection(existedSection, section);
            existedSection.updateForConnect(section);
        }
        this.values.add(section);
    }

    public static Sections empty() {
        return new Sections(new ArrayList<>());
    }

    public List<Station> extractStationsApplyOrderingUpStationToDownStation() {
        List<Station> stations = new ArrayList<>();

        final Map<Long, Section> sectionByUpStationId = toMapForSectionByUpStationId();
        Section section = upBoundLastSection();
        while (section != null) {
            stations.add(section.getUpStation());
            section = sectionByUpStationId.get(section.getDownStation().getId());
        }

        stations.add(downBoundLastStation());
        return stations;
    }

    private Map<Long, Section> toMapForSectionByUpStationId() {
        return this.values.stream()
            .collect(toMap(section -> section.getUpStation().getId(), Function.identity()));
    }

    private Section upBoundLastSection() {
        final Station upBoundLastStation = upBoundLastStation();
        return this.values.stream()
            .filter(section -> section.getUpStation().equals(upBoundLastStation))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("상행역이 속하는 구간은 한 개가 있어야 합니다."));
    }

    private Optional<Section> upBoundSection(Section standardSection) {
        return this.values.stream()
            .filter(section -> section.getUpStation().equals(standardSection.getUpStation()))
            .findFirst();
    }

    private Station upBoundLastStation() {
        Set<Station> upStations = getUpStations();
        upStations.removeAll(getDownStations());

        if (upStations.size() != LAST_STATION_COUNT) {
            throw new IllegalStateException("상행 종착역은 한 개가 있어야 합니다.");
        }

        return upStations.stream().findAny().get();
    }

    private Station downBoundLastStation() {
        Set<Station> downStations = getDownStations();
        downStations.removeAll(getUpStations());

        if (downStations.size() != LAST_STATION_COUNT) {
            throw new IllegalStateException("하행 종착역은 한 개가 있어야 합니다.");
        }

        return downStations.stream().findAny().get();
    }

    private Set<Station> getUpStations() {
        return this.values.stream()
            .map(Section::getUpStation)
            .collect(toSet());
    }

    private Set<Station> getDownStations() {
        return this.values.stream()
            .map(Section::getDownStation)
            .collect(toSet());
    }

    private Set<Station> getAllStations() {
        Set<Station> stations = new HashSet<>();
        stations.addAll(getUpStations());
        stations.addAll(getDownStations());
        return stations;
    }

    private void validateDistanceWhenConnectInExistedSection(Section existedSection, Section section) {
        if (existedSection.getDistance().lessThanOrEqual(section.getDistance())) {
            throw new IllegalArgumentException("역 사이에 새로운 역을 등록할 경우 기존 역 사이 길이보다 작아야 합니다");
        }
    }

    private void validateAlreadyExistedStations(Section section) {
        if (isExistedUpStationAndDownStation(section)) {
            throw new IllegalArgumentException("상행역과 하행역이 이미 노선에 모두 등록되어 있습니다");
        }
    }

    private boolean isExistedUpStationAndDownStation(Section section) {
        Set<Station> stations = getAllStations();
        return stations.contains(section.getUpStation())
            && stations.contains(section.getDownStation());
    }

    List<Section> getValues() {
        return values;
    }
}
