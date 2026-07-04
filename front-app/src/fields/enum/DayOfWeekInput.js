import { AutocompleteArrayInput } from 'react-admin';
import * as React from "react";

const DayOfWeekInput = ({ record = {}, source}) => {
    return (
        <AutocompleteArrayInput source={source} choices={[
            { id: 'MONDAY', name: 'Monday' },
            { id: 'TUESDAY', name: 'Tuesday' },
            { id: 'WEDNESDAY', name: 'Wednesday' },
            { id: 'THURSDAY', name: 'Thursday' },
            { id: 'FRIDAY', name: 'Friday' },
            { id: 'SATURDAY', name: 'Saturday' },
            { id: 'SUNDAY', name: 'Sunday' }
        ]} />
    )
};

export default DayOfWeekInput;
