package to.orbis.dashboard.services;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.NearbySearchRequest;
import com.google.maps.model.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.models.entity.PlaceAddress;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.maps.model.AddressType.ROUTE;
import static com.google.maps.model.AddressType.STREET_ADDRESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlaceService {
    private final GeoApiContext geoApiContext;

    public PlaceAddress getAddress(GeoJsonPoint coordinates) {
        return getPlaceAddress(coordinates);
    }

    @SneakyThrows
    private PlaceAddress getPlaceAddress(GeoJsonPoint coordinates) {
        try {
            return Arrays.stream(GeocodingApi.newRequest(geoApiContext).latlng(new LatLng(coordinates.getY(), coordinates.getX())).await())
                    .filter(it -> Arrays.asList(it.types).contains(STREET_ADDRESS) || Arrays.asList(it.types).contains(ROUTE))
                    .findFirst()
                    .map(googlePlace -> PlaceAddress.builder()
                            .country(getAddressPart(googlePlace.addressComponents, List.of(AddressComponentType.COUNTRY)))
                            .city(getAddressPart(googlePlace.addressComponents, List.of(AddressComponentType.LOCALITY, AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2)))
                            .street(getAddressPart(googlePlace.addressComponents, List.of(AddressComponentType.STREET_ADDRESS, AddressComponentType.ROUTE)))
                            .neighberhood(getAddressPart(googlePlace.addressComponents, List.of(AddressComponentType.SUBLOCALITY_LEVEL_1, AddressComponentType.SUBLOCALITY_LEVEL_2, AddressComponentType.SUBLOCALITY_LEVEL_3, AddressComponentType.NEIGHBORHOOD)))
                            .number(getAddressPart(googlePlace.addressComponents, List.of(AddressComponentType.STREET_NUMBER)))
                            .postalCode(getAddressPart(googlePlace.addressComponents, List.of(AddressComponentType.POSTAL_CODE)))
                            .fullAddress(googlePlace.formattedAddress)
                            .build())
                    .orElse(null);
        } catch (Throwable e) {
            return null;
        }
    }

    private String getAddressPart(AddressComponent[] components, List<AddressComponentType> types) {
        return Arrays.stream(components)
                .filter(component -> Arrays.stream(component.types).anyMatch(types::contains))
                .map(component -> component.longName)
                .findFirst()
                .orElse("");
    }

    private String getPlace(GeoJsonPoint coordinates) {
        var sublocalities = getPlaces(coordinates, "sublocality");

        if (sublocalities.isEmpty()) {
            var localities = getPlaces(coordinates, "locality");
            if (localities.isEmpty()) {
                return null;
            } else {
                return localities.get(0).placeId;
            }
        } else {
            return sublocalities.get(0).placeId;
        }
    }

    @SneakyThrows
    private List<PlacesSearchResult> getPlaces(GeoJsonPoint coordinates, String type) {
        var query = new NearbySearchRequest(geoApiContext).location(new LatLng(coordinates.getY(), coordinates.getX()))
                .radius(200).rankby(RankBy.PROMINENCE);
        var result = query.await();
        return Arrays.stream(result.results)
                .filter(gPlace -> !Arrays.asList(gPlace.types).contains("political"))
                .collect(Collectors.toList());
    }
}
