package nl.fontys.s3.ticketwave_s3.Domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GeocodingResponse {
    private List<Result> results;

    @Setter
    @Getter
    public static class Result {
        private Geometry geometry;

    }

    @Setter
    @Getter
    public static class Geometry {
        private Location location;

    }

    @Setter
    @Getter
    public static class Location {
        private double lat;
        private double lng;

    }
}
