import logo from './logo.svg';
import './App.css';
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

function App() {
  return (
    <MuiThemeProvider theme={theme}>
      <Home />
    </MuiThemeProvider>
  );
}

export default App;
