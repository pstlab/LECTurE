/*
 * Copyright (C) 2018 Riccardo De Benedictis
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
package it.cnr.istc.lecture.webapp;

import it.cnr.istc.lecture.webapp.api.Credentials;
import it.cnr.istc.lecture.webapp.api.InitResponse;
import it.cnr.istc.lecture.webapp.api.Lesson;
import it.cnr.istc.lecture.webapp.api.NewUserRequest;
import it.cnr.istc.lecture.webapp.api.User;
import it.cnr.istc.lecture.webapp.api.messages.Event;
import it.cnr.istc.lecture.webapp.api.model.LessonModel;
import it.cnr.istc.lecture.webapp.entities.UserEntity;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author Riccardo De Benedictis
 */
@Path("/")
public class LECTurEResource {

    private static final Logger LOG = Logger.getLogger(LECTurEResource.class.getName());
    private static final Jsonb JSONB = JsonbBuilder.create();
    @Context
    private UriInfo context;
    @PersistenceContext
    private EntityManager em;
    @Resource
    private UserTransaction utx;
    @EJB
    private LECTurEBean ctx;

    /**
     * Creates a new instance of UsersResource
     */
    public LECTurEResource() {
    }

    @POST
    @Path("newUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public User newUser(NewUserRequest new_user) {
        try {
            utx.begin();
            UserEntity u = new UserEntity();
            u.setEmail(new_user.getEmail());
            u.setPassword(new_user.getPassword());
            u.setFirstName(new_user.getFirstName());
            u.setLastName(new_user.getLastName());
            em.persist(u);
            utx.commit();
            ctx.newUser(u.getId());
            new_user.getParameters().entrySet().forEach(entry -> {
                ctx.newParameter(u.getId(), entry.getKey());
                if (entry.getValue() != null) {
                    ctx.newParameterValue(u.getId(), entry.getKey().getName(), entry.getValue());
                }
            });
            return new User(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), ctx.getParameters(u.getId()));
        } catch (IllegalStateException | SecurityException | HeuristicMixedException | HeuristicRollbackException | NotSupportedException | RollbackException | SystemException ex) {
            try {
                utx.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.log(Level.SEVERE, null, ex1);
            }
            throw new WebApplicationException(ex.getMessage());
        }
    }

    @GET
    @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<User> getUsers() {
        List<UserEntity> users = em.createQuery("SELECT u FROM UserEntity u", UserEntity.class).getResultList();
        return users.stream().map(u -> new User(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), ctx.getParameters(u.getId()))).collect(Collectors.toList());
    }

    @GET
    @Path("find_users/{search_string}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<User> findUsers(@PathParam("search_string") String search_string) {
        TypedQuery<UserEntity> query = em.createQuery("SELECT u FROM UserEntity u WHERE u.first_name LIKE :search_string OR u.last_name LIKE :search_string", UserEntity.class);
        query.setParameter("search_string", search_string);
        return query.getResultList().stream().map(u -> new User(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), null)).collect(Collectors.toList());
    }

    @GET
    @Path("users/{user_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("user_id") long user_id) {
        UserEntity u = em.find(UserEntity.class, user_id);
        if (u != null) {
            return new User(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), ctx.getParameters(u.getId()));
        } else {
            throw new WebApplicationException("Cannot find user id");
        }
    }

    @DELETE
    @Path("delete_user/{user_id}")
    public void deleteUser(@PathParam("user_id") long user_id) {
        try {
            utx.begin();
            UserEntity u = em.find(UserEntity.class, user_id);
            em.remove(u);
            utx.commit();
            ctx.deleteUser(user_id);
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException ex) {
            try {
                utx.rollback();
            } catch (IllegalStateException | SecurityException | SystemException ex1) {
                LOG.log(Level.SEVERE, null, ex1);
            }
            throw new WebApplicationException(ex.getMessage());
        }
    }

    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public InitResponse login(Credentials credentials) {
        try {
            TypedQuery<UserEntity> query = em.createQuery("SELECT u FROM UserEntity u WHERE u.email = :email AND u.password = :password", UserEntity.class);
            query.setParameter("email", credentials.getEmail());
            query.setParameter("password", credentials.getPassword());
            UserEntity u = query.getSingleResult();

            credentials.getParameters().entrySet().forEach(entry -> {
                ctx.newParameter(u.getId(), entry.getKey());
                if (entry.getValue() != null) {
                    ctx.newParameterValue(u.getId(), entry.getKey().getName(), entry.getValue());
                }
            });

            User user = new User(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), null);
            Collection<LessonModel> models = u.getModels().stream().map(model -> JSONB.fromJson(model.getModel(), LessonModel.class)).collect(Collectors.toList());
            List<Lesson> following_lessons = u.getLessons().stream().map(lesson -> {
                Lesson l = ctx.getLessonManager(lesson.getId()).getLesson();
                return new Lesson(l.getId(), l.getTeacherId(), l.getName(), l.getState(), l.getTime(), l.getModel(), l.getRoles(), null, l.getTokens());
            }).collect(Collectors.toList());
            List<User> students = u.getStudents().stream().map(std -> new User(std.getId(), std.getEmail(), std.getFirstName(), std.getLastName(), ctx.getParameters(std.getId()))).collect(Collectors.toList());
            List<Lesson> followed_lessons = u.getRoles().stream().map(role -> {
                Lesson l = ctx.getLessonManager(role.getLesson().getId()).getLesson();
                List<Event> events = l.getEvents().stream().filter(e -> e.getRole().equals(role.getName())).collect(Collectors.toList());
                return new Lesson(l.getId(), l.getTeacherId(), l.getName(), l.getState(), l.getTime(), null, l.getRoles(), events, null);
            }).collect(Collectors.toList());
            List<User> teachers = u.getTeachers().stream().map(tc -> new User(tc.getId(), tc.getEmail(), tc.getFirstName(), tc.getLastName(), null)).collect(Collectors.toList());

            InitResponse init = new InitResponse(user, models, following_lessons, students, followed_lessons, teachers);
            return init;
        } catch (NoResultException e) {
            throw new WebApplicationException(e.getLocalizedMessage(), Response.Status.UNAUTHORIZED);
        }
    }
}
