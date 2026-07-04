import { AutocompleteInput } from 'react-admin';
import * as React from "react";

const PlaceCategoryInput = ({ record = {}, source}) => {
    return (
        <AutocompleteInput source="type" choices={[
            { id: 'SHOPPING', name: 'SHOPPING' },
            { id: 'PARK', name: 'PARK' },
            { id: 'BAR', name: 'BAR' },
            { id: 'RESTAURANT', name: 'RESTAURANT' },
            { id: 'SCHOOL', name: 'SCHOOL' },
            { id: 'LOCATION', name: 'LOCATION' },
            { id: 'TWO_BUILDINGS', name: 'TWO_BUILDINGS' },
            { id: 'SPORTS_CENTER', name: 'SPORTS_CENTER' },
            { id: 'CASTLE', name: 'CASTLE' },
            { id: 'HOUSE_2', name: 'HOUSE_2' },
            { id: 'MUSIC', name: 'MUSIC' },
            { id: 'FAST_FOOD', name: 'FAST_FOOD' },
            { id: 'HOUSE', name: 'HOUSE' },
            { id: 'BUILDING', name: 'BUILDING' },
            { id: 'BEACH', name: 'BEACH' },
        ]} />
    )
};

export default PlaceCategoryInput;