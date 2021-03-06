import * as React from 'react';
import './App.css';
import Home from './pages/Home';
import About from './pages/About';
import Feed from './pages/Feed';
import Login from './pages/Login';
import Register from "./pages/Register";
import User from "./pages/User";
import Create from "./pages/Create";
import Search from "./pages/Search";
import Edit from "./pages/Edit";
import { ChakraProvider } from "@chakra-ui/react";

import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link
} from "react-router-dom";

function App() {
  return (
      <ChakraProvider>

          <Router>

            <Switch>

                <Route exact path="/login" component={Login} />

                <Route exact path="/register" component={Register}/>

                <Route exact path="/about" component={About} />

                <Route exact path="/feed" component={Feed} />

                <Route exact path="/" component={Home} />

                <Route path="/user/:uid"  component={User} />

                <Route path="/search/:name" component={Search}/>

                <Route exact path="/create" component={Create} />

                <Route path="/edit/:pid" component={Edit} />

            </Switch>

          </Router>

      </ChakraProvider>
  )
}

export default App;
