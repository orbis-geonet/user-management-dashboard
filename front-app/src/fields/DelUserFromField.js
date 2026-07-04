import {Button, useNotify, useRefresh} from 'react-admin';
import * as React from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
});

const DelUserFromField = ({record, goalType, goalId, goal, entityName, source}) => {
    const classes = useStyles();
    const notify = useNotify();
    const refresh = useRefresh();

    const onClick = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));
        fetch(serviceUrl + "/" + goal + "/" + entityName + "/" + goalType + "?goal_id=" + goalId + "&user_key=" + record[source],
            {method: 'DELETE', headers: {'Authorization': `Bearer ${token}`}})
            .then(response => {
                notify(`${entityName} was deleted from ${goalType}`)
                refresh()
            })
            .catch(console.error);
    }

    return <Button className={classes.button} label="Delete" onClick={onClick}/>
}

export default DelUserFromField;
