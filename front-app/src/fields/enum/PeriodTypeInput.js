import { RadioButtonGroupInput } from 'react-admin';
import * as React from "react";

const PeriodTypeInput = ({ record = {}, source}) => {
    return (
        <RadioButtonGroupInput source={source} choices={[
            { id: 'PERIOD', name: 'Periodically' },
            { id: 'THE_SAME_TIME', name: 'At the same time' }
        ]} />
    )
};

export default PeriodTypeInput;
