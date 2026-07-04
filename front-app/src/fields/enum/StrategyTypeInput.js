import { RadioButtonGroupInput } from 'react-admin';
import * as React from "react";

const StrategyTypeInput = ({ record = {}, source}) => {
    return (
        <RadioButtonGroupInput source={source} choices={[
            { id: 'RANDOM_PICK', name: 'Random pick strategy' },
            { id: 'ROUND_ROBIN_PICK', name: 'Round-robin pick strategy' },
            { id: 'LOCATION_PICK', name: 'Location pick strategy' }
        ]} />
    )
};

export default StrategyTypeInput;
