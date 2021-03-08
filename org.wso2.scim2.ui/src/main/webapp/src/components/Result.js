import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Accordion from '@material-ui/core/Accordion';
import AccordionSummary from '@material-ui/core/AccordionSummary';
import AccordionDetails from '@material-ui/core/AccordionDetails';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Container from '@material-ui/core/Container';
import Tab from './TabPanel';
import Badge from '@material-ui/core/Badge';
import theme from '../util/theme';

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
    flexBasis: '98%',
  },
}));

export default function SimpleAccordion(props) {
  const classes = useStyles();

  const statusColor = (status) => {
    if (status == 0) {
      return '#FF0000';
    } else if (status == 1) {
      return '#00D100';
    } else {
      return '#FFCE56';
    }
  };

  const rectangle = (
    <div
      style={{
        backgroundColor: '#FF0000',
        width: 20,
        height: 20,
        marginRight: 5,
      }}
    />
  );

  const rectangle1 = (
    <div
      style={{
        backgroundColor: '#00D100',
        width: 20,
        height: 20,
        marginRight: 5,
      }}
    />
  );

  const rectangle2 = (
    <div
      style={{
        backgroundColor: '#ffeb3b',
        width: 20,
        height: 20,
        marginRight: 5,
      }}
    />
  );

  return (
    <div className={classes.root}>
      <Accordion>
        <AccordionSummary
          expandIcon={<ExpandMoreIcon />}
          aria-controls="panel1a-content"
          id="panel1a-header"
          style={{ flexDirection: 'row-reverse' }}
        >
          {props.result.status === 1 ? (
            <Badge>{rectangle1}</Badge>
          ) : props.result.status === 2 ? (
            <Badge>{rectangle2}</Badge>
          ) : (
            <Badge>{rectangle}</Badge>
          )}
          <div className={classes.column}>
            <Typography className={classes.heading}>
              {props.result.name}
            </Typography>
          </div>
          <div className={classes.column}>
            <Typography className={classes.secondaryHeading}>
              Time : {props.result.elapsedTime}ms
            </Typography>
          </div>
        </AccordionSummary>
        <AccordionDetails>
          <Typography>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse
            malesuada lacus ex, sit amet blandit leo lobortis eget.
          </Typography>
          <Tab />
        </AccordionDetails>
      </Accordion>
      {/* <Accordion>
        <AccordionSummary
          expandIcon={<ExpandMoreIcon />}
          aria-controls="panel2a-content"
          id="panel2a-header"
        >
          <Typography className={classes.heading}>Accordion 2</Typography>
        </AccordionSummary>
        <AccordionDetails>
          <Typography>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse
            malesuada lacus ex, sit amet blandit leo lobortis eget.
          </Typography>
        </AccordionDetails>
      </Accordion> */}
    </div>
  );
}
