import { RadioButtonGroupInput } from 'react-admin';
import * as React from "react";

const TheSameTimeTypeInput = ({ record = {}, source}) => {
    return (
        <RadioButtonGroupInput source={source} choices={[
            { id: 'DAY_OF_MONTH', name: 'Day of month' },
            { id: 'DAY_OF_WEEK', name: 'Day of week' }
        ]} />
    )
};

export default TheSameTimeTypeInput;
