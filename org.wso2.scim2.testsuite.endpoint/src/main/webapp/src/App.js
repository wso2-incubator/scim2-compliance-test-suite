import logo from './logo.svg';
import './App.css';
import { toast } from 'react-toastify';
import CssBaseline from '@material-ui/core/CssBaseline';

// Material-ui
import {
  createMuiTheme,
  ThemeProvider as MuiThemeProvider,
} from '@material-ui/core/styles';

// Components
import Header from './components/Header';
import Footer from './components/Footer';
import themeObject from './util/theme';
import Home from './screens/Home';

const theme = createMuiTheme(themeObject);

toast.configure();

function App() {
  return (
    <MuiThemeProvider theme={theme}>
      <CssBaseline />
      <Home />
    </MuiThemeProvider>
  );
}

export default App;
