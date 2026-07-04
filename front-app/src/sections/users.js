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

import { ListActions, RecordTitle, ShowActions, CreateActions } from '../tools/actions'
import ImageField from "../fields/ImageField";
import ImageInputMy from "../fields/ImageInputMy";
import GenderInput from "../fields/GenderInput";
import DeleteFollowButton from "../button/DeleteFollowButton";
import AddUserToField from "../fields/AddUserToField";
import PostsField from "../fields/PostsField";
import ImageStatusInput from "../fields/enum/ImageStatusInput";

export const userFilters = [
    <TextInput label="id" source="id"/>,
    <TextInput label="fullShareLink" source="fullShareLink"/>,
    <TextInput label="email" source="email"/>,
    <TextInput label="User name" source="displayName"/>,
    <BooleanInput source="superUser" />,
    <TextInput label="Upload file name" source="uploadFileName"/>,
    <ImageStatusInput source="imageUploadStatus"/>
];

export const UsersList = (props) => (
    <List {...props} sort={{ field: 'createTimestamp', order: 'DESC' }} actions={<ListActions/>} filters={userFilters} bulkActionButtons={false}>
        <Datagrid>
            <FunctionField source="id" label="ID" render={record => record.id ? `${record.id}` : '-'}/>
            <FunctionField source="email" render={record => record.email ? `${record.email}` : '-'}/>
            <FunctionField source="displayName" render={record => record.displayName ? `${record.displayName}` : '-'}/>
            <FunctionField source="fullShareLink" render={record => record.fullShareLink ? `${record.fullShareLink}` : '-'}/>
            <BooleanField source="superAdmin"/>
            <BooleanField source="deleted"/>
            <DateField source="createTimestamp" label="Created time"/>
            <DateField source="timestamp" label="Updated time"/>
            <FunctionField source="uploadFileName" label="Upload file name" render={record => record.uploadFileName ? `${record.uploadFileName}` : '-'}/>
            <FunctionField source="imageUploadStatus" label="Image status" render={record => record.imageUploadStatus ? `${record.imageUploadStatus}` : '-'}/>
            <ShowButton label=""/>
            <EditButton label=""/>
        </Datagrid>
    </List>
);

export const UserShow = (props) => {
    return (
        <Show {...props} title={<RecordTitle/>} actions={<ShowActions/>}>
            <TabbedShowLayout>
                <Tab label="Common information">
                    <FunctionField source="id" render={record => record.id ? `${record.id}` : '-'}/>
                    <FunctionField source="userKey" render={record => record.userKey ? `${record.userKey}` : '-'}/>
                    <FunctionField source="displayName" render={record => record.displayName ? `${record.displayName}` : '-'}/>
                    <FunctionField source="slug" render={record => record.slug ? `${record.slug}` : '-'}/>
                    <FunctionField source="email" render={record => record.email ? `${record.email}` : '-'}/>
                    <FunctionField source="dateOfBirth" render={record => record.dateOfBirth ? `${record.dateOfBirth}` : '-'}/>
                    <FunctionField source="gender" render={record => record.gender ? `${record.gender}` : '-'}/>
                    <FunctionField source="language" render={record => record.language ? `${record.language}` : '-'}/>
                    <FunctionField source="imageName" render={record => record.imageName ? `${record.imageName}` : '-'}/>
                    <ImageField imageBucket="profilePictures"/>
                    <FunctionField source="providerImageUrl" render={record => record.providerImageUrl ? `${record.providerImageUrl}` : '-'}/>
                    <FunctionField source="unit" render={record => record.unit ? `${record.unit}` : '-'}/>
                    <UrlField source="shareLink"/>
                    <UrlField source="fullShareLink"/>
                    <DateField showTime={true} source="createTimestamp" label="Created time"/>
                    <DateField showTime={true} source="timestamp" label="Update time"/>
                    <DateField showTime={true} source="activeServerTimestamp"/>
                    <BooleanField source="superAdmin"/>
                    <BooleanField source="deleted"/>
                    <BooleanField source="accountPrivate"/>
                    <BooleanField source="reported"/>
                    <FunctionField source="imageUploadStatus" label="Image status" render={record => record.imageUploadStatus ? `${record.imageUploadStatus}` : '-'}/>
                    <UrlField source="uploadingLink" label="Uploading link"/>
                    <FunctionField source="errorMessage" label="Error message" render={record => record.errorMessage ? `${record.errorMessage}` : '-'}/>
                </Tab>
                <Tab label="Statistic">
                    <FunctionField source="groupCount"
                                   render={record => record.groupCount ? `${record.groupCount}` : '0'}/>
                    <FunctionField source="followersCount"
                                   render={record => record.followersCount ? `${record.followersCount}` : '0'}/>
                    <FunctionField source="followedCount"
                                   render={record => record.followedCount ? `${record.followedCount}` : '0'}/>
                </Tab>
                <Tab label="Posts">
                    <PostsField sortSource="userKey" {...props}/>
                </Tab>
                <Tab label="Users">
                    <ArrayField source="followers">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="followId"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                            <BooleanField source="accepted"/>
                            <BooleanField source="seen"/>
                        </Datagrid>
                    </ArrayField>
                    <ArrayField source="followeds">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="followId"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                            <BooleanField source="accepted"/>
                            <BooleanField source="seen"/>
                        </Datagrid>
                    </ArrayField>
                </Tab>
            </TabbedShowLayout>
        </Show>
    )
};

