import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Accordion from '@material-ui/core/Accordion';
import AccordionSummary from '@material-ui/core/AccordionSummary';
import AccordionDetails from '@material-ui/core/AccordionDetails';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Container from '@material-ui/core/Container';
import Tab from './TabPanel';
import Badge from '@material-ui/core/Badge';
import theme from '../util/theme';
import { Box, Button } from '@material-ui/core';
import purple from '@material-ui/core/colors/purple';
import red from '@material-ui/core/colors/red';
import Header from './Header';
import JSONPretty from 'react-json-pretty';
//import jsonTheme2 from 'react-json-pretty/themes/adventure_time.css';

const useStyles = makeStyles((theme) => ({
  root: {
    width: '90%',
    margin: 10,
    borderRadius: 25,
  },
  heading: {
    fontSize: theme.typography.pxToRem(15),
    fontWeight: 550,
  },
  secondaryHeading: {
    fontSize: theme.typography.pxToRem(15),
    color: theme.palette.text.secondary,
  },
  column: {
    flexBasis: '100%',
  },
}));

export default function Assertion(props) {
  const classes = useStyles();
  const [assertionData, setAssertionData] = React.useState();

  return (
    <div>
      <Accordion elevation={0}>
        <AccordionSummary
          expandIcon={<ExpandMoreIcon />}
          aria-controls="panel2a-content"
          id="panel2a-header"
          style={{ flexDirection: 'row-reverse', marginLeft: 25 }}
        >
          {props.assertion.content.status === 'Success' ? (
            <Typography style={{ color: '#00D100' }}>
              {' '}
              {props.assertion.name}{' '}
            </Typography>
          ) : (
            <Typography style={{ color: '#FF0000' }}>
              {' '}
              {props.assertion.name}{' '}
            </Typography>
          )}
        </AccordionSummary>
        <AccordionDetails>
          <List component="nav">
            {props.assertion.content.actual ? (
              <ListItem>Actual : {props.assertion.content.actual}</ListItem>
            ) : null}
            {props.assertion.content.expected ? (
              <ListItem>Expected : {props.assertion.content.expected}</ListItem>
            ) : null}
            {props.assertion.content.message ? (
              <ListItem>{props.assertion.content.message}</ListItem>
            ) : null}
          </List>
        </AccordionDetails>
      </Accordion>
    </div>
  );
}
