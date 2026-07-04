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
    DeleteButton,
    DateField,
    FunctionField,
    TextInput,
    ArrayField,
    useNotify,
    useRefresh,
    useRedirect,
    Toolbar,
    SingleFieldList,
    ChipField,
    TopToolbar,
    FilterButton,
    CreateButton,
    TextField, ListButton, BooleanField, BooleanInput, NumberInput, DateTimeInput
} from 'react-admin';
import Chip from '@material-ui/core/Chip';
import { CreateActions } from '../tools/actions'
import SendDraftEmailButton from "../button/SendDraftEmailButton";
import SendAllEmailsButton from "../button/SendAllEmailsButton";
import FileInputMy from "../fields/FileInputMy";
import StopEmailsCampaignButton from "../button/StopEmailsCampaignButton";
import SeeBarButton from "../statistic/SeeBarButton";
import SeePieChartButton from "../statistic/SeePieChartButton";
import ExportButtonMy from "../button/ExportButtonMy";
import {makeStyles} from "@material-ui/core/styles";
import SeeAllEmailsButton from "../button/SeeAllEmailsButton";
import AddTagToCampaign from "../fields/AddTagToCampaign";
import DelTagFromCampaign from "../fields/DelTagFromCampaign";
import TimeZoneInput from "../fields/enum/TimeZoneInput";
import {CloneEmailCampaign} from "../fields/CloneEmailCampaign";
import BulkUploadEmailCampaignsButton from "../button/BulkUploadEmailCampaignsButton";
import NonClickableChipField from "../fields/NonClickableChipField";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
    div: {
        width: '100%'
    },
});

export const emailCampaignsFilters = [
    <TextInput label="id" source="id"/>,
    <TextInput label="name" source="name"/>
];

export const EmailCampaignTitle = ({ record }) => {
    return <span>Email campaign {record ? `"${record.name}" (${record.id})` : ''}</span>;
};

const EmailCampaignActions = (props) => {
    return (
        <TopToolbar>
            <FilterButton/>
            <CreateButton/>
            <BulkUploadEmailCampaignsButton />
        </TopToolbar>
    )
};

const ShowEmailCampaignActions = ({ basePath, data, resource }) => (
    <TopToolbar>
        <CreateButton/>
        <SendDraftEmailButton record={data}/>
        <SendAllEmailsButton record={data}/>
        <StopEmailsCampaignButton record={data}/>
        <EditButton basePath={basePath} record={data}/>
        <DeleteButton basePath={basePath} record={data} resource={resource} />
        <BulkUploadEmailCampaignsButton />
        <ListButton/>
    </TopToolbar>
);

export const EmailCampaignsList = (props) => (
    <List {...props} sort={{ field: 'cratedTime', order: 'DESC' }} actions={<EmailCampaignActions/>} filters={emailCampaignsFilters} bulkActionButtons={false}>
        <Datagrid>
            <FunctionField source="id" label="ID" render={record => record.id ? `${record.id}` : '-'}/>
            <FunctionField source="name" label="Name" render={record => record.name ? `${record.name}` : '-'}/>
            <FunctionField source="status" label="Status" render={record => record.status ? `${record.status}` : '-'}/>
            <DateField source="cratedTime" label="Created time"/>
            <DateField source="startDate" showTime={true} label="Start time"/>
            <DateField source="remindDate" showTime={true} label="Remind time"/>
            <BooleanField source="autoSend"/>
            <ArrayField source="tags">
                <SingleFieldList linkType={false}>
                    <NonClickableChipField source="text" />
                </SingleFieldList>
            </ArrayField>
            <ShowButton label=""/>
            <EditButton label=""/>
            <DeleteButton label=""/>
            <FunctionField render={recordIn => <CloneEmailCampaign emailCampaignId={recordIn.id}/>}/>
        </Datagrid>
    </List>
);

