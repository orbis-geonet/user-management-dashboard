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
    BooleanInput,
    UrlField,
    ArrayField,
    TextField, useNotify, useRefresh, useRedirect,
    Toolbar
} from 'react-admin';
import { ColorField, ColorInput } from 'react-admin-color-input';

import { ListActions, RecordTitle, ShowActions, CreateActions } from '../tools/actions'
import GoogleMapInput from "../fields/GoogleMapInput";
import GoogleMapField from "../fields/GoogleMapField";
import ImageField from "../fields/ImageField";
import ImageInputMy from "../fields/ImageInputMy";
import AddUserToField from "../fields/AddUserToField";
import DelUserFromField from "../fields/DelUserFromField";
import AddSetToField from "../fields/AddSetToField";
import PostsField from "../fields/PostsField";
import ImageStatusInput from "../fields/enum/ImageStatusInput";

export const groupFilters = [
    <TextInput label="ID" source="id"/>,
    <TextInput label="Name" source="name"/>,
    <TextInput label="Share link" source="fullShareLink"/>,
    <TextInput label="Upload file name" source="uploadFileName"/>,
    <ImageStatusInput source="imageUploadStatus"/>
];

export const GroupsList = (props) => (
    <List {...props} sort={{ field: 'createTimestamp', order: 'DESC' }} actions={<ListActions/>} filters={groupFilters} bulkActionButtons={false}>
        <Datagrid>
            <FunctionField source="id" label="ID" render={record => record.id ? `${record.id}` : '-'}/>
            <FunctionField source="name" label="Name" render={record => record.name ? `${record.name}` : '-'}/>
            <BooleanField source="deleted" label="Deleted"/>
            <FunctionField source="fullShareLink" label="Full share link" render={record => record.fullShareLink ? `${record.fullShareLink}` : '-'}/>
            <FunctionField source="uploadFileName" label="Upload file name" render={record => record.uploadFileName ? `${record.uploadFileName}` : '-'}/>
            <DateField source="createTimestamp" label="Created time"/>
            <DateField source="timestamp" label="Updated time"/>
            <DateField source="lastActivity"/>
            <FunctionField source="imageUploadStatus" label="Image status" render={record => record.imageUploadStatus ? `${record.imageUploadStatus}` : '-'}/>
            <ShowButton label=""/>
            <EditButton label=""/>
        </Datagrid>
    </List>
);

