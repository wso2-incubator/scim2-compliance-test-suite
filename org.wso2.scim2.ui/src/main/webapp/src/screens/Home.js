import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import Card from '@material-ui/core/Card';
import Grid from '@material-ui/core/Grid';
import Drawer from '@material-ui/core/Drawer';
import Settings from '@material-ui/icons/Settings';

// List
import Checkbox from '@material-ui/core/Checkbox';
import IconButton from '@material-ui/core/IconButton';
import LaunchIcon from '@material-ui/icons/Launch';
import Button from '@material-ui/core/Button';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';
import Collapse from '@material-ui/core/Collapse';
import ExpandLess from '@material-ui/icons/ExpandLess';
import ExpandMore from '@material-ui/icons/ExpandMore';

// Components.
import Header from '../components/Header';
import Footer from '../components/Footer';
import TestList from '../components/TestList';

const useStyles = makeStyles((theme) => ({
  root: {
    display: 'flex',
  },
  menuButton: {
    marginRight: theme.spacing(2),
  },
  title: {
    flexGrow: 1,
    textAlign: 'center',
  },
  paper: {
    height: 500,
    width: 100,
  },
  drawer: {
    width: 350,
  },
  // Styles for the `Paper` component rendered by `Drawer`.
  drawerPaper: {
    width: 'inherit',
    paddingTop: 64, // Equal to AppBar height.
  },
  content: {
    flexGrow: 1,
    padding: theme.spacing(15),
  },
  rightIcons: {
    marginRight: '-12px',
  },
  nested: {
    paddingLeft: theme.spacing(9),
  },
}));

const testCases = [
  'ServiceProvider Config',
  'Schemas',
  'ResourceTypes',
  'User Endpoint',
  'Group Endpoint',
  'Me Endpoint',
  'Bulk Endpoint',
  'Roles Endpoint',
];

const serviceProviderConfigSubTests = [
  { name: 'Get ServiceProviderConfig', stateName: 'serviceProviderConfigGet' },
];

const schemasSubTests = [{ name: 'Get Schemas', stateName: 'schemasGet' }];

const resourceTypesSubTests = [
  { name: 'Get ResourceTypes', stateName: 'resourceTypesGet' },
];

const userSubTests = [
  { name: 'Get User', stateName: 'userGet' },
  { name: 'Get User By Id', stateName: 'userGetById' },
  { name: 'Post User', stateName: 'userPost' },
  { name: 'Update User', stateName: 'userPut' },
  { name: 'Patch User', stateName: 'userPatch' },
  { name: 'Delete User', stateName: 'userDelete' },
  { name: 'Search User', stateName: 'userSearch' },
];

const groupSubTests = [
  { name: 'Get Group', stateName: 'groupGet' },
  { name: 'Post Group', stateName: 'groupPost' },
];

const RolesSubTests = [
  { name: 'Get Group', stateName: 'groupGet' },
  { name: 'Post Group', stateName: 'groupPost' },
];

const tests = [
  {
    id: 1,
    name: 'ServiceProvider Config',
    sub: serviceProviderConfigSubTests,
  },
  {
    id: 2,
    name: 'Schemas',
    sub: schemasSubTests,
  },
  {
    id: 3,
    name: 'ResourceTypes',
    sub: resourceTypesSubTests,
  },
  {
    id: 4,
    name: 'User Endpoint',
    sub: userSubTests,
  },
  {
    id: 5,
    name: 'Group Endpoint',
    sub: groupSubTests,
  },
  {
    id: 6,
    name: 'Me Endpoint',
    sub: groupSubTests,
  },
  {
    id: 7,
    name: 'Bulk Endpoint',
    sub: groupSubTests,
  },
  {
    id: 8,
    name: 'Roles Endpoint',
    sub: groupSubTests,
  },
];

