/*
 * Copyright (C) 2018 ISTC - CNR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.istc.lecture.desktopapp;

import it.cnr.istc.lecture.desktopapp.api.Credentials;
import it.cnr.istc.lecture.desktopapp.api.InitResponse;
import it.cnr.istc.lecture.desktopapp.api.NewUserRequest;
import it.cnr.istc.lecture.desktopapp.api.Parameter;
import it.cnr.istc.lecture.desktopapp.api.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Riccardo De Benedictis
 */
public class Context {

    private static final Logger LOG = Logger.getLogger(Context.class.getName());
    private static final Jsonb JSONB = JsonbBuilder.create();
    private static Context ctx;

    public static Context getContext() {
        if (ctx == null) {
            ctx = new Context();
        }
        return ctx;
    }
    private final Properties properties = new Properties();
    private final Client client = ClientBuilder.newClient();
    private final WebTarget target;
    private Stage stage;
    private final ObjectProperty<User> user = new SimpleObjectProperty<>();

    private Context() {
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        this.target = client.target("http://" + properties.getProperty("host", "localhost") + ":" + properties.getProperty("service-port", "8080")).path("LECTurE-WebApp").path("LECTurE");
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    public ObjectProperty<User> userProperty() {
        return user;
    }

    public void login(String email, String password) {
        Credentials credentials = new Credentials(email, password, load_pars());
        InitResponse init = target.path("login").request(MediaType.APPLICATION_JSON).post(Entity.json(credentials), InitResponse.class);
        user.set(init.getUser());
    }

    public void logout() {
    }

    public void newUser(String email, String password, String first_name, String last_name) {
        NewUserRequest new_user = new NewUserRequest(email, password, first_name, last_name, load_pars());
        User u = target.path("newUser").request(MediaType.APPLICATION_JSON).post(Entity.json(new_user), User.class);
        user.set(u);
    }

    private static Map<Parameter, Map<String, String>> load_pars() {
        Collection<Parameter> pars = JSONB.fromJson(Context.class.getResourceAsStream("/parameters/types.json"), new ArrayList<Parameter>() {
        }.getClass().getGenericSuperclass());
        Map<String, Map<String, String>> values = JSONB.fromJson(Context.class.getResourceAsStream("/parameters/values.json"), new HashMap<String, Map<String, String>>() {
        }.getClass().getGenericSuperclass());

        Map<Parameter, Map<String, String>> parameters = new HashMap<>();
        for (Parameter par : pars) {
            parameters.put(par, values.get(par.getName()));
        }
        return parameters;
    }
}
