import * as React from "react";
import {
    List,
    Datagrid,
    ShowButton,
    BooleanField,
    DateField,
    ChipField,
    FunctionField,
    TopToolbar,
    FilterButton,
} from 'react-admin';
import FlaggedEntityTypeInput from "../fields/enum/FlaggedEntityTypeInput";
import DeleteFlaggedEntityButton from "../button/DeleteFlaggedEntityButton";
import SeeFlaggedEntityButton from "../button/SeeFlaggedEntityButton";
import SolvedFlaggedEntityButton from "../button/SolvedFlaggedEntityButton";

export const reportedFilters = [
    <FlaggedEntityTypeInput source="type"/>
];

const FlaggedActions = (props) => {
    return (
        <TopToolbar>
            <FilterButton/>
        </TopToolbar>
    )
};

export const FlaggedList = props => (
    <List {...props} sort={{ field: 'reportedTime', order: 'DESC' }} actions={<FlaggedActions/>} filters={reportedFilters} bulkActionButtons={false}>
        <Datagrid>
            <FunctionField label="id" source="id" render={record => record.id ? `${record.id}` : '-'}/>
            <ChipField label="Type" source="type"/>
            <FunctionField label="Title" source="name" render={record => record.name ? `${record.name}` : '-'}/>
            <FunctionField label="Details" source="details" render={record => record.details ? `${record.details}` : '-'}/>
            <BooleanField label="Deleted" source="deleted"/>
            <BooleanField label="Reported Solved" source="reportedSolved"/>
            <DateField source="reportedTime" label="Reported time"/>
            {/*<DateField source="createTimestamp" label="Created time"/>*/}
            {/*<DateField source="timestamp" label="Last update time"/>*/}
            <SeeFlaggedEntityButton {...props}/>
            <DeleteFlaggedEntityButton {...props}/>
            <SolvedFlaggedEntityButton {...props}/>
        </Datagrid>
    </List>
);
