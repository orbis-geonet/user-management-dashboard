import { RadioButtonGroupInput } from 'react-admin';
import * as React from "react";

const PlaceStrategyTypeInput = ({ record = {}, source}) => {
    return (
        <RadioButtonGroupInput source={source} defaultValue="ROUND_ROBIN_PICK" choices={[
            { id: 'RANDOM_PICK', name: 'Random pick strategy' },
            { id: 'ROUND_ROBIN_PICK', name: 'Round-robin pick strategy' }
        ]} />
    )
};

export default PlaceStrategyTypeInput;
