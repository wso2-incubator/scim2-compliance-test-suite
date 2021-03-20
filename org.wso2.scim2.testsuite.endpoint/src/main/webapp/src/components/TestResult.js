import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardActionArea from '@material-ui/core/CardActionArea';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import CardMedia from '@material-ui/core/CardMedia';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import { Doughnut } from 'react-chartjs-2';

const useStyles = makeStyles({
  root: {
    width: 1000,
    height: 600,
    borderRadius: 25,
    margin: 10,
  },
  media: {
    height: 140,
  },
});

export default function TestResult(props) {
  const classes = useStyles();

  return (
    <Card className={classes.root} elevation={8}>
      <CardContent>
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <Typography variant="h6">
            Test Case Name : {props.result.name}
          </Typography>
        </div>
      </CardContent>
    </Card>
  );
}
