import {AutocompleteArrayInput, AutocompleteInput} from 'react-admin';
import * as React from "react";

const ImageStatusInput = ({ record = {}, source}) => {
    return (
        <AutocompleteInput source={source} choices={[
            { id: 'WAITING', name: 'WAITING' },
            { id: 'UPLOADED', name: 'UPLOADED' },
            { id: 'ERROR', name: 'ERROR' }
        ]} />
    )
};

export default ImageStatusInput;