export const UserCreate = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();

    const onSuccess = () => {
        notify(`A new group saved`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Create {...props} actions={<CreateActions/>} onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false} toolbar={<Toolbar alwaysEnableSaveButton />}>
                <FormTab label="Common information">
                    <TextInput source="displayName"/>
                    <TextInput source="email" validate={required()}/>
                    <TextInput source="dateOfBirth"/>
                    <GenderInput label="Gender" source="gender"/>
                    <TextInput source="language"/>
                    <ImageInputMy imageBucket="profilePictures"/>
                    <TextInput source="providerImageUrl"/>
                    <TextInput source="unit"/>
                    <TextInput source="shareLink"/>
                    <TextInput source="fullShareLink"/>
                    <DateTimeInput showTime={true} source="timestamp" label="Update time"/>
                    <DateTimeInput showTime={true} source="activeServerTimestamp"/>
                    <BooleanInput source="superAdmin"/>
                    <BooleanInput source="deleted"/>
                    <BooleanInput source="accountPrivate"/>
                    <BooleanInput source="reported"/>
                </FormTab>
                {/*<FormTab label="Location">*/}
                {/*    <GoogleMapInput record={null}/>*/}
                {/*</FormTab>*/}
            </TabbedForm>
        </Create>
    );
}

export const UserEdit = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();

    const onSuccess = () => {
        notify(`A user was edited`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Edit {...props} title={<RecordTitle/>} mutationMode="pessimistic" actions={<CreateActions/>} onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false} toolbar={<Toolbar alwaysEnableSaveButton />}>
                <FormTab label="Common information">
                    <TextInput disabled source="id"/>
                    <TextInput source="userKey"/>
                    <TextInput source="displayName"/>
                    <TextInput source="email" validate={required()}/>
                    <TextInput source="dateOfBirth"/>
                    <GenderInput label="Gender" source="gender"/>
                    <TextInput source="language"/>
                    <ImageInputMy imageBucket="profilePictures"/>
                    <TextInput source="providerImageUrl"/>
                    <TextInput source="unit"/>
                    <TextInput source="shareLink"/>
                    <TextInput source="fullShareLink"/>
                    <DateTimeInput showTime={true} source="timestamp" label="Update time"/>
                    <DateTimeInput showTime={true} source="activeServerTimestamp"/>
                    <BooleanInput source="superAdmin"/>
                    <BooleanInput source="deleted"/>
                    <BooleanInput source="accountPrivate"/>
                    <BooleanInput source="reported"/>
                </FormTab>
                <FormTab label="Posts">
                    <PostsField sortSource="userKey" {...props}/>
                </FormTab>
                <FormTab label="Users">
                    <ArrayField source="followers">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="followId"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                            <BooleanField source="accepted"/>
                            <BooleanField source="seen"/>
                            <DeleteFollowButton {...props}/>
                        </Datagrid>
                    </ArrayField>
                    <AddUserToField props={props} userType="followers" goalId={props.id} goal="follows" sourceType="users" source="userKey"/>
                    <ArrayField source="followeds">
                        <Datagrid>
                            <TextField source="id"/>
                            <TextField source="followId"/>
                            <TextField source="email"/>
                            <TextField source="userKey"/>
                            <BooleanField source="accepted"/>
                            <BooleanField source="seen"/>
                            <DeleteFollowButton {...props}/>
                        </Datagrid>
                    </ArrayField>
                    <AddUserToField props={props} userType="followeds" goalId={props.id} goal="follows" sourceType="users" source="userKey"/>
                </FormTab>
            </TabbedForm>
        </Edit>
    );
}