export default function Home() {
  const classes = useStyles();
  const [testCases, setTestcases] = React.useState(tests);
  const [open, setOpen] = React.useState(false);
  const [state, setState] = React.useState({
    userGet: false,
    userGetById: false,
    userPut: false,
    userPost: false,
    userPatch: false,
    userSearch: false,
    userDelete: false,
  });

  const handleCheckbox = (parentId, parentIndex, childId, childIndex) => {
    const tests = testCases;
    const test = tests[parentIndex];
    const features = test.sub;
    const allChildrenSelected = test || false;
    console.log(features);

    if (typeof childIndex !== 'undefined' || typeof childId !== 'undefined') {
      // [SCENARIO] - a child checkbox was selected.
      console.log('child clicked: ', childIndex);

      // get total of checked sub checkboxes in existing array
      let checkedCount = 0;
      features.forEach((feature) => feature.checked && checkedCount++);

      if (!features[childIndex].checked) {
        // [SCENARIO] - previous state of child was 'FALSE'
        console.log('in child function, previous state is false');
        // [TASK] - set the child to selected
        const modifiedFeature = {
          ...features[childIndex],
          checked: true,
        };

        // [TASK] - insert modified child back into parent
        features[childIndex] = modifiedFeature;
        const modifiedFeatures = features;

        // [TASK] - check if parent is selected (is this the first child to be selected)
        if (checkedCount === 0) {
          const modifiedtest = {
            ...test,
            checked: true,
            sub: modifiedFeatures,
          };
          // insert the test back into the list
          tests[parentIndex] = modifiedtest;
          const modifiedtests = tests;

          setTestcases([...modifiedtests]);
        } else {
          // non-first child checkbox being selected
          const modifiedtest = {
            ...test,
            sub: modifiedFeatures,
          };
          // insert the test back into the list
          tests[parentIndex] = modifiedtest;
          const modifiedtests = tests;

          setTestcases([...modifiedtests]);
        }
      } else {
        // [SCENARIO] - previous state of child was 'TRUE'
        console.log('in child function, previous state is true');
        // [TASK] - deselect the child checkbox
        const modifiedFeature = {
          ...features[childIndex],
          checked: false,
        };

        // [TASK] - insert modified child back into parent
        features[childIndex] = modifiedFeature;
        const modifiedFeatures = features;

        if (checkedCount === 1) {
          // [SCENARIO] - deselecting the last child checkbox
          const modifiedtest = {
            ...test,
            checked: false,
            expanded: false,
            allChildrenSelected: false,
            sub: modifiedFeatures,
          };
          // insert the test back into the list
          tests[parentIndex] = modifiedtest;
          const modifiedtests = tests;

          setTestcases([...modifiedtests]);
        } else {
          console.log('in here');
          // [SCENARIO] - just deselecting a non-first child checkbox
          const modifiedtest = {
            ...test,
            allChildrenSelected: false,
            sub: modifiedFeatures,
          };
          // insert the test back into the list
          tests[parentIndex] = modifiedtest;
          const modifiedtests = tests;

          setTestcases([...modifiedtests]);
        }
      }
    } else {
      // [SCENARIO] - a parent checkbox was selected.
      console.log('parent clicked: ', parentIndex);
      if (!test.checked) {
        // [SCENARIO] - previous state of parent was 'FALSE'

        // [TASK] - select all features
        const modifiedFeatures = features.map((feature) => ({
          ...feature,
          checked: !feature.checked,
        }));

        // [TASK] - set modified test
        const modifiedtest = {
          ...test,
          checked: true,
          expanded: true,
          allChildrenSelected: true,

          sub: modifiedFeatures,
        };

        // [TASK] - put the test back in the list of tests
        tests[parentIndex] = modifiedtest;
        const modifiedtests = tests;

        // [TASK] - set state of overall list
        setTestcases([...modifiedtests]);
      } else {
        // [SCENARIO] - previous state of parent was 'TRUE'

        if (allChildrenSelected) {
          // [SCENARIO] - all children checkboxs selected. deselect and close expansion
          console.log('all children are currently selected');
          // [TASK] - deselect all features
          const modifiedFeatures = features.map((feature) => ({
            ...feature,
            checked: false,
          }));

          // [TASK] - set modified test
          const modifiedtest = {
            ...test,
            checked: false,
            expanded: false,
            allChildrenSelected: false,

            sub: modifiedFeatures,
          };

          // [TASK] - put the test back in the list of tests
          tests[parentIndex] = modifiedtest;
          const modifiedtests = tests;

          // [TASK] - set state of overall list
          setTestcases([...modifiedtests]);
        } else {
          // [SCENARIO] - not all children are selected.

          // [TASK] - select all remaining children checkboxs
          // TODO - way to skip already true ones?
          const modifiedFeatures = features.map((feature) => ({
            ...feature,
            checked: true,
          }));

          // [TASK] - set modified test
          const modifiedtest = {
            ...test,
            allChildrenSelected: true,
            expanded: true,

            sub: modifiedFeatures,
          };

          // [TASK] - put the test back in the list of tests
          tests[parentIndex] = modifiedtest;
          const modifiedtests = tests;

          // [TASK] - set state of overall list
          setTestcases([...modifiedtests]);
        }
      }
    }
  };

  // const handlePrimaryChange = (event) => {
  //   console.log(event.target.name);
  //   if (event.target.name == 'User Endpoint') {
  //     //handleClick();
  //     setState({
  //       ...state,
  //       userGet: event.target.checked,
  //       userGetById: event.target.checked,
  //     });
  //   }
  // };

  const handlePrimaryChange = (id, index) => {
    const test = testCases[index];
    const { subTests } = test.sub;

    var t = {
      ...testCases.filter((test) => test.id == id)[0],
      Checked: !testCases.filter((test) => test.id == id)[0].Checked,
      expanded: !testCases.filter((test) => test.id == id)[0].expanded,
    };
    if (t.checked == true) {
      t.sub.map((s) => (s.checked = true));
    } else {
      t.sub.map((s) => (s.checked = false));
    }
    const tests = testCases;
    tests[index] = t;

    setTestcases(tests);
    console.log(testCases);
  };

  const handleClick = (id, index) => {
    console.log(testCases);
    var x = Array.from(testCases).filter((test) => test.id == id)[0];

    var t = {
      ...testCases.filter((test) => test.id == id)[0],
      expanded: !testCases.filter((test) => test.id == id)[0].expanded,
    };
    const tests = testCases;
    tests[index] = t;

    setTestcases([...tests]);

    //setOpen(!open);
  };

  const handleSecondaryChange = (event) => {
    console.log(state);
    setState({ ...state, [event.target.name]: event.target.checked });
  };

  return (
    <div className={classes.root}>
      <Header className="App-header" />
      <Drawer
        variant="permanent"
        className={classes.drawer}
        classes={{ paper: classes.drawerPaper }}
      >
        <div>
          <Typography variant="h6" className={classes.title}>
            Test Cases
          </Typography>
          <Settings />
        </div>
        <List component="nav">
          {testCases.map((t, parentIndex) => (
            <div>
              <ListItem dense key={parentIndex}>
                <ListItemIcon>
                  <Checkbox
                    disableRipple
                    edge="start"
                    checked={!!t.checked}
                    onChange={() => handleCheckbox(t.id, parentIndex)}
                    name={t.name}
                  />
                </ListItemIcon>
                <ListItemIcon>
                  <Button
                    disableFocusRipple
                    disableRipple
                    classes={{ outlined: classes.button }}
                    variant="outlined"
                    size="small"
                  >
                    {t.name}
                  </Button>
                </ListItemIcon>
                <ListItemSecondaryAction>
                  <IconButton
                    className={classes.rightIcons}
                    onClick={() => {
                      handleClick(t.id, parentIndex);
                    }}
                    name={t.name}
                  >
                    {console.log(t.expanded)}
                    {t.expanded ? <ExpandLess /> : <ExpandMore />}
                  </IconButton>
                </ListItemSecondaryAction>
              </ListItem>
              <Collapse unmountOnExit in={t.expanded || false} timeout="auto">
                {t.sub.map((s, childIndex) => (
                  <div>
                    <List component="div" disablePadding>
                      <ListItem
                        dense
                        className={classes.nested}
                        key={childIndex}
                      >
                        <ListItemIcon>
                          <Checkbox
                            // disableRipple
                            edge="start"
                            checked={!!s.checked}
                            tabIndex={-1}
                            onChange={() => {
                              handleCheckbox(
                                t.id,
                                parentIndex,
                                t.id,
                                childIndex
                              );
                            }}
                            name="userGet"
                          />
                        </ListItemIcon>
                        <ListItemText primary={s.name} />
                      </ListItem>
                    </List>
                  </div>
                ))}
              </Collapse>
            </div>
          ))}

          {/* <ListItem dense key={2}>
            <ListItemIcon>
              <Checkbox
                disableRipple
                edge="start"
                onChange={handlePrimaryChange}
                name="user"
              />
            </ListItemIcon>
            <ListItemIcon>
              <Button
                disableFocusRipple
                disableRipple
                classes={{ outlined: classes.button }}
                variant="outlined"
                size="small"
              >
                User Endpoint
              </Button>
            </ListItemIcon>
            <ListItemSecondaryAction>
              <IconButton className={classes.rightIcons} onClick={handleClick}>
                {open ? <ExpandLess /> : <ExpandMore />}
              </IconButton>
            </ListItemSecondaryAction>
          </ListItem>
          <Collapse unmountOnExit in={open} timeout="auto">
            <List component="div" disablePadding>
              <ListItem dense className={classes.nested}>
                <ListItemIcon>
                  <Checkbox
                    disableRipple
                    edge="start"
                    checked={state.userGet}
                    tabIndex={-1}
                    onChange={handleSecondaryChange}
                    name="userGet"
                  />
                </ListItemIcon>
                <ListItemText primary="Get Method" />
              </ListItem>
              <ListItem dense className={classes.nested}>
                <ListItemIcon>
                  <Checkbox
                    disableRipple
                    edge="start"
                    checked={state.userGetById}
                    tabIndex={-1}
                    onChange={handleSecondaryChange}
                    name="userGetById"
                  />
                </ListItemIcon>
                <ListItemText primary="Get by id" />
              </ListItem>
            </List>
          </Collapse> */}
        </List>
      </Drawer>

      <main className={classes.content}>
        <Typography paragraph>
          Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
          eiusmod tempor incididunt ut labore et dolore magna aliqua. Rhoncus
          dolor purus non enim praesent elementum facilisis leo vel. Risus at
          ultrices mi tempus imperdiet. Semper risus in hendrerit gravida rutrum
          quisque non tellus. Convallis convallis tellus id interdum velit
          laoreet id donec ultrices. Odio morbi quis commodo odio aenean sed
          adipiscing. Amet nisl suscipit adipiscing bibendum est ultricies
          integer quis. Cursus euismod quis viverra nibh cras. Metus vulputate
          eu scelerisque felis imperdiet proin fermentum leo. Mauris commodo
          quis imperdiet massa tincidunt. Cras tincidunt lobortis feugiat
          vivamus at augue. At augue eget arcu dictum varius duis at consectetur
          lorem. Velit sed ullamcorper morbi tincidunt. Lorem donec massa sapien
          faucibus et molestie ac.
        </Typography>
        <Typography paragraph>
          Consequat mauris nunc congue nisi vitae suscipit. Fringilla est
          ullamcorper eget nulla facilisi etiam dignissim diam. Pulvinar
          elementum integer enim neque volutpat ac tincidunt. Ornare suspendisse
          sed nisi lacus sed viverra tellus. Purus sit amet volutpat consequat
          mauris. Elementum eu facilisis sed odio morbi. Euismod lacinia at quis
          risus sed vulputate odio. Morbi tincidunt ornare massa eget egestas
          purus viverra accumsan in. In hendrerit gravida rutrum quisque non
          tellus orci ac. Pellentesque nec nam aliquam sem et tortor. Habitant
          morbi tristique senectus et. Adipiscing elit duis tristique
          sollicitudin nibh sit. Ornare aenean euismod elementum nisi quis
          eleifend. Commodo viverra maecenas accumsan lacus vel facilisis. Nulla
          posuere sollicitudin aliquam ultrices sagittis orci a.
        </Typography>
        <Typography paragraph>
          Consequat mauris nunc congue nisi vitae suscipit. Fringilla est
          ullamcorper eget nulla facilisi etiam dignissim diam. Pulvinar
          elementum integer enim neque volutpat ac tincidunt. Ornare suspendisse
          sed nisi lacus sed viverra tellus. Purus sit amet volutpat consequat
          mauris. Elementum eu facilisis sed odio morbi. Euismod lacinia at quis
          risus sed vulputate odio. Morbi tincidunt ornare massa eget egestas
          purus viverra accumsan in. In hendrerit gravida rutrum quisque non
          tellus orci ac. Pellentesque nec nam aliquam sem et tortor. Habitant
          morbi tristique senectus et. Adipiscing elit duis tristique
          sollicitudin nibh sit. Ornare aenean euismod elementum nisi quis
          eleifend. Commodo viverra maecenas accumsan lacus vel facilisis. Nulla
          posuere sollicitudin aliquam ultrices sagittis orci a.
        </Typography>
        <Typography paragraph>
          Consequat mauris nunc congue nisi vitae suscipit. Fringilla est
          ullamcorper eget nulla facilisi etiam dignissim diam. Pulvinar
          elementum integer enim neque volutpat ac tincidunt. Ornare suspendisse
          sed nisi lacus sed viverra tellus. Purus sit amet volutpat consequat
          mauris. Elementum eu facilisis sed odio morbi. Euismod lacinia at quis
          risus sed vulputate odio. Morbi tincidunt ornare massa eget egestas
          purus viverra accumsan in. In hendrerit gravida rutrum quisque non
          tellus orci ac. Pellentesque nec nam aliquam sem et tortor. Habitant
          morbi tristique senectus et. Adipiscing elit duis tristique
          sollicitudin nibh sit. Ornare aenean euismod elementum nisi quis
          eleifend. Commodo viverra maecenas accumsan lacus vel facilisis. Nulla
          posuere sollicitudin aliquam ultrices sagittis orci a.
        </Typography>
      </main>
      <Footer />
    </div>
  );
}
