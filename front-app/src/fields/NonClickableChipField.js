import * as React from 'react';
import { useRecordContext } from 'react-admin';
import Chip from '@material-ui/core/Chip';
import get from 'lodash/get';

const NonClickableChipField = ({ source, ...rest }) => {
    const record = useRecordContext();
    const value = get(record, source);

    return (
        <Chip
            label={value}
            clickable={false}
            style={{ margin: '4px', cursor: 'inherit' }} // Mimic default ChipField margin
            {...rest}
        />
    );
};

// Add default props for compatibility if needed, although usually not required for simple fields
// NonClickableChipField.defaultProps = {
//     addLabel: true,
// };

export default NonClickableChipField; 