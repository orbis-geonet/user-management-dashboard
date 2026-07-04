package to.orbis.dashboard.utils.mappers;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import to.orbis.dashboard.models.dto.PointDto;

public class PointMapper {

    public static GeoJsonPoint toGeoJsonPoint(PointDto point) {
        if (point == null || point.getLat() == null || point.getLng() == null) {
            return null;
        }
        return new GeoJsonPoint(point.getLng(), point.getLat());
    }

    public static PointDto toPointDto(GeoJsonPoint point) {
        if (point == null) {
            return null;
        }

        return new PointDto(point.getX(), point.getY());
    }
}
