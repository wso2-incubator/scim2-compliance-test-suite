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
    width: '90%',
    height: 600,
    borderRadius: 25,
    margin: 10,
  },
  media: {
    height: 140,
  },
});

var options = {
  legend: {
    position: 'bottom',
    labels: {
      boxWidth: 10,
    },
  },
};

const pieData = {
  labels: ['Green', 'Red', 'Yellow'],
  datasets: [
    {
      data: [300, 50, 100],
      backgroundColor: ['#00D100', '#FF0000', '#ffeb3b'],
    },
  ],
};

export default function Summary(props) {
  const classes = useStyles();
  const [data, setData] = React.useState({
    labels: ['Success', 'Failed', 'skipped'],
    datasets: [
      {
        data: [
          props.statistics.success,
          props.statistics.failed,
          props.statistics.skipped,
        ],
        backgroundColor: ['#00D100', '#FF0000', '#FFCE56'],
      },
    ],
  });

  return (
    <Card className={classes.root} elevation={8}>
      <CardContent>
        <Typography variant="h6" style={{ fontWeight: 1000 }}>
          Summary :
        </Typography>
        <Doughnut data={data} options={options} />
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
          }}
        >
          <Typography
            variant="subtitle1"
            style={{ fontWeight: 700, align: 'center' }}
          >
            Total Results : {props.statistics.total}
          </Typography>
          <Typography
            variant="subtitle1"
            style={{ fontWeight: 700, align: 'right' }}
          >
            Time(s) : {props.statistics.time / 1000}
          </Typography>
        </div>
      </CardContent>
    </Card>
  );
}
