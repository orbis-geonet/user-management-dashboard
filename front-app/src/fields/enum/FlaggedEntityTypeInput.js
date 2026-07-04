import {AutocompleteInput} from 'react-admin';
import * as React from "react";

const FlaggedEntityTypeInput = ({ record = {}, source}) => {
    return (
        <AutocompleteInput source="type" choices={[
            { id: 'POST', name: 'POST' },
            { id: 'GROUP', name: 'GROUP' },
            { id: 'USER', name: 'USER' },
            { id: 'PLACE', name: 'PLACE' }
        ]} />
    )
};

export default FlaggedEntityTypeInput;
