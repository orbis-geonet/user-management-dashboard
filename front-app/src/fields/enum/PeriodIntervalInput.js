import { RadioButtonGroupInput } from 'react-admin';
import * as React from "react";

const PeriodIntervalInput = ({ record = {}, source}) => {
    return (
        <RadioButtonGroupInput source={source} choices={[
            { id: 'MINUTE', name: 'Minutes' },
            { id: 'HOUR', name: 'Hours' },
            { id: 'DAY', name: 'Days' },
            { id: 'WEEK', name: 'Weeks' },
            { id: 'MONTH', name: 'Months' },
            { id: 'YEAR', name: 'Years' }
        ]} />
    )
};

export default PeriodIntervalInput;
