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
import Tooltip from '@material-ui/core/Tooltip';
import { Box, Button } from '@material-ui/core';

// colors
import purple from '@material-ui/core/colors/purple';
import red from '@material-ui/core/colors/red';

// Icons
import CheckCircleIcon from '@material-ui/icons/CheckCircle';
import CancelIcon from '@material-ui/icons/Cancel';
import ErrorIcon from '@material-ui/icons/Error';
import WarningIcon from '@material-ui/icons/Warning';
import ScheduleIcon from '@material-ui/icons/Schedule';
import LensIcon from '@material-ui/icons/Lens';

// Components
import Assertion from './Assertion';

const useStyles = makeStyles((theme) => ({
  root: {
    width: '90%',
    margin: 10,
    borderRadius: 25,
  },
  heading: {
    fontSize: theme.typography.pxToRem(15),
    fontWeight: 501,
    color: 'rgba(0, 0, 0, 0.54)',
    lineHeight: 1.6,
    letterSpacing: '0.0075em',
  },
  secondaryHeading: {
    fontSize: theme.typography.pxToRem(15),
    color: theme.palette.text.secondary,
  },
  column: {
    flexBasis: '100%',
  },
  column2: {
    flexBasis: '15%',
  },
}));

export default function SimpleAccordion(props) {
  const classes = useStyles();
  const [assertionData, setAssertionData] = React.useState();
  var arrOfObjects = [];

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

  const assertion = () => {
    var assertionName = [];
    var assertionContent = [];
    var assertionStatus = [];
    var content = props.result.wire.tests.split('\n');
    var object = {
      name: '',
      content: { status: '', actual: '', expected: '', message: '' },
    };

    content.map((t) => {
      if (t.includes(':') && !t.includes('Status')) {
        assertionContent.push(t);
      }
    });

    assertionContent.push('');
    assertionContent.push('');
    content.map((t) => {
      if (t.includes('Status')) {
        assertionStatus.push(t);
      }
    });

    content.map((t) => {
      if (!t.includes(':')) {
        if (t.length === 0) {
          //console.log(t);
        } else {
          assertionName.push(t);
        }
      }
    });

    for (var i = 0; i < assertionName.length; i++) {
      arrOfObjects.push(object);
    }

    var j = 0;
    assertionName.map((n, i) => {
      var messagePresent = false;
      var a = {
        name: n,
        content: {
          status: assertionStatus[i].split(' ')[2],
          actual: assertionContent[j].includes('Actual')
            ? assertionContent[j].split(' ')[2]
            : '',
          expected: assertionContent[j + 1].includes('Expected')
            ? assertionContent[j + 1].split(' ')[2]
            : '',
          message:
            assertionContent[j].includes('Test') ||
            assertionContent[j].includes('Message')
              ? assertionContent[j]
              : '',
        },
      };
      if (assertionContent[j].includes('Actual')) {
        j = j + 2;
      } else {
        j++;
      }
      const assertions = arrOfObjects;
      assertions[i] = a;

      arrOfObjects = assertions;
    });

    setAssertionData([...arrOfObjects]);
    console.log(assertionData);
  };

  return (
    <div className={classes.root}>
      <Accordion style={{ borderRadius: 10 }}>
        <AccordionSummary
          expandIcon={<ExpandMoreIcon />}
          aria-controls="panel1a-content"
          id="panel1a-header"
          style={{ flexDirection: 'row-reverse' }}
          onClick={assertion}
        >
          {props.result.status === 1 ? (
            <CheckCircleIcon
              style={{ color: '#32CD32', marginRight: 3, paddingBottom: 5 }}
            />
          ) : // <Badge>{rectangle1}</Badge>
          props.result.status === 2 ? (
            // <Badge>{rectangle2}</Badge>
            <WarningIcon
              style={{ color: '#FFCE56', marginRight: 3, paddingBottom: 5 }}
            />
          ) : (
            <CancelIcon
              style={{ color: '#bb3f3f', marginRight: 3, paddingBottom: 5 }}
            />
            // <Badge>{rectangle}</Badge>
          )}
          <div className={classes.column}>
            <Typography className={classes.heading} variant="h6">
              {props.result.name}
            </Typography>
          </div>
          <Tooltip title="Status Code" arrow placement="bottom">
            <LensIcon
              style={{ color: '#9e9e9e', marginRight: 3, paddingBottom: 4.5 }}
            />
          </Tooltip>
          <div className={classes.column2}>
            <Typography className={classes.secondaryHeading}>
              {props.result.wire.responseStatus}
            </Typography>
          </div>
          <Tooltip title="Elapsed Time" arrow placement="bottom">
            <ScheduleIcon
              style={{ color: '#9e9e9e', marginRight: 3, paddingBottom: 4.5 }}
            />
          </Tooltip>
          <div className={classes.column2}>
            <Typography className={classes.secondaryHeading}>
              {/* <TimelapseIcon style={{ paddingTop: 7 }} /> */}
              {props.result.elapsedTime} ms
            </Typography>
          </div>
        </AccordionSummary>
        <AccordionDetails>
          <div
            style={{
              backgroundColor: 'rgba(65,68,78,1)',
              width: '60%',
              height: 48,
              flex: 1,
              display: 'inline-flex',
              //flexDirection: 'row',
              //  justifyContent: 'space-between',
              padding: 10,
              marginLeft: 35,
            }}
          >
            <Button
              disableFocusRipple
              disableRipple
              style={{ backgroundColor: purple[500] }}
            >
              {' '}
              <Typography style={{ color: '#FFFFFF' }}>
                {props.result.wire.requestType}
              </Typography>
            </Button>
            <Typography style={{ color: '#FFFFFF', marginLeft: 15 }}>
              {props.result.wire.requestUri}
            </Typography>
          </div>
        </AccordionDetails>
        {props.result.message != '' ? (
          <Accordion elevation={0}>
            <AccordionSummary
              expandIcon={<ExpandMoreIcon />}
              aria-controls="panel2a-content"
              id="panel2a-header"
              style={{ flexDirection: 'row-reverse', marginLeft: 20 }}
            >
              <Typography className={classes.heading}> Caused By </Typography>
            </AccordionSummary>
            <AccordionDetails>
              <div
                style={{
                  backgroundColor: '#FFFFFF',
                  borderStyle: 'solid',
                  borderColor: 'rgba(65,68,78,1)',
                  width: '60%',
                  height: 48,
                  flex: 1,
                  display: 'inline-flex',
                  //flexDirection: 'row',
                  //  justifyContent: 'space-between',
                  padding: 10,
                  marginLeft: 35,
                }}
              >
                <Typography style={{ color: red[500] }}>
                  {props.result.message}
                </Typography>
              </div>
            </AccordionDetails>
          </Accordion>
        ) : null}
        <Accordion elevation={0}>
          <AccordionSummary
            expandIcon={<ExpandMoreIcon />}
            aria-controls="panel2a-content"
            id="panel2a-header"
            style={{ flexDirection: 'row-reverse', marginLeft: 20 }}
          >
            <Typography className={classes.heading}> Request </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Tab
              Headers={props.result.wire.requestHeaders}
              Body={props.result.wire.requestBody}
            />
          </AccordionDetails>
        </Accordion>
        <Accordion elevation={0}>
          <AccordionSummary
            expandIcon={<ExpandMoreIcon />}
            aria-controls="panel2a-content"
            id="panel2a-header"
            style={{ flexDirection: 'row-reverse', marginLeft: 20 }}
          >
            <Typography className={classes.heading}> Response </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Tab
              Headers={props.result.wire.responseHeaders}
              Body={props.result.wire.responseBody}
            />
          </AccordionDetails>
        </Accordion>
        <Accordion elevation={0} style={{ borderRadius: 10 }}>
          <AccordionSummary
            expandIcon={<ExpandMoreIcon />}
            aria-controls="panel2a-content"
            id="panel2a-header"
            style={{ flexDirection: 'row-reverse', marginLeft: 20 }}
          >
            <Typography className={classes.heading}> Assertions </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <div style={{ flex: 1 }}>
              {assertionData
                ? assertionData.map((assertion) => {
                    {
                      console.log(assertion.name);
                    }
                    return <Assertion assertion={assertion} />;
                  })
                : null}
            </div>
          </AccordionDetails>
        </Accordion>
      </Accordion>
    </div>
  );
}
