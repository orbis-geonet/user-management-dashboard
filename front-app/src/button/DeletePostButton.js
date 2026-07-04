import {Button, useNotify, useRefresh} from 'react-admin';
import * as React from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold',
        // This is JSS syntax to target a deeper element using css selector, here the svg icon for this button
        '& svg': { color: 'orange' }
    },
});

const DeletePostButton = ({record, postUrl}) => {
    const classes = useStyles();
    const notify = useNotify();
    const refresh = useRefresh();

    const onClick = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));
        fetch(serviceUrl + postUrl + record.id, {method: 'DELETE', headers:{'Authorization': `Bearer ${token}`}})
            .then(response => {
                notify("Post was deleted")
                refresh()
            })
            .catch(console.error);
    }

    return <Button className={classes.button} label="Delete post" onClick={onClick}
    />
}

export default DeletePostButton;
