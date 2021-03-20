import React from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import JSONPretty from 'react-json-pretty';
import jsonTheme from 'react-json-pretty/themes/monikai.css';
import blue from '@material-ui/core/colors/blue';

function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box p={3}>
          <Typography>{children}</Typography>
        </Box>
      )}
    </div>
  );
}

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.any.isRequired,
  value: PropTypes.any.isRequired,
};

function a11yProps(index) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`,
  };
}

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
    backgroundColor: '#272822',
    marginLeft: 35,
  },
}));

export default function SimpleTabs({ Headers, Body }) {
  const classes = useStyles();
  const [value, setValue] = React.useState(0);

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  return (
    <div className={classes.root}>
      <AppBar
        position="static"
        style={{ backgroundColor: '#FFFFFF' }}
        elevation={0}
      >
        <Tabs
          value={value}
          onChange={handleChange}
          aria-label="simple tabs example"
          indicatorColor="primary"
          textColor="primary"
        >
          <Tab label="Headers" {...a11yProps(0)} style={{ fontWeight: 600 }} />
          <Tab label="Body" {...a11yProps(1)} style={{ fontWeight: 600 }} />
        </Tabs>
      </AppBar>
      <TabPanel value={value} index={0}>
        <JSONPretty
          id="json-pretty"
          data={Headers}
          theme={jsonTheme}
          style={{ flex: 1, overflowY: 'scroll', height: 250 }}
        ></JSONPretty>
      </TabPanel>
      <TabPanel value={value} index={1}>
        <JSONPretty
          id="json-pretty"
          data={Body != '' ? Body : 'No Content to show'}
          theme={jsonTheme}
          style={{ flex: 1, overflowY: 'scroll', height: 250 }}
        ></JSONPretty>
      </TabPanel>
    </div>
  );
}
