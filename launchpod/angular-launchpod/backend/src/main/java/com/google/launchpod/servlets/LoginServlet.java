// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.launchpod.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.launchpod.data.LoginStatus;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login-status")
public class LoginServlet extends HttpServlet {
  private static final Gson GSON = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    UserService userService = UserServiceFactory.getUserService();
    String urlToRedirectTo = "/index.html";
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String logoutUrl = userService.createLogoutURL(urlToRedirectTo);

      String loginMessage = "<p>Logged in as " + userEmail + ". <a href=\"" + logoutUrl + "\">Logout</a>.</p>";
      LoginStatus loginStatus = new LoginStatus(true, loginMessage);

      response.getWriter().println(GSON.toJson(loginStatus));
    } else {
      String loginUrl = userService.createLoginURL(urlToRedirectTo);

      String loginMessage = loginUrl;
      LoginStatus loginStatus = new LoginStatus(false, loginMessage);

      response.getWriter().println(GSON.toJson(loginStatus));
    }
  }
}
