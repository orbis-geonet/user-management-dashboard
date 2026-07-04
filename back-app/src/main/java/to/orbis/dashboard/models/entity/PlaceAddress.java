package to.orbis.dashboard.models.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceAddress {
    private String fullAddress;
    private String country;
    private String city;
    private String street;
    private String number;
    private String neighberhood;
    private String postalCode;

    @Override
    public String toString() {
        return "FullAddress: '" + fullAddress + '\'' +
                ", country: '" + country + '\'' +
                ", city: '" + city + '\'' +
                ", street: '" + street + '\'' +
                ", number: '" + number + '\'' +
                ", neighberhood:'" + neighberhood + '\'' +
                ", postalCode:'" + postalCode + '\'';
    }
}
