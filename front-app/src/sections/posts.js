import * as React from "react";
import {
    List,
    Datagrid,
    Show,
    Create,
    Edit,
    ReferenceField,
    required,
    ShowButton,
    EditButton,
    TextField,
    BooleanField,
    DateField,
    ChipField,
    FunctionField,
    TextInput,
    BooleanInput,
    TabbedShowLayout,
    Tab,
    ArrayField,
    TopToolbar,
    FilterButton,
    CreateButton, NumberField
} from 'react-admin';

import {ListActions, ShowActions, RecordTitle, CreateActions} from '../tools/actions'
import GoogleMapField from "../fields/GoogleMapField";
import UrlFieldMy from "../fields/UrlField";
import {makeStyles} from '@material-ui/core/styles';
import PostTypeInput from "../fields/enum/PostTypeInput";
import ExportButtonMy from "../button/ExportButtonMy";
import ImportButtonMy from "../button/ImportButtonMy";

const useStyles = makeStyles({
    div: {
        width: '100%'
    },
});

export const postsFilters = [
    <TextInput label="ID" source="id"/>,
    <PostTypeInput source="Type"/>,
    <TextInput label="Title" source="title"/>,
    <TextInput label="Details" source="details"/>,
    <BooleanInput label="Reported" source="reported"/>,
    <TextInput label="Share link" source="fullShareLink"/>,

];

const PostActions = (props) => {
    return (
        <TopToolbar>
            <FilterButton/>
            <ExportButtonMy basePath={props.basePath} fileType="CSV"/>
            <ImportButtonMy props={props} importUrl="/import" fileType="CSV"/>
        </TopToolbar>
    )
};

export const PostList = props => (
    <List {...props} sort={{ field: 'timestamp', order: 'DESC' }}
          actions={<PostActions/>} filters={postsFilters} bulkActionButtons={false}>
        <Datagrid>
            <FunctionField label="id" source="id" render={record => record.id ? `${record.id}` : '-'}/>
            <ChipField label="Type" source="type"/>
            <FunctionField label="Title" source="title" render={record => record.title ? `${record.title}` : '-'}/>
            <FunctionField label="Details" source="details" render={record => record.details ? `${record.details}` : '-'}/>
            <FunctionField label="User name" source="userName" render={record => record.userName ? `${record.userName}` : '-'}/>
            <BooleanField label="Deleted" source="deleted"/>
            <BooleanField label="Reported" source="reported"/>
            <DateField source="timestamp" label="Last update time"/>
            <ShowButton label=""/>
        </Datagrid>
    </List>
);

export const PostShow = (props) => {
    const classes = useStyles();

    return (
        <Show {...props} title={<RecordTitle/>}>
            <TabbedShowLayout>
                <Tab label="Common information">
                    <FunctionField source="id" render={record => record.id ? `${record.id}` : '-'}/>
                    <FunctionField source="postKey" render={record => record.postKey ? `${record.postKey}` : '-'}/>
                    <ChipField label="Type" source="type"/>
                    <FunctionField source="title" render={record => record.title ? `${record.title}` : '-'}/>
                    <FunctionField source="details" render={record => record.details ? `${record.details}` : '-'}/>
                    <FunctionField source="address" render={record => record.address ? `${record.address}` : '-'}/>
                    <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
                    <BooleanField source="reported"/>
                    <BooleanField source="deleted"/>
                    <FunctionField source="shareLink" render={record => record.shareLink ? `${record.shareLink}` : '-'}/>
                    <FunctionField source="fullShareLink" render={record => record.fullShareLink ? `${record.fullShareLink}` : '-'}/>

                    <ArrayField source="mediaUrls">
                        <Datagrid>
                            <UrlFieldMy source="url"/>
                        </Datagrid>
                    </ArrayField>

                    <DateField showTime={true} source="timestamp"/>
                    <DateField showTime={true} source="plannedTime"/>
                    <DateField showTime={true} source="plannedEndTime"/>

                    <hr width="300px" size="2" color="#008080"/>
                    <div className={classes.div}>
                        <h3><b>User Information</b></h3>
                    </div>
                    <FunctionField source="user" label="User Id" render={record => record.user ? `${record.user.id}` : '-'}/>
                    <FunctionField source="user" label="User Email" render={record => record.user ? `${record.user.email}` : '-'}/>
                    <FunctionField source="user" label="User Name" render={record => record.user ? `${record.user.userName}` : '-'}/>

                    <hr width="300px" size="2" color="#008080"/>
                    <div className={classes.div}>
                        <h3><b>Group Information</b></h3>
                    </div>
                    <FunctionField source="group" label="Group Id" render={record => record.group ? `${record.group.id}` : '-'}/>
                    <FunctionField source="group" label="Group name" render={record => record.group ? `${record.group.name}` : '-'}/>
                    <FunctionField source="group" label="Group description" render={record => record.group ? `${record.group.description}` : '-'}/>

                    <hr width="300px" size="2" color="#008080"/>
                    <div className={classes.div}>
                        <h3><b>Place Information</b></h3>
                    </div>
                    <FunctionField source="place" label="Place Id" render={record => record.place ? `${record.place.id}` : '-'}/>
                    <FunctionField source="place" label="Place name" render={record => record.place ? `${record.place.name}` : '-'}/>
                    <FunctionField source="place" label="Place description" render={record => record.place ? `${record.place.description}` : '-'}/>
                    <hr width="300px" size="2" color="#008080"/>
                </Tab>
                <Tab label="Statistic">
                    <NumberField source="commentsNumber"/>
                </Tab>
                <Tab label="Location">
                    <FunctionField source="coordinates"
                                   render={record => record.coordinates
                                       ? `lat = ${record.coordinates.lat}, lng = ${record.coordinates.lng}`
                                       : '-'}/>
                    <GoogleMapField source="coordinates"/>
                </Tab>
            </TabbedShowLayout>
        </Show>
    );
}
