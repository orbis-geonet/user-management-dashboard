import {googleKeyMy} from "../tools/serviceUrl";
import React from "react";
import {
    GoogleMap,
    useLoadScript,
    Marker,
    InfoWindow,
} from "@react-google-maps/api";

const libraries = ["places"];
const mapContainerStyle = {
    height: "300px",
    width: "80%",
};

const GoogleMapField = ({record, source}) => {

    const latIn = record[source].lat;
    const lngIn = record[source].lng;

    const { isLoaded, loadError } = useLoadScript({
        googleMapsApiKey: googleKeyMy,
        libraries,
    });

    const marker = React.useState(
        {
            lat: latIn,
            lng: lngIn
        });
    const [selected, setSelected] = React.useState()

    const mapRef = React.useRef();
    const onMapLoad = React.useCallback((map) => {
        mapRef.current = map;
    }, [])

    if (loadError) return "Error";
    if (!isLoaded) return "Loading...";

    return <div>
        <GoogleMap
            mapContainerStyle={mapContainerStyle}
            zoom={14}
            center={{ lat: latIn, lng: lngIn }}
            onLoad={onMapLoad}
        >
            <Marker
                position={{ lat: latIn, lng: lngIn }}
                onClick={() => {
                    setSelected(marker)
                }}
            />
            {selected ? (
                <InfoWindow
                    position={{ lat: latIn, lng: lngIn }}
                    onCloseClick={() => {
                        setSelected(null)
                    }}
                >
                    <div>
                        <h3>Current location</h3>
                        <p>lat: {latIn}</p>
                        <p>lng: {lngIn}</p>
                    </div>
                </InfoWindow>
            ) : null
            }
        </GoogleMap>
    </div>
};

export default GoogleMapField;
