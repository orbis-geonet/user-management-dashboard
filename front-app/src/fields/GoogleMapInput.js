import {googleKeyMy} from "../tools/serviceUrl";
import React from "react";
import {
    GoogleMap,
    useLoadScript,
    Marker,
    InfoWindow,
} from "@react-google-maps/api";
import usePlacesAutocomplete, {
    getGeocode,
    getLatLng,
} from "use-places-autocomplete";

import "@reach/combobox/styles.css";
import {TextInput} from "react-admin";

const libraries = ["places"];
const mapContainerStyle = {
    height: "300px",
    width: "80%",
};

const GoogleMapInput = ({record, source}) => {
    const { isLoaded, loadError } = useLoadScript({
        googleMapsApiKey: googleKeyMy,
        libraries,
    });

    // console.log(record)

    const latIn = record == null ? 51.5072178 : record.coordinates.lat;
    const lngIn = record == null ? -0.1275862 : record.coordinates.lng;

    const [marker, setMarker] = React.useState(
        {
            lat: latIn,
            lng: lngIn
        });
    const [selected, setSelected] = React.useState()

    const onMapClick = React.useCallback((event) => {
        setMarker(
            {
                lat: event.latLng.lat(),
                lng: event.latLng.lng()
            }
        );
    }, [])

    const mapRef = React.useRef();
    const onMapLoad = React.useCallback((map) => {
        mapRef.current = map;
    }, [])

    const setCoordinates = React.useCallback(({lat, lng}) => {
        setMarker(
            {
                lat: lat,
                lng: lng
            }
        );
    }, [])

    const panTo = React.useCallback(({lat, lng}) => {
        mapRef.current.panTo({lat, lng})
    }, [])

    if (loadError) return "Error";
    if (!isLoaded) return "Loading...";

    return <div key="mapDiv">
        <TextInput lable="Lat" name="coordinatesNew.lat" defaultValue={marker.lat} initialValue={marker.lat} />
        <TextInput lable="Lng" name="coordinatesNew.lng" defaultValue={marker.lng} initialValue={marker.lng} />
        <Search key="search" panTo={panTo} onSearchFinish={setCoordinates}/>
        <GoogleMap
            mapContainerStyle={mapContainerStyle}
            zoom={14}
            center={{lat: marker.lat, lng: marker.lng}}
            onClick={onMapClick}
            onLoad={onMapLoad}
            key="MapInput"
        >
            <Marker
                key={`${marker.lat}-${marker.lng}`}
                position={{ lat: marker.lat, lng: marker.lng }}
                onClick={() => {
                    setSelected(marker)
                }}
            />
            {selected ? (
                <InfoWindow
                    position={{ lat: marker.lat, lng: marker.lng }}
                    onCloseClick={() => {
                        setSelected(null)
                    }}
                    key="InfoWindow"
                >
                    <div key="info-dev">
                        <h3 key="div-h3">Current location</h3>
                        <p key="div-p1">lat: {marker.lat}</p>
                        <p key="div-p2">lng: {marker.lng}</p>
                    </div>
                </InfoWindow>
            ) : null
            }
        </GoogleMap>
    </div>
};

function Search ({panTo, onSearchFinish}) {
    const {
        ready,
        value,
        suggestions: { status, data },
        setValue,
        clearSuggestions,
    } = usePlacesAutocomplete({});

    const handleInput = (e) => {
        setValue(e.target.value);
    };

    const handleSelect =
        ({ description }) =>
            () => {
                setValue(description, false);
                clearSuggestions();

                // Get latitude and longitude via utility functions
                getGeocode({ address: description })
                    .then((results) => getLatLng(results[0]))
                    .then(({ lat, lng }) => {
                        console.log("📍 Coordinates: ", { lat, lng });
                        onSearchFinish({lat, lng})
                        panTo({lat, lng})
                    })
                    .catch((error) => {
                        console.log("😱 Error: ", error);
                    });
            };

    const renderSuggestions = () =>
        data.map((suggestion) => {
            const {
                place_id,
                structured_formatting: { main_text, secondary_text },
            } = suggestion;

            return (
                <li key={place_id} onClick={handleSelect(suggestion)}>
                    <strong key={`${place_id}_strong`}>{main_text}</strong>
                    <small key={`${place_id}_small`}>{secondary_text}</small>
                </li>
            );
        });
    
    return (
        <div key="Search_key">
            <input
                value={value}
                onChange={handleInput}
                disabled={!ready}
                placeholder="What is location?"
                key="search_input_place"
            />
            {status === "OK" && <ul key="ss">{renderSuggestions()}</ul>}
        </div>
    )
}

export default GoogleMapInput;



