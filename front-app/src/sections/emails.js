import * as React from "react";
import {
    List,
    Datagrid,
    DateInput,
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
    BooleanInput,
    UrlField,
    ArrayField,
    useNotify, useRefresh, useRedirect,
    Toolbar, SingleFieldList, ChipField, DeleteButton, SimpleFormIterator, ArrayInput, Show, TextField
} from 'react-admin';

import { ListActions, RecordTitle, ShowActions, CreateActions } from '../tools/actions'
import ResendLastEmailButton from "../button/ResendLastEmailButton";
import DelTagFromCampaign from "../fields/DelTagFromCampaign";
import AddTagToCampaign from "../fields/AddTagToCampaign";

export const emailsFilters = [
    <TextInput label="id" source="id"/>,
    <TextInput label="Name" source="name"/>,
    <TextInput label="Company name" source="companyName"/>,
    <TextInput label="Email" source="mail"/>,
    <TextInput label="Tag" source="tag"/>,
    <BooleanInput label="Mail was open" source="open"/>,
    <BooleanInput label="Has phone" source="hasPhone"/>,
    <DateInput label="Next call date" source="nextCallDate"/>
];

export const EmailsList = (props) => (
    <List {...props} sort={{ field: 'cratedTime', order: 'DESC' }} actions={<ListActions/>} filters={emailsFilters} bulkActionButtons={false}>
        <Datagrid>
            <FunctionField source="id" label="ID" render={record => record.id ? `${record.id}` : '-'}/>
            <FunctionField source="name" label="Name" render={record => record.name ? `${record.name}` : '-'}/>
            <FunctionField source="companyName" label="Company name" render={record => record.companyName ? `${record.companyName}` : '-'}/>
            <FunctionField source="mail" label="Mail" render={record => record.mail ? `${record.mail}` : '-'}/>
            <FunctionField source="phoneNumber" label="Phone" render={record => record.phoneNumber ? `${record.phoneNumber}` : '-'}/>
            <DateField source="cratedTime" label="Created time"/>
            <DateField source="nextCallDate" label="Next call date"/>
            <DateField source="lastOpenEmailTime" label="Last open email"/>
            <ArrayField source="tags">
                <SingleFieldList>
                    <ChipField clickable="false" source="text"/>
                </SingleFieldList>
            </ArrayField>
            <BooleanField source="unsubscribed"/>
            <FunctionField render={record => <ResendLastEmailButton recipientId={record.id}/> }/>
            <ShowButton label=""/>
            <EditButton label=""/>
            <DeleteButton label=""/>
        </Datagrid>
    </List>
);

export const EmailShow = (props) => {
    return (
        <Show {...props} title={<RecordTitle/>} actions={<ShowActions/>}>
            <TabbedShowLayout>
                <Tab label="Common information">
                    <FunctionField source="id" render={record => record.id ? `${record.id}` : '-'}/>
                    <FunctionField source="emailKey" render={record => record.emailKey ? `${record.emailKey}` : '-'}/>
                    <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
                    <FunctionField source="companyName" render={record => record.companyName ? `${record.companyName}` : '-'}/>
                    <FunctionField source="mail" render={record => record.mail ? `${record.mail}` : '-'}/>
                    <FunctionField source="phoneNumber" render={record => record.phoneNumber ? `${record.phoneNumber}` : '-'}/>
                    <UrlField source="webSite" render={record => record.webSite ? `${record.webSite}` : '-'}/>
                    <DateField showTime={true} source="cratedTime" label="Created time"/>
                    <DateField showTime={true} source="lastOpenEmailTime" label="Last open email time"/>
                    <BooleanField source="unsubscribed"/>
                </Tab>
                <Tab label="Tag">
                    <ArrayField source="tags">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="tagName"/>
                            <DelTagFromCampaign goalId={props.id} goalName="emails" source="id"/>
                        </Datagrid>
                    </ArrayField>
                </Tab>
                <Tab label="Comments">
                    <FunctionField source="comment" render={record => record.comment ? `${record.comment}` : '-'}/>
                    <DateField source="nextCallDate" label="Next call date"/>
                    <DateField source="lastCallDate" label="Last call date"/>
                </Tab>
            </TabbedShowLayout>
        </Show>
    )
};

export const EmailCreate = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();

    const onSuccess = () => {
        notify(`A new email saved`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Create {...props} actions={<CreateActions/>} mutationMode="pessimistic" onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false}>
                <FormTab label="Common information">
                    <TextInput source="name"/>
                    <TextInput source="companyName"/>
                    <TextInput source="mail" validate={required()}/>
                    <TextInput source="phoneNumber"/>
                    <TextInput source="webSite"/>
                </FormTab>
                <FormTab label="Comments">
                    <TextInput source="comment" multiline fullWidth/>
                    <DateInput source="nextCallDate" label="Next call date"/>
                    <DateInput source="lastCallDate" label="Last call date"/>
                </FormTab>
            </TabbedForm>
        </Create>
    );
}

export const EmailEdit = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();

    const onSuccess = ({ data }) => {
        console.log(data)
        notify(`A email was edited`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Edit {...props} title={<RecordTitle/>} mutationMode="pessimistic" actions={<CreateActions/>} onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false} toolbar={<Toolbar alwaysEnableSaveButton />}>
                <FormTab label="Common information">
                    <TextInput disabled source="id"/>
                    <TextInput disabled source="emailKey"/>
                    <TextInput source="name"/>
                    <TextInput source="companyName"/>
                    <TextInput source="mail" validate={required()}/>
                    <TextInput source="phoneNumber"/>
                    <TextInput source="webSite"/>
                    <DateTimeInput disabled showTime={true} source="cratedTime" label="Created time"/>
                    <DateTimeInput disabled showTime={true} source="lastOpenEmailTime" label="Last open email time"/>
                    <BooleanInput source="unsubscribed"/>
                </FormTab>
                <FormTab label="Tag">
                    <ArrayField source="tags">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="tagName"/>
                            <DelTagFromCampaign goalId={props.id} goalName="emails" source="id"/>
                        </Datagrid>
                    </ArrayField>
                    <AddTagToCampaign props={props} goalPath="emailCampaignTags" goalName="emails" goalId={props.id} source="id"/>
                </FormTab>
                <FormTab label="Comments">
                    <TextInput source="comment" multiline fullWidth/>
                    <DateInput source="nextCallDate" label="Next call date"/>
                    <DateInput source="lastCallDate" label="Last call date"/>
                </FormTab>
            </TabbedForm>
        </Edit>
    );
}
