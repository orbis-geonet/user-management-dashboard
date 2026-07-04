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
import {userSetsFilters} from "../sections/userSets";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
});

const AddSetToField = ({userType, goalId, goal}) => {
    const classes = useStyles();
    const refresh = useRefresh();

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
            <Button className={classes.button} label="Add set" onClick={onClick}/>
            <Dialog
                fullScreen
                open={show}
                onClose={handleClose}
            >
                <DialogTitle>Users List</DialogTitle>
                <DialogContent>
                    <List resource="sets" basePath="/sets"
                          sort={{ field: 'timestamp', order: 'DESC' }}
                          hasCreate={false} exporter={false}
                          filters={userSetsFilters}
                          bulkActionButtons={false}
                    >
                        <Datagrid>
                            <FunctionField source="id" render={record => record.id ? `${record.id}` : '-'}/>
                            <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
                            <AddButton userType={userType} goalId={goalId} goal={goal}/>
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

const AddButton = ({record, source, userType, goalId, goal}) => {
    const notify = useNotify();

    const addOnClick = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'))
        console.log(record)
        fetch(serviceUrl + "/" + goal + "/sets", {
            method: 'POST',
            headers: {'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json'},
            body: JSON.stringify({'type': userType, 'goalId': goalId, 'userKey': record.id})
        })
            .then(response => { notify(`User was added to ${userType}`)})
            .catch(console.error);
    }

    return (
        <Button label="Add" onClick={addOnClick}/>
    )
}

export default AddSetToField;
