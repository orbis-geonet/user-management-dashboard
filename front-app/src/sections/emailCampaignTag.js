import * as React from "react";
import {
    List,
    Datagrid,
    Create,
    Edit,
    TabbedForm,
    FormTab,
    EditButton,
    FunctionField,
    TextInput,
    useNotify,
    useRefresh,
    useRedirect,
    Toolbar,
    TopToolbar,
    FilterButton,
    CreateButton,
} from 'react-admin';

import { CreateActions } from '../tools/actions'
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
    div: {
        width: '100%'
    },
});

export const campaignTagsFilters = [
    <TextInput label="id" source="id"/>,
    <TextInput label="name" source="name"/>
];

export const EmailCampaignTagTitle = ({ record }) => {
    return <span>Email campaign tag {record ? `"${record.name}" (${record.id})` : ''}</span>;
};

const EmailCampaignTagActions = (props) => {
    return (
        <TopToolbar>
            <FilterButton/>
            <CreateButton/>
        </TopToolbar>
    )
};

export const EmailCampaignTagsList = (props) => (
    <List {...props} sort={{ field: 'cratedTime', order: 'DESC' }} actions={<EmailCampaignTagActions/>} filters={campaignTagsFilters} bulkActionButtons={false}>
        <Datagrid>
            <FunctionField source="id" label="ID" render={record => record.id ? `${record.id}` : '-'}/>
            <FunctionField source="name" label="Name" render={record => record.name ? `${record.name}` : '-'}/>
            <EditButton label=""/>
        </Datagrid>
    </List>
);

export const EmailCampaignTagsCreate = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();
    const classes = useStyles();

    const onSuccess = () => {
        notify(`A new email campaign saved`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Create {...props} actions={<CreateActions/>} mutationMode="pessimistic" onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false}>
                <FormTab label="Common information">
                    <TextInput source="name"/>
                </FormTab>
            </TabbedForm>
        </Create>
    );
}

export const EmailCampaignTagsEdit = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();
    const classes = useStyles();

    const onSuccess = ({ data }) => {
        console.log(data)
        notify(`A email campaign was edited`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Edit {...props} title={<EmailCampaignTagTitle/>} mutationMode="pessimistic" actions={<CreateActions/>} onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false} toolbar={<Toolbar alwaysEnableSaveButton />}>
                <FormTab label="Common information">
                    <TextInput disabled source="id"/>
                    <TextInput source="name"/>
                </FormTab>
            </TabbedForm>
        </Edit>
    );
}
