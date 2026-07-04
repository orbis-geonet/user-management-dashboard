import {
    Datagrid,
    List,
    TextField,
    BooleanField,
    ArrayField,
    AutocompleteInput, DeleteButton, DateField, useRefresh, Button, FunctionField
} from "react-admin";
import {Fragment, useState} from "react";
import * as React from "react";
import UrlFieldMy from "./UrlField";
import {makeStyles} from "@material-ui/core/styles";
import DeletePostButton from "../button/DeletePostButton";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import {userSetsFilters} from "../sections/userSets";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";
import GoogleMapField from "./GoogleMapField";
import PostTypeInput from "./enum/PostTypeInput";

const postsFilters = [
    <PostTypeInput source="type" alwaysOn/>,
];

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold',
        // This is JSS syntax to target a deeper element using css selector, here the svg icon for this button
        '& svg': { color: 'orange' }
    },
});

const SeePostLocationButton = ({record, source}) => {
    const classes = useStyles();

    const [show, setShow] = useState(false);

    const handleClose = () => {
        setShow(false);
    }
    const handleShow = () => setShow(true);

    const onClick = () => {
        handleShow()
    }

    return (
        <Fragment>
            <Button className={classes.button} label="See location" onClick={onClick}/>
            <Dialog
                fullWidth
                open={show}
                onClose={handleClose}
            >
                <DialogTitle>Post location</DialogTitle>
                <DialogContent>
                    <GoogleMapField source="coordinates" record={record}/>
                </DialogContent>
                <DialogActions>
                    <Button label="ra.action.cancel" onClick={handleClose} >
                        <IconCancel />
                    </Button>
                </DialogActions>
            </Dialog>
        </Fragment>
    )
}

const PostsField = ({record, sortSource}) => {
    const sortValue = record[sortSource];

    return (
        <Fragment>
            <List resource="posts" basePath="/posts"
                  filterDefaultValues={{type:'TEXT', sortSource: sortSource, sortValue: sortValue}}
                  sort={{ field: 'timestamp', order: 'DESC' }}
                  hasCreate={false} exporter={false}
                  filters={postsFilters}
                  bulkActionButtons={false}
            >
                <Datagrid>
                    <TextField source="type"/>
                    <TextField source="title"/>
                    <TextField fullWidth={false} source="details"/>
                    <BooleanField source="deleted"/>
                    <ArrayField source="mediaUrls">
                        <Datagrid>
                            <UrlFieldMy source="url"/>
                        </Datagrid>
                    </ArrayField>
                    <DateField showTime={true} source="timestamp" label="Update time"/>
                    <DeletePostButton postUrl="/posts/" record={record}/>
                    <SeePostLocationButton source="coordinates"/>
                </Datagrid>
            </List>
        </Fragment>
    )
}

export default PostsField;
