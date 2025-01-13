package nl.fontys.s3.ticketwave_s3.Service;

import nl.fontys.s3.ticketwave_s3.Domain.GeocodingResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeocodingService {

    private static final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String API_KEY = "AIzaSyDVQ7Ia5SxOM0A_zJzyWzcvKF7qI0M80qQ";

    public double[] getCoordinates(String location) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format("%s?address=%s&key=%s",
                    GEOCODING_API_URL, location.replace(" ", "+"), API_KEY);

            GeocodingResponse response = restTemplate.getForObject(url, GeocodingResponse.class);
            System.out.println("Geocoding API URL: " + url);

            if (response != null && !response.getResults().isEmpty()) {
                // Extract the first result's location
                double lat = response.getResults().get(0).getGeometry().getLocation().getLat();
                double lng = response.getResults().get(0).getGeometry().getLocation().getLng();
                System.out.println("Parsed Coordinates: lat=" + lat + ", lng=" + lng);
                return new double[]{lat, lng};
            } else {
                System.err.println("Geocoding API returned no results for location: " + location);
                throw new RuntimeException("Location not found: " + location);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch coordinates for location: " + location);
            throw new RuntimeException("Failed to fetch coordinates for location: " + location, e);
        }
    }

}
