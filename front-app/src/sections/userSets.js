import * as React from "react";
import {
    List,
    Datagrid,
    Show,
    Create,
    Edit,
    required,
    TabbedShowLayout,
    Tab,
    TabbedForm,
    FormTab,
    ShowButton,
    EditButton,
    BooleanField,
    DateField,
    FunctionField,
    TextInput,
    DateTimeInput,
    ArrayField,
    TextField, useNotify, useRefresh, useRedirect,
    Toolbar, BooleanInput, TopToolbar, FilterButton, CreateButton
} from 'react-admin';

import { ListActions, RecordTitle, ShowActions, CreateActions } from '../tools/actions'
import AddUserToField from "../fields/AddUserToField";
import DelUserFromField from "../fields/DelUserFromField";
import ExportButtonMy from "../button/ExportButtonMy";
import ImportButtonMy from "../button/ImportButtonMy";

export const userSetsFilters = [
    <TextInput label="id" source="id"/>,
    <TextInput label="name" source="name"/>,
    <TextInput label="Upload file name" source="uploadFileName"/>
];

const SetActions = (props) => {
    return (
        <TopToolbar>
            <ExportButtonMy basePath={props.basePath} fileType="JSON"/>
            <ImportButtonMy props={props} importUrl="/import" fileType="JSON"/>
            <FilterButton/>
            <CreateButton/>
        </TopToolbar>
    )
};

export const UserSetsList = (props) => (
    <List {...props} sort={{ field: 'timestamp', order: 'DESC' }}
          actions={<SetActions/>}
          filters={userSetsFilters}
          bulkActionButtons={false}
    >
        <Datagrid>
            <FunctionField source="id" label="ID" render={record => record.id ? `${record.id}` : '-'}/>
            <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
            <BooleanField source="deleted"/>
            <FunctionField source="numberMembers" render={record => record.numberMembers ? `${record.numberMembers}` : '-'}/>
            <DateField source="timestamp" label="Updated time"/>
            <FunctionField source="uploadFileName" label="Upload file name" render={record => record.uploadFileName ? `${record.uploadFileName}` : '-'}/>
            <ShowButton label=""/>
            <EditButton label=""/>
        </Datagrid>
    </List>
);

export const UserSetsShow = (props) => {
    return (
        <Show {...props} title={<RecordTitle/>} actions={<ShowActions/>}>
            <TabbedShowLayout>
                <Tab label="Common information">
                    <FunctionField source="id" render={record => record.id ? `${record.id}` : '-'}/>
                    <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
                    <FunctionField source="description" render={record => record.description ? `${record.description}` : '-'}/>
                    <FunctionField source="numberMembers" render={record => record.numberMembers ? `${record.numberMembers}` : '-'}/>
                    <DateField showTime={true} source="timestamp" label="Update time"/>
                    <BooleanField source="deleted"/>
                    <FunctionField source="uploadFileName" label="Upload file name" render={record => record.uploadFileName ? `${record.uploadFileName}` : '-'}/>
                </Tab>
                <Tab label="Users">
                    <ArrayField source="users">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                        </Datagrid>
                    </ArrayField>
                </Tab>
            </TabbedShowLayout>
        </Show>
    )
};

export const UserSetsCreate = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();

    const onSuccess = () => {
        notify(`A new set saved`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Create {...props} actions={<CreateActions/>} onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false} toolbar={<Toolbar alwaysEnableSaveButton />}>
                <FormTab label="Common information">
                    <TextInput source="name" validate={required()}/>
                    <TextInput source="description" />
                </FormTab>
            </TabbedForm>
        </Create>
    );
}

export const UserSetsEdit = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();

    const onSuccess = () => {
        notify(`A set was edited`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Edit {...props} title={<RecordTitle/>} mutationMode="pessimistic" actions={<CreateActions/>} onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false} toolbar={<Toolbar alwaysEnableSaveButton />}>
                <FormTab label="Common information">
                    <TextInput disabled source="id"/>
                    <TextInput source="name"/>
                    <TextInput source="description"/>
                    <TextInput disabled source="numberMembers"/>
                    <BooleanInput source="deleted"/>
                    <DateTimeInput showTime={true} source="timestamp" label="Update time"/>
                </FormTab>
                <FormTab label="Users">
                    <ArrayField source="users">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                            <DelUserFromField goalType="set" goal="sets" entityName="users" goalId={props.id} source="userKey"/>
                        </Datagrid>
                    </ArrayField>
                    <AddUserToField props={props} userType="set" goalId={props.id} goal="sets" sourceType="users" source="userKey"/>
                </FormTab>
            </TabbedForm>
        </Edit>
    );
}
