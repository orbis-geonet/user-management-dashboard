import * as React from "react";
import {
    List,
    Datagrid,
    Show,
    Create,
    Edit,
    required,
    ShowButton,
    EditButton,
    BooleanField,
    DateField,
    ChipField,
    FunctionField,
    TextInput,
    BooleanInput,
    TabbedShowLayout,
    Tab,
    TabbedForm,
    FormTab,
    DateTimeInput,
    useNotify,
    useRefresh,
    useRedirect,
    Toolbar,
    ArrayField,
    TextField,
    ArrayInput,
    SimpleFormIterator,
    TopToolbar, FilterButton, CreateButton, UrlField
} from 'react-admin';

import {ListActions, RecordTitle, CreateActions} from '../tools/actions'
import ImageField from "../fields/ImageField";
import GoogleMapField from "../fields/GoogleMapField";
import ImageInputMy from "../fields/ImageInputMy";
import GoogleMapInput from "../fields/GoogleMapInput";
import PlaceCategoryInput from "../fields/PlaceCategoryInput";
import PostsField from "../fields/PostsField";
import ExportButtonMy from "../button/ExportButtonMy";
import ImportButtonMy from "../button/ImportButtonMy";
import PolygonCalculationButton from "../button/PolygonCalculationButton";
import ImageStatusInput from "../fields/enum/ImageStatusInput";

export const placeFilters = [
    <TextInput label="ID" source="id"/>,
    <TextInput label="Name" source="name"/>,
    <TextInput label="Share link" source="fullShareLink"/>,
    <TextInput label="Upload file name" source="uploadFileName"/>,
    <ImageStatusInput source="imageUploadStatus"/>
];

export const PlacesListActions = (props) => {
    return (
        <TopToolbar>
            <FilterButton/>
            <CreateButton/>
            <PolygonCalculationButton/>
            <ExportButtonMy basePath={props.basePath} fileType="CSV"/>
            <ImportButtonMy props={props} importUrl="/import" fileType="CSV"/>
        </TopToolbar>
    )
};

export const PlacesList = props => (
    <List {...props} sort={{ field: 'createTimestamp', order: 'DESC' }} actions={<PlacesListActions/>} filters={placeFilters} bulkActionButtons={false}>
        <Datagrid>
            <FunctionField label="ID" source="id" render={record => record.id ? `${record.id}` : '-'}/>
            <FunctionField label="Name" source="name" render={record => record.name ? `${record.name}` : '-'}/>
            <ChipField label="Type" source="type"/>
            <BooleanField label="Deleted" source="deleted"/>
            <FunctionField source="fullShareLink" label="Full share link" render={record => record.fullShareLink ? `${record.fullShareLink}` : '-'}/>
            <FunctionField source="uploadFileName" label="Upload file name" render={record => record.uploadFileName ? `${record.uploadFileName}` : '-'}/>
            <DateField source="createTimestamp" label="Created time"/>
            <DateField source="timestamp" label="Updated time"/>
            <FunctionField source="imageUploadStatus" label="Image status" render={record => record.imageUploadStatus ? `${record.imageUploadStatus}` : '-'}/>
            <ShowButton label=""/>
            <EditButton label=""/>
        </Datagrid>
    </List>
);

export const PlaceShow = (props) => (
    <Show {...props} title={<RecordTitle/>}>
            <TabbedShowLayout>
                <Tab label="Common information">
                    <FunctionField source="id" render={record => record.id ? `${record.id}` : '-'}/>
                    <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
                    <FunctionField source="slug" render={record => record.slug ? `${record.slug}` : '-'}/>
                    <FunctionField source="placeKey" render={record => record.placeKey ? `${record.placeKey}` : '-'}/>
                    <ChipField label="Type" source="type"/>
                    <FunctionField source="userCreated"
                                   render={record => record.userCreated ? `${record.userCreated.email}` : '-'}/>
                    <FunctionField source="imageName"
                                   render={record => record.imageName ? `${record.imageName}` : '-'}/>
                    <ImageField imageBucket="placePictures"/>
                    <FunctionField source="source" render={record => record.source ? `${record.source}` : '-'}/>
                    <FunctionField source="address" render={record => record.address ? `${record.address}` : '-'}/>
                    <FunctionField source="phone" render={record => record.phone ? `${record.phone}` : '-'}/>
                    <FunctionField source="website" render={record => record.website ? `${record.website}` : '-'}/>
                    <FunctionField source="totalRate" render={record => record.totalRate ? `${record.totalRate}` : '-'}/>
                    <FunctionField source="countRates" render={record => record.countRates ? `${record.countRates}` : '-'}/>
                    <ArrayField source="workingHours">
                        <Datagrid>
                            <TextField source="day"/>
                            <TextField source="time"/>
                        </Datagrid>
                    </ArrayField>
                    <FunctionField source="googleAddress" render={record => record.googleAddress ? `${record.googleAddress}` : '-'}/>
                    <FunctionField source="description" render={record => record.description ? `${record.description}` : '-'}/>
                    <FunctionField source="dominantGroup"
                                   render={record => record.dominantGroup ? `${record.dominantGroup.name}` : '-'}/>
                    <FunctionField source="groupCreated"
                                   render={record => record.groupCreated ? `${record.groupCreated.name}` : '-'}/>
                    <DateField showTime={true} source="lastCheckInTimestamp"/>
                    <DateField showTime={true} source="lastSizeChangeTimestamp"/>
                    <BooleanField source="deleted"/>
                    <DateField showTime={true} source="creationServerTimestamp"/>
                    <DateField showTime={true} source="createTimestamp" label="Created time"/>
                    <DateField showTime={true} source="timestamp" label="Update time"/>
                    <FunctionField source="googlePlaceId" render={record => record.googlePlaceId ? `${record.googlePlaceId}` : '-'}/>
                    <FunctionField source="lastSize" render={record => record.lastSize ? `${record.lastSize}` : '-'}/>
                    <BooleanField source="reported"/>
                    <FunctionField source="shareLink" render={record => record.shareLink ? `${record.shareLink}` : '-'}/>
                    <FunctionField source="fullShareLink" render={record => record.fullShareLink ? `${record.fullShareLink}` : '-'}/>
                    <FunctionField source="uploadFileName" label="Upload file name" render={record => record.uploadFileName ? `${record.uploadFileName}` : '-'}/>
                    <FunctionField source="imageUploadStatus" label="Image status" render={record => record.imageUploadStatus ? `${record.imageUploadStatus}` : '-'}/>
                    <UrlField source="uploadingLink" label="Uploading link"/>
                    <FunctionField source="errorMessage" label="Error message" render={record => record.errorMessage ? `${record.errorMessage}` : '-'}/>
                </Tab>
                <Tab label="Location">
                    <FunctionField source="coordinates"
                                   render={record => record.coordinates
                                       ? `lat = ${record.coordinates.lat}, lng = ${record.coordinates.lng}`
                                       : '-'}/>
                    <GoogleMapField source="coordinates"/>
                </Tab>
                <Tab label="Posts">
                    <PostsField sortSource="placeKey" {...props}/>
                </Tab>
            </TabbedShowLayout>
        </Show>
);

