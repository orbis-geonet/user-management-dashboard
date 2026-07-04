import {Button, useNotify, useRefresh} from "react-admin";
import {serviceUrl} from "../tools/serviceUrl";
import * as React from "react";
import {makeStyles} from '@material-ui/core/styles';

const useStyles = makeStyles({
    div: {
        width: '100%'
    },
});

const DelTagFromCampaign = ({record, goalName, goalId, source}) => {
    const classes = useStyles();
    const notify = useNotify();
    const refresh = useRefresh();

    const onClick = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));
        fetch(`${serviceUrl}/${goalName}/${goalId}/del-tag?tagId=${record[source]}`,
            {method: 'DELETE', headers: {'Authorization': `Bearer ${token}`}})
            .then(response => {
                notify(`Tag was deleted from EmailCampaign`)
                refresh()
            })
            .catch(console.error);
    }

    return <Button className={classes.button} label="Delete" onClick={onClick}/>
}

export default DelTagFromCampaign;
