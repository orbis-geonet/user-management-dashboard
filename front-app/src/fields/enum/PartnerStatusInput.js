import {AutocompleteInput} from 'react-admin';
import * as React from "react";

const PartnerStatusInput = ({ record = {}, source}) => {
    return (
        <AutocompleteInput source={source} choices={[
            { id: 'CREATED', name: 'CREATED' },
            { id: 'READY', name: 'READY' }
        ]} />
    )
};

export default PartnerStatusInput;