export const EmailCampaignsShow = (props) => {
    const classes = useStyles();

    return (
        <Show {...props} title={<EmailCampaignTitle/>} actions={<ShowEmailCampaignActions/>}>
            <TabbedShowLayout>
                <Tab label="Common information">
                    <FunctionField source="id" render={record => record.id ? `${record.id}` : '-'}/>
                    <FunctionField source="emailCampaignKey" render={record => record.emailCampaignKey ? `${record.emailCampaignKey}` : '-'}/>
                    <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
                    <FunctionField source="status" render={record => record.status ? `${record.status}` : '-'}/>
                    <FunctionField source="error" render={record => record.error ? `${record.error}` : '-'}/>
                    <FunctionField source="emailCount" render={record => record.emailCount ? `${record.emailCount}` : '-'}/>
                    <BooleanField source="autoSend"/>
                    <DateField showTime={true} source="cratedTime" label="Created time"/>
                    <SeeAllEmailsButton campaignId={props.id}/>
                </Tab>
                <Tab label="Time settings">
                    <FunctionField source="timeZone" render={record => record.timeZone ? `${record.timeZone}` : '-'}/>
                    <DateField showTime={true} source="startDate" label="Start date"/>
                    <DateField showTime={true} source="remindDate" label="Remind date"/>
                    <FunctionField source="sendOpenEmailIn" render={record => record.sendOpenEmailIn ? `${record.sendOpenEmailIn}` : '-'}/>
                </Tab>
                <Tab label="Tag">
                    <BooleanField source="useAllEmails"/>
                    <hr width="300px" size="2" color="#008080"/>
                    <div className={classes.div}>

                    </div>
                    <FunctionField lable="" source="parentEmailCampaignKey" render={record => record.parentEmailCampaignKey ?
                        <div>
                            <div className={classes.div}>
                                <p>Use open and use not open works only for cloned email campaign</p>
                                <p>This campaign was cloned from campaign with id: {record.parentEmailCampaignKey}</p>
                            </div>
                            <div>
                                <p>Use open emails</p>
                                <BooleanField source="useOpen"/>
                            </div>
                            <div>
                                <p>Use not open emails</p>
                                <BooleanField source="useNotOpen"/>
                            </div>
                            <hr width="300px" size="2" color="#008080"/>
                        </div>
                     : null}/>


                    <ArrayField source="tags">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="tagName"/>
                            <DelTagFromCampaign goalId={props.id} goalName="emailCampaigns" source="id"/>
                        </Datagrid>
                    </ArrayField>
                </Tab>
                <Tab label="First mail/Reminder">
                    <BooleanField source="sendReminder"/>
                    <FunctionField source="mailSubjectFirst" render={record => record.mailSubjectFirst ? `${record.mailSubjectFirst}` : '-'}/>
                    <BooleanField source="mailBodyFileFirst"/>
                    <FunctionField source="mailBodyFirst" render={record => record.mailBodyFirst ? `${record.mailBodyFirst}` : '-'}/>
                    <FileInputMy second={false} showUploadButton={false}/>
                </Tab>
                <Tab label="Mail when user open first email">
                    <BooleanField source="sendSecondEmail"/>
                    <FunctionField source="mailSubjectSecond" render={record => record.mailSubjectSecond ? `${record.mailSubjectSecond}` : '-'}/>
                    <BooleanField source="mailBodyFileSecond"/>
                    <FunctionField source="mailBodySecond" render={record => record.mailBodySecond ? `${record.mailBodySecond}` : '-'}/>
                    <FileInputMy second={true} showUploadButton={false}/>
                </Tab>
                <Tab label="Statistic">
                    <ExportButtonMy campaignId={props.id} fileType="CSV"/>
                    <ArrayField source="statistic.statisticDtoList">
                        <Datagrid>
                            <TextField source="name"/>
                            <TextField source="count"/>
                            <TextField source="percent"/>
                        </Datagrid>
                    </ArrayField>
                    <SeeBarButton source="statistic"/>
                    <SeePieChartButton source="statistic"/>
                </Tab>
            </TabbedShowLayout>
        </Show>
    )
};

