import * as React from "react";
import {
    List,
    Datagrid,
    FunctionField,
    TextInput, TopToolbar, FilterButton
} from 'react-admin';

import PartnerStatusInput from "../fields/enum/PartnerStatusInput";

export const partnerFilters = [
    <TextInput label="ID" source="id"/>,
    <PartnerStatusInput source="partnerStatus"/>
];

export const PartnerActions = (props) => {
    return (
        <TopToolbar>
            <FilterButton/>
        </TopToolbar>
    )
};

export const PartnerList = (props) => (
    <List {...props} sort={{ field: 'createTimestamp', order: 'DESC' }} actions={<PartnerActions/>} filters={partnerFilters} bulkActionButtons={false}>
        <Datagrid>
            <FunctionField source="id" label="ID" render={record => record.id ? `${record.id}` : '-'}/>
            <FunctionField source="userKey" label="User key" render={record => record.userKey ? `${record.userKey}` : '-'}/>
            <FunctionField source="displayName" label="Display name" render={record => record.displayName ? `${record.displayName}` : '-'}/>
            <FunctionField source="email" label="Email" render={record => record.email ? `${record.email}` : '-'}/>
            <FunctionField source="status" label="Status" render={record => record.status ? `${record.status}` : '-'}/>
            <FunctionField source="countUsers" label="Count users" render={record => record.countUsers ? `${record.countUsers}` : '0'}/>
            <FunctionField source="countGroups" label="Count group" render={record => record.countGroups ? `${record.countGroups}` : '0'}/>
            <FunctionField source="partnerLink" label="Partner link" render={record => record.partnerLink ? `${record.partnerLink}` : '-'}/>
        </Datagrid>
    </List>
);