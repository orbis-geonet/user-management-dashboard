import * as React from "react";
import { Admin, Resource } from 'react-admin';
import authProvider from './auth/authProvider';
import dataProvider from './tools/dataProvider';

import { PlacesList, PlaceShow, PlaceCreate, PlaceEdit } from './sections/places';
import { GroupsList, GroupShow, GroupCreate, GroupEdit } from './sections/groups';
import { UsersList, UserShow, UserCreate, UserEdit } from './sections/users';
import { ReportList } from "./sections/reports";
import { UserSetsCreate, UserSetsEdit, UserSetsList, UserSetsShow } from "./sections/userSets";
import { StatisticList, StatisticShow } from "./sections/statistics";
import { PostList, PostShow } from "./sections/posts";
import { FlaggedList } from "./sections/flagged";
import {EmailCreate, EmailEdit, EmailShow, EmailsList} from "./sections/emails";
import {
    EmailCampaignsCreate,
    EmailCampaignsEdit,
    EmailCampaignsList,
    EmailCampaignsShow
} from "./sections/emailCampaign";
import {PartnerList} from "./sections/partners";

const App = () => (
    <Admin dataProvider={dataProvider} authProvider={authProvider}>
        <Resource name='groups' list = {GroupsList} show={GroupShow} create={GroupCreate} edit={GroupEdit}/>
        <Resource name="places" list = {PlacesList} show={PlaceShow} create={PlaceCreate} edit={PlaceEdit}/>
        <Resource name="users" list = {UsersList} show={UserShow} create={UserCreate} edit={UserEdit}/>
        <Resource name="sets" list = {UserSetsList} show={UserSetsShow} create={UserSetsCreate} edit={UserSetsEdit}/>
        <Resource name="statistics" list = {StatisticList} show={StatisticShow}/>
        <Resource name="posts" list={PostList} show={PostShow}/>
        <Resource name="flagged" list={FlaggedList}/>
        <Resource name="emails" list={EmailsList} show={EmailShow} create={EmailCreate} edit={EmailEdit}/>
        <Resource name="emailCampaigns" list={EmailCampaignsList} show={EmailCampaignsShow} create={EmailCampaignsCreate} edit={EmailCampaignsEdit}/>
        <Resource name="emailCampaignTags"/>
        <Resource name="emailCampaigns/emailCampaignInfo"/>
        <Resource name="reports" list={ReportList}/>
        <Resource name="partners" list={PartnerList}/>
    </Admin>
);

export default App;
