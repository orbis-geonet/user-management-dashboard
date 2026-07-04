import {
    Button,
    useNotify,
    Labeled,
    TextField,
    useRefresh,
    Show,
    SimpleShowLayout,
    Tab,
    FunctionField, ArrayField, Datagrid
} from 'react-admin';
import React, {Fragment, useRef, useState} from "react";
import {serviceUrl} from "../tools/serviceUrl";

import { makeStyles } from '@material-ui/core/styles';
import axios from "axios";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import IconCancel from "@material-ui/icons/Cancel";
import IconContentAdd from "@material-ui/icons/Add";
import {Bar} from "react-chartjs-2";
import {RecordTitle} from "../tools/actions";
import SeeBarButton from "../statistic/SeeBarButton";
import CustomPeriodSettingButton from "../statistic/CustomPeriodSettingButton";
import SeePieChartButton from "../statistic/SeePieChartButton";

const useStyles = makeStyles({
    button: {
        fontWeight: 'bold'
    },
    div: {
        width: '100%'
    },
});

const SeeEmailsCampaignStatisticButton = ({campaignId}) => {
    const classes = useStyles();
    const notify = useNotify();
    const refresh = useRefresh();
    const dataRef = useRef(null);

    const [show, setShow] = useState(false);
    const [statisticData, setStatisticData] = useState(null);

    const handleClose = () => setShow(false);
    const handleShow = () => setShow(true);

    const onClick = () => {
        const { token } = JSON.parse(localStorage.getItem('auth'));

        axios.get(`${serviceUrl}/emailCampaigns/statistic/${campaignId}`,
            {headers: {
                'Content-Type': 'application/json', 'Authorization': `Bearer ${token}`}})
            .then((response) => {
                notify("Data was gotten.")

                // val inputData = response.data
                const inputData = response.data
                setStatisticData(inputData)
                dataRef.current = inputData;

                console.log(statisticData)

                handleShow()
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
            <Button className={classes.button} label="See statistic" onClick={onClick}/>
            <Dialog
                fullWidth
                open={show}
                onClose={handleClose}
                aria-label={`Statistic`}
            >
                <DialogTitle>Statistic</DialogTitle>
                <DialogContent>
                    <ArrayField source={statisticData}>
                        <Datagrid>
                            <TextField source="name"/>
                            <TextField source="count"/>
                            <TextField source="percent"/>
                        </Datagrid>
                    </ArrayField>
                    <SeeBarButton source="statistic"/>
                    <SeePieChartButton source="statistic"/>
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

export default SeeEmailsCampaignStatisticButton;
