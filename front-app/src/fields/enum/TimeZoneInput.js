import { AutocompleteInput } from 'react-admin';
import * as React from "react";

const TimeZoneInput = ({ record = {}, source}) => {
    return (
        <AutocompleteInput emptyText="00:00" source={source} choices={[
            { id: '-11:00', name: '-11:00' },
            { id: '-10:00', name: '-10:00' },
            { id: '-09:00', name: '-09:00' },
            { id: '-08:00', name: '-08:00' },
            { id: '-07:00', name: '-07:00' },
            { id: '-06:00', name: '-06:00' },
            { id: '-05:00', name: '-05:00' },
            { id: '-04:00', name: '-04:00' },
            { id: '-03:00', name: '-03:00' },
            { id: '-02:00', name: '-02:00' },
            { id: '-01:00', name: '-01:00' },
            { id: '00:00', name: '00:00' },
            { id: '+01:00', name: '+01:00' },
            { id: '+02:00', name: '+02:00' },
            { id: '+03:00', name: '+03:00' },
            { id: '+04:00', name: '+04:00' },
            { id: '+05:00', name: '+05:00' },
            { id: '+06:00', name: '+06:00' },
            { id: '+07:00', name: '+07:00' },
            { id: '+08:00', name: '+08:00' },
            { id: '+09:00', name: '+09:00' },
            { id: '+10:00', name: '+10:00' },
            { id: '+11:00', name: '+11:00' },
            { id: '+12:00', name: '+12:00' },
        ]} />
    )
};

export default TimeZoneInput;
