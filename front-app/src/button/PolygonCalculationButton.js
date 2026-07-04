import {Button, useNotify, useRefresh} from 'react-admin';
import * as React from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';
import {Fragment, useRef, useState} from "react";
import axios from 'axios';
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold',
        '& svg': { color: 'orange' }
    },
});

const PolygonCalculationButton = ({props}) => {
    const classes = useStyles();
    const notify = useNotify();
    const refresh = useRefresh();

    const onClick = () => {
        const {token} = JSON.parse(localStorage.getItem('auth'));
        // axios.post(serviceUrl + props.basePath + '/import',
        axios.post(`${serviceUrl}/places/polygon-calculations`,
            {},
            {headers: {
                    'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify("Polygon calculations.")
                refresh()
            })
            .catch((error) => {
                if (error.response) {
                    notify("Something is going wrong. Try again." + error.response.data.message)
                } else {
                    notify("Something is going wrong. Try again." + error)
                }
            })
    }

    return (
        <Fragment>
            <Button {...props} className={classes.button} label={`Polygon calculation`} onClick={onClick}/>
        </Fragment>
        )
}

export default PolygonCalculationButton;
