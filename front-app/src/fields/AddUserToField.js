import {
    Button,
    Datagrid,
    FunctionField,
    List,
    useNotify,
    useRefresh
} from 'react-admin';
import * as React from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';
import {Fragment, useState} from "react";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";
import {userFilters} from "../sections/users"
import {placeFilters} from "../sections/places"
import {groupFilters} from "../sections/groups"
import {userSetsFilters} from "../sections/userSets"
import ImportButtonMy from "../button/ImportButtonMy";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
});

const AddUserToField = ({props, userType, goalId, goal, sourceType, source, showImportButton}) => {
    const classes = useStyles();
    const refresh = useRefresh();
    const filters = sourceType === "users" ? userFilters :
        (sourceType === "places" ? placeFilters :
            (sourceType === "groups" ? groupFilters : (
                sourceType === "sets" ? userSetsFilters : null)))

    const [show, setShow] = useState(false);

    const handleClose = () => {
        setShow(false);
        refresh();
    }
    const handleShow = () => setShow(true);

    const onClick = () => {
        handleShow()
    }

    return (
        <Fragment>
            <Button className={classes.button} label="Add" onClick={onClick}/>
            {showImportButton ? <ImportButtonMy props={props} importUrl={`/import/places/${goalId}`} fileType="CSV"/> : null}
            <Dialog
                fullScreen
                open={show}
                onClose={handleClose}
            >
                <DialogTitle>Entity List</DialogTitle>
                <DialogContent>
                    <List resource={sourceType} basePath={"/" + sourceType}
                          sort={{ field: 'timestamp', order: 'DESC' }}
                          hasCreate={false} exporter={false}
                          filters={filters}
                          bulkActionButtons={false}
                    >
                        <Datagrid>
                            <FunctionField source="id" render={record => record.id ? `${record.id}` : '-'}/>
                            <FunctionField source="name" render={record => record.name ? `${record.name}` : (record.displayName ? `${record.displayName}` : '-')}/>
                            <FunctionField source="description" render={record => record.description ? `${record.description}` : '-'}/>
                            <FunctionField source="email" render={record => record.email ? `${record.email}` : '-'}/>
                            <AddButton userType={userType} goalId={goalId} goal={goal} sourceType={sourceType} source={source}/>
                        </Datagrid>
                    </List>
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

const AddButton = ({record, source, userType, goalId, sourceType, goal}) => {
    const notify = useNotify();

    const addOnClick = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));
        fetch(serviceUrl + "/" + goal + "/" + sourceType, {
            method: 'POST',
            headers: {'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json'},
            body: JSON.stringify({'type': userType, 'goalId': goalId, 'userKey': record[source]})
        })
            .then(response => { notify(`Entity was added to ${userType}`)})
            .catch(error => {
                if (error.response) {
                    notify("Something is going wrong. Try again." + error.response.data.message)
                } else {
                    notify("Something is going wrong. Try again." + error)
                }
            });
    }

    return (
        <Button label="Add" onClick={addOnClick}/>
    )
}

export default AddUserToField;
