import {CreateButton, EditButton, ListButton, TopToolbar, DeleteButton, FilterButton} from "react-admin";
import ExportButtonMy from "../button/ExportButtonMy";
import ImportButtonMy from "../button/ImportButtonMy";

import * as React from "react";

export const RecordTitle = ({ record }) => {
    return <span>Group {record ? `"${record.name}" (${record.id})` : ''}</span>;
};

export const ListActions = (props) => {
    return (
        <TopToolbar>
            <FilterButton/>
            <CreateButton/>
            <ExportButtonMy basePath={props.basePath} fileType="CSV"/>
            <ImportButtonMy props={props} importUrl="/import" fileType="CSV"/>
        </TopToolbar>
    )
};

export const ShowActions = ({ basePath, data, resource }) => (
    <TopToolbar>
        <CreateButton/>
        <EditButton basePath={basePath} record={data}/>
        <DeleteButton basePath={basePath} record={data} resource={resource} />
        <ListButton/>
    </TopToolbar>
);

export const CreateActions = ({ basePath, data, resource }) => (
    <TopToolbar>
        <DeleteButton basePath={basePath} record={data} resource={resource} />
        <ListButton/>
    </TopToolbar>
);