export const PlaceCreate = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();

    const onSuccess = () => {
        notify(`A new place saved`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Create {...props} actions={<CreateActions/>} mutationMode="pessimistic" onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false}>
                <FormTab label="Common information">
                    <TextInput source="name" validate={required()}/>
                    <TextInput source="description" validate={required()} options={{multiLine: true}}/>
                    <PlaceCategoryInput label="Type" source="type"/>
                    <TextInput source="address" />
                    <TextInput source="phone" />
                    <TextInput source="website" />
                    <ArrayInput source="workingHours">
                        <SimpleFormIterator inline>
                            <TextInput source="day" label="Day" helperText={false} />
                            <TextInput source="time" label="Time" helperText={false} />
                        </SimpleFormIterator>
                    </ArrayInput>
                    <ImageInputMy imageBucket="placePictures" record={null}/>
                    <TextInput source="googlePlaceId" />
                </FormTab>
                <FormTab label="Location">
                    <GoogleMapInput record={null}/>
                </FormTab>
            </TabbedForm>
        </Create>
    );
}

export const PlaceEdit = (props) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const redirect = useRedirect();

    const onSuccess = () => {
        notify(`A place was edited`);
        redirect(props.basePath);
        refresh();
    };

    return (
        <Edit {...props} title={<RecordTitle/>} mutationMode="pessimistic" actions={<CreateActions/>} onSuccess={onSuccess}>
            <TabbedForm submitOnEnter={false} toolbar={<Toolbar alwaysEnableSaveButton />}>
                <FormTab label="Common information">
                    <TextInput disabled source="id"/>
                    <TextInput source="name" validate={required()}/>
                    <TextInput source="description"/>
                    <TextInput source="placeKey"/>
                    <PlaceCategoryInput label="Type" source="type"/>
                    <TextInput source="userCreatedKey"/>
                    <ImageInputMy imageBucket="placePictures"/>
                    <TextInput source="source"/>
                    <TextInput source="address"/>
                    <TextInput source="phone" />
                    <TextInput source="website" />
                    <ArrayInput source="workingHours">
                        <SimpleFormIterator inline>
                            <TextInput source="day" label="Day" helperText={false} />
                            <TextInput source="time" label="Time" helperText={false} />
                        </SimpleFormIterator>
                    </ArrayInput>
                    <DateTimeInput showTime={true} source="lastCheckInTimestamp"/>
                    <DateTimeInput showTime={true} source="lastSizeChangeTimestamp"/>
                    <TextInput source="dominantGroupKey"/>
                    <BooleanInput source="deleted"/>
                    <DateTimeInput showTime={true} source="creationServerTimestamp"/>
                    <DateTimeInput showTime={true} source="timestamp"/>
                    <TextInput source="groupCreatedKey"/>
                    <TextInput source="googlePlaceId"/>
                    <TextInput source="lastSize"/>
                    <BooleanInput source="reported"/>
                    <TextInput source="shareLink"/>
                    <TextInput source="fullShareLink"/>
                </FormTab>
                <FormTab label="Location">
                    <GoogleMapInput/>
                </FormTab>
                <FormTab label="Posts">
                    <PostsField sortSource="placeKey" {...props}/>
                </FormTab>
            </TabbedForm>
        </Edit>
    );
}
