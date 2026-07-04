import {AutocompleteInput, RadioButtonGroupInput} from 'react-admin';
import * as React from "react";

const PostTypeInput = ({ record = {}, source}) => {
    return (
        <AutocompleteInput source="type" choices={[
            { id: 'TEXT', name: 'TEXT' },
            { id: 'IMAGE', name: 'IMAGE' },
            { id: 'AUDIO', name: 'AUDIO' },
            { id: 'CHECK_IN', name: 'CHECK_IN' },
            { id: 'EVENT', name: 'EVENT' },
            { id: 'VIDEO', name: 'VIDEO' },
            { id: 'ALL', name: 'ALL' }
        ]} />
    )
};

export default PostTypeInput;
