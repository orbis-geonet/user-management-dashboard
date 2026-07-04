import {
    BooleanField, Button, DateField,
    FunctionField, UrlField, Show, SimpleShowLayout
} from 'react-admin';
import * as React from "react";

import { makeStyles } from '@material-ui/core/styles';
import {Fragment, useState} from "react";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";
import ImageField from "./ImageField";
import {ColorField} from "react-admin-color-input";
import GoogleMapField from "./GoogleMapField";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
});

const EntityInformationField = ({props, record, sourceType, source}) => {
    const classes = useStyles();

    const [show, setShow] = useState(false);

    const handleClose = () => { setShow(false) }
    const handleShow = () => setShow(true);

    const onClick = () => { handleShow() }

    return (
        <Fragment>
            <Button className={classes.button} label="See" onClick={onClick}/>
            <Dialog
                fullScreen
                open={show}
                onClose={handleClose}
            >
                <DialogTitle>Entity Information</DialogTitle>
                <DialogContent>
                    <Show {...props}
                          resource={sourceType}
                          basePath={"/" + sourceType}
                          id={record[source]}
                          hasEdit={false}
                    >
                        <SimpleShowLayout>
                                <FunctionField source="id" render={recordIn => recordIn.id ? `${recordIn.id}` : '-'}/>
                                <FunctionField source="groupKey" render={recordIn => recordIn.groupKey ? `${recordIn.groupKey}` : '-'}/>
                                <FunctionField source="name" render={recordIn => recordIn.name ? `${recordIn.name}` : '-'}/>
                                <FunctionField source="description"
                                               render={recordIn => recordIn.description ? `${recordIn.description}` : '-'}/>
                                <FunctionField source="image"
                                               render={recordIn => recordIn.imageName ? `${recordIn.imageName}` : '-'}/>
                                <ImageField imageBucket="groupPictures"/>
                                <ColorField source="strokeColorHex"/>
                                <DateField showTime={true} source="timestamp"/>
                                <BooleanField source="deleted"/>
                                <BooleanField source="reported"/>
                                <FunctionField source="os" render={recordIn => recordIn.os ? `${recordIn.os}` : '-'}/>
                                <UrlField source="shareLink"/>
                                <FunctionField source="location"
                                               render={recordIn => recordIn.coordinates
                                                   ? `lat = ${recordIn.coordinates.lat}, lng = ${recordIn.coordinates.lng}`
                                                   : '-'}/>
                                <GoogleMapField source="coordinates" record={record}/>
                        </SimpleShowLayout>
                    </Show>
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

export default EntityInformationField;