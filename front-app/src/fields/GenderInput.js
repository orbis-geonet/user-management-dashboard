import { AutocompleteInput } from 'react-admin';
import * as React from "react";

const GenderInput = ({ record = {}, source}) => {
    return (
        <AutocompleteInput source="gender" choices={[
            { id: 'FEMALE', name: 'FEMALE' },
            { id: 'MALE', name: 'MALE' }
        ]} />
    )
};

export default GenderInput;
