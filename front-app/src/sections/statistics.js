import * as React from "react";
import {
    List,
    Datagrid,
    Show,
    TabbedShowLayout,
    Tab,
    ShowButton,
    FunctionField,
    ArrayField,
    TextField, TopToolbar,  ListButton
} from 'react-admin';

import { RecordTitle } from '../tools/actions'
import SeeBarButton from "../statistic/SeeBarButton";
import CustomPeriodSettingButton from "../statistic/CustomPeriodSettingButton";

const StatisticShowActions = ({ basePath, data, resource }) => (
    <TopToolbar>
        <ListButton/>
    </TopToolbar>
);

export const StatisticList = (props) => (
    <List {...props} sort={{ field: 'timestamp', order: 'DESC' }}
          actions={null}
          filters={null}
          bulkActionButtons={false}
    >
        <Datagrid>
            <FunctionField source="id" label="ID" render={record => record.id ? `${record.id}` : '-'}/>
            <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
            <ShowButton label=""/>
        </Datagrid>
    </List>
);

export const StatisticShow = (props) => {
    return (
        <Show {...props} title={<RecordTitle/>} actions={<StatisticShowActions/>}>
            <TabbedShowLayout>
                <Tab label="Last month information">
                    <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
                    <FunctionField source="activityResultLastMonth.name" label="Activity title:"
                                   render={record => (record.activityResultLastMonth && record.activityResultLastMonth.name)
                                       ? `${record.activityResultLastMonth.name}` : '-'}/>
                    <ArrayField source="activityResultLastMonth.activity">
                        <Datagrid>
                            <TextField source="title"/>
                            <TextField source="numbers"/>
                        </Datagrid>
                    </ArrayField>
                    <SeeBarButton source="activityResultLastMonth"/>
                </Tab>
                <Tab label="Custom period information">
                    <FunctionField source="name" render={record => record.name ? `${record.name}` : '-'}/>
                    <FunctionField source="activityResultCustomSetting.name" label="Activity title:"
                                   render={record => (record.activityResultCustomSetting && record.activityResultCustomSetting.name)
                                       ? `${record.activityResultCustomSetting.name}` : '-'}/>
                    <ArrayField source="activityResultCustomSetting.activity">
                        <Datagrid>
                            <TextField source="title"/>
                            <TextField source="numbers"/>
                        </Datagrid>
                    </ArrayField>
                    <SeeBarButton record={props.record} source="activityResultCustomSetting"/>
                    <CustomPeriodSettingButton props={props} record={props.record} source="type"/>
                </Tab>
            </TabbedShowLayout>
        </Show>
    )
}