export const EmailCampaignsCreate = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();
    const classes = useStyles();

    const onSuccess = () => {
        notify(`A new email campaign saved`);
        redirect(props.basePath);
        refresh();
    };

    const dateParse = v => {
        return v
    }

    return (
        <Create {...props} actions={<CreateActions/>} mutationMode="pessimistic" onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false}>
                <FormTab label="Common information">
                    <TextInput source="name"/>
                </FormTab>
                <FormTab label="Time settings">
                    <TimeZoneInput source="timeZone" label="Time zone for dates"/>
                    <DateTimeInput source="startDate" label="Start date" validate={required()} parse={dateParse}/>
                    <DateTimeInput source="remindDate" label="Remind date" parse={dateParse}/>
                    <NumberInput source="sendOpenEmailIn" defaultValue={3}/>
                    <BooleanInput source="autoSend"/>
                </FormTab>
                <FormTab label="First mail/Reminder">
                    <BooleanInput source="sendReminder" defaultValue="true"/>
                    <div className={classes.div}>
                        <p><b>If you want to use company name in subject add &#123;&#123;companyName&#125;&#125;.</b></p>
                    </div>
                    <TextInput source="mailSubjectFirst" validate={required()}/>
                    <BooleanInput source="mailBodyFileFirst" defaultValue={false} validate={required()}/>
                    <TextInput source="mailBodyFirst"/>
                    <FileInputMy second={false} showUploadButton={true} record={null}/>
                </FormTab>
                <FormTab label="Tag">
                    <BooleanInput source="useAllEmails"/>
                </FormTab>
                <FormTab label="Mail when user open first email">
                    <BooleanInput source="sendSecondEmail" defaultValue="true"/>
                    <div className={classes.div}>
                        <p><b>If you want to use company name in subject add &#123;&#123;companyName&#125;&#125;.</b></p>
                    </div>
                    <TextInput source="mailSubjectSecond"/>
                    <BooleanInput source="mailBodyFileSecond" defaultValue={false}/>
                    <TextInput source="mailBodySecond"/>
                    <FileInputMy second={true} showUploadButton={true} record={null}/>
                </FormTab>
            </TabbedForm>
        </Create>
    );
}

export const EmailCampaignsEdit = (props) => {
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

    const dateParse = v => {
        return v
    }

    return (
        <Edit {...props} title={<EmailCampaignTitle/>} mutationMode="pessimistic" actions={<CreateActions/>} onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false} toolbar={<Toolbar alwaysEnableSaveButton />}>
                <FormTab label="Common information">
                    <TextInput disabled source="id"/>
                    <TextInput disabled source="emailCampaignKey"/>
                    <TextInput source="name"/>
                    <TextInput disabled source="status"/>
                    <BooleanInput source="autoSend"/>
                </FormTab>
                <FormTab label="Time settings">
                    <TimeZoneInput source="timeZone" label="Time zone for dates"/>
                    <DateTimeInput source="startDate" label="Start date" validate={required()} parse={dateParse}/>
                    <DateTimeInput source="remindDate" label="Remind date" parse={dateParse}/>
                    <NumberInput source="sendOpenEmailIn"/>
                </FormTab>
                <FormTab label="Tag">
                    <BooleanInput source="useAllEmails"/>
                    <ArrayField source="tags">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="tagName"/>
                            <DelTagFromCampaign goalId={props.id} goalName="emailCampaigns" source="id"/>
                        </Datagrid>
                    </ArrayField>
                    <AddTagToCampaign props={props} goalPath="emailCampaignTags" goalName="emailCampaigns" goalId={props.id} source="id"/>
                </FormTab>
                <FormTab label="First mail/Reminder">
                    <BooleanInput source="sendReminder" defaultValue="true"/>
                    <div className={classes.div}>
                        <p><b>If you want to use company name in subject add &#123;&#123;companyName&#125;&#125;.</b></p>
                    </div>
                    <TextInput source="mailSubjectFirst" validate={required()}/>
                    <BooleanInput source="mailBodyFileFirst" validate={required()}/>
                    <TextInput source="mailBodyFirst"/>
                    <FileInputMy second={false} showUploadButton={true}/>
                </FormTab>
                <FormTab label="Mail when user open first email">
                    <BooleanInput source="sendSecondEmail" defaultValue="true"/>
                    <div className={classes.div}>
                        <p><b>If you want to use company name in subject add &#123;&#123;companyName&#125;&#125;.</b></p>
                    </div>
                    <TextInput source="mailSubjectSecond"/>
                    <BooleanInput source="mailBodyFileSecond"/>
                    <TextInput source="mailBodySecond"/>
                    <FileInputMy second={true} showUploadButton={true}/>
                </FormTab>
            </TabbedForm>
        </Edit>
    );
}
