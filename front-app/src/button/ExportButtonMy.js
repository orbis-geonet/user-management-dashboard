import {Button, useNotify, FunctionField, SimpleForm, TextInput, required, NumberInput} from 'react-admin';
import React, {Fragment, useState} from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';
import axios from "axios";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";
import DraftTypeInput from "../fields/enum/DraftTypeInput";
import IconContentAdd from "@material-ui/icons/Add";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
});

const ExportButtonMy = ({basePath, campaignId, fileType}) => {
    const classes = useStyles();
    const notify = useNotify();
    const [maxCount, setMaxCount] = useState(null);

    const [show, setShow] = useState(false);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    // const divContext = `Please choose range. Max range is 1000. Numbers of entities ${maxCount}`;
    //
    const onClick = () => {
        handleShow()
        // const { token } = JSON.parse(localStorage.getItem('auth'));
        // axios.get(`${serviceUrl}${basePath}/count?id=${campaignId}`,
        //     {headers: {'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
        //     .then((response) => {
        //         setMaxCount(response.data);
        //         console.log(maxCount);
        //         handleShow()
        //     })
    }

    const onSubmit = (event) => {
        const from = document.getElementById("from").value
        const till = document.getElementById("till").value

        handleClose();

        const { token } = JSON.parse(localStorage.getItem('auth'));

        axios.get(`${serviceUrl}${basePath}/export?from=${from}&till=${till}&id=${campaignId}&fileType=${fileType}`,
            {headers: {
                    'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify("Export is starting.")
                handleClose()
            })
            .catch((error) => {
                console.log(error)
                if (error.response) {
                    notify("Something is going wrong. Try again." + error.response.data.message)
                } else {
                    notify("Something is going wrong. Try again." + error)
                }
            })
     }

    return (
        <Fragment>
            <Button className={classes.button} label={`Export ${fileType}`} onClick={onClick}/>
            <Dialog
                fullWidth
                open={show}
                onClose={handleClose}
                aria-label={`Export`}
            >
                <DialogTitle>Import</DialogTitle>
                <DialogContent>
                    <SimpleForm onSubmit={onSubmit} toolbar={null}>
                        <NumberInput source="from" id="from" defaultValue="0" validate={required()}/>
                        <NumberInput source="till" id="till" defaultValue="1000" validate={required()}/>
                    </SimpleForm>
                </DialogContent>
                <DialogActions>
                    <Button onClick={onSubmit} label="Export">
                        <IconContentAdd />
                    </Button>
                    <Button label="ra.action.cancel" onClick={handleClose} >
                        <IconCancel />
                    </Button>
                </DialogActions>
            </Dialog>
        </Fragment>
    )
}

export default ExportButtonMy;
