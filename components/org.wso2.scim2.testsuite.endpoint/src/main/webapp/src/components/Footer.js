import React from 'react';
import Typography from '@material-ui/core/Typography';
import { makeStyles } from '@material-ui/core/styles';
import Link from '@material-ui/core/Link';
import AppBar from '@material-ui/core/AppBar';

function Copyright() {
  return (
    <Typography variant="body2" color="textSecondary">
      {'Copyright © '}
      <Link color="inherit" href="https://material-ui.com/">
        Your Website
      </Link>{' '}
      {new Date().getFullYear()}
      {'.'}
    </Typography>
  );
}

const useStyles = makeStyles((theme) => ({
  footer: {
    top: 'auto',
    bottom: 0,
    zIndex: theme.zIndex.drawer + 1,
  },
  title: {
    flexGrow: 1,
  },
}));

export default function Footer() {
  const classes = useStyles();

  return (
    <AppBar position="fixed" className={classes.footer}>
      <Typography variant="body1" align="right">
        {' '}
        {new Date().getFullYear()} WSO2 Inc. All Rights Reserved
      </Typography>
    </AppBar>
  );
}
