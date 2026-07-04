import {AutocompleteInput, RadioButtonGroupInput} from 'react-admin';
import * as React from "react";

const PeriodStatisticTypeInput = ({ record = {}, source}) => {
    return (
        <AutocompleteInput source="periodType"  choices={[
            { id: 'DAY', name: 'DAY' },
            { id: 'WEEK', name: 'WEEK' },
            { id: 'MONTH', name: 'MONTH' }
        ]} />
    )
};

export default PeriodStatisticTypeInput;
