import * as React from "react";
import {
    List,
    Datagrid,
    FunctionField, DateField
} from 'react-admin';
import LoadExportFileButton from "../button/LoadExportFileButton";


export const ReportList = (props) => (
    <List {...props} sort={{ field: 'timestamp', order: 'DESC' }} exporter={false} bulkActionButtons={false}>
        <Datagrid>
            <FunctionField source="id" label="ID" render={record => record.id ? `${record.id}` : '-'}/>
            <FunctionField source="name" label="Name" render={record => record.name ? `${record.name}` : '-'}/>
            <FunctionField source="status" label="status" render={record => record.status ? `${record.status}` : '-'}/>
            <FunctionField source="errorMessage" label="Error Message" render={record => record.errorMessage ? `${record.errorMessage}` : '-'}/>
            <DateField source="timestamp" showTime={true} label="Created time"/>
            <LoadExportFileButton {...props}/>
        </Datagrid>
    </List>
);
