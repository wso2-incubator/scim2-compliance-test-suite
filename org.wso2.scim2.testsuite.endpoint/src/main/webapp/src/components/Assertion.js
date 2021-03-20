import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Accordion from '@material-ui/core/Accordion';
import AccordionSummary from '@material-ui/core/AccordionSummary';
import AccordionDetails from '@material-ui/core/AccordionDetails';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import { Divider } from '@material-ui/core';

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
              <TableContainer
                component={Paper}
                style={{ marginLeft: 64, width: '96%' }}
              >
                <Table
                  className={classes.table}
                  aria-label="simple table"
                  stickyHeader
                >
                  <TableHead>
                    <TableRow>
                      <TableCell>Actual</TableCell>
                      <Divider orientation="vertical" />
                      <TableCell align="left">Expected</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody style={{ backgroundColor: '#e3f2fd' }}>
                    <TableRow key={1}>
                      <TableCell align="left">
                        {props.assertion.content.actual}
                      </TableCell>
                      <Divider orientation="vertical" />
                      <Divider orientation="vertical" />
                      <Divider orientation="vertical" />
                      <TableCell align="left">
                        {props.assertion.content.expected}
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            ) : null}
            {/* {props.assertion.content.expected ? (
              <ListItem>Expected : {props.assertion.content.expected}</ListItem>
            ) : null} */}
            {props.assertion.content.message ? (
              <ListItem style={{ marginLeft: 44 }}>
                {props.assertion.content.message}
              </ListItem>
            ) : null}
          </List>
        </AccordionDetails>
      </Accordion>
    </div>
  );
}
