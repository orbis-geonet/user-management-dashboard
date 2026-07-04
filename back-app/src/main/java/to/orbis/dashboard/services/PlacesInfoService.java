package to.orbis.dashboard.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import to.orbis.dashboard.config.property.OpenMapConfiguration;
import to.orbis.dashboard.models.dto.openstreetmap.OpenMapPlace;
import to.orbis.dashboard.models.dto.openstreetmap.PlaceInfoDto;
import to.orbis.dashboard.utils.mappers.OpenMapMapper;

import java.io.IOException;
import java.net.URI;

@Service
@RequiredArgsConstructor
public class PlacesInfoService {
    private final OpenMapConfiguration openMapConfiguration;
    private final CloseableHttpClient client;
    private final OpenMapMapper openMapMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public String findCityByCoordinates(GeoJsonPoint coordinates) {
        PlaceInfoDto placeInfoDto = findPlace(coordinates.getY(), coordinates.getX());

        if (placeInfoDto.getCity() != null) {
            return placeInfoDto.getCity();
        } else if (placeInfoDto.getCounty() != null) {
            return placeInfoDto.getCounty();
        } else {
            return placeInfoDto.getCountry();
        }
    }

    public String findCityByCoordinates(Point coordinates) {
        PlaceInfoDto placeInfoDto = findPlace(coordinates.getY(), coordinates.getX());

        if (placeInfoDto.getCity() != null) {
            return placeInfoDto.getCity();
        } else if (placeInfoDto.getCounty() != null) {
            return placeInfoDto.getCounty();
        } else {
            return placeInfoDto.getCountry();
        }
    }

    @SneakyThrows
    public PlaceInfoDto findPlace(Double latitude, Double longitude) {
        URI url = UriComponentsBuilder.fromHttpUrl(openMapConfiguration.getSearchUrl())
                .queryParam("format", "jsonv2")
                .queryParam("zoom", 14)
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .build()
                .toUri();
        HttpGet httpGet = new HttpGet(url);

        try (CloseableHttpResponse response = client.execute(httpGet)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());

            OpenMapPlace place = objectMapper.readValue(jsonResponse, OpenMapPlace.class);
            return openMapMapper.toPlaceInfo(place);
        } catch (IOException e) {
            // Log and handle the exception as needed
            throw new RuntimeException("Error fetching place info", e);
        }
    }
}