export const GroupShow = (props) => {
    return (
        <Show {...props} title={<RecordTitle/>} actions={<ShowActions/>}>
            <TabbedShowLayout>
                <Tab label="Common information">
                    <FunctionField source="id" render={record => record.id ? `${record.id}` : '-'}/>
                    <FunctionField source="groupKey" render={record => record.groupKey ? `${record.groupKey}` : '-'}/>
                    <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
                    <FunctionField source="slug" render={record => record.slug ? `${record.slug}` : '-'}/>
                    <FunctionField source="description"
                                   render={record => record.description ? `${record.description}` : '-'}/>
                    <FunctionField source="image"
                                   render={record => record.imageName ? `${record.imageName}` : '-'}/>
                    <ImageField imageBucket="groupPictures"/>
                    <ColorField source="strokeColorHex"/>
                    <DateField showTime={true} source="createTimestamp" label="Created time"/>
                    <DateField showTime={true} source="timestamp" label="Update time"/>
                    <BooleanField source="deleted"/>
                    <BooleanField source="reported"/>
                    <FunctionField source="os" render={record => record.os ? `${record.os}` : '-'}/>
                    <UrlField source="shareLink"/>
                    <UrlField source="fullShareLink"/>
                    <FunctionField source="uploadFileName" label="Upload file name" render={record => record.uploadFileName ? `${record.uploadFileName}` : '-'}/>
                    <FunctionField source="imageUploadStatus" label="Image status" render={record => record.imageUploadStatus ? `${record.imageUploadStatus}` : '-'}/>
                    <UrlField source="uploadingLink" label="Uploading link"/>
                    <FunctionField source="errorMessage" label="Error message" render={record => record.errorMessage ? `${record.errorMessage}` : '-'}/>
                </Tab>
                <Tab label="Location">
                    <FunctionField source="location"
                                   render={record => record.coordinates
                                       ? `lat = ${record.coordinates.lat}, lng = ${record.coordinates.lng}`
                                       : '-'}/>
                    <GoogleMapField source="coordinates"/>
                </Tab>
                <Tab label="Statistic">
                    <FunctionField source="adminsCount"
                                   render={record => record.adminsCount ? `${record.adminsCount}` : '0'}/>
                    <FunctionField source="followersCount"
                                   render={record => record.followersCount ? `${record.followersCount}` : '0'}/>
                    <FunctionField source="membersCount"
                                   render={record => record.membersCount ? `${record.membersCount}` : '0'}/>
                    <FunctionField source="bannedCount"
                                   render={record => record.bannedCount ? `${record.bannedCount}` : '0'}/>
                    <FunctionField source="storiesHiddenCount"
                                   render={record => record.storiesHiddenCount ? `${record.storiesHiddenCount}` : '0'}/>
                    <FunctionField source="checkInCount"
                                   render={record => record.checkInCount ? `${record.checkInCount}` : '0'}/>
                    <DateField showTime={true} source="lastActivity"/>
                    <ArrayField source="posts">
                        <Datagrid>
                            <TextField source="type"/>
                            <TextField source="count"/>
                        </Datagrid>
                    </ArrayField>
                </Tab>
                <Tab label="Posts">
                    <PostsField sortSource="groupKey" {...props}/>
                </Tab>
                <Tab label="Users fields">
                    <ArrayField source="admins">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                        </Datagrid>
                    </ArrayField>
                    <ArrayField source="mainAdmin">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                        </Datagrid>
                    </ArrayField>
                    <ArrayField source="followers">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                        </Datagrid>
                    </ArrayField>
                    <ArrayField source="members">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                        </Datagrid>
                    </ArrayField>
                    <ArrayField source="banned">
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

export const GroupCreate = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();

    const onSuccess = () => {
        notify(`A new group saved`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Create {...props} actions={<CreateActions/>} mutationMode="pessimistic" onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false}>
                <FormTab label="Common information">
                    <TextInput source="name" validate={required()}/>
                    <TextInput source="description" validate={required()} options={{multiLine: true}}/>
                    <ColorInput source="strokeColorHex"/>
                    <ImageInputMy imageBucket="groupPictures" record={null}/>
                </FormTab>
                <FormTab label="Location">
                    <GoogleMapInput record={null}/>
                </FormTab>
            </TabbedForm>
        </Create>
    );
}

export const GroupEdit = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();

    const onSuccess = ({ data }) => {
        console.log(data)
        notify(`A group was edited`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Edit {...props} title={<RecordTitle/>} mutationMode="pessimistic" actions={<CreateActions/>} onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false} toolbar={<Toolbar alwaysEnableSaveButton />}>
                <FormTab label="Common information">
                    <TextInput disabled source="id" />
                    <TextInput source="groupKey" />
                    <TextInput source="name" validate={required()} />
                    <TextInput source="description" options={{ multiLine: true }} />
                    <ImageInputMy imageBucket="groupPictures"/>
                    <ColorInput source="strokeColorHex"/>
                    <DateTimeInput source="timestamp" />
                    <BooleanInput source="deleted" />
                    <BooleanInput source="reported" />
                    <TextInput source="os" />
                    <TextInput source="shareLink" />
                    <TextInput source="fullShareLink" />
                </FormTab>
                <FormTab label="Location">
                    <GoogleMapInput/>
                </FormTab>
                <FormTab label="Posts">
                    <PostsField sortSource="groupKey" {...props}/>
                </FormTab>
                <FormTab label="Users fields">
                    <ArrayField source="admins">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                            <DelUserFromField goalType="admins" goal="groups" entityName="users" goalId={props.id} source="userKey"/>
                        </Datagrid>
                    </ArrayField>
                    <AddUserToField props={props} userType="admins" goalId={props.id} goal="groups" sourceType="users" source="userKey"/>
                    <AddSetToField props={props} userType="admins" goalId={props.id} goal="groups"/>
                    <ArrayField source="followers">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                            <DelUserFromField goalType="followers" goal="groups" entityName="users" goalId={props.id} source="userKey"/>
                        </Datagrid>
                    </ArrayField>
                    <AddUserToField props={props} userType="followers" goalId={props.id} goal="groups" sourceType="users" source="userKey"/>
                    <AddSetToField props={props} userType="followers" goalId={props.id} goal="groups"/>
                    <ArrayField source="members">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                            <DelUserFromField goalType="members" goal="groups" entityName="users" goalId={props.id} source="userKey"/>
                        </Datagrid>
                    </ArrayField>
                    <AddUserToField props={props} userType="members" goalId={props.id} goal="groups" sourceType="users" source="userKey"/>
                    <AddSetToField props={props} userType="members" goalId={props.id} goal="groups"/>
                    <ArrayField source="banned">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                            <DelUserFromField goalType="banned" goal="groups" entityName="users" goalId={props.id}/>
                        </Datagrid>
                    </ArrayField>
                    <AddUserToField props={props} userType="banned" goalId={props.id} goal="groups" sourceType="users" source="userKey"/>
                    <AddSetToField props={props} userType="banned" goalId={props.id} goal="groups"/>
                </FormTab>
            </TabbedForm>
        </Edit>
    );
}
