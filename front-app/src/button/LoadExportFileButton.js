import {Button, useNotify} from 'react-admin';
import React, {Fragment, useState} from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold',
        // This is JSS syntax to target a deeper element using css selector, here the svg icon for this button
        '& svg': { color: 'orange' }
    },
});

const LoadExportFileButton = ({props, record}) => {
    const classes = useStyles();
    const notify = useNotify();

    const onClick = () => {
        if (record.status !== "READY") {
            notify("File is not ready or deleted. Please wait status READY")
            return;
        }
        const { token } = JSON.parse(localStorage.getItem('auth'));
        fetch(serviceUrl + '/reports/' + record.id, {method: 'GET', headers:{'Authorization': `Bearer ${token}`}})
            .then(response => response.blob())
            .then(blob => {
                const link = document.createElement("a");
                link.href = URL.createObjectURL(blob);
                link.download = record.name;
                link.click();
            })
            .catch(console.error);
     }

     const checkHidden = () => {
        return record.status !== "READY"
     }


    return (
        <Fragment>
            <Button {...props} className={classes.button} label="Download" onClick={onClick} hidden={checkHidden}/>
        </Fragment>
    )
}

export default LoadExportFileButton;
