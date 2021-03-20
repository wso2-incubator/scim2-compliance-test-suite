import React from 'react';
import PropTypes from 'prop-types';
import CircularProgress from '@material-ui/core/CircularProgress';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';

function CircularProgressWithLabel(props) {
  return (
    <Box>
      <CircularProgress
        variant="determinate"
        {...props}
        color="secondary"
        style={{ left: '60%', position: 'absolute', top: '44vh' }}
        size="6rem"
      />
      <Box
        top="49vh"
        left="62.3%"
        position="absolute"
        display="flex"
        alignItems="center"
        justifyContent="center"
      >
        <Typography
          variant="caption"
          component="div"
          color="primary"
        >{`${Math.round(props.value)}%`}</Typography>
      </Box>
    </Box>
  );
}

CircularProgressWithLabel.propTypes = {
  /**
   * The value of the progress indicator for the determinate variant.
   * Value between 0 and 100.
   */
  value: PropTypes.number.isRequired,
};

export default function Progress(props) {
  const [progress, setProgress] = React.useState(10);

  React.useEffect(() => {
    {
      console.log(props.value);
    }
    const timer = setInterval(() => {
      setProgress((prevProgress) =>
        prevProgress >= 90 ? 98 : prevProgress + 10
      );
    }, props.value * 100);
    return () => {
      clearInterval(timer);
    };
  }, []);

  return <CircularProgressWithLabel value={progress} />;
}
