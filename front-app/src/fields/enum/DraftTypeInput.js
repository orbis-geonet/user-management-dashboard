import {AutocompleteInput, RadioButtonGroupInput} from 'react-admin';
import * as React from "react";

const DraftTypeInput = ({ record = {}, source}) => {
    return (
        <AutocompleteInput source="emailNumber" choices={[
            { id: '1', name: '1' },
            { id: '2', name: '2' }
        ]} />
    )
};

export default DraftTypeInput;
